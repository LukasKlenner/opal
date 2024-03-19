/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.tac.fpcf.analyses.alias.pointsto

import scala.collection.mutable

import org.opalj.br.Method
import org.opalj.br.analyses.DeclaredMethodsKey
import org.opalj.br.analyses.SomeProject
import org.opalj.br.analyses.VirtualFormalParameter
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
import org.opalj.br.fpcf.properties.alias.AliasFormalParameter
import org.opalj.br.fpcf.properties.alias.AliasNull
import org.opalj.br.fpcf.properties.alias.AliasReturnValue
import org.opalj.br.fpcf.properties.alias.AliasSourceElement
import org.opalj.br.fpcf.properties.alias.AliasStaticField
import org.opalj.br.fpcf.properties.alias.AliasUVar
import org.opalj.br.fpcf.properties.alias.FieldReference
import org.opalj.br.fpcf.properties.cg.Callers
import org.opalj.br.fpcf.properties.cg.NoCallers
import org.opalj.fpcf.PropertyBounds
import org.opalj.fpcf.PropertyStore
import org.opalj.tac.Call
import org.opalj.tac.DUVar
import org.opalj.tac.ExprStmt
import org.opalj.tac.PutField
import org.opalj.tac.TACMethodParameter
import org.opalj.tac.TACode
import org.opalj.tac.UVar
import org.opalj.tac.fpcf.analyses.alias.persistentUVar
import org.opalj.tac.fpcf.analyses.pointsto.AbstractPointsToAnalysis
import org.opalj.tac.fpcf.analyses.pointsto.AllocationSiteBasedAnalysis
import org.opalj.tac.fpcf.properties.TACAI
import org.opalj.value.ValueInformation

/**
 * An alias analysis based on points-to information that are computed using allocation sites.
 * @param project The project
 */
class AllocationSitePointsToBasedAliasAnalysis(final val project: SomeProject)
    extends AbstractPointsToBasedAliasAnalysis with AbstractPointsToAnalysis with AllocationSiteBasedAnalysis

/**
 * A scheduler for a lazy, allocation site, points-to based alias analysis.
 */
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

/**
 * A best effort scheduler for a eager, allocation site, points-to based alias analysis.
 *
 * This scheduler is best effort because it does not guarantee that all possible entities are computed.
 *
 * Only reachable methods are considered.
 *
 * Warning: This scheduler should only be used for very small projects. Otherwise, due to the quadratic blowup of the
 * number of entities, the analysis will take a lot of time and resources to complete.
 */
object EagerPointsToBasedAliasAnalysisScheduler extends PointsToBasedAliasAnalysisScheduler
    with BasicFPCFEagerAnalysisScheduler {

    override def start(p: SomeProject, ps: PropertyStore, unused: Null): FPCFAnalysis = {
        val analysis = new AllocationSitePointsToBasedAliasAnalysis(p)

        val simpleContexts = p.get(SimpleContextsKey)
        val declaredMethods = p.get(DeclaredMethodsKey)

        val methods = declaredMethods.declaredMethods

        val callersProperties = ps(methods.to(Iterable), Callers)
        val reachableMethods = callersProperties
            .filterNot(_.asFinal.p == NoCallers)
            .map { v => v.e -> v.ub }
            .toMap

        type Tac = TACode[TACMethodParameter, DUVar[ValueInformation]]

        val uVars: mutable.Set[AliasUVar] = mutable.Set.empty[AliasUVar]

        val fieldReferences: mutable.Set[FieldReference] = mutable.Set.empty[FieldReference]

        def handleCall(call: Call[_], m: Method, tac: Tac): Unit = {
            call.allParams.foreach {
                case uVar: UVar[ValueInformation] => uVars += AliasUVar(persistentUVar(uVar)(tac.stmts), m, p)
                case _                            =>
            }

            if (call.receiverOption.isDefined) {
                call.receiverOption.get match {
                    case uVar: UVar[ValueInformation] => uVars += AliasUVar(persistentUVar(uVar)(tac.stmts), m, p)
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
                            case call: Call[_] => handleCall(call, m, tac)
                            case ExprStmt(_, expr) => expr match {
                                    case uVar: UVar[ValueInformation] =>
                                        uVars += AliasUVar(persistentUVar(uVar)(tac.stmts), m, p)
                                    case call: Call[_] => handleCall(call, m, tac)
                                    case _             =>
                                }
                            case PutField(_, _, name, declaredFieldType, UVar(_, objRefDefSites), _) =>
                                fieldReferences +=
                                    FieldReference(
                                        m.classFile.findField(name, declaredFieldType).get,
                                        simpleContexts(declaredMethods(m)),
                                        objRefDefSites
                                    )
                            case _ =>
                        }
                    }

                })
            )

        val formalParameters = p
            .get(VirtualFormalParametersKey)
            .virtualFormalParameters
            .filter(fp => reachableMethods.contains(fp.method))

        val thisParameter = reachableMethods.keys
            .filter(m => !m.definedMethod.isStatic)
            .map(m => VirtualFormalParameter(m, -1))

        val staticFields = p.allFields.filter(_.isStatic)

        val aliasEntities = {
            Seq(AliasNull) ++
                formalParameters.map(AliasFormalParameter) ++
                thisParameter.map(AliasFormalParameter) ++
                staticFields.map(AliasStaticField) ++
                fieldReferences.map(AliasField) ++
                reachableMethods.keys.map(m => AliasReturnValue(m.definedMethod, p)) ++
                uVars
        }

        def getContext(ase: AliasSourceElement): Context = {
            if (ase.isMethodBound) {
                simpleContexts(declaredMethods(ase.method))
            } else {
                NoContext
            }
        }

        val entities = (for (e1 <- aliasEntities; e2 <- aliasEntities) yield (e1, e2))
            .distinct
            .map(e => AliasEntity(getContext(e._1), getContext(e._2), e._1, e._2))
            .distinct

        ps.scheduleEagerComputationsForEntities(entities)(analysis.determineAlias)
        analysis
    }

    override def derivesEagerly: Set[PropertyBounds] = Set(derivedProperty)

}
