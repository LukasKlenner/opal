/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.tac.fpcf.analyses.alias.pointsto

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
import org.opalj.tac.common.DefinitionSitesKey
import org.opalj.tac.fpcf.analyses.alias.AliasDS
import org.opalj.tac.fpcf.analyses.alias.AliasEntity
import org.opalj.tac.fpcf.analyses.alias.AliasFP
import org.opalj.tac.fpcf.analyses.alias.AliasSourceElement
import org.opalj.tac.fpcf.analyses.pointsto.AbstractPointsToAnalysis
import org.opalj.tac.fpcf.analyses.pointsto.AllocationSiteBasedAnalysis

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

        val allocationSites = p
            .get(DefinitionSitesKey)
            .getAllocationSites
            .filter(as => reachableMethods.contains(declaredMethods(as.method)))
        val formalParameters = p
            .get(VirtualFormalParametersKey)
            .virtualFormalParameters
            .filter(fp => reachableMethods.contains(fp.method))

        val aliasEntities: Seq[AliasSourceElement] = allocationSites.map(AliasDS(_, p)) ++ formalParameters
            .map(AliasFP)

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

}