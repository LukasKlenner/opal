/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj
package tac
package cg

import org.opalj.br.analyses.ProjectInformationKeys
import org.opalj.br.analyses.SomeProject
import org.opalj.br.analyses.VirtualFormalParametersKey
import org.opalj.br.fpcf.FPCFAnalysisScheduler
import org.opalj.br.fpcf.properties.CallStringContextsKey
import org.opalj.tac.common.DefinitionSitesKey
import org.opalj.tac.fpcf.analyses.cg.CFA_k_l_TypeIterator

/**
 * A [[org.opalj.br.analyses.ProjectInformationKey]] to compute a [[CallGraph]] based on
 * the points-to analysis.
 *
 * @see [[CallGraphKey]] for further details.
 *
 * @author Dominik Helm
 */
object CFA_1_1_CallGraphKey extends CallGraphKey {

    override def requirements(project: SomeProject): ProjectInformationKeys = {
        Seq(DefinitionSitesKey, VirtualFormalParametersKey, CallStringContextsKey) ++:
            super.requirements(project)
    }

    override protected def callGraphSchedulers(
        project: SomeProject
    ): Iterable[FPCFAnalysisScheduler] = {
        AllocationSiteBasedPointsToCallGraphKey.callGraphSchedulers(project)
    }
    override def getTypeIterator(project: SomeProject) =
        new CFA_k_l_TypeIterator(project, 1, 1)
}
