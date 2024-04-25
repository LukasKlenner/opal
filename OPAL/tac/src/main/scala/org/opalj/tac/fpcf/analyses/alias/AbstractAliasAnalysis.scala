/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj
package tac
package fpcf
package analyses
package alias

import org.opalj.br.fpcf.FPCFAnalysis
import org.opalj.br.fpcf.properties.alias.Alias
import org.opalj.br.fpcf.properties.alias.AliasEntity
import org.opalj.fpcf.Entity
import org.opalj.fpcf.InterimResult
import org.opalj.fpcf.ProperPropertyComputationResult
import org.opalj.fpcf.Result
import org.opalj.fpcf.SomeEPS
import org.opalj.value.ValueInformation

/**
 * A base trait for all alias analyses.
 */
trait AbstractAliasAnalysis extends FPCFAnalysis {

    protected[this] type AnalysisContext <: AliasAnalysisContext
    protected[this] type AnalysisState <: AliasAnalysisState
    protected[this] type Tac = TACode[TACMethodParameter, DUVar[ValueInformation]]

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
     * Creates the result of the analysis based on the current state.
     */
    protected[this] def createResult()(
        implicit
        state:   AnalysisState,
        context: AnalysisContext
    ): ProperPropertyComputationResult

    /**
     * Creates a final [[Result]] with the given alias property.
     */
    protected[this] def result(alias: Alias)(implicit context: AnalysisContext): ProperPropertyComputationResult = {
        Result(context.entity, alias)
    }

    /**
     * Creates a intermediate result for the given upper and lower bounds of the alias properties.
     */
    protected[this] def interimResult(lb: Alias, ub: Alias)(implicit
        context: AnalysisContext,
        state:   AnalysisState
    ): ProperPropertyComputationResult = {
        if (lb == ub) result(lb)
        else InterimResult(context.entity, lb, ub, state.getDependees, continuation)
    }

    /**
     * A continuation function that will be invoked when an entity-property pair that this analysis depends on
     * is updated
     */
    protected[this] def continuation(
        someEPS: SomeEPS
    )(
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
