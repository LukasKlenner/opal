/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj
package tac
package fpcf
package analyses
package alias

import org.opalj.br.fpcf.FPCFAnalysis
import org.opalj.br.fpcf.properties.alias.AliasEntity
import org.opalj.fpcf.Entity
import org.opalj.fpcf.ProperPropertyComputationResult

/**
 * A base trait for all alias analyses.
 */
trait AbstractAliasAnalysis extends FPCFAnalysis {

    protected[this] type AnalysisContext <: AliasAnalysisContext
    protected[this] type AnalysisState <: AliasAnalysisState

    /**
     * Determines the alias relation for the given entity.
     * @param e The entity to determine the aliasing information for.
     * @return The result of the computation.
     */
    def determineAlias(e: Entity): ProperPropertyComputationResult = {
        e match {
            case entity: AliasEntity =>
                doDetermineAlias(createContext(entity), createState)
            case _ => throw new UnknownError("unhandled entity type")
        }
    }

    /**
     * Called to determine the alias relation for the given entity.
     *
     * This method is implemented by the concrete alias analysis.
     *
     * @param context The context to determine the aliasing information for.
     * @param state The state to use for the computation.
     * @return
     */
    protected[this] def doDetermineAlias(
        implicit
        context: AnalysisContext,
        state:   AnalysisState
    ): ProperPropertyComputationResult

    /**
     * Creates the state to use for the computation.
     */
    protected[this] def createState: AnalysisState

    /**
     * Creates the context to use for the computation.
     */
    protected[this] def createContext(
        entity: AliasEntity
    ): AnalysisContext

}
