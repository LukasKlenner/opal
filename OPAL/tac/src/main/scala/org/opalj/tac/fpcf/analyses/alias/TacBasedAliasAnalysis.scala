/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org
package opalj
package tac
package fpcf
package analyses
package alias

import org.opalj.br.Method
import org.opalj.br.analyses.VirtualFormalParameter
import org.opalj.br.fpcf.properties.MayAlias
import org.opalj.br.fpcf.properties.MustAlias
import org.opalj.fpcf.InterimResult
import org.opalj.fpcf.ProperPropertyComputationResult
import org.opalj.fpcf.Result
import org.opalj.fpcf.SomeEPS
import org.opalj.fpcf.UBP
import org.opalj.tac.common.DefinitionSiteLike
import org.opalj.tac.fpcf.properties.TACAI

trait TacBasedAliasAnalysis extends AbstractAliasAnalysis {

    override def doDetermineAlias(
        implicit
        context: AliasAnalysisContext,
        state:   AliasAnalysisState
    ): ProperPropertyComputationResult = {
        assert(context.element1.isInstanceOf[AliasSourceElement])
        assert(context.element2.isInstanceOf[AliasSourceElement])
        assert(context.element1.method == context.element2.method)

        val method = context.element1.method

        if (context.element1 == context.element2) {
            context.element1.element match {
                case _: DefinitionSiteLike     => return Result(context.entity, MayAlias) //DS might be inside loop
                case _: VirtualFormalParameter => return Result(context.entity, MustAlias)
                case _                         => throw new UnknownError("unhandled entity type")
            }
        }

        retrieveTAC(method)
        if (state.tacai.isDefined) {
            analyzeTAC()
        } else {
            InterimResult(context.entity, MayAlias, MayAlias, state.getDependees, continuation)
        }

    }

    protected[this] def analyzeTAC()(
        implicit
        context: AliasAnalysisContext,
        state:   AliasAnalysisState
    ): ProperPropertyComputationResult

    private[this] def retrieveTAC(
        m: Method
    )(
        implicit
        context: AliasAnalysisContext,
        state:   AliasAnalysisState
    ): Unit = {
        val tacai = propertyStore(m, TACAI.key)

        if (tacai.isRefinable) {
            state.addDependency(tacai)
        }

        if (tacai.hasUBP && tacai.ub.tac.isDefined) {
            state.updateTACAI(tacai.ub.tac.get)
        }
    }

    protected[this] def continuation(
        someEPS: SomeEPS
    )(
        implicit
        context: AliasAnalysisContext,
        state:   AliasAnalysisState
    ): ProperPropertyComputationResult = {
        someEPS match {
            case UBP(ub: TACAI) =>
                state.removeDependency(someEPS)
                if (someEPS.isRefinable) {
                    state.addDependency(someEPS)
                }
                if (ub.tac.isDefined) {
                    state.updateTACAI(ub.tac.get)
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
