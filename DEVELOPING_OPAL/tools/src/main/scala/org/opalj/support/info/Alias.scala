/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.support.info

import java.net.URL
import org.opalj.br.analyses.BasicReport
import org.opalj.br.analyses.Project
import org.opalj.br.analyses.ProjectAnalysisApplication
import org.opalj.br.analyses.ReportableAnalysisResult
import org.opalj.br.fpcf.FPCFAnalysesManagerKey
import org.opalj.br.fpcf.PropertyStoreKey
import org.opalj.br.fpcf.properties.alias.MayAlias
import org.opalj.br.fpcf.properties.alias.MustAlias
import org.opalj.br.fpcf.properties.alias.NoAlias
import org.opalj.tac.cg.AllocationSiteBasedPointsToCallGraphKey
import org.opalj.tac.fpcf.analyses.alias.pointsto.EagerFieldAccessAllocationSitePointsToBasedAliasAnalysisScheduler
import org.opalj.util.PerformanceEvaluation.time
import org.opalj.util.Seconds

object Alias extends ProjectAnalysisApplication {
    override def doAnalyze(
        project:       Project[URL],
        parameters:    Seq[String],
        isInterrupted: () => Boolean
    ): ReportableAnalysisResult = {

        val ps = project.get(PropertyStoreKey)

        var callGraphTime = Seconds.None
        var analysisTime = Seconds.None

        time {
            project.get(AllocationSiteBasedPointsToCallGraphKey)
        } { t => callGraphTime = t.toSeconds }

        time {
            project.get(FPCFAnalysesManagerKey).runAll(
                EagerFieldAccessAllocationSitePointsToBasedAliasAnalysisScheduler
            )
        } { t => analysisTime = t.toSeconds }

        val mayAlias = ps.finalEntities(MayAlias).toSeq
        val noAlias = ps.finalEntities(NoAlias).toSeq
        val mustAlias = ps.finalEntities(MustAlias).toSeq

        val message =
            s"""|# mayAlias Pairs: ${mayAlias.size}
          |# noAlias Pairs: ${noAlias.size}
          |# mustAlias Pairs: ${mustAlias.size}
          |Call Graph Construction Time: $callGraphTime
          |Alias Analysis Time: $analysisTime
          |"""

        BasicReport(message.stripMargin('|'))

    }
}
