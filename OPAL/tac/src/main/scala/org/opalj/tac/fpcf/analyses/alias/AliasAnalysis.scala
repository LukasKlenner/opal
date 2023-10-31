/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.tac.fpcf.analyses.alias

import org.opalj.br.Method
import org.opalj.br.analyses.DeclaredMethodsKey
import org.opalj.br.analyses.ProjectInformationKeys
import org.opalj.br.analyses.SomeProject
import org.opalj.br.analyses.VirtualFormalParametersKey
import org.opalj.br.analyses.cg.IsOverridableMethodKey
import org.opalj.br.fpcf.FPCFAnalysis
import org.opalj.br.fpcf.FPCFAnalysisScheduler
import org.opalj.br.fpcf.properties.Context
import org.opalj.br.fpcf.BasicFPCFEagerAnalysisScheduler
import org.opalj.br.fpcf.properties.MayAlias
import org.opalj.br.fpcf.properties.MustAlias
import org.opalj.br.fpcf.properties.NoAlias
import org.opalj.br.fpcf.properties.Alias
import org.opalj.br.fpcf.properties.SimpleContextsKey
import org.opalj.fpcf.Entity
import org.opalj.fpcf.InterimResult
import org.opalj.fpcf.ProperPropertyComputationResult
import org.opalj.fpcf.PropertyBounds
import org.opalj.fpcf.PropertyStore
import org.opalj.fpcf.Result
import org.opalj.fpcf.SomeEPS
import org.opalj.fpcf.UBP
import org.opalj.tac.Assignment
import org.opalj.tac.DUVar
import org.opalj.tac.StaticMethodCall
import org.opalj.tac.Stmt
import org.opalj.tac.cg.TypeIteratorKey
import org.opalj.tac.common.DefinitionSiteLike
import org.opalj.tac.common.DefinitionSitesKey
import org.opalj.tac.fpcf.analyses.cg.TypeIterator
import org.opalj.tac.fpcf.properties.TACAI
import org.opalj.tac.fpcf.properties.cg.Callees
import org.opalj.tac.fpcf.properties.cg.Callers
import org.opalj.tac.fpcf.properties.cg.NoCallers
import org.opalj.value.ValueInformation

import scala.annotation.switch
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class AliasAnalysis(final val project: SomeProject) extends FPCFAnalysis {

  def doDetermineAlias(
      implicit
      context: AliasAnalysisContext,
      state: AliasAnalysisState
  ): ProperPropertyComputationResult = {
    assert(context.entity1.isInstanceOf[DefinitionSiteLike])
    assert(context.entity2.isInstanceOf[DefinitionSiteLike])
    assert(context.targetMethod1 == context.targetMethod2)

    val method = context.targetMethod1

    if (context.entity1 == context.entity2) {
      return Result(context.entity, MustAlias)
    }



    retrieveTAC(method)
    if (state.getTacai.isDefined) {
      analyzeTAC()
    } else {
      InterimResult(context.entity, MayAlias, MayAlias, state.getDependees, c)
    }

  }

  protected[this] def analyzeTAC()(
      implicit
      context: AliasAnalysisContext,
      state: AliasAnalysisState
  ): ProperPropertyComputationResult = {
    assert(state.getTacai.isDefined)

    for (use <- state.getUses1) {

      if (state.getUses2.contains(use)) {
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
      state: AliasAnalysisState
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
  )(implicit state: AliasAnalysisState, context: AliasAnalysisContext): Unit = {
    (stmt.astID: @switch) match {
      //case Assignment.ASTID => handleAssignment(stmt.asAssignment)(state)
      case StaticMethodCall.ASTID =>
        handleStaticMethodCall(stmt.asStaticMethodCall)
      case _                =>
    }
  }

  protected[this] def handleStaticMethodCall(
                                        methodCall: StaticMethodCall[V]
                                      )(implicit state: AliasAnalysisState, context: AliasAnalysisContext): Unit = {

    for (arg <- methodCall.params) {
      arg match {
        case v: V =>
          if (v.definedBy.contains(context.entity1.asInstanceOf[DefinitionSiteLike].pc) && v.definedBy.contains(context.entity2.asInstanceOf[DefinitionSiteLike].pc)) {
            state.setMayAlias(true)
          }

        case _ => throw new UnknownError("unhandled arg type")
      }
    }

  }

  protected[this] def handleAssignment(
      assignment: Assignment[V]
  )(state: AliasAnalysisState): Unit = {
    val left = assignment.targetVar
    val right = assignment.expr

    right match {
      case v: DUVar[ValueInformation] =>
        if (!state.getPointsTo.contains(v)) {
          state.getPointsTo.put(v, mutable.Set.empty[V])
        }

        state.getPointsTo(left).add(v)
    }
  }

  protected[this] def returnResult(
      implicit
      context: AliasAnalysisContext,
      state: AliasAnalysisState
  ): ProperPropertyComputationResult = {
    if (!state.hasDependees) {

      val pointsTo1: Seq[V] = state
        .getPointsTo(
          getDUVar(state.getPointsTo.keys, context.entity1.asInstanceOf[DefinitionSiteLike])
        )
        .toSeq
      val pointsTo2 = state
        .getPointsTo(
          getDUVar(state.getPointsTo.keys, context.entity2.asInstanceOf[DefinitionSiteLike])
        )
        .toSeq

      if (pointsTo1.size == pointsTo2.size && pointsTo1.head == pointsTo2.head) {
        Result(context.entity, MustAlias)
      } else if (pointsTo1.forall(!pointsTo2.contains(_))) {
        Result(context.entity, NoAlias)
      }

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

  private[this] def getDUVar(vars: Iterable[V], definitionSiteLike: DefinitionSiteLike): V = {
    vars.find(v => v.definedBy.contains(definitionSiteLike.pc)).get
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
      case (c: Context, ds1: DefinitionSiteLike, ds2: DefinitionSiteLike) =>
        doDetermineAlias(
          new AliasAnalysisContext(
            e.asInstanceOf[(Context, DefinitionSiteLike, DefinitionSiteLike)],
            ds1.method,
            ds2.method,
            project,
            propertyStore
          ),
          new AliasAnalysisState
        )
//      case (c: Context, var1: V, var2: V, ) =>
//        doDetermineAlias(
//          new AliasAnalysisContext(
//            e.asInstanceOf[(Context, V, V)],
//            var1.definedBy.head.method,
//            var2.definedBy.head.method,
//            project,
//            propertyStore
//          ),
//          new AliasAnalysisState
//        )
    }
  }

}

sealed trait AliasAnalysisScheduler extends FPCFAnalysisScheduler {

  override def requiredProjectInformation: ProjectInformationKeys =
    Seq(DeclaredMethodsKey, VirtualFormalParametersKey, IsOverridableMethodKey, TypeIteratorKey)

  final def derivedProperty: PropertyBounds = PropertyBounds.lub(Alias)

  override def uses: Set[PropertyBounds] = Set(
    PropertyBounds.ub(TACAI),
    PropertyBounds.ub(Callees),
    PropertyBounds.lub(Alias)
  )
}
object EagerAliasAnalysis
    extends AliasAnalysisScheduler
    with BasicFPCFEagerAnalysisScheduler {

  override def requiredProjectInformation: ProjectInformationKeys =
    super.requiredProjectInformation ++ Seq(DefinitionSitesKey, SimpleContextsKey)

  override def start(p: SomeProject, ps: PropertyStore, unused: Null): FPCFAnalysis = {
    val analysis = new AliasAnalysis(p)

    val declaredMethods = p.get(DeclaredMethodsKey)
    implicit val typeIterator: TypeIterator = p.get(TypeIteratorKey)

    val methods = declaredMethods.declaredMethods
    val callersProperties = ps(methods.to(Iterable), Callers)
    assert(callersProperties.forall(_.isFinal))

    val reachableMethods = callersProperties
      .filterNot(_.asFinal.p == NoCallers)
      .map { v =>
        v.e -> v.ub
      }
      .toMap

    val allAllocationSites = p.get(DefinitionSitesKey).getAllocationSites
    val entities: ArrayBuffer[(Context, Entity, Entity)] = ArrayBuffer.empty

    for (as1 <- allAllocationSites) {
      if (reachableMethods.contains(declaredMethods(as1.method))) {
        for (as2 <- allAllocationSites) {
          if (as1 != as2 && reachableMethods.contains(declaredMethods(as2.method))) {
            val dm = declaredMethods(as1.method)
            entities.addAll(reachableMethods(dm).calleeContexts(dm).iterator
              .filter(c => !(entities.contains((c, as1, as2)) || entities.contains((c, as2, as1))))
              .map((_, as1, as2)))
          }
        }
      }
    }

    ps.scheduleEagerComputationsForEntities(entities)(analysis.determineAlias)
    analysis
  }

  override def derivesEagerly: Set[PropertyBounds] = Set(derivedProperty)

  override def uses: Set[PropertyBounds] = super.uses + PropertyBounds.finalP(Callers)

  override def derivesCollaboratively: Set[PropertyBounds] = Set.empty

}
