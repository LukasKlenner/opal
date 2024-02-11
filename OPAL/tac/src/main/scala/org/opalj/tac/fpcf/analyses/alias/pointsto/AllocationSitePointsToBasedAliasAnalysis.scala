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
import org.opalj.br.fpcf.properties.cg.Callers
import org.opalj.br.fpcf.properties.cg.NoCallers
import org.opalj.br.fpcf.properties.pointsto.AllocationSitePointsToSet
import org.opalj.fpcf.PropertyBounds
import org.opalj.fpcf.PropertyStore
import org.opalj.tac.Assignment
import org.opalj.tac.Call
import org.opalj.tac.DVar
import org.opalj.tac.UVar
import org.opalj.tac.common.DefinitionSitesKey
import org.opalj.tac.fpcf.analyses.alias.AliasDVar
import org.opalj.tac.fpcf.analyses.alias.AliasEntity
import org.opalj.tac.fpcf.analyses.alias.AliasFP
import org.opalj.tac.fpcf.analyses.alias.AliasSourceElement
import org.opalj.tac.fpcf.analyses.alias.AliasUVar
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

        val methods = declaredMethods.declaredMethods
        val callersProperties = ps(methods.to(Iterable), Callers)
        assert(callersProperties.forall(_.isFinal))

        val reachableMethods = callersProperties
            .filterNot(_.asFinal.p == NoCallers)
            .map { v =>
                v.e -> v.ub
            }
            .toMap

        val uVars: mutable.Set[(UVar[ValueInformation], Method)] = mutable.Set.empty[(UVar[ValueInformation], Method)]
        val dVars: mutable.Set[(DVar[ValueInformation], Method)] = mutable.Set.empty[(DVar[ValueInformation], Method)]


        reachableMethods.keys
              .filter(m => m.hasSingleDefinedMethod || m.hasMultipleDefinedMethods)
              .foreach(m => m.foreachDefinedMethod(m => {

                if (m.body.isDefined) {
                  val tac = ps(m, TACAI.key)
                  //forAllSubexpressions
                  tac.asFinal.p.tac.get.stmts.foreach {
                    case Assignment(_, dVar: DVar[ValueInformation], _) => dVars.add((dVar, m))
                    case call: Call[_] => {
                      call.allParams.foreach {
                        case uVar: UVar[ValueInformation] => uVars.add((uVar, m))
                        case _ =>
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
          uVars.map(uVar => AliasUVar(uVar._1, uVar._2, p)) ++
          dVars.map(dVar => AliasDVar(dVar._1, dVar._2, p))

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