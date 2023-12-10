/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.tac.fpcf.analyses.alias

import org.opalj.br.analyses.DeclaredMethodsKey
import org.opalj.br.analyses.ProjectInformationKeys
import org.opalj.br.analyses.SomeProject
import org.opalj.br.analyses.VirtualFormalParametersKey
import org.opalj.br.analyses.cg.IsOverridableMethodKey
import org.opalj.br.fpcf.BasicFPCFEagerAnalysisScheduler
import org.opalj.br.fpcf.FPCFAnalysis
import org.opalj.br.fpcf.FPCFAnalysisScheduler
import org.opalj.br.fpcf.properties.Alias
import org.opalj.br.fpcf.properties.MayAlias
import org.opalj.br.fpcf.properties.SimpleContextsKey
import org.opalj.br.fpcf.properties.pointsto.AllocationSitePointsToSet
import org.opalj.br.fpcf.properties.pointsto.TypeBasedPointsToSet
import org.opalj.fpcf.EPK
import org.opalj.fpcf.Entity
import org.opalj.fpcf.ProperPropertyComputationResult
import org.opalj.fpcf.PropertyBounds
import org.opalj.fpcf.PropertyKey
import org.opalj.fpcf.PropertyStore
import org.opalj.fpcf.Result
import org.opalj.tac.cg.TypeIteratorKey
import org.opalj.tac.common.DefinitionSitesKey
import org.opalj.tac.fpcf.analyses.pointsto.TypeBasedPointsToAnalysis
import org.opalj.tac.fpcf.properties.TACAI
import org.opalj.tac.fpcf.properties.cg.Callees
import org.opalj.tac.fpcf.properties.cg.Callers
import org.opalj.tac.fpcf.properties.cg.NoCallers

import scala.collection.mutable.ArrayBuffer

class PointsToBasedAliasAnalysis( final val project: SomeProject) extends TacBasedAliasAnalysis {

    override type AnalysisContext = PointsToBasedAliasAnalysisContext
    override type AnalysisState = AliasAnalysisState

    override def analyzeTAC()(implicit context: AnalysisContext, state: AnalysisState): ProperPropertyComputationResult = {

        val pointsToAnalysis = new AliasPointsToAnalysis(context)

        val epk = EPK(
            pointsToAnalysis.toEntity(context.entity.element1.definitionSite),
            pointsToAnalysis._pointsToPropertyKey
        )
        val pointsTo = propertyStore(epk)
        print(pointsTo)

        Result(context.entity, MayAlias)

    }

    override protected[this] def createState: AnalysisState = new AliasAnalysisState

    override protected[this] def createContext(
        entity: AliasEntity
    ): PointsToBasedAliasAnalysisContext =
        new PointsToBasedAliasAnalysisContext(entity, project, propertyStore)

    private class AliasPointsToAnalysis(val aliasAnalysisContext: AnalysisContext)
        extends TypeBasedPointsToAnalysis(project) {

        val _pointsToPropertyKey: PropertyKey[TypeBasedPointsToSet] = pointsToPropertyKey

        def toEntity(defSite: Int): Entity = super.toEntity(defSite)(state)

        private val state: super.State = new super.State(
            aliasAnalysisContext.typeIterator
                .newContext(aliasAnalysisContext.element1.declaredMethod)
                .asInstanceOf[ContextType],
            propertyStore(aliasAnalysisContext.element1.method, TACAI.key)
        )

    }

}

sealed trait PointsToBasedAliasAnalysisScheduler extends FPCFAnalysisScheduler {

    override def requiredProjectInformation: ProjectInformationKeys =
        Seq(DeclaredMethodsKey, VirtualFormalParametersKey, IsOverridableMethodKey, TypeIteratorKey)

    final def derivedProperty: PropertyBounds = PropertyBounds.lub(Alias)

    override def uses: Set[PropertyBounds] = Set(
        PropertyBounds.ub(TACAI),
        PropertyBounds.ub(Callees),
        PropertyBounds.ub(AllocationSitePointsToSet)
    )
}

object EagerPointsToBasedAliasAnalysis extends PointsToBasedAliasAnalysisScheduler with BasicFPCFEagerAnalysisScheduler {

    override def requiredProjectInformation: ProjectInformationKeys =
        super.requiredProjectInformation ++ Seq(DefinitionSitesKey, SimpleContextsKey)

    override def start(p: SomeProject, ps: PropertyStore, unused: Null): FPCFAnalysis = {
        val analysis = new PointsToBasedAliasAnalysis(p)
        val simpleContexts = p.get(SimpleContextsKey)
        val declaredMethods = p.get(DeclaredMethodsKey)

        val methods = declaredMethods.declaredMethods
        val callersProperties = ps(methods.to(Iterable), Callers)
        assert(callersProperties.forall(_.isFinal))

        val reachableMethods = callersProperties
            .filterNot(_.asFinal.p == NoCallers)
            .map { v =>
                v.e -> v.ub
            }
            .toMap

        val allocationSites = p
            .get(DefinitionSitesKey)
            .getAllocationSites
            .filter(as => reachableMethods.contains(declaredMethods(as.method)))
        val formalParameters = p
            .get(VirtualFormalParametersKey)
            .virtualFormalParameters
            .filter(fp => reachableMethods.contains(fp.method))

        val aliasEntities: Seq[AliasSourceElement] = allocationSites.map(AliasDS(_, p)) ++ formalParameters
            .map(AliasFP)

        val entities: ArrayBuffer[AliasEntity] = ArrayBuffer.empty

        for (e1 <- aliasEntities) {
            for (e2 <- aliasEntities) {
                if (e1 != e2 && e1.method == e2.method) {
                    val context = simpleContexts(declaredMethods(e1.method))
                    val entity = AliasEntity(context, e1, e2)

                    if (!entities.contains(entity)) {
                        entities.addOne(entity)
                    }
                }
            }

        }

        ps.scheduleEagerComputationsForEntities(entities)(analysis.determineAlias)
        analysis
    }

    override def derivesEagerly: Set[PropertyBounds] = Set(derivedProperty)

    override def uses: Set[PropertyBounds] = super.uses + PropertyBounds.finalP(Callers) + PropertyBounds.finalP(AllocationSitePointsToSet)

    override def derivesCollaboratively: Set[PropertyBounds] = Set.empty

}
