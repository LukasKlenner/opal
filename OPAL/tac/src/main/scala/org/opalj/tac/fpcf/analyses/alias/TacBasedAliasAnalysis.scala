/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org
package opalj
package tac
package fpcf
package analyses
package alias

import org.opalj.br.Method
import org.opalj.br.fpcf.properties.alias.AliasSourceElement
import org.opalj.br.fpcf.properties.alias.MayAlias
import org.opalj.fpcf.EOptionP
import org.opalj.fpcf.InterimResult
import org.opalj.fpcf.ProperPropertyComputationResult
import org.opalj.fpcf.SomeEPS
import org.opalj.fpcf.UBP
import org.opalj.tac.fpcf.properties.TACAI

trait TacBasedAliasAnalysis extends AbstractAliasAnalysis {

    override def doDetermineAlias(
        implicit
        context: AnalysisContext,
        state:   AnalysisState
    ): ProperPropertyComputationResult = {
        assert(context.element1.isInstanceOf[AliasSourceElement])
        assert(context.element2.isInstanceOf[AliasSourceElement])

        if (context.element1.isMethodBound) retrieveTAC(context.element1.method)
        if (context.element2.isMethodBound) retrieveTAC(context.element2.method)

        if ((!context.element1.isMethodBound || state.tacai1.isDefined) &&
            (!context.element2.isMethodBound || state.tacai2.isDefined)) {
            analyzeTAC()
        } else {
            InterimResult(context.entity, MayAlias, MayAlias, state.getDependees, continuation)
        }

    }

    protected[this] def analyzeTAC()(
        implicit
        context: AnalysisContext,
        state:   AnalysisState
    ): ProperPropertyComputationResult

    private[this] def retrieveTAC(
        m: Method
    )(
        implicit
        context: AnalysisContext,
        state:   AnalysisState
    ): Unit = {
        val tacai: EOptionP[Method, TACAI] = propertyStore(m, TACAI.key)

        state.addTacEPSToMethod(tacai.asEPS, m)

        if (tacai.isRefinable) {
            state.addDependency(tacai)
        }

        if (tacai.hasUBP && tacai.ub.tac.isDefined) {
            state.updateTACAI(m, tacai.ub.tac.get)
        }
    }

    protected[this] def continuation(
        someEPS: SomeEPS
    )(
        implicit
        context: AnalysisContext,
        state:   AnalysisState
    ): ProperPropertyComputationResult = {
        someEPS match {
            case UBP(ub: TACAI) =>
                state.removeDependency(someEPS)
                if (someEPS.isRefinable) {
                    state.addDependency(someEPS)
                }
                if (ub.tac.isDefined) {
                    state.updateTACAI(state.getMethodForTacEPS(someEPS), ub.tac.get)
                    analyzeTAC()
                } else {
                    InterimResult(
                        context.entity,
                        MayAlias,
                        MayAlias,
                        state.getDependees,
                        continuation
                    )
                }
            case _ =>
                throw new UnknownError(s"unhandled alias property (${someEPS.ub} for ${someEPS.e}")
        }
    }

}
