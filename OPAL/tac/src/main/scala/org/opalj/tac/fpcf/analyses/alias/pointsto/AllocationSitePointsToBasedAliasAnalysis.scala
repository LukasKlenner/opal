/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.tac.fpcf.analyses.alias.pointsto

import scala.collection.mutable

import org.opalj.br.Method
import org.opalj.br.analyses.DeclaredMethodsKey
import org.opalj.br.analyses.SomeProject
import org.opalj.br.analyses.VirtualFormalParametersKey
import org.opalj.br.fpcf.BasicFPCFEagerAnalysisScheduler
import org.opalj.br.fpcf.BasicFPCFLazyAnalysisScheduler
import org.opalj.br.fpcf.FPCFAnalysis
import org.opalj.br.fpcf.properties.Context
import org.opalj.br.fpcf.properties.NoContext
import org.opalj.br.fpcf.properties.SimpleContextsKey
import org.opalj.br.fpcf.properties.alias.Alias
import org.opalj.br.fpcf.properties.alias.AliasEntity
import org.opalj.br.fpcf.properties.alias.AliasField
import org.opalj.br.fpcf.properties.alias.AliasFP
import org.opalj.br.fpcf.properties.alias.AliasReturnValue
import org.opalj.br.fpcf.properties.alias.AliasSourceElement
import org.opalj.br.fpcf.properties.alias.AliasUVar
import org.opalj.br.fpcf.properties.cg.Callers
import org.opalj.br.fpcf.properties.cg.NoCallers
import org.opalj.fpcf.PropertyBounds
import org.opalj.fpcf.PropertyStore
import org.opalj.tac.Assignment
import org.opalj.tac.Call
import org.opalj.tac.DUVar
import org.opalj.tac.DVar
import org.opalj.tac.ExprStmt
import org.opalj.tac.TACMethodParameter
import org.opalj.tac.TACode
import org.opalj.tac.UVar
import org.opalj.tac.fpcf.analyses.alias.persistentUVar
import org.opalj.tac.fpcf.analyses.pointsto.AbstractPointsToAnalysis
import org.opalj.tac.fpcf.analyses.pointsto.AllocationSiteBasedAnalysis
import org.opalj.tac.fpcf.properties.TACAI
import org.opalj.value.ValueInformation

class AllocationSitePointsToBasedAliasAnalysis(final val project: SomeProject)
    extends AbstractPointsToBasedAliasAnalysis with AbstractPointsToAnalysis with AllocationSiteBasedAnalysis

object LazyPointsToBasedAliasAnalysisScheduler extends PointsToBasedAliasAnalysisScheduler
    with BasicFPCFLazyAnalysisScheduler {
    override def derivesLazily: Some[PropertyBounds] = Some(derivedProperty)

    override def register(
        project:       SomeProject,
        propertyStore: PropertyStore,
        i:             LazyPointsToBasedAliasAnalysisScheduler.InitializationData
    ): FPCFAnalysis = {

        val analysis = new AllocationSitePointsToBasedAliasAnalysis(project)

        propertyStore.registerLazyPropertyComputation(
            Alias.key,
            analysis.determineAlias
        )

        analysis
    }
}

object EagerPointsToBasedAliasAnalysisScheduler extends PointsToBasedAliasAnalysisScheduler
    with BasicFPCFEagerAnalysisScheduler {

    override def start(p: SomeProject, ps: PropertyStore, unused: Null): FPCFAnalysis = {
        val analysis = new AllocationSitePointsToBasedAliasAnalysis(p)
        val simpleContexts = p.get(SimpleContextsKey)
        val declaredMethods = p.get(DeclaredMethodsKey)

        val methods = declaredMethods.declaredMethods
        val callersProperties = ps(methods.to(Iterable), Callers)
        assert(callersProperties.forall(_.isFinal))

        val reachableMethods = callersProperties
            .filterNot(_.asFinal.p == NoCallers)
            .map { v => v.e -> v.ub }
            .toMap

        type Tac = TACode[TACMethodParameter, DUVar[ValueInformation]]

        val uVars: mutable.Set[(UVar[ValueInformation], Method, Tac)] =
            mutable.Set.empty[(UVar[ValueInformation], Method, Tac)]
        val dVars: mutable.Set[(DVar[ValueInformation], Method, Tac)] =
            mutable.Set.empty[(DVar[ValueInformation], Method, Tac)]

        def handleCall(call: Call[_], m: Method, tac: Tac) = {
            call.allParams.foreach {
                case uVar: UVar[ValueInformation] => uVars.add((uVar, m, tac))
                case _                            =>
            }

            if (call.receiverOption.isDefined) {
                call.receiverOption.get match {
                    case uVar: UVar[ValueInformation] => uVars.add((uVar, m, tac))
                    case _                            =>
                }
            }
        }

        reachableMethods.keys
            .filter(m => m.hasSingleDefinedMethod || m.hasMultipleDefinedMethods)
            .foreach(m =>
                m.foreachDefinedMethod(m => {

                    if (m.body.isDefined) {
                        val tac = ps(m, TACAI.key).asFinal.p.tac.get

                        tac.stmts.foreach {
                            case Assignment(_, dVar: DVar[ValueInformation], _) => dVars.add((dVar, m, tac))
                            case call: Call[_]                                  => handleCall(call, m, tac)
                            case ExprStmt(_, expr) => expr match {
                                    case uVar: UVar[ValueInformation] => uVars.add((uVar, m, tac))
                                    case call: Call[_]                => handleCall(call, m, tac)
                                    case _                            =>
                                }
                            case _ =>
                        }
                    }

                })
            )
        val formalParameters = p
            .get(VirtualFormalParametersKey)
            .virtualFormalParameters
            .filter(fp => reachableMethods.contains(fp.method))

        val fields = p.allFields
            .filter(f => reachableMethods.exists(_._1.declaringClassType.fqn.eq(f.classFile.fqn)))

        val aliasEntities: Seq[AliasSourceElement] = formalParameters.map(AliasFP).toSeq ++
            uVars.map(uVar => AliasUVar(persistentUVar(uVar._1)(uVar._3.stmts), uVar._2, p)) ++
            fields.map(AliasField)

        def getContext(ase: AliasSourceElement): Context = {
            if (ase.isMethodBound) {
                simpleContexts(declaredMethods(ase.method))
            } else {
                NoContext
            }
        }

        def getClass(e: AliasSourceElement): String = {
            e match {
                case AliasFP(fp)                 => fp.method.definedMethod.classFile.fqn
                case AliasUVar(_, m, _)          => m.classFile.fqn
                case AliasField(field)           => field.classFile.fqn
                case AliasReturnValue(method, _) => method.classFile.fqn
                case _                           => ""
            }
        }

        val entities = (for (e1 <- aliasEntities; e2 <- aliasEntities) yield (e1, e2))
            .distinct
            .filterNot(e => e._1.isMethodBound && e._2.isMethodBound && e._1.method != e._2.method)
            .filterNot(e => e._1.isAliasField && !e._2.isAliasField && getClass(e._1) != getClass(e._2))
            .filterNot(e => !e._1.isAliasField && e._2.isAliasField && getClass(e._1) != getClass(e._2))
            .map(e => AliasEntity(getContext(e._1), getContext(e._2), e._1, e._2))
            .distinct

        ps.scheduleEagerComputationsForEntities(entities)(analysis.determineAlias)
        analysis
    }

    override def derivesEagerly: Set[PropertyBounds] = Set(derivedProperty)

}
