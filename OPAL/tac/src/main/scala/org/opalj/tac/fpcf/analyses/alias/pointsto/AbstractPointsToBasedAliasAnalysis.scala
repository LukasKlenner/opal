/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.tac.fpcf.analyses.alias.pointsto

import org.opalj.br.analyses.DeclaredMethodsKey
import org.opalj.br.analyses.ProjectInformationKeys
import org.opalj.br.analyses.VirtualFormalParametersKey
import org.opalj.br.analyses.cg.IsOverridableMethodKey
import org.opalj.br.fpcf.FPCFAnalysisScheduler
import org.opalj.br.fpcf.properties.Alias
import org.opalj.br.fpcf.properties.MayAlias
import org.opalj.br.fpcf.properties.NoAlias
import org.opalj.br.fpcf.properties.cg.Callees
import org.opalj.br.fpcf.properties.pointsto.AllocationSitePointsToSet
import org.opalj.fpcf.EPK
import org.opalj.fpcf.ProperPropertyComputationResult
import org.opalj.fpcf.PropertyBounds
import org.opalj.fpcf.Result
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

      try {
        val epk = EPK(
          pointsto.toEntity(context.element1.definitionSite, context.context, state.tacai1.get.stmts),
          pointsToPropertyKey
        )

        val pointsTo = propertyStore(epk)

        print(pointsTo)
      } catch {
        case e: Throwable => {
          println("Exception: " + e)
          Result(context.entity, NoAlias)
        }
      }

        Result(context.entity, MayAlias)

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
