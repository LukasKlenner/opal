/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj
package tac
package fpcf
package analyses
package alias
package pointsto

import org.opalj.br.Field
import org.opalj.br.Method
import org.opalj.br.ObjectType
import org.opalj.br.PC
import org.opalj.br.ReferenceType
import org.opalj.br.analyses.DeclaredFieldsKey
import org.opalj.br.analyses.DeclaredMethodsKey
import org.opalj.br.analyses.SomeProject
import org.opalj.br.fpcf.BasicFPCFEagerAnalysisScheduler
import org.opalj.br.fpcf.BasicFPCFLazyAnalysisScheduler
import org.opalj.br.fpcf.FPCFAnalysis
import org.opalj.br.fpcf.properties.Context
import org.opalj.br.fpcf.properties.SimpleContextsKey
import org.opalj.br.fpcf.properties.alias.Alias
import org.opalj.br.fpcf.properties.alias.AliasEntity
import org.opalj.br.fpcf.properties.alias.AliasField
import org.opalj.br.fpcf.properties.alias.AliasSourceElement
import org.opalj.br.fpcf.properties.alias.FieldReference
import org.opalj.br.fpcf.properties.cg.Callers
import org.opalj.br.fpcf.properties.pointsto
import org.opalj.br.fpcf.properties.pointsto.AllocationSite
import org.opalj.br.fpcf.properties.pointsto.longToAllocationSite
import org.opalj.collection.immutable.IntTrieSet
import org.opalj.fpcf.Entity
import org.opalj.fpcf.PropertyBounds
import org.opalj.fpcf.PropertyStore
import org.opalj.tac.fpcf.analyses.pointsto.AbstractPointsToAnalysis
import org.opalj.tac.fpcf.analyses.pointsto.AllocationSiteBasedAnalysis
import org.opalj.tac.fpcf.properties.TACAI

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * An alias analysis based on points-to information that contain the possible allocation sites of an element.
 * @param project The project
 */
class AllocationSitePointsToBasedAliasAnalysis(final val project: SomeProject)
    extends AbstractPointsToBasedAliasAnalysis
    with AbstractPointsToAnalysis
    with AllocationSiteBasedAnalysis
    with AllocationSiteAndTacBasedAliasAnalysis {

    override protected[this] type AnalysisState = AllocationSitePointsToBasedAliasAnalysisState

    override protected[this] def handlePointsToSetElement(
        ase:            AliasSourceElement,
        pointsToEntity: Entity,
        element:        pointsto.AllocationSite
    )(
        implicit
        state:   AnalysisState,
        context: AnalysisContext
    ): Unit = {

        val encodedAllocationSite: AllocationSite = element

        val (allocContext, pc, _): (Context, PC, Int) = longToAllocationSite(encodedAllocationSite)

        state.addPointsTo(ase, allocContext, pc)
        state.incPointsToElementsHandled(ase, pointsToEntity)
    }

    /**
     * Creates the state to use for the computation.
     */
    override protected[this] def createState: AnalysisState = new AllocationSitePointsToBasedAliasAnalysisState

}

/**
 * A scheduler for a lazy, allocation site, points-to based alias analysis.
 */
object LazyAllocationSitePointsToBasedAliasAnalysisScheduler extends PointsToBasedAliasAnalysisScheduler
    with BasicFPCFLazyAnalysisScheduler {

    override def derivesLazily: Some[PropertyBounds] = Some(derivedProperty)

    override def register(
        project:       SomeProject,
        propertyStore: PropertyStore,
        i:             LazyAllocationSitePointsToBasedAliasAnalysisScheduler.InitializationData
    ): FPCFAnalysis = {

        val analysis = new AllocationSitePointsToBasedAliasAnalysis(project)

        propertyStore.registerLazyPropertyComputation(
            Alias.key,
            analysis.determineAlias
        )

        analysis
    }
}

object EagerFieldAccessAllocationSitePointsToBasedAliasAnalysisScheduler extends PointsToBasedAliasAnalysisScheduler
    with BasicFPCFEagerAnalysisScheduler {

    override def start(p: SomeProject, ps: PropertyStore, unused: Null): FPCFAnalysis = {
        val analysis = new AllocationSitePointsToBasedAliasAnalysis(p)
        val simpleContexts = p.get(SimpleContextsKey)
        val declaredMethods = p.get(DeclaredMethodsKey)
        val declaredFields = p.get(DeclaredFieldsKey)

        val methods = declaredMethods.declaredMethods
        val callersProperties = ps(methods.to(Iterable), Callers)
        assert(callersProperties.forall(_.isFinal))

        val reachableMethods = callersProperties
            // .filterNot(_.asFinal.p == NoCallers)
            .map { v => v.e -> v.ub }
            .toMap

        val aliasFieldsMap: mutable.Map[Field, mutable.Set[AliasField]] = mutable.Map.empty[Field, mutable.Set[AliasField]]

        def handleFieldAccess(
            declaringClass: ObjectType,
            name:           String,
            fieldType:      ReferenceType,
            method:         Method,
            objRefDefSites: IntTrieSet
        ): Unit = {
            val declaredField = declaredFields(declaringClass, name, fieldType)

            if (declaredField.isDefinedField) {
                aliasFieldsMap.getOrElseUpdate(declaredField.definedField, mutable.Set.empty).addOne(AliasField(
                    FieldReference(
                        declaredField.definedField,
                        simpleContexts(declaredMethods(method)),
                        objRefDefSites
                    )
                ))
            }
        }

        reachableMethods.keys
            .filter(m => m.hasSingleDefinedMethod || m.hasMultipleDefinedMethods)
            .foreach(m =>
                m.foreachDefinedMethod(m => {

                    if (m.body.isDefined) {
                        val tac = ps(m, TACAI.key).asFinal.p.tac.get

                        tac.stmts.foreach {
                            case Assignment(
                                    _,
                                    _,
                                    GetField(_, declaringClass, name, fieldType: ReferenceType, UVar(_, objRefDefSites))
                                ) => handleFieldAccess(declaringClass, name, fieldType, m, objRefDefSites)
                            case PutField(
                                    _,
                                    declaringClass,
                                    name,
                                    fieldType: ReferenceType,
                                    UVar(_, objRefDefSites),
                                    _
                                ) => handleFieldAccess(declaringClass, name, fieldType, m, objRefDefSites)
                            case _ =>
                        }
                    }

                })
            )

//        aliasFields = aliasFields.distinct // TODO schöner?

        val entities = ArrayBuffer.empty[AliasEntity]
//
        for (aliasFields <- aliasFieldsMap.values.map(_.toSeq)) {
            for (i <- aliasFields.indices) {
                val e1 = aliasFields(i)
                for (j <- i + 1 until aliasFields.size) {
                    val e2 = aliasFields(j)
                    // TODO auf selbe klasse beschränken?
                    entities += AliasEntity(e1.fieldReference.context, e2.fieldReference.context, e1, e2)
                }
            }
        }

        ps.scheduleEagerComputationsForEntities(entities)(analysis.determineAlias)
        analysis
    }

    override def derivesEagerly: Set[PropertyBounds] = Set(derivedProperty)

}
