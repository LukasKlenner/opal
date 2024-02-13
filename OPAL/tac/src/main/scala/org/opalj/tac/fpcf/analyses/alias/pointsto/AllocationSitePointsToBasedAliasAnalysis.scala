/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.tac.fpcf.analyses.alias.pointsto

import org.opalj.br.Method
import org.opalj.br.analyses.DeclaredMethodsKey
import org.opalj.br.analyses.ProjectInformationKeys
import org.opalj.br.analyses.SomeProject
import org.opalj.br.analyses.VirtualFormalParametersKey
import org.opalj.br.fpcf.BasicFPCFEagerAnalysisScheduler
import org.opalj.br.fpcf.FPCFAnalysis
import org.opalj.br.fpcf.properties.SimpleContextsKey
import org.opalj.br.fpcf.properties.alias.AliasDS
import org.opalj.br.fpcf.properties.alias.AliasEntity
import org.opalj.br.fpcf.properties.alias.AliasFP
import org.opalj.br.fpcf.properties.alias.AliasSourceElement
import org.opalj.br.fpcf.properties.alias.AliasUVar
import org.opalj.br.fpcf.properties.cg.Callers
import org.opalj.br.fpcf.properties.cg.NoCallers
import org.opalj.br.fpcf.properties.pointsto.AllocationSitePointsToSet
import org.opalj.fpcf.PropertyBounds
import org.opalj.fpcf.PropertyStore
import org.opalj.tac.Assignment
import org.opalj.tac.Call
import org.opalj.tac.DUVar
import org.opalj.tac.DVar
import org.opalj.tac.ExprStmt
import org.opalj.tac.TACMethodParameter
import org.opalj.tac.TACode
import org.opalj.tac.UVar
import org.opalj.tac.common.DefinitionSitesKey
import org.opalj.tac.fpcf.analyses.alias.pcOfDefSite
import org.opalj.tac.fpcf.analyses.alias.persistentUVar
import org.opalj.tac.fpcf.analyses.pointsto.AbstractPointsToAnalysis
import org.opalj.tac.fpcf.analyses.pointsto.AllocationSiteBasedAnalysis
import org.opalj.tac.fpcf.properties.TACAI
import org.opalj.value.ValueInformation

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class AllocationSitePointsToBasedAliasAnalysis( final val project: SomeProject)
    extends AbstractPointsToBasedAliasAnalysis with AbstractPointsToAnalysis with AllocationSiteBasedAnalysis

object EagerPointsToBasedAliasAnalysisScheduler extends PointsToBasedAliasAnalysisScheduler with BasicFPCFEagerAnalysisScheduler {

    override def requiredProjectInformation: ProjectInformationKeys =
        super.requiredProjectInformation ++ Seq(DefinitionSitesKey, SimpleContextsKey)

    override def start(p: SomeProject, ps: PropertyStore, unused: Null): FPCFAnalysis = {
        val analysis = new AllocationSitePointsToBasedAliasAnalysis(p)
        val simpleContexts = p.get(SimpleContextsKey)
        val declaredMethods = p.get(DeclaredMethodsKey)
        //val declaredFields = p.get(DeclaredFieldsKey)

        val methods = declaredMethods.declaredMethods
        val callersProperties = ps(methods.to(Iterable), Callers)
        assert(callersProperties.forall(_.isFinal))

        val reachableMethods = callersProperties
            .filterNot(_.asFinal.p == NoCallers)
            .map { v =>
                v.e -> v.ub
            }
            .toMap

        type Tac = TACode[TACMethodParameter, DUVar[ValueInformation]]

        val uVars: mutable.Set[(UVar[ValueInformation], Method, Tac)] = mutable.Set.empty[(UVar[ValueInformation], Method, Tac)]
        val dVars: mutable.Set[(DVar[ValueInformation], Method, Tac)] = mutable.Set.empty[(DVar[ValueInformation], Method, Tac)]

        def handleCall(call: Call[_], m: Method, tac: Tac) = {
            call.allParams.foreach {
                case uVar: UVar[ValueInformation] => uVars.add((uVar, m, tac))
                case _                            =>
            }

            if (call.receiverOption.isDefined) {
                call.receiverOption.get match {
                    case uVar: UVar[ValueInformation] => uVars.add((uVar, m, tac))
                    case _                            =>
                }
            }
        }

        reachableMethods.keys
            .filter(m => m.hasSingleDefinedMethod || m.hasMultipleDefinedMethods)
            .foreach(m => m.foreachDefinedMethod(m => {

                if (m.body.isDefined) {
                    val tac = ps(m, TACAI.key).asFinal.p.tac.get

                    tac.stmts.foreach {
                        case Assignment(_, dVar: DVar[ValueInformation], _) => dVars.add((dVar, m, tac))
                        case call: Call[_]                                  => handleCall(call, m, tac)
                        case ExprStmt(_, expr) => {
                            expr match {
                                case uVar: UVar[ValueInformation] => uVars.add((uVar, m, tac))
                                case call: Call[_]                => handleCall(call, m, tac)
                                case _                            =>
                            }
                        }
                        case _ =>
                    }
                }

            }))
        val formalParameters = p
            .get(VirtualFormalParametersKey)
            .virtualFormalParameters
            .filter(fp => reachableMethods.contains(fp.method))

        val aliasEntities: Seq[AliasSourceElement] = formalParameters.map(AliasFP).toSeq ++
            uVars.map(uVar => AliasUVar(persistentUVar(uVar._1)(uVar._3.stmts), uVar._2, p)) ++
            dVars.map(dVar => AliasDS(pcOfDefSite(dVar._1.origin)(dVar._3.stmts), dVar._2, p))

        val entities: ArrayBuffer[AliasEntity] = ArrayBuffer.empty

        for (e1 <- aliasEntities) {
            for (e2 <- aliasEntities) {
                if (e1 != e2 && e1.method == e2.method) { //TODO auch für verschiedene Methoden
                    val context = simpleContexts(declaredMethods(e1.method))
                    val entity = AliasEntity(context, e1, e2)

                    if (!entities.contains(entity)) {
                        entities.addOne(entity)
                    }
                }
            }

        }

        ps.scheduleEagerComputationsForEntities(entities)(analysis.determineAlias)
        analysis
    }

    override def derivesEagerly: Set[PropertyBounds] = Set(derivedProperty)

    override def uses: Set[PropertyBounds] = super.uses + PropertyBounds.finalP(Callers) + PropertyBounds.finalP(AllocationSitePointsToSet)

    override def derivesCollaboratively: Set[PropertyBounds] = Set.empty

    /*private def addAllUVars(method: Method, buffer: ArrayBuffer[UVar[ValueInformation]])
                           (implicit ps: PropertyStore): (Method => Unit) = {
      m => {

        if (method.body.isDefined) {
          val tac = ps(method, TACAI.key)

          tac.asFinal.p.tac.get.stmts.foreach(stmt => {

          })
        }
      }
    }*/
}