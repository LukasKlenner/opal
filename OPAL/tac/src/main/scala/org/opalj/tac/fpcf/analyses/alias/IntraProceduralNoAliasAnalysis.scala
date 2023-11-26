/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org
package opalj
package tac
package fpcf
package analyses
package alias

import org.opalj.br.analyses.DeclaredMethodsKey
import org.opalj.br.analyses.ProjectInformationKeys
import org.opalj.br.analyses.SomeProject
import org.opalj.br.analyses.VirtualFormalParametersKey
import org.opalj.br.analyses.cg.IsOverridableMethodKey
import org.opalj.br.fpcf.BasicFPCFEagerAnalysisScheduler
import org.opalj.br.fpcf.FPCFAnalysis
import org.opalj.br.fpcf.FPCFAnalysisScheduler
import org.opalj.br.fpcf.properties.Alias
import org.opalj.br.fpcf.properties.Context
import org.opalj.br.fpcf.properties.SimpleContextsKey
import org.opalj.fpcf.PropertyBounds
import org.opalj.fpcf.PropertyStore
import org.opalj.tac.StaticMethodCall
import org.opalj.tac.cg.TypeIteratorKey
import org.opalj.tac.common.DefinitionSiteLike
import org.opalj.tac.common.DefinitionSitesKey
import org.opalj.tac.fpcf.properties.TACAI
import org.opalj.tac.fpcf.properties.cg.Callees
import org.opalj.tac.fpcf.properties.cg.Callers
import org.opalj.tac.fpcf.properties.cg.NoCallers

import scala.collection.mutable.ArrayBuffer

class IntraProceduralNoAliasAnalysis( final val project: SomeProject) extends AbstractAliasAnalysis {

    protected[this] def handleStaticMethodCall(
        methodCall: StaticMethodCall[V]
    )(context: AliasAnalysisContext): Unit = {

        for (arg <- methodCall.params) {
            arg match {
                case v: V =>
                    if (v.definedBy.contains(context.entity1.asInstanceOf[DefinitionSiteLike].pc) && v.definedBy
                        .contains(context.entity2.asInstanceOf[DefinitionSiteLike].pc)) {
                        //state.setMayAlias(true)
                    }

                case _ => throw new UnknownError("unhandled arg type")
            }
        }

    }

    override type AnalysisContext = AliasAnalysisContext
    override type AnalysisState = AliasAnalysisState

    override protected[this] def createState: AliasAnalysisState =
        new AliasAnalysisState

    override protected[this] def handleAssignment(
        assignment: Assignment[V]
    )(implicit context: AliasAnalysisContext, state: AliasAnalysisState): Unit = {}

    override protected[this] def createContext(
        entity: (Context, AliasEntity, AliasEntity)
    ): AliasAnalysisContext =
        new AliasAnalysisContext(entity, project, propertyStore)
}

sealed trait AliasAnalysisScheduler extends FPCFAnalysisScheduler {

    override def requiredProjectInformation: ProjectInformationKeys =
        Seq(DeclaredMethodsKey, VirtualFormalParametersKey, IsOverridableMethodKey, TypeIteratorKey)

    final def derivedProperty: PropertyBounds = PropertyBounds.lub(Alias)

    override def uses: Set[PropertyBounds] = Set(
        PropertyBounds.ub(TACAI),
        PropertyBounds.ub(Callees),
        PropertyBounds.lub(Alias)
    )
}
object EagerAliasAnalysis extends AliasAnalysisScheduler with BasicFPCFEagerAnalysisScheduler {

    override def requiredProjectInformation: ProjectInformationKeys =
        super.requiredProjectInformation ++ Seq(DefinitionSitesKey, SimpleContextsKey)

    override def start(p: SomeProject, ps: PropertyStore, unused: Null): FPCFAnalysis = {
        val analysis = new IntraProceduralNoAliasAnalysis(p)
        val simpleContexts = p.get(SimpleContextsKey)

        /*val declaredMethods = p.get(DeclaredMethodsKey)
    val simpleContexts = p.get(SimpleContextsKey)

    val allAllocationSites = p.get(DefinitionSitesKey).getAllocationSites

    val entities: ArrayBuffer[(Context, Entity, Entity)] = ArrayBuffer.empty

    for (as1 <- allAllocationSites) {
        for (as2 <- allAllocationSites) {
          if (as1 != as2 && as1.method == as2.method) {
            val dm = declaredMethods(as1.method)
            val entity = (simpleContexts(dm), as1, as2)
            if (!entities.contains(entity) && !entities.contains((simpleContexts(dm), as2, as1))) {
              entities.addOne(entity)
            }
          }

      }
    }*/

        val declaredMethods = p.get(DeclaredMethodsKey)
        //implicit val typeIterator: TypeIterator = p.get(TypeIteratorKey)

        val methods = declaredMethods.declaredMethods
        val callersProperties = ps(methods.to(Iterable), Callers)
        assert(callersProperties.forall(_.isFinal))

        val reachableMethods = callersProperties
            .filterNot(_.asFinal.p == NoCallers)
            .map { v =>
                v.e -> v.ub
            }
            .toMap

        val allocationSites = p.get(DefinitionSitesKey).getAllocationSites.filter(as => reachableMethods.contains(declaredMethods(as.method)))
        val formalParameters = p.get(VirtualFormalParametersKey).virtualFormalParameters.filter(fp => reachableMethods.contains(fp.method))

        val aliasEntities: Seq[AliasEntity] = allocationSites.map(AliasDS) ++ formalParameters.map(AliasFP)

        val entities: ArrayBuffer[(Context, AliasEntity, AliasEntity)] = ArrayBuffer.empty

        for (e1 <- aliasEntities) {
            for (e2 <- aliasEntities) {
                if (e1 != e2 && e1.method == e2.method) {
                    val context = simpleContexts(declaredMethods(e1.method))
                    val entity = if (e1.hashCode() < e2.hashCode()) (context, e1, e2) else (context, e2, e1)

                    if (!entities.contains(entity)) {
                        entities.addOne(entity)
                    }
                    //                        val dm = declaredMethods(as1.method)
                    //                        entities.addAll(
                    //                            reachableMethods(dm)
                    //                                .calleeContexts(dm)
                    //                                .iterator
                    //                                .filter(
                    //                                    c => !(entities.contains((c, as1, as2)) || entities.contains((c, as2, as1)))
                    //                                )
                    //                                .map((_, as1, as2))
                    //                        )
                }
            }

        }

        ps.scheduleEagerComputationsForEntities(entities)(analysis.determineAlias)
        analysis
    }

    override def derivesEagerly: Set[PropertyBounds] = Set(derivedProperty)

    override def uses: Set[PropertyBounds] = super.uses + PropertyBounds.finalP(Callers)

    override def derivesCollaboratively: Set[PropertyBounds] = Set.empty

}
