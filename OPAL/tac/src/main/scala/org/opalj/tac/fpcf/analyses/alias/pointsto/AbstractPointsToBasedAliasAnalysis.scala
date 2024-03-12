/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj
package tac
package fpcf
package analyses
package alias
package pointsto

import org.opalj.br.Field
import org.opalj.br.PC
import org.opalj.br.analyses.DeclaredFieldsKey
import org.opalj.br.analyses.DeclaredMethodsKey
import org.opalj.br.analyses.ProjectInformationKeys
import org.opalj.br.analyses.VirtualFormalParametersKey
import org.opalj.br.fpcf.FPCFAnalysisScheduler
import org.opalj.br.fpcf.properties.Context
import org.opalj.br.fpcf.properties.SimpleContext
import org.opalj.br.fpcf.properties.SimpleContextsKey
import org.opalj.br.fpcf.properties.alias.Alias
import org.opalj.br.fpcf.properties.alias.AliasEntity
import org.opalj.br.fpcf.properties.alias.AliasField
import org.opalj.br.fpcf.properties.alias.AliasFP
import org.opalj.br.fpcf.properties.alias.AliasReturnValue
import org.opalj.br.fpcf.properties.alias.AliasSourceElement
import org.opalj.br.fpcf.properties.alias.AliasUVar
import org.opalj.br.fpcf.properties.alias.MayAlias
import org.opalj.br.fpcf.properties.alias.MustAlias
import org.opalj.br.fpcf.properties.alias.NoAlias
import org.opalj.br.fpcf.properties.cg.Callees
import org.opalj.br.fpcf.properties.pointsto.AllocationSite
import org.opalj.br.fpcf.properties.pointsto.AllocationSitePointsToSet
import org.opalj.br.fpcf.properties.pointsto.longToAllocationSite
import org.opalj.fpcf.Entity
import org.opalj.fpcf.EOptionP
import org.opalj.fpcf.InterimResult
import org.opalj.fpcf.ProperPropertyComputationResult
import org.opalj.fpcf.PropertyBounds
import org.opalj.fpcf.Result
import org.opalj.fpcf.SomeEPS
import org.opalj.fpcf.UBP
import org.opalj.tac.cg.TypeIteratorKey
import org.opalj.tac.common.DefinitionSitesKey
import org.opalj.tac.fpcf.analyses.alias.TacBasedAliasAnalysis
import org.opalj.tac.fpcf.analyses.pointsto.AbstractPointsToBasedAnalysis
import org.opalj.tac.fpcf.analyses.pointsto.toEntity
import org.opalj.tac.fpcf.properties.TACAI
import org.opalj.value.ValueInformation

trait AbstractPointsToBasedAliasAnalysis extends TacBasedAliasAnalysis with AbstractPointsToBasedAnalysis {

    override protected[this] type AnalysisContext = PointsToBasedAliasAnalysisContext
    override protected[this] type AnalysisState = PointsToBasedAliasAnalysisState

    private[this] type Tac = TACode[TACMethodParameter, DUVar[ValueInformation]]
    private[this] type AliasPointsToSet = Set[(Context, PC)]

    override protected[this] def analyzeTAC()(implicit
        context: AnalysisContext,
        state:   AnalysisState
    ): ProperPropertyComputationResult = {

        handleEntity(context.element1, state.tacai1)
        handleEntity(context.element2, state.tacai2)

        createResult()
    }

    private[this] def handleEntity(ase: AliasSourceElement, tac: Option[Tac])(
        implicit
        state:   AnalysisState,
        context: AnalysisContext
    ): Unit = {

        ase match {
            case AliasUVar(uVar, _, _) =>
                uVar.defSites.foreach(ds => {
                    handlePointsToEntity(ase, getPointsTo(ds, context.contextOf(ase), tac.get))
                })

            case AliasFP(fp) => handlePointsToEntity(ase, getPointsTo(fp.origin, context.contextOf(ase), tac.get))

            case AliasField(field) => handlePointsToEntity(ase, getPointsToOfField(field))

            case arv: AliasReturnValue => handlePointsToEntity(ase, getPointsToOfReturnValue(arv.callContext))

            case _ =>
        }
    }

    private[this] def getPointsTo(defSite: Int, context: Context, tac: Tac): EOptionP[Entity, PointsToSet] = {
        propertyStore(
            toEntity(if (defSite < 0) defSite else tac.properStmtIndexForPC(defSite), context, tac.stmts),
            pointsToPropertyKey
        )
    }

    private[this] def getPointsToOfField(field: Field): EOptionP[Entity, PointsToSet] = {
        propertyStore(
            declaredFields.apply(field),
            pointsToPropertyKey
        )
    }

    private[this] def getPointsToOfReturnValue(callContext: Context): EOptionP[Entity, PointsToSet] = {
        propertyStore(
            callContext,
            pointsToPropertyKey
        )
    }

    private[this] def handlePointsToEntity(ase: AliasSourceElement, eps: EOptionP[Entity, PointsToSet])(
        implicit
        state:   AnalysisState,
        context: AnalysisContext
    ): Unit = {

        val e: Entity = eps.e

        if (eps.isRefinable) {
            state.addDependency(eps)
            state.addElementDependency(ase, eps)
        }

        handlePointsToSet(ase, e, eps.ub.asInstanceOf[AllocationSitePointsToSet])
        state.setSomePointsTo()
    }

    private[this] def handlePointsToSet(ase: AliasSourceElement, e: Entity, pointsToSet: AllocationSitePointsToSet)(
        implicit
        state:   AnalysisState,
        context: AnalysisContext
    ): Unit = {

        pointsToSet.forNewestNElements(pointsToSet.numElements - state.pointsToElementsHandled(ase, e)) { value =>
            val encodedAllocationSite: AllocationSite = value

            val allocationSite: (Context, PC, Int) = longToAllocationSite(encodedAllocationSite)

            state.addPointsTo(ase, (allocationSite._1, allocationSite._2))
            state.incPointsToElementsHandled(ase, e)
        }

        if (pointsToSet.pointsToNull)
            state.setPointsToNull(ase)

    }

    private[this] def createResult()(
        implicit
        state:   AnalysisState,
        context: AnalysisContext
    ): ProperPropertyComputationResult = {

        val pointsTo1 = state.pointsTo1
        val pointsTo2 = state.pointsTo2

        if (context.element1.isAliasNull) {
            val relation = if (state.pointsToNull2)
                if (pointsTo2.isEmpty) MustAlias else MayAlias
            else NoAlias

            return if (state.hasDependees)
                InterimResult(context.entity, relation, MayAlias, state.getDependees, continuation)
            else Result(context.entity, relation)
        }

        if (!state.somePointsTo) {
            return InterimResult(context.entity, NoAlias, MayAlias, state.getDependees, continuation)
        }

        val intersection = pointsTo1.intersect(pointsTo2)

        if (intersection.isEmpty) {
            return if (state.hasDependees)
                InterimResult(context.entity, NoAlias, MayAlias, state.getDependees, continuation)
            else Result(context.entity, NoAlias)

        } else if (checkMustAlias(intersection)) {
            return if (state.hasDependees)
                InterimResult(context.entity, MustAlias, MayAlias, state.getDependees, continuation)
            else Result(context.entity, MustAlias)
        }

        Result(context.entity, MayAlias)
    }

    private[this] def checkMustAlias(intersection: AliasPointsToSet)(implicit
        state:   AnalysisState,
        context: AnalysisContext
    ): Boolean = {

        val pointsTo1 = state.pointsTo1
        val pointsTo2 = state.pointsTo2

        if (intersection.size != 1 ||
            pointsTo1.size != 1 ||
            pointsTo2.size != 1 ||
            state.pointsToNull1 ||
            state.pointsToNull2
        ) return false

        // they refer to the same allocation site but aren't necessarily the same object (e.g. if the allocation site
        // is inside a loop and is executed multiple times)

        val (pointsToContext, pc) = pointsTo1.head
        val method = pointsToContext.asInstanceOf[SimpleContext].method

        if (method.name == "<clinit>") {
            // static initializer are executed only once
            return true
        }

        if (context.element1.isAliasUVar &&
            context.element2.isAliasUVar &&
            context.element1.declaredMethod == method &&
            context.element2.declaredMethod == method
        ) {

            // Both elements are uVars that point to the same allocation site and both are inside the method of the allocation site
            // -> they must alias if the allocation site is executed only once (i.e. no loop or recursion)

            val defSite1 = context.element1.asAliasUVar.uVar.defSites
            val defSite2 = context.element2.asAliasUVar.uVar.defSites

            if (defSite1.size != 1 || defSite1.size != 1) return false // shouldn't happen

            if (defSite1.head != defSite2.head) return false // different def sites but same allocation site -> might be different objects (e.q. due to recursion)

            val tac = state.tacai1.get

            for (stmt <- state.tacai1.get.stmts) {
                stmt match {
                    case goto: Goto =>
                        val targetPC = tac.stmts(goto.targetStmt).pc
                        if (targetPC <= pc && goto.pc >= pc) return false // jumping from behind the allocation site in front of it -> might break aliasing
                    case _ =>
                }
            }

            return true
        }

        false
    }

    override protected[this] def continuation(someEPS: SomeEPS)(
        implicit
        context: AnalysisContext,
        state:   AnalysisState
    ): ProperPropertyComputationResult = {

        someEPS match {
            case UBP(pointsToSet: AllocationSitePointsToSet) =>
                val element1Dependence = state.element1HasDependency(someEPS)
                val element2Dependence = state.element2HasDependency(someEPS)

                if (element1Dependence) handlePointsToSet(context.element1, someEPS.e, pointsToSet)
                if (element2Dependence) handlePointsToSet(context.element2, someEPS.e, pointsToSet)

                state.removeElementDependency(someEPS)

                if (someEPS.isRefinable) {
                    if (element1Dependence) state.addElementDependency(context.element1, someEPS)
                    if (element2Dependence) state.addElementDependency(context.element2, someEPS)
                }

                createResult()
            case _ => super.continuation(someEPS)
        }
    }

    override protected[this] def createState: AnalysisState = new PointsToBasedAliasAnalysisState

    override protected[this] def createContext(
        entity: AliasEntity
    ): PointsToBasedAliasAnalysisContext =
        new PointsToBasedAliasAnalysisContext(entity, project, propertyStore)

}

trait PointsToBasedAliasAnalysisScheduler extends FPCFAnalysisScheduler {

    override def requiredProjectInformation: ProjectInformationKeys =
        Seq(
            DeclaredMethodsKey,
            VirtualFormalParametersKey,
            TypeIteratorKey,
            DefinitionSitesKey,
            SimpleContextsKey,
            DeclaredFieldsKey
        )

    final def derivedProperty: PropertyBounds = PropertyBounds.lub(Alias)

    override def uses: Set[PropertyBounds] = Set(
        PropertyBounds.ub(TACAI),
        PropertyBounds.ub(Callees),
        PropertyBounds.ub(AllocationSitePointsToSet)
    )

    override def derivesCollaboratively: Set[PropertyBounds] = Set.empty
}
