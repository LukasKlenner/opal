/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org
package opalj
package tac
package fpcf
package analyses
package alias

import org.opalj.br.Method
import org.opalj.br.analyses.VirtualFormalParameter
import org.opalj.br.fpcf.FPCFAnalysis
import org.opalj.br.fpcf.properties.Context
import org.opalj.br.fpcf.properties.MayAlias
import org.opalj.fpcf.Result
import org.opalj.br.fpcf.properties.MustAlias
import org.opalj.br.fpcf.properties.NoAlias
import org.opalj.fpcf.Entity
import org.opalj.fpcf.InterimResult
import org.opalj.fpcf.ProperPropertyComputationResult
import org.opalj.fpcf.SomeEPS
import org.opalj.fpcf.UBP
import org.opalj.tac.common.DefinitionSiteLike
import org.opalj.tac.fpcf.properties.TACAI

import scala.annotation.switch

trait AbstractAliasAnalysis extends FPCFAnalysis {

    type AnalysisContext <: AliasAnalysisContext
    type AnalysisState <: AliasAnalysisState

    def doDetermineAlias(
        implicit
        context: AliasAnalysisContext,
        state:   AliasAnalysisState
    ): ProperPropertyComputationResult = {
        assert(context.entity1.isInstanceOf[AliasEntity])
        assert(context.entity2.isInstanceOf[AliasEntity])
        assert(context.entity1.method == context.entity2.method)

        val method = context.entity1.method

        if (context.entity1 == context.entity2) {
            context.entity1.entity match {
                case _: DefinitionSiteLike     => return Result(context.entity, MayAlias) //DS might be inside loop
                case _: VirtualFormalParameter => return Result(context.entity, MustAlias)
                case _                         => throw new UnknownError("unhandled entity type")
            }
        }

        retrieveTAC(method)
        if (state.tacai.isDefined) {
            analyzeTAC()
        } else {
            InterimResult(context.entity, MayAlias, MayAlias, state.getDependees, c)
        }

    }

    protected[this] def analyzeTAC()(
        implicit
        context: AliasAnalysisContext,
        state:   AliasAnalysisState
    ): ProperPropertyComputationResult = {
        assert(state.tacai.isDefined)

        for (use <- state.uses1) {

            if (state.uses2.contains(use)) {
                return Result(context.entity, MayAlias)
            }

            //      checkStmtForAlias(state.getTacai.get.stmts(use))
            //
            //      if (state.getMayAlias) {
            //        return Result(context.entity, MayAlias)
            //      }
        }

        Result(context.entity, NoAlias)
        //returnResult
    }

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

    protected[this] def checkStmtForAlias(
        stmt: Stmt[V]
    )(implicit state: AnalysisState, context: AnalysisContext): Unit = {
        (stmt.astID: @switch) match {
            case Assignment.ASTID => handleAssignment(stmt.asAssignment)

            case _                =>
        }
    }

    protected[this] def handleAssignment(
        assignment: Assignment[V]
    )(
        implicit
        context: AnalysisContext,
        state:   AnalysisState
    ): Unit

    protected[this] def returnResult(
        implicit
        context: AliasAnalysisContext,
        state:   AliasAnalysisState
    ): ProperPropertyComputationResult = {
        if (!state.hasDependees) {

            //            val pointsTo1: Seq[V] = state
            //                .getPointsTo(
            //                    getDUVar(state.getPointsTo.keys, context.entity1.asInstanceOf[DefinitionSiteLike])
            //                )
            //                .toSeq
            //            val pointsTo2 = state
            //                .getPointsTo(
            //                    getDUVar(state.getPointsTo.keys, context.entity2.asInstanceOf[DefinitionSiteLike])
            //                )
            //                .toSeq
            //
            //            if (pointsTo1.size == pointsTo2.size && pointsTo1.head == pointsTo2.head) {
            //                Result(context.entity, MustAlias)
            //            } else if (pointsTo1.forall(!pointsTo2.contains(_))) {
            //                Result(context.entity, NoAlias)
            //            }

            Result(context.entity, MayAlias)

        } else {
            InterimResult(
                context.entity,
                MayAlias,
                MayAlias,
                state.getDependees,
                c
            )
        }
    }

    //    private[this] def getDUVar(vars: Iterable[V], definitionSiteLike: DefinitionSiteLike): V = {
    //        vars.find(v => v.definedBy.contains(definitionSiteLike.pc)).get
    //    }

    protected[this] def c(
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
                        c
                    )
                }
            case _ =>
                throw new UnknownError(s"unhandled alias property (${someEPS.ub} for ${someEPS.e}")
        }
    }

    def determineAlias(e: Entity): ProperPropertyComputationResult = {
        e.asInstanceOf[(Context, Entity, Entity)] match {
            case (c: Context, e1: AliasEntity, e2: AliasEntity) =>
                doDetermineAlias(
                    createContext(e.asInstanceOf[(Context, AliasEntity, AliasEntity)]),
                    createState
                )
            case _ => throw new UnknownError("unhandled entity type")
        }
    }

    protected[this] def createState: AnalysisState

    protected[this] def createContext(
        entity: (Context, AliasEntity, AliasEntity)
    ): AnalysisContext

}
