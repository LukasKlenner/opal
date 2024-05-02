/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj
package tac
package fpcf
package analyses
package alias

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

import org.opalj.br.DeclaredField
import org.opalj.br.Method
import org.opalj.br.MethodDescriptor
import org.opalj.br.ObjectType
import org.opalj.br.ReferenceType
import org.opalj.br.analyses.DeclaredFieldsKey
import org.opalj.br.analyses.DeclaredMethodsKey
import org.opalj.br.analyses.SomeProject
import org.opalj.br.fpcf.BasicFPCFEagerAnalysisScheduler
import org.opalj.br.fpcf.FPCFAnalysis
import org.opalj.br.fpcf.analyses.SimpleContextProvider
import org.opalj.br.fpcf.properties.CallStringContext
import org.opalj.br.fpcf.properties.Context
import org.opalj.br.fpcf.properties.SimpleContexts
import org.opalj.br.fpcf.properties.SimpleContextsKey
import org.opalj.br.fpcf.properties.alias.Alias
import org.opalj.br.fpcf.properties.alias.AliasEntity
import org.opalj.br.fpcf.properties.alias.AliasUVar
import org.opalj.br.fpcf.properties.alias.MayAlias
import org.opalj.br.fpcf.properties.alias.NoAlias
import org.opalj.br.fpcf.properties.alias.PersistentUVar
import org.opalj.br.fpcf.properties.cg.Callers
import org.opalj.br.fpcf.properties.cg.NoCallers
import org.opalj.fpcf.EOptionP
import org.opalj.fpcf.InterimResult
import org.opalj.fpcf.ProperPropertyComputationResult
import org.opalj.fpcf.PropertyBounds
import org.opalj.fpcf.PropertyStore
import org.opalj.fpcf.SomeEPS
import org.opalj.fpcf.UBP
import org.opalj.tac.cg.TypeIteratorKey
import org.opalj.tac.fpcf.analyses.cg.TypeIterator
import org.opalj.tac.fpcf.properties.TACAI
import org.opalj.value.ValueInformation

/**
 * A base trait for all alias analyses based on the TACAI.
 */
trait TacBasedAliasAnalysis extends AbstractAliasAnalysis {

    override protected[this] type AnalysisState <: TacBasedAliasAnalysisState

    override def doDetermineAlias(
        implicit
        context: AnalysisContext,
        state:   AnalysisState
    ): ProperPropertyComputationResult = {

        if (context.element1.isMethodBound) retrieveTAC(context.element1.method)
        if (context.element2.isMethodBound) retrieveTAC(context.element2.method)

        if (bothTacaisDefined) analyzeTAC()
        else interimResult(NoAlias, MayAlias)
    }

    /**
     * Computes the alias relation of the [[org.opalj.br.fpcf.properties.alias.AliasEntity]] using the TAC representation
     * of the corresponding methods.
     *
     * This method is called when the TACs of the methods of both elements are available. If an element is not method
     * bound, it is not considered.
     *
     * @return The result of the computation.
     */
    protected[this] def analyzeTAC()(
        implicit
        context: AnalysisContext,
        state:   AnalysisState
    ): ProperPropertyComputationResult

    /**
     * Retrieves the TACAI for the given method.
     */
    private[this] def retrieveTAC(
        m: Method
    )(
        implicit
        context: AnalysisContext,
        state:   AnalysisState
    ): Unit = {
        val tacai: EOptionP[Method, TACAI] = propertyStore(m, TACAI.key)

        state.addTacEPSToMethod(tacai.asEPS, m)

        if (tacai.isRefinable) {
            state.addDependency(tacai)
        }

        if (tacai.hasUBP && tacai.ub.tac.isDefined) {
            state.updateTACAI(m, tacai.ub.tac.get)
        }
    }

    /**
     * Continues the computation when a TACAI property is updated.
     */
    override protected[this] def continuation(
        someEPS: SomeEPS
    )(
        implicit
        context: AnalysisContext,
        state:   AnalysisState
    ): ProperPropertyComputationResult = {
        someEPS match {
            case UBP(ub: TACAI) =>
                state.removeDependency(someEPS)

                if (someEPS.isRefinable) state.addDependency(someEPS)
                if (ub.tac.isDefined) state.updateTACAI(state.getMethodForTacEPS(someEPS), ub.tac.get)

                if (bothTacaisDefined) analyzeTAC()
                else InterimResult(context.entity, NoAlias, MayAlias, state.getDependees, continuation)
            case _ =>
                throw new UnknownError(s"unhandled property (${someEPS.ub} for ${someEPS.e}")
        }
    }

    /**
     * @return `true` if both TACs are defined. If one of the elements is not method bound, it is not considered.
     */
    private[this] def bothTacaisDefined(
        implicit
        context: AnalysisContext,
        state:   AnalysisState
    ): Boolean = {
        (!context.element1.isMethodBound || state.tacai1.isDefined) &&
        (!context.element2.isMethodBound || state.tacai2.isDefined)
    }

}

trait EagerFieldAccessAliasAnalysisScheduler extends BasicFPCFEagerAnalysisScheduler {

    def createAnalysis(p: SomeProject): TacBasedAliasAnalysis

    override def start(p: SomeProject, ps: PropertyStore, unused: Null): FPCFAnalysis = {
        val analysis = createAnalysis(p)

        implicit val simpleContexts: SimpleContexts = p.get(SimpleContextsKey)
        implicit val typeIterator: TypeIterator = p.get(TypeIteratorKey)
        val declaredMethods = p.get(DeclaredMethodsKey)
        val declaredFields = p.get(DeclaredFieldsKey)

        val methods = declaredMethods.declaredMethods
        val callersProperties = ps(methods.to(Iterable), Callers)
        assert(callersProperties.forall(_.isFinal))

        val reachableMethods = callersProperties
            .filterNot(_.asFinal.p == NoCallers)
            .map { v => v.e -> v.ub }
            .toMap

        val aliasFieldsMap: mutable.Map[DeclaredField, mutable.Set[AliasUVar]] =
            mutable.Map.empty[DeclaredField, mutable.Set[AliasUVar]]

        def handleFieldAccess(
            tac:            TACode[TACMethodParameter, DUVar[ValueInformation]],
            declaringClass: ObjectType,
            name:           String,
            fieldType:      ReferenceType,
            method:         Method,
            objRef:         UVar[ValueInformation]
        ): Unit = {
            val declaredField = declaredFields(declaringClass, name, fieldType)

            if (fieldType == ObjectType.Object && name == "value") {
                return
            }

            aliasFieldsMap.getOrElseUpdate(declaredField, mutable.Set.empty).addOne(AliasUVar(
                PersistentUVar(
                    objRef.value,
                    objRef.defSites.map(i => if (i < 0) i else tac.stmts(i).pc)
                ),
                method,
                p
            ))
        }

        reachableMethods.keys
            .filter(m => m.hasSingleDefinedMethod || m.hasMultipleDefinedMethods)
            .foreach(m =>
                m.foreachDefinedMethod(m => {

                    if (m.body.isDefined) {
                        val tac = ps(m, TACAI.key).asFinal.p.tac.get

                        tac.stmts.foreach {
                            case Assignment(
                                    _,
                                    _,
                                    GetField(_, declaringClass, name, fieldType: ReferenceType, objRef: UVar[_])
                                ) => handleFieldAccess(tac, declaringClass, name, fieldType, m, objRef)
                            case PutField(
                                    _,
                                    declaringClass,
                                    name,
                                    fieldType: ReferenceType,
                                    objRef: UVar[_],
                                    _
                                ) => handleFieldAccess(tac, declaringClass, name, fieldType, m, objRef)
                            case _ =>
                        }
                    }

                })
            )

        val entities = ArrayBuffer.empty[AliasEntity]

        for (aliasFields <- aliasFieldsMap.values.map(_.toSeq)) {
            for (i <- aliasFields.indices) {
                val e1 = aliasFields(i)
                for (j <- i + 1 until aliasFields.size) {
                    val e2 = aliasFields(j)

                    val context1 = createContext(e1, ps)
                    val context2 = createContext(e2, ps)

                    if (e1.method == e2.method) {
                        for (c1 <- context1) {
                            entities += AliasEntity(c1, c1, e1, e2)
                        }
                    } else {
                        for (c1 <- context1; c2 <- context2) {
                            entities += AliasEntity(c1, c2, e1, e2)
                        }
                    }
                }
            }
        }

        ps.scheduleEagerComputationsForEntities(entities.distinct)(analysis.determineAlias)
        analysis
    }

    private[this] def createContext(uVar: AliasUVar, ps: PropertyStore)(
        implicit
        simpleContexts: SimpleContexts,
        typeIterator:   TypeIterator
    ): Iterable[Context] = {
        val declaredMethod = uVar.declaredMethod

        typeIterator match {
            case _: SimpleContextProvider => Seq(simpleContexts(declaredMethod))
            case _ => ps(declaredMethod, Callers.key).ub.callContexts(declaredMethod).iterator.toSeq.map(
                    _._1.asInstanceOf[CallStringContext]
                )
        }

    }

    override def derivesEagerly: Set[PropertyBounds] = Set(PropertyBounds.lub(Alias))

}

trait EagerMethodInvocationAliasAnalysisScheduler extends BasicFPCFEagerAnalysisScheduler {

    def createAnalysis(p: SomeProject): TacBasedAliasAnalysis

    override def start(p: SomeProject, ps: PropertyStore, unused: Null): FPCFAnalysis = {
        val analysis = createAnalysis(p)

        implicit val simpleContexts: SimpleContexts = p.get(SimpleContextsKey)
        implicit val typeIterator: TypeIterator = p.get(TypeIteratorKey)
        val declaredMethods = p.get(DeclaredMethodsKey)

        val methods = declaredMethods.declaredMethods
        val callersProperties = ps(methods.to(Iterable), Callers)
        assert(callersProperties.forall(_.isFinal))

        val reachableMethods = callersProperties
            .filterNot(_.asFinal.p == NoCallers)
            .map { v => v.e -> v.ub }
            .toMap

        type Invocation = (ReferenceType, String, MethodDescriptor)

        val aliasUVarsMap: mutable.Map[ObjectType, mutable.Map[Invocation, mutable.Set[AliasUVar]]] =
            mutable.Map.empty[ObjectType, mutable.Map[Invocation, mutable.Set[AliasUVar]]]

        reachableMethods.keys
            .filter(m => m.hasSingleDefinedMethod || m.hasMultipleDefinedMethods)
            .foreach(m => m.foreachDefinedMethod(handleMethod(_, ps, p, aliasUVarsMap)))

        val entities = ArrayBuffer.empty[AliasEntity]

        for (uVarsInClass <- aliasUVarsMap.values) {
            for (uVarsForMethod <- uVarsInClass.values.map(_.toSeq)) {
                for (i <- uVarsForMethod.indices) {
                    val e1 = uVarsForMethod(i)
                    for (j <- i + 1 until uVarsForMethod.size) {
                        val e2 = uVarsForMethod(j)

                        val context1 = createContext(e1, ps)
                        val context2 = createContext(e2, ps)

                        if (e1.method == e2.method) {
                            for (c1 <- context1) {
                                entities += AliasEntity(c1, c1, e1, e2)
                            }
                        } else {
                            for (c1 <- context1; c2 <- context2) {
                                entities += AliasEntity(c1, c2, e1, e2)
                            }
                        }
                    }
                }
            }
        }

        ps.scheduleEagerComputationsForEntities(entities.distinct)(analysis.determineAlias)
        analysis
    }

    private def handleMethod(
        m:  Method,
        ps: PropertyStore,
        p:  SomeProject,
        aliasUVarsMap: mutable.Map[
            ObjectType,
            mutable.Map[(ReferenceType, String, MethodDescriptor), mutable.Set[AliasUVar]]
        ]
    ): Unit = {
        if (m.body.isDefined) {
            val tac = ps(m, TACAI.key).asFinal.p.tac.get

            val uVarMap = aliasUVarsMap.getOrElseUpdate(m.classFile.thisType, mutable.Map.empty)

            def handleCall(call: Call[V]): Unit = {

                if (call.receiverOption.isEmpty || call.name == "<init>" || call.name == "<clinit>") {
                    return
                }

                val receiver = call.receiverOption.get.asVar

                if (receiver.definedBy.size == 1 && receiver.definedBy.head == -1) {
                    return
                }

                uVarMap.getOrElseUpdate(
                    (call.declaringClass, call.name, call.descriptor),
                    mutable.Set.empty
                ).addOne(AliasUVar(
                    PersistentUVar(
                        receiver.value,
                        receiver.definedBy.map(i => if (i < 0) i else tac.stmts(i).pc)
                    ),
                    m,
                    p
                ))
            }

            for (stmt <- tac.stmts) {
                stmt match {
                    case c: Call[_]                   => handleCall(c.asInstanceOf[Call[V]])
                    case Assignment(_, _, c: Call[_]) => handleCall(c.asInstanceOf[Call[V]])
                    case ExprStmt(_, c: Call[_])      => handleCall(c.asInstanceOf[Call[V]])
                    case _                            =>
                }
            }

            //                        tac.stmts.foreach {
            //                            case c: Call[_] => handleCall(c.asInstanceOf[Call[V]])
            //                            case Assignment(_, _, c: Call[_]) => handleCall(c.asInstanceOf[Call[V]])
            //                            case _ =>
            //                        }
        }

    }

    private[this] def createContext(uVar: AliasUVar, ps: PropertyStore)(
        implicit
        simpleContexts: SimpleContexts,
        typeIterator:   TypeIterator
    ): Iterable[Context] = {
        val declaredMethod = uVar.declaredMethod

        typeIterator match {
            case _: SimpleContextProvider => Seq(simpleContexts(declaredMethod))
            case _ => ps(declaredMethod, Callers.key).ub.callContexts(declaredMethod).iterator.toSeq.map(
                    _._1.asInstanceOf[CallStringContext]
                )
        }

    }

    override def derivesEagerly: Set[PropertyBounds] = Set(PropertyBounds.lub(Alias))

}
