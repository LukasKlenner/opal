/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.tac.fpcf.analyses.alias

import org.opalj.br.analyses.SomeProject
import org.opalj.fpcf.ProperPropertyComputationResult

class PointsToBasedAliasAnalysis(final val project: SomeProject) extends AbstractAliasAnalysis {

  override type AnalysisContext = AliasAnalysisContext
  override type AnalysisState = AliasAnalysisState
  override def doDetermineAlias(
      implicit context: AliasAnalysisContext,
      state: AliasAnalysisState
  ): ProperPropertyComputationResult = ???

  override protected[this] def createState: AnalysisState = new AliasAnalysisState

  override protected[this] def createContext(
      entity: AliasEntity
  ): AliasAnalysisContext =
    new AliasAnalysisContext(entity, project, propertyStore)

}
