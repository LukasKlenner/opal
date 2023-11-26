/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.tac.fpcf.analyses.alias

import org.opalj.br.analyses.SomeProject
import org.opalj.br.fpcf.properties.Context
import org.opalj.tac.Assignment

class PointsToBasedAliasAnalysis( final val project: SomeProject) extends AbstractAliasAnalysis {

    override type AnalysisContext = AliasAnalysisContext
    override type AnalysisState = AliasAnalysisState

    override protected[this] def handleAssignment(
        assignment: Assignment[V]
    )(implicit context: AnalysisContext, state: AnalysisState): Unit = {}

    override protected[this] def createState: AnalysisState = new AliasAnalysisState

    override protected[this] def createContext(
        entity: (Context, AliasEntity, AliasEntity)
    ): AliasAnalysisContext =
        new AliasAnalysisContext(entity, project, propertyStore)
}
