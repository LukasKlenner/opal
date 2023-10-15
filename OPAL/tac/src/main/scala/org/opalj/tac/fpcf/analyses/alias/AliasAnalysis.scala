/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.tac.fpcf.analyses.alias

import org.opalj.br.Method
import org.opalj.br.analyses.SomeProject
import org.opalj.br.fpcf.FPCFAnalysis
import org.opalj.br.fpcf.properties.Context
import org.opalj.br.fpcf.properties.MayAlias
import org.opalj.br.fpcf.properties.MustAlias
import org.opalj.fpcf.Entity
import org.opalj.fpcf.InterimResult
import org.opalj.fpcf.ProperPropertyComputationResult
import org.opalj.fpcf.SomeEPS
import org.opalj.fpcf.UBP
import org.opalj.tac.Assignment
import org.opalj.tac.DUVar
import org.opalj.tac.Stmt
import org.opalj.tac.common.DefinitionSiteLike
import org.opalj.tac.fpcf.properties.TACAI
import org.opalj.value.ValueInformation

import scala.annotation.switch
import scala.collection.mutable

class AliasAnalysis(final val project: SomeProject) extends FPCFAnalysis {

  def doDetermineAlias(implicit
                       context: AliasAnalysisContext,
                       state: AliasAnalysisState
                      ): ProperPropertyComputationResult = {
    assert(context.entity1.isInstanceOf[DefinitionSiteLike])
    assert(context.entity2.isInstanceOf[DefinitionSiteLike])
    assert(context.targetMethod1 == context.targetMethod2)

    val method = context.targetMethod1

    retrieveTAC(method)
    if (state.getTacai.isDefined) {
      analyzeTAC()
    } else {
      InterimResult(context.entity, MayAlias, MayAlias, state.dependees, c)
    }

  }

  protected[this] def analyzeTAC()(
    implicit
    context: AliasAnalysisContext,
    state: AliasAnalysisState
  ): ProperPropertyComputationResult = {
    assert(state.getTacai.isDefined)
    // for every use-site, check its escape state
    for (use <- state.getUses1) {
      checkStmtForAlias(state.getTacai.get.stmts(use))
    }
    //TODO
    InterimResult(context.entity, MustAlias, MustAlias, state.dependees, c)
  }

  private[this] def retrieveTAC(
                                 m: Method
                               )(implicit
                                 context: AliasAnalysisContext,
                                 state: AliasAnalysisState): Unit = {
    val tacai = propertyStore(m, TACAI.key)

    if (tacai.isRefinable) {
      state.addDependency(tacai)
    }

    if (tacai.hasUBP && tacai.ub.tac.isDefined) {
      state.updateTACAI(tacai.ub.tac.get)
    }
  }

  private[this] def checkStmtForAlias(
                                       stmt: Stmt[V]
                                     )(implicit context: AliasAnalysisContext, state: AliasAnalysisState): Unit = {
    (stmt.astID: @switch) match {
      case Assignment.ASTID => handleAssignment(stmt.asAssignment)
      case - =>
    }
  }

  protected[this] def handleAssignment(
                                        assignment: Assignment[V]
                                      )(implicit context: AliasAnalysisContext, state: AliasAnalysisState): Unit = {
    val left = assignment.targetVar
    val right = assignment.expr

    right match {
      case v: DUVar[ValueInformation] =>
        if (!state.getPointsTo.contains(v)) {
          state.getPointsTo.put(v, mutable.Set.empty[V])
        }

        var A = state.getPointsTo.get(left).get.add()
    }
  }

  protected[this] def c(
                         someEPS: SomeEPS
                       )(
                         implicit
                         context: AliasAnalysisContext,
                         state: AliasAnalysisState
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
            state.dependees,
            c
          )
        }
      case _ =>
        throw new UnknownError(s"unhandled escape property (${someEPS.ub} for ${someEPS.e}")
    }
  }

  def determineAlias(e: Entity): ProperPropertyComputationResult = {
    e.asInstanceOf[(Context, Entity, Entity)] match {
      case (c: Context, ds1: DefinitionSiteLike, ds2: DefinitionSiteLike) ⇒

    }
  }

}
