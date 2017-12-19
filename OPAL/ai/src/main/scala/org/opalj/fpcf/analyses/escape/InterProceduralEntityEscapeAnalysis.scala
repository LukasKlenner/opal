/* BSD 2-Clause License:
 * Copyright (c) 2009 - 2017
 * Software Technology Group
 * Department of Computer Science
 * Technische Universität Darmstadt
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.opalj
package fpcf
package analyses
package escape

import org.opalj.br.ReferenceType
import org.opalj.br.MethodDescriptor
import org.opalj.br.ObjectType
import org.opalj.br.VirtualMethod
import org.opalj.br.Method
import org.opalj.br.analyses.FormalParameters
import org.opalj.br.analyses.SomeProject
import org.opalj.br.cfg.CFG
import org.opalj.ai.Domain
import org.opalj.ai.ValueOrigin
import org.opalj.ai.domain.RecordDefUse
import org.opalj.br.VirtualForwardingMethod
import org.opalj.br.analyses.VirtualFormalParameters
import org.opalj.br.analyses.FormalParameter
import org.opalj.br.analyses.VirtualFormalParameter
import org.opalj.br.analyses.cg.IsOverridableMethodKey
import org.opalj.collection.immutable.IntTrieSet
import org.opalj.fpcf.properties.EscapeProperty
import org.opalj.fpcf.properties.EscapeInCallee
import org.opalj.fpcf.properties.NoEscape
import org.opalj.fpcf.properties.GlobalEscape
import org.opalj.fpcf.properties.EscapeViaStaticField
import org.opalj.fpcf.properties.EscapeViaHeapObject
import org.opalj.fpcf.properties.EscapeViaReturn
import org.opalj.fpcf.properties.AtMost
import org.opalj.fpcf.properties.EscapeViaAbnormalReturn
import org.opalj.fpcf.properties.EscapeViaParameterAndAbnormalReturn
import org.opalj.fpcf.properties.EscapeViaParameterAndReturn
import org.opalj.fpcf.properties.Conditional
import org.opalj.fpcf.properties.EscapeViaNormalAndAbnormalReturn
import org.opalj.fpcf.properties.EscapeViaParameterAndNormalAndAbnormalReturn
import org.opalj.fpcf.properties.EscapeViaParameter
import org.opalj.tac.Expr
import org.opalj.tac.Stmt
import org.opalj.tac.DUVar
import org.opalj.tac.StaticMethodCall
import org.opalj.tac.VirtualMethodCall
import org.opalj.tac.NonVirtualFunctionCall
import org.opalj.tac.VirtualFunctionCall
import org.opalj.tac.StaticFunctionCall
import org.opalj.tac.NonVirtualMethodCall

trait AbstractInterProceduralEntityEscapeAnalysis extends AbstractEntityEscapeAnalysis {
    val project: SomeProject
    val formalParameters: FormalParameters
    val targetMethod: VirtualMethod
    val propertyStore: PropertyStore

    //TODO Move to non entity based analysis
    private[this] val isMethodOverridable: Method ⇒ Answer = project.get(IsOverridableMethodKey)
    val virtualFormalParameters: VirtualFormalParameters

    // STATE MUTATED DURING THE ANALYSIS
    private[this] val dependeeCache: scala.collection.mutable.Map[Entity, EOptionP[Entity, EscapeProperty]] = scala.collection.mutable.Map()
    private[this] val hasReturnValueUseSites = scala.collection.mutable.Set[Entity]()

    protected[this] override def handleStaticMethodCall(call: StaticMethodCall[V]): Unit = {
        handleStaticCall(
            call.declaringClass, call.isInterface, call.name, call.descriptor, call.params, hasAssignment = false
        )
    }

    protected[this] override def handleStaticFunctionCall(
        call: StaticFunctionCall[V], hasAssignment: Boolean
    ): Unit = {
        handleStaticCall(
            call.declaringClass,
            call.isInterface,
            call.name,
            call.descriptor,
            call.params,
            hasAssignment
        )
    }

    protected[this] override def handleVirtualMethodCall(call: VirtualMethodCall[V]): Unit = {
        handleVirtualCall(
            call.declaringClass,
            call.isInterface,
            call.name,
            call.descriptor,
            call.receiver,
            call.params,
            hasAssignment = false
        )
    }

    protected[this] override def handleVirtualFunctionCall(
        call: VirtualFunctionCall[V], hasAssignment: Boolean
    ): Unit = {
        handleVirtualCall(
            call.declaringClass,
            call.isInterface,
            call.name,
            call.descriptor,
            call.receiver,
            call.params,
            hasAssignment
        )
    }

    protected[this] override def handleParameterOfConstructor(call: NonVirtualMethodCall[V]): Unit = {
        val methodO = project.specialCall(
            call.declaringClass.asObjectType,
            call.isInterface,
            call.name,
            call.descriptor
        )
        checkParams(methodO, call.params, hasAssignment = false)
    }

    protected[this] override def handleNonVirtualAndNonConstructorCall(call: NonVirtualMethodCall[V]): Unit = {
        val methodO = project.specialCall(
            call.declaringClass.asObjectType,
            call.isInterface,
            call.name,
            call.descriptor
        )
        checkParams(methodO, call.params, hasAssignment = false)
        if (usesDefSite(call.receiver))
            handleCall(methodO, 0, hasAssignment = false)
    }

    protected[this] override def handleNonVirtualFunctionCall(
        call: NonVirtualFunctionCall[V], hasAssignment: Boolean
    ): Unit = {
        val methodO = project.specialCall(
            call.declaringClass.asObjectType,
            call.isInterface,
            call.name,
            call.descriptor
        )
        checkParams(methodO, call.params, hasAssignment)
        if (usesDefSite(call.receiver))
            handleCall(methodO, 0, hasAssignment)
    }

    private[this] def handleStaticCall(
        dc:            ReferenceType,
        isI:           Boolean,
        name:          String,
        descr:         MethodDescriptor,
        params:        Seq[Expr[V]],
        hasAssignment: Boolean
    ): Unit = {
        checkParams(project.staticCall(dc.asObjectType, isI, name, descr), params, hasAssignment)
    }

    private[this] def handleVirtualCall(
        dc:            ReferenceType,
        isI:           Boolean,
        name:          String,
        descr:         MethodDescriptor,
        receiver:      Expr[V],
        params:        Seq[Expr[V]],
        hasAssignment: Boolean
    ): Unit = {
        assert(receiver.isVar)
        val value = receiver.asVar.value.asDomainReferenceValue
        if (dc.isArrayType) {
            val methodO = project.instanceCall(ObjectType.Object, ObjectType.Object, name, descr)
            checkParams(methodO, params, hasAssignment)
            if (usesDefSite(receiver)) handleCall(methodO, 0, hasAssignment)
        } else if (value.isPrecise) {
            if (value.isNull.isNoOrUnknown) {
                val valueType = value.valueType.get
                assert(targetMethod.declaringClassType.isObjectType)
                val methodO = project.instanceCall(targetMethod.declaringClassType.asObjectType, valueType, name, descr)
                checkParams(methodO, params, hasAssignment)
                if (usesDefSite(receiver)) handleCall(methodO, 0, hasAssignment)
            } else {
                // the receiver is null, the method is not invoked and the object does not escape
            }
        } else {
            val target = project.instanceCall(targetMethod.declaringClassType.asObjectType, dc, name, descr)
            if (target.isEmpty || isMethodOverridable(target.value).isNotNo) {
                // the type of the virtual call is extensible and the analysis mode is library like
                // therefore the method could be overriden and we do not know if the object escapes
                // TODO: to optimize performance, we do not let the analysis run against the existing methods
                meetMostRestrictive(AtMost(EscapeInCallee))
            } else {
                val vm = VirtualForwardingMethod(dc, name, descr, target.value)
                if (project.isSignaturePolymorphic(vm.target.classFile.thisType, vm.target)) {
                    //IMPROVE
                    meetMostRestrictive(AtMost(EscapeInCallee))
                    //TODO check if this is to much (param contains def-site)
                } else {
                    if (usesDefSite(receiver)) {
                        val fp = virtualFormalParameters(vm)
                        assert(fp(0) ne null)
                        handleEscapeState(fp(0), hasAssignment)

                    }
                    for (i ← params.indices) {
                        if (usesDefSite(params(i))) {
                            val fp = virtualFormalParameters(vm)
                            assert(fp(i + 1) ne null)
                            handleEscapeState(fp(i + 1), hasAssignment)
                        }
                    }
                }

            }
        }

    }

    private[this] def checkParams(
        methodO: org.opalj.Result[Method], params: Seq[Expr[V]], hasAssignment: Boolean
    ): Unit = {
        for (i ← params.indices) {
            if (usesDefSite(params(i)))
                handleCall(methodO, i + 1, hasAssignment)
        }
    }

    private[this] def handleCall(
        methodO: org.opalj.Result[Method], param: Int, hasAssignment: Boolean
    ): Unit = {
        methodO match {
            case Success(method) ⇒
                if (project.isSignaturePolymorphic(method.classFile.thisType, method)) {
                    //IMPROVE
                    meetMostRestrictive(AtMost(EscapeInCallee))
                } else {
                    val fp = formalParameters(method)(param)

                    // for self recursive calls, we do not need handle the call any further
                    if (fp != entity) {
                        handleEscapeState(fp, hasAssignment)
                    }
                }
            case _ ⇒ meetMostRestrictive(AtMost(EscapeInCallee))
        }
    }

    private[this] def handleEscapeState(fp: Entity, hasAssignment: Boolean): Unit = {
        /* This is crucial for the analysis. the dependees set is not allowed to
         * contain duplicates. Due to very long target methods it could be the case
         * that multiple queries to the property store result in either an EP or an
         * EPK. Therefore we cache the result to have it consistent.
         */
        assert(fp.isInstanceOf[FormalParameter] || fp.isInstanceOf[VirtualFormalParameter])
        val escapeState = dependeeCache.getOrElseUpdate(fp, propertyStore(fp, EscapeProperty.key))

        escapeState match {
            case EP(_, NoEscape | EscapeInCallee) ⇒ meetMostRestrictive(EscapeInCallee)
            case EP(_, GlobalEscape)              ⇒ meetMostRestrictive(GlobalEscape)
            case EP(_, EscapeViaStaticField)      ⇒ meetMostRestrictive(EscapeViaStaticField)
            case EP(_, EscapeViaHeapObject)       ⇒ meetMostRestrictive(EscapeViaHeapObject)
            case EP(_, EscapeViaReturn) if hasAssignment ⇒
                meetMostRestrictive(AtMost(EscapeInCallee))
            case EP(_, EscapeViaReturn) ⇒ meetMostRestrictive(EscapeInCallee)
            // we do not track parameters or exceptions in the callee side
            case EP(_, p) if p.isFinal  ⇒ meetMostRestrictive(AtMost(EscapeInCallee))
            case EP(_, AtMost(_))       ⇒ meetMostRestrictive(AtMost(EscapeInCallee))
            case ep @ EP(_, Conditional(AtMost(_))) ⇒
                assert(ep.e.isInstanceOf[FormalParameter] || ep.e.isInstanceOf[VirtualFormalParameter])

                meetMostRestrictive(AtMost(EscapeInCallee))

                dependees += ep
            case ep @ EP(_, Conditional(EscapeViaReturn)) ⇒
                assert(ep.e.isInstanceOf[FormalParameter] || ep.e.isInstanceOf[VirtualFormalParameter])

                if (hasAssignment) {
                    meetMostRestrictive(AtMost(EscapeInCallee))
                    hasReturnValueUseSites += fp
                } else
                    meetMostRestrictive(EscapeInCallee)

                dependees += ep

            case epkOrConditional ⇒
                assert(epkOrConditional.e.isInstanceOf[FormalParameter] || epkOrConditional.e.isInstanceOf[VirtualFormalParameter])

                meetMostRestrictive(EscapeInCallee)

                if (hasAssignment)
                    hasReturnValueUseSites += fp

                dependees += epkOrConditional
        }
    }

    abstract override protected[this] def c(
        other: Entity, p: Property, u: UpdateType
    ): PropertyComputationResult = {
        other match {
            case FormalParameter(m, -1) if m.isConstructor ⇒
                throw new RuntimeException("can't handle the this-reference of the constructor")

            // this entity is passed as parameter (or this local) to a method
            case _: FormalParameter | _: VirtualFormalParameter ⇒ p match {

                case GlobalEscape         ⇒ Result(entity, GlobalEscape)

                case EscapeViaStaticField ⇒ Result(entity, EscapeViaStaticField)

                case EscapeViaHeapObject  ⇒ Result(entity, EscapeViaHeapObject)

                case NoEscape | EscapeInCallee ⇒
                    removeFromDependeesAndComputeResult(other, EscapeInCallee)

                case EscapeViaReturn ⇒
                    /*
                     * IMPROVE we do not further track the return value of the callee.
                     * But the org.opalj.ai.domain.l2.DefaultPerformInvocationsDomainWithCFGAndDefUse
                     * eliminates the assignments, if the function called is identity-like
                     */
                    if (hasReturnValueUseSites contains other)
                        removeFromDependeesAndComputeResult(other, AtMost(EscapeInCallee))
                    else
                        removeFromDependeesAndComputeResult(other, EscapeInCallee)

                case EscapeViaParameter ⇒
                    // IMPROVE we do not further track the field of the actual parameter
                    removeFromDependeesAndComputeResult(other, AtMost(EscapeInCallee))

                case EscapeViaAbnormalReturn ⇒
                    // IMPROVE we do not further track the exception thrown in the callee
                    removeFromDependeesAndComputeResult(other, AtMost(EscapeInCallee))

                case EscapeViaParameterAndAbnormalReturn | EscapeViaNormalAndAbnormalReturn |
                    EscapeViaParameterAndAbnormalReturn | EscapeViaParameterAndReturn |
                    EscapeViaParameterAndNormalAndAbnormalReturn ⇒
                    // combines the cases above
                    removeFromDependeesAndComputeResult(other, AtMost(EscapeInCallee))

                case AtMost(_) ⇒
                    removeFromDependeesAndComputeResult(other, AtMost(EscapeInCallee))

                case p @ Conditional(NoEscape) ⇒
                    performIntermediateUpdate(other, p, EscapeInCallee)

                case p @ Conditional(EscapeInCallee) ⇒
                    performIntermediateUpdate(other, p, EscapeInCallee)

                case p @ Conditional(EscapeViaReturn) ⇒
                    if (hasReturnValueUseSites contains other)
                        performIntermediateUpdate(other, p, AtMost(EscapeInCallee))
                    else
                        performIntermediateUpdate(other, p, EscapeInCallee)

                case p @ Conditional(_) ⇒
                    performIntermediateUpdate(other, p, AtMost(EscapeInCallee))

                case _ ⇒
                    throw new UnknownError(s"unexpected escape property ($p) for $other")
            }
            case _ ⇒ super.c(other, p, u)
        }
    }
}

class InterProceduralEntityEscapeAnalysis(
        val entity:                  Entity,
        val defSite:                 ValueOrigin,
        val uses:                    IntTrieSet,
        val code:                    Array[Stmt[DUVar[(Domain with RecordDefUse)#DomainValue]]],
        val cfg:                     CFG,
        val formalParameters:        FormalParameters,
        val virtualFormalParameters: VirtualFormalParameters,
        val targetMethod:            VirtualMethod,
        val propertyStore:           PropertyStore,
        val project:                 SomeProject
) extends DefaultEntityEscapeAnalysis
    with AbstractInterProceduralEntityEscapeAnalysis
    with ConstructorSensitiveEntityEscapeAnalysis
    with ConfigurationBasedConstructorEscapeAnalysis
    with SimpleFieldAwareEntityEscapeAnalysis
    with ExceptionAwareEntityEscapeAnalysis
