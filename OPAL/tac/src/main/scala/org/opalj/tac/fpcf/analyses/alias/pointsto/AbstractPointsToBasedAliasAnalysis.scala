/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj
package tac
package fpcf
package analyses
package alias
package pointsto

import org.opalj.br.PC
import org.opalj.br.analyses.DeclaredMethodsKey
import org.opalj.br.analyses.ProjectInformationKeys
import org.opalj.br.analyses.VirtualFormalParametersKey
import org.opalj.br.analyses.cg.IsOverridableMethodKey
import org.opalj.br.fpcf.FPCFAnalysisScheduler
import org.opalj.br.fpcf.properties.Alias
import org.opalj.br.fpcf.properties.Context
import org.opalj.br.fpcf.properties.MayAlias
import org.opalj.br.fpcf.properties.MustAlias
import org.opalj.br.fpcf.properties.NoAlias
import org.opalj.br.fpcf.properties.cg.Callees
import org.opalj.br.fpcf.properties.pointsto.AllocationSite
import org.opalj.br.fpcf.properties.pointsto.AllocationSitePointsToSet
import org.opalj.br.fpcf.properties.pointsto.longToAllocationSite
import org.opalj.fpcf.EOptionP
import org.opalj.fpcf.EPK
import org.opalj.fpcf.Entity
import org.opalj.fpcf.FinalEP
import org.opalj.fpcf.InterimResult
import org.opalj.fpcf.ProperPropertyComputationResult
import org.opalj.fpcf.PropertyBounds
import org.opalj.fpcf.Result
import org.opalj.fpcf.SomeEPS
import org.opalj.fpcf.UBP
import org.opalj.tac.cg.TypeIteratorKey
import org.opalj.tac.fpcf.analyses.alias.AliasEntity
import org.opalj.tac.fpcf.analyses.alias.TacBasedAliasAnalysis
import org.opalj.tac.fpcf.analyses.pointsto.toEntity
import org.opalj.tac.fpcf.analyses.pointsto.AbstractPointsToBasedAnalysis
import org.opalj.tac.fpcf.properties.TACAI
import org.opalj.value.ValueInformation

trait AbstractPointsToBasedAliasAnalysis extends TacBasedAliasAnalysis with AbstractPointsToBasedAnalysis {

    override type AnalysisContext = PointsToBasedAliasAnalysisContext
    override type AnalysisState = PointsToBasedAliasAnalysisState

    override def analyzeTAC()(implicit context: AnalysisContext, state: AnalysisState): ProperPropertyComputationResult = {

        //val pointsTo1 = propertyStore(toEntity(state.defSite1, context.context, state.tacai1.get.stmts), pointsToPropertyKey)
        //val pointsTo2 = propertyStore(toEntity(state.defSite2, context.context, state.tacai2.get.stmts), pointsToPropertyKey)

        //handlePointsToEntity(context.element1, pointsTo1)
        //handlePointsToEntity(context.element2, pointsTo2)

        /*for (useSite <- state.uses1) {
            handleUseSite(context.element1, useSite, state.tacai1.get.stmts(useSite))
        }

        for (useSite <- state.uses2) {
            handleUseSite(context.element2, useSite, state.tacai2.get.stmts(useSite))
        }*/

        handleEntity(context.element1, state.tacai1.get.stmts)
        handleEntity(context.element2, state.tacai2.get.stmts)

        createResult(context.entity)
    }

    private[this] def handleEntity(ase: AliasSourceElement, stmts: Array[Stmt[DUVar[ValueInformation]]])
              (implicit state: AnalysisState, context: AnalysisContext): Unit = {

        ase match {
          case AliasUVar(uVar, method, project) => {
              uVar.defSites.foreach(ds => {
                  handlePointsToEntity(ase, getPointsTo(ds, context.context, stmts))
              })
          }
          case AliasDVar(dVar, method, project) => {
              handlePointsToEntity(ase, getPointsTo(dVar.origin, context.context, stmts))
          }
          case AliasFP(fp) => {
              handlePointsToEntity(ase, getPointsTo(fp.origin, context.context, stmts))
          }
          case _ =>
        }

    }

    private[this] def getPointsTo(defSite: Int, context: Context, stmts: Array[Stmt[DUVar[ValueInformation]]]): EOptionP[Entity, PointsToSet] = {

        propertyStore(toEntity(defSite, context, stmts), pointsToPropertyKey)
    }

    /*private[this] def handleUseSite(ase: AliasSourceElement, useSite: Int, stmt: Stmt[DUVar[ValueInformation]])
                                   (implicit state: AnalysisState, context: AnalysisContext): Unit = {

        //stmt match {
        //  case call: Call[DUVar[ValueInformation]] => handleCall(call, ase)
        //}

        val pointsTo = propertyStore(toEntity(useSite, context.context, state.tacai1.get.stmts), pointsToPropertyKey)

        handlePointsToEntity(ase, pointsTo)
    }*/

    /*private[this] def handleCall(call: Call[DUVar[ValueInformation]], ase: AliasSourceElement)
                                (implicit state: AnalysisState, context: AnalysisContext): Unit = {

    }*/

    private[this] def handlePointsToEntity(ase: AliasSourceElement, eps: EOptionP[Entity, PointsToSet])
                                          (implicit state: AnalysisState, context: AnalysisContext): Unit = {

        val e: Entity = eps.e
        state.addEntityToAliasSourceElement(e, ase)

        eps match {
            case EPK(_, _) => {
                state.addDependency(eps)
            }
            case FinalEP(_, pointsTo) => {
              handlePointsToSet(ase, e, pointsTo.asInstanceOf[AllocationSitePointsToSet])
              state.setSomePointsTo()
            }
            case UBP(ub) => {
                handlePointsToSet(ase, e, ub.asInstanceOf[AllocationSitePointsToSet])
                state.addDependency(eps)
                state.setSomePointsTo()
            }
            case _ => throw new UnknownError("unhandled entity type")
        }
    }

    /**
     * Points-To nicht fertig -> MustAlias niemals sicher?
     */

    private[this] def handlePointsToSet(ase: AliasSourceElement, e: Entity, pointsToSet: AllocationSitePointsToSet)(
      implicit state: AnalysisState, context: AnalysisContext): Unit = {

       pointsToSet.forNewestNElements(pointsToSet.numElements - state.pointsToElementsHandled((e, ase))) { value =>

           val encodedAllocationSite: AllocationSite = value

           val allocationSite: (Context, PC, Int) = longToAllocationSite(encodedAllocationSite)

           state.addPointsTo(ase, (allocationSite._1, allocationSite._2))
           state.incPointsToElementsHandled((e, ase))
       }

    }

    private[this] def createResult(e: AliasEntity) (implicit state: AnalysisState, context: AnalysisContext): ProperPropertyComputationResult = {

        if (!state.somePointsTo) {
            return InterimResult(context.entity, MayAlias, MustAlias, state.getDependees, continuation)
        }

        val pointsTo1 = state.pointsTo1
        val pointsTo2 = state.pointsTo2

        val intersection = pointsTo1.intersect(pointsTo2)

        if (intersection.isEmpty) {
            return Result(context.entity, NoAlias)
        } else if (intersection.size == 1 && pointsTo1.size == 1 && pointsTo2.size == 1) {
            return Result(context.entity, MustAlias) //TODO nicht immer sicher
        }

        Result(context.entity, MayAlias)
    }

    override protected[this] def continuation(someEPS: SomeEPS)(implicit
        context: AnalysisContext,
        state: AnalysisState
    ): ProperPropertyComputationResult = {

        someEPS match {
            case UBP(ub: AllocationSitePointsToSet) => {

                print(ub)
                Result(context.entity, MayAlias)
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
        Seq(DeclaredMethodsKey, VirtualFormalParametersKey, IsOverridableMethodKey, TypeIteratorKey)

    final def derivedProperty: PropertyBounds = PropertyBounds.lub(Alias)

    override def uses: Set[PropertyBounds] = Set(
        PropertyBounds.ub(TACAI),
        PropertyBounds.ub(Callees),
        PropertyBounds.ub(AllocationSitePointsToSet)
    )
}
