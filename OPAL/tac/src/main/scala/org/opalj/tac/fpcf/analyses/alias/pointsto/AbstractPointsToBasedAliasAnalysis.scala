/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.tac.fpcf.analyses.alias.pointsto

import org.opalj.br.analyses.DeclaredMethodsKey
import org.opalj.br.analyses.ProjectInformationKeys
import org.opalj.br.analyses.VirtualFormalParametersKey
import org.opalj.br.analyses.cg.IsOverridableMethodKey
import org.opalj.br.fpcf.FPCFAnalysisScheduler
import org.opalj.br.fpcf.properties.Alias
import org.opalj.br.fpcf.properties.MayAlias
import org.opalj.br.fpcf.properties.MustAlias
import org.opalj.br.fpcf.properties.cg.Callees
import org.opalj.br.fpcf.properties.pointsto.AllocationSitePointsToSet
import org.opalj.fpcf.EPK
import org.opalj.fpcf.InterimResult
import org.opalj.fpcf.LBP
import org.opalj.fpcf.ProperPropertyComputationResult
import org.opalj.fpcf.PropertyBounds
import org.opalj.fpcf.Result
import org.opalj.fpcf.SomeEPS
import org.opalj.tac.cg.TypeIteratorKey
import org.opalj.tac.fpcf.analyses.alias.AliasAnalysisState
import org.opalj.tac.fpcf.analyses.alias.AliasEntity
import org.opalj.tac.fpcf.analyses.alias.TacBasedAliasAnalysis
import org.opalj.tac.fpcf.analyses.pointsto
import org.opalj.tac.fpcf.analyses.pointsto.AbstractPointsToBasedAnalysis
import org.opalj.tac.fpcf.properties.TACAI

trait AbstractPointsToBasedAliasAnalysis extends TacBasedAliasAnalysis with AbstractPointsToBasedAnalysis {

    override type AnalysisContext = PointsToBasedAliasAnalysisContext
    override type AnalysisState = AliasAnalysisState

    override def analyzeTAC()(implicit context: AnalysisContext, state: AnalysisState): ProperPropertyComputationResult = {

        val epk1 = EPK(
            pointsto.toEntity(state.defSite1, context.context, state.tacai1.get.stmts),
            pointsToPropertyKey
        )
        val epk2 = EPK(
            pointsto.toEntity(state.defSite2, context.context, state.tacai2.get.stmts),
            pointsToPropertyKey
        )

        val pointsTo1 = propertyStore(epk1)
        val pointsTo2 = propertyStore(epk2)

        print(pointsTo1)

        pointsTo1 match {
            case EPK(e, value) =>
                state.addDependency(pointsTo1)
            case _ => throw new UnknownError("unhandled entity type")
        }

        pointsTo2 match {
            case EPK(e, value) =>
                state.addDependency(pointsTo2)
            case _ => throw new UnknownError("unhandled entity type")
        }

        InterimResult(context.entity, MayAlias, MustAlias, state.getDependees, continuation)
    }

    override protected[this] def continuation(someEPS: SomeEPS)(implicit
        context: PointsToBasedAliasAnalysisContext,
                                                                state: AliasAnalysisState
    ): ProperPropertyComputationResult = {

        someEPS match {
            case LBP(lb: AllocationSitePointsToSet) => {

                print(lb)
                Result(context.entity, MayAlias)
            }
            case _ => super.continuation(someEPS)
        }
    }

    override protected[this] def createState: AnalysisState = new AliasAnalysisState

    override protected[this] def createContext(
        entity: AliasEntity
    ): PointsToBasedAliasAnalysisContext =
        new PointsToBasedAliasAnalysisContext(entity, project, propertyStore)

}

trait PointsToBasedAliasAnalysisScheduler extends FPCFAnalysisScheduler {

    override def requiredProjectInformation: ProjectInformationKeys =
        Seq(DeclaredMethodsKey, VirtualFormalParametersKey, IsOverridableMethodKey, TypeIteratorKey)

    final def derivedProperty: PropertyBounds = PropertyBounds.lub(Alias)

    override def uses: Set[PropertyBounds] = Set(
        PropertyBounds.ub(TACAI),
        PropertyBounds.ub(Callees),
        PropertyBounds.ub(AllocationSitePointsToSet)
    )
}
