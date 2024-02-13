/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.support.info

import org.opalj.br.analyses.BasicReport
import org.opalj.br.analyses.Project
import org.opalj.br.analyses.ProjectAnalysisApplication
import org.opalj.br.analyses.ReportableAnalysisResult
import org.opalj.br.fpcf.FPCFAnalysesManagerKey
import org.opalj.br.fpcf.properties.alias.MayAlias
import org.opalj.br.fpcf.properties.alias.MustAlias
import org.opalj.br.fpcf.properties.alias.NoAlias
import org.opalj.tac.cg.AllocationSiteBasedPointsToCallGraphKey
import org.opalj.tac.fpcf.analyses.alias.pointsto.EagerPointsToBasedAliasAnalysisScheduler

import java.net.URL

object Alias extends ProjectAnalysisApplication {
    override def doAnalyze(
        project:       Project[URL],
        parameters:    Seq[String],
        isInterrupted: () => Boolean
    ): ReportableAnalysisResult = {

        project.get(AllocationSiteBasedPointsToCallGraphKey)

        val (ps, _ /*executed analyses*/ ) = project.get(FPCFAnalysesManagerKey).runAll(
            //AllocationSiteBasedPointsToAnalysisScheduler,
            EagerPointsToBasedAliasAnalysisScheduler
        )

        val mayAlias = ps.finalEntities(MayAlias).toSeq
        val noAlias = ps.finalEntities(NoAlias).toSeq
        val mustAlias = ps.finalEntities(MustAlias).toSeq

        val message =
            s"""|# mayAlias Pairs: ${mayAlias.size}
          |# noAlias Pairs: ${noAlias.size}
          |# mustAlias Pairs: ${mustAlias.size}
          |"""

        BasicReport(message.stripMargin('|'))

    }
}
