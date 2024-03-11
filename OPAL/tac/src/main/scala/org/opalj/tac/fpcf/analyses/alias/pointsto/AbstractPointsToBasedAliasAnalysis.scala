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
import org.opalj.br.fpcf.properties.alias.AliasDS
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

    override type AnalysisContext = PointsToBasedAliasAnalysisContext
    override type AnalysisState = PointsToBasedAliasAnalysisState

    type Tac = TACode[TACMethodParameter, DUVar[ValueInformation]]
    type AliasPointsToSet = Set[(Context, PC)]

    override def analyzeTAC()(implicit
        context: AnalysisContext,
        state:   AnalysisState
    ): ProperPropertyComputationResult = {

        handleEntity(context.element1, state.tacai1)
        handleEntity(context.element2, state.tacai2)

        createResult()
    }

    private[this] def handleEntity(ase: AliasSourceElement, tac: Option[Tac])(implicit
        state:   AnalysisState,
        context: AnalysisContext
    ): Unit = {

        ase match {
            case AliasUVar(uVar, _, _) =>
                uVar.defSites.foreach(ds => {
                    handlePointsToEntity(ase, getPointsTo(ds, context.contextOf(ase), tac.get))
                })

            case AliasDS(pc, _, _) => handlePointsToEntity(ase, getPointsTo(pc, context.contextOf(ase), tac.get))

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

    private[this] def handlePointsToEntity(ase: AliasSourceElement, eps: EOptionP[Entity, PointsToSet])(implicit
        state:   AnalysisState,
        context: AnalysisContext
    ): Unit = {

        val e: Entity = eps.e
        state.addEntityToAliasSourceElement(e, ase)

        if (eps.isRefinable) {
            state.addDependency(eps)
            state.setPointsToFinal(e, false)
            return
        } else {
            state.setPointsToFinal(e, true)
        }

        handlePointsToSet(ase, e, eps.ub.asInstanceOf[AllocationSitePointsToSet])
        state.setSomePointsTo()
    }

    private[this] def handlePointsToSet(ase: AliasSourceElement, e: Entity, pointsToSet: AllocationSitePointsToSet)(
        implicit
        state:   AnalysisState,
        context: AnalysisContext
    ): Unit = {

        pointsToSet.forNewestNElements(pointsToSet.numElements - state.pointsToElementsHandled((e, ase))) { value =>
            val encodedAllocationSite: AllocationSite = value

            val allocationSite: (Context, PC, Int) = longToAllocationSite(encodedAllocationSite)

            state.addPointsTo(ase, (allocationSite._1, allocationSite._2))
            state.incPointsToElementsHandled((e, ase))
        }

        if (pointsToSet.pointsToNull)
            state.setPointsToNull(ase)

    }

    private[this] def createResult()(implicit
        state:   AnalysisState,
        context: AnalysisContext
    ): ProperPropertyComputationResult = {

        if (!state.somePointsTo) {
            return InterimResult(context.entity, NoAlias, MayAlias, state.getDependees, continuation)
        }

        val pointsTo1 = state.pointsTo1
        val pointsTo2 = state.pointsTo2

        val intersection = pointsTo1.intersect(pointsTo2)

        if (intersection.isEmpty) {
            return if (state.allPointsToFinal()) Result(context.entity, NoAlias)
            else InterimResult(context.entity, NoAlias, MayAlias, state.getDependees, continuation)

        } else if (checkMustAlias(intersection)) {
            return if (state.allPointsToFinal()) Result(context.entity, MustAlias)
            else InterimResult(context.entity, MustAlias, MayAlias, state.getDependees, continuation)
        }

        Result(context.entity, MayAlias)
    }

    private[this] def checkMustAlias(intersection: AliasPointsToSet)(implicit state: AnalysisState): Boolean = {

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

        val (context, pc) = pointsTo1.head
        val method = context.asInstanceOf[SimpleContext].method

        if (method.name == "<clinit>") {
            // static initializer are executed only once
            return true
        }

        // TODO more checks

        false
    }

    override protected[this] def continuation(someEPS: SomeEPS)(implicit
        context: AnalysisContext,
        state:   AnalysisState
    ): ProperPropertyComputationResult = {

        someEPS match {
            case UBP(ub: AllocationSitePointsToSet) => {

                val e: Entity = someEPS.e
                val ase: AliasSourceElement = state.entityToAliasSourceElement(e)

                handlePointsToSet(ase, e, ub)

                createResult()
            }
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
