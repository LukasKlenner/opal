/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj
package tac
package fpcf
package analyses
package alias

import scala.collection.mutable.ArrayBuffer

import org.opalj.br.VirtualDeclaredMethod
import org.opalj.br.analyses.DeclaredMethodsKey
import org.opalj.br.analyses.ProjectInformationKeys
import org.opalj.br.analyses.SomeProject
import org.opalj.br.analyses.VirtualFormalParameter
import org.opalj.br.analyses.VirtualFormalParametersKey
import org.opalj.br.analyses.cg.IsOverridableMethodKey
import org.opalj.br.fpcf.BasicFPCFEagerAnalysisScheduler
import org.opalj.br.fpcf.FPCFAnalysis
import org.opalj.br.fpcf.FPCFAnalysisScheduler
import org.opalj.br.fpcf.properties
import org.opalj.br.fpcf.properties.SimpleContextsKey
import org.opalj.br.fpcf.properties.alias.Alias
import org.opalj.br.fpcf.properties.alias.AliasEntity
import org.opalj.br.fpcf.properties.alias.AliasFormalParameter
import org.opalj.br.fpcf.properties.alias.AliasNull
import org.opalj.br.fpcf.properties.alias.AliasReturnValue
import org.opalj.br.fpcf.properties.alias.AliasSourceElement
import org.opalj.br.fpcf.properties.alias.MayAlias
import org.opalj.br.fpcf.properties.alias.MustAlias
import org.opalj.br.fpcf.properties.alias.NoAlias
import org.opalj.br.fpcf.properties.cg.Callees
import org.opalj.br.fpcf.properties.cg.Callers
import org.opalj.br.fpcf.properties.cg.NoCallers
import org.opalj.fpcf.ProperPropertyComputationResult
import org.opalj.fpcf.PropertyBounds
import org.opalj.fpcf.PropertyStore
import org.opalj.fpcf.Result
import org.opalj.tac.cg.TypeIteratorKey
import org.opalj.tac.common.DefinitionSiteLike
import org.opalj.tac.common.DefinitionSitesKey
import org.opalj.tac.fpcf.properties.TACAI

class IntraProceduralNoAliasAnalysis(final val project: SomeProject) extends TacBasedAliasAnalysis {

    override type AnalysisContext = AliasAnalysisContext
    override type AnalysisState = TacBasedAliasAnalysisState

    protected[this] def analyzeTAC()(
        implicit
        context: AnalysisContext,
        state:   AnalysisState
    ): ProperPropertyComputationResult = {
        assert(state.tacai1.isDefined)

        if (context.entity.bothElementsMethodBound && !context.entity.elementsInSameMethod) {
            return result(MayAlias) // two elements in different methods are not yet supported
        }

        if (context.element1 == context.element2) {
            context.element1.element match {
                case _: DefinitionSiteLike     => return Result(context.entity, MayAlias) // DS might be inside loop
                case _: VirtualFormalParameter => return Result(context.entity, MustAlias)
                case _                         => throw new UnknownError("unhandled entity type")
            }
        }

        (context.element1, context.element2) match {
            case (rv1: AliasReturnValue, rv2: AliasReturnValue) =>
                if (allReturnExpr.forall(isNullReturn)) return result(MustAlias)
                result(MayAlias)

            case (rv: AliasReturnValue, e2) => {

                e2 match {
                    case AliasNull =>
                        if (allReturnExpr.forall(isNullReturn)) return result(MustAlias)

                        // all returns return a local variable that is only assigned to a new allocation
                        if (allReturnExpr.forall(expr => {
                                if (expr.isVar) {
                                    val defSites = expr.asVar.definedBy.filter(_ >= 0)
                                    var nonNewDefSite = false
                                    for (defSite <- defSites) {
                                        val defStmt = state.tacai1.get.stmts(defSite)
                                        if (!defStmt.isAssignment || !defStmt.asAssignment.expr.isNew) {
                                            nonNewDefSite = true
                                        }
                                    }
                                    !nonNewDefSite
                                } else
                                    false
                            })
                        ) return result(NoAlias)

                        result(MayAlias)
                    case _ =>
                        val anyMatch: Boolean = false
                        for (returnExpr <- allReturnExpr) {

                            if (returnExpr.isVar) {
//                                if (returnExpr.asVar.definedBy.contains(state.defSite2)) {
//                                    anyMatch = true;
//                                }
                            }

                        }

                        if (!anyMatch) result(NoAlias)
                        else result(MayAlias)
                }

            }
            case (AliasNull, e2) => {

                e2 match {
                    case AliasNull =>
                        result(MustAlias)
                    case _ =>
                        result(NoAlias)

                }
            }
            case (fp1: AliasFormalParameter, fp2: AliasFormalParameter) => result(MayAlias)
            case _ => {
//                for (use <- state.uses1) {
//
//                    if (state.uses2.contains(use)) {
//                        return result(MayAlias)
//                    }
//                }
                result(NoAlias)
            }

        }
    }

    private[this] def allReturnExpr(implicit state: AnalysisState): Array[Expr[V]] = {
        state.tacai1.get.stmts.filter(stmt => stmt.isReturnValue).map(_.asReturnValue.expr)
    }

    private[this] def isNullReturn(expr: Expr[V]): Boolean = {
        if (!expr.isVar) false
        if (!expr.asVar.value.isReferenceValue) false
        expr.asVar.value.asReferenceValue.isNull.isYes
    }

    private[this] def result(alias: Alias)(implicit context: AnalysisContext) = {
        Result(context.entity, alias)
    }

    override protected[this] def createState: AnalysisState = new TacBasedAliasAnalysisState

    override protected[this] def createContext(
        entity: AliasEntity
    ): AliasAnalysisContext =
        new AliasAnalysisContext(entity, project, propertyStore)
}

sealed trait IntraProceduralAliasAnalysisScheduler extends FPCFAnalysisScheduler {

    override def requiredProjectInformation: ProjectInformationKeys =
        Seq(DeclaredMethodsKey, VirtualFormalParametersKey, IsOverridableMethodKey, TypeIteratorKey)

    final def derivedProperty: PropertyBounds = PropertyBounds.lub(Alias)

    override def uses: Set[PropertyBounds] = Set(
        PropertyBounds.ub(TACAI),
        PropertyBounds.ub(Callees)
    )
}

object EagerIntraProceduralAliasAnalysis extends IntraProceduralAliasAnalysisScheduler
    with BasicFPCFEagerAnalysisScheduler {

    override def requiredProjectInformation: ProjectInformationKeys =
        super.requiredProjectInformation ++ Seq(DefinitionSitesKey, SimpleContextsKey)

    override def start(p: SomeProject, ps: PropertyStore, unused: Null): FPCFAnalysis = {
        val analysis = new IntraProceduralNoAliasAnalysis(p)
        val simpleContexts = p.get(SimpleContextsKey)
        val declaredMethods = p.get(DeclaredMethodsKey)

        val methods = declaredMethods.declaredMethods
        val callersProperties = ps(methods.to(Iterable), Callers)
        assert(callersProperties.forall(_.isFinal))

        val reachableMethods = callersProperties
            .filterNot(_.asFinal.p == NoCallers)
            .map { v => v.e -> v.ub }
            .toMap

        val formalParameters = p
            .get(VirtualFormalParametersKey)
            .virtualFormalParameters
            .filter(fp => reachableMethods.contains(fp.method))
        val returnValues = reachableMethods.keys
            .filter(!_.isInstanceOf[VirtualDeclaredMethod])
            .filter(_.descriptor.returnType.isReferenceType)

        val aliasEntities: Seq[AliasSourceElement] =
            (formalParameters.map(AliasFormalParameter) ++
                returnValues.map(m => AliasReturnValue(m.definedMethod, p))).toSeq

        val entities: ArrayBuffer[AliasEntity] = ArrayBuffer.empty

        for (e1 <- aliasEntities) {
            for (e2 <- aliasEntities) {
                if (e1 != e2 && e1.method == e2.method) {
                    val context = simpleContexts(declaredMethods(e1.method))
                    val entity = properties.alias.AliasEntity(context, context, e1, e2) // TODO do properly

                    if (!entities.contains(entity)) {
                        entities.addOne(entity)
                    }
                }
            }
        }

        for (e1 <- aliasEntities) {
            val context = simpleContexts(declaredMethods(e1.method))
            val entity = properties.alias.AliasEntity(context, context, e1, AliasNull) // TODO do properly
            entities.addOne(entity)
        }

        ps.scheduleEagerComputationsForEntities(entities)(analysis.determineAlias)
        analysis
    }

    override def derivesEagerly: Set[PropertyBounds] = Set(derivedProperty)

    override def uses: Set[PropertyBounds] = super.uses + PropertyBounds.finalP(Callers)

    override def derivesCollaboratively: Set[PropertyBounds] = Set.empty

}
