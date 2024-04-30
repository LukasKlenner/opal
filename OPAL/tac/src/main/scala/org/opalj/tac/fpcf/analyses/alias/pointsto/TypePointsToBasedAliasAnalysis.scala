/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.tac.fpcf.analyses.alias.pointsto

import org.opalj.br.ReferenceType
import org.opalj.br.analyses.DeclaredFieldsKey
import org.opalj.br.analyses.DeclaredMethodsKey
import org.opalj.br.analyses.SomeProject
import org.opalj.br.fpcf.BasicFPCFEagerAnalysisScheduler
import org.opalj.br.fpcf.BasicFPCFLazyAnalysisScheduler
import org.opalj.br.fpcf.FPCFAnalysis
import org.opalj.br.fpcf.properties.SimpleContextsKey
import org.opalj.br.fpcf.properties.alias.Alias
import org.opalj.br.fpcf.properties.alias.AliasEntity
import org.opalj.br.fpcf.properties.alias.AliasField
import org.opalj.br.fpcf.properties.alias.AliasSourceElement
import org.opalj.br.fpcf.properties.alias.FieldReference
import org.opalj.br.fpcf.properties.cg.Callers
import org.opalj.fpcf.Entity
import org.opalj.fpcf.PropertyBounds
import org.opalj.fpcf.PropertyStore
import org.opalj.tac.Assignment
import org.opalj.tac.GetField
import org.opalj.tac.PutField
import org.opalj.tac.UVar
import org.opalj.tac.fpcf.analyses.alias.TacBasedAliasAnalysis
import org.opalj.tac.fpcf.analyses.alias.TypeBasedAliasAnalysis
import org.opalj.tac.fpcf.analyses.pointsto.AbstractPointsToAnalysis
import org.opalj.tac.fpcf.analyses.pointsto.TypeBasedAnalysis
import org.opalj.tac.fpcf.properties.TACAI

import scala.collection.mutable.ArrayBuffer

/**
 * An alias analysis based on points-to information that contain the possible [[ReferenceType]]s of an element.
 * @param project The project
 */
class TypePointsToBasedAliasAnalysis(final val project: SomeProject)
    extends AbstractPointsToBasedAliasAnalysis
    with AbstractPointsToAnalysis
    with TypeBasedAnalysis
    with TypeBasedAliasAnalysis
    with TacBasedAliasAnalysis {

    override protected[this] type AnalysisState = TypePointsToBasedAliasAnalysisState

    override protected[this] def handlePointsToSetElement(
        ase:            AliasSourceElement,
        pointsToEntity: Entity,
        element:        ReferenceType
    )(
        implicit
        state:   AnalysisState,
        context: AnalysisContext
    ): Unit = {
        state.addPointsTo(ase, element)
        state.incPointsToElementsHandled(ase, pointsToEntity)
    }
    /**
     * Creates the state to use for the computation.
     */
    override protected[this] def createState: AnalysisState = new TypePointsToBasedAliasAnalysisState
}

/**
 * A scheduler for a lazy, type, points-to based alias analysis.
 */
object LazyTypePointsToBasedAliasAnalysisScheduler extends PointsToBasedAliasAnalysisScheduler
    with BasicFPCFLazyAnalysisScheduler {
    override def derivesLazily: Some[PropertyBounds] = Some(derivedProperty)

    override def register(
        project:       SomeProject,
        propertyStore: PropertyStore,
        i:             LazyTypePointsToBasedAliasAnalysisScheduler.InitializationData
    ): FPCFAnalysis = {

        val analysis = new TypePointsToBasedAliasAnalysis(project)

        propertyStore.registerLazyPropertyComputation(
            Alias.key,
            analysis.determineAlias
        )

        analysis
    }
}

object EagerFieldAccessTypePointsToBasedAliasAnalysisScheduler extends PointsToBasedAliasAnalysisScheduler
    with BasicFPCFEagerAnalysisScheduler {

    override def start(p: SomeProject, ps: PropertyStore, unused: Null): FPCFAnalysis = {
        val analysis = new TypePointsToBasedAliasAnalysis(p)
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

        var aliasFields: ArrayBuffer[AliasField] = ArrayBuffer.empty[AliasField]

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
                            ) => aliasFields += AliasField(
                                FieldReference(
                                    declaredFields(declaringClass, name, fieldType).definedField,
                                    simpleContexts(declaredMethods(m)),
                                    objRefDefSites
                                )
                            )
                            case PutField(
                            _,
                            declaringClass,
                            name,
                            fieldType: ReferenceType,
                            UVar(_, objRefDefSites),
                            _
                            ) => aliasFields += AliasField(
                                FieldReference(
                                    declaredFields(declaringClass, name, fieldType).definedField,
                                    simpleContexts(declaredMethods(m)),
                                    objRefDefSites
                                )
                            )
                            case _ =>
                        }
                    }

                })
            )

        aliasFields = aliasFields.distinct //TODO schöner?

        val entities = ArrayBuffer.empty[AliasEntity]

        for (i <- aliasFields.indices) {
            val e1 = aliasFields(i)
            for (j <- i + 1 until aliasFields.size) {
                val e2 = aliasFields(j)
                //TODO auf selbe klasse beschränken?
                entities += AliasEntity(e1.fieldReference.context, e2.fieldReference.context, e1, e2)
            }
        }

        ps.scheduleEagerComputationsForEntities(entities)(analysis.determineAlias)
        analysis
    }

    override def derivesEagerly: Set[PropertyBounds] = Set(derivedProperty)

}
