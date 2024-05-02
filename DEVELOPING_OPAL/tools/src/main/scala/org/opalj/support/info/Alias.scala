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
import org.opalj.tac.cg.CFA_1_0_CallGraphKey
import org.opalj.tac.cg.CFA_1_1_CallGraphKey
import org.opalj.tac.cg.TypeBasedPointsToCallGraphKey
import org.opalj.tac.fpcf.analyses.alias.pointsto.EagerFieldAccessAllocationSitePointsToBasedAliasAnalysisScheduler
import org.opalj.util.PerformanceEvaluation.time
import org.opalj.util.Seconds

object Alias extends ProjectAnalysisApplication {
    override def doAnalyze(
        project:       Project[URL],
        parameters:    Seq[String],
        isInterrupted: () => Boolean
    ): ReportableAnalysisResult = {

        var cgAlgorithm: String = "PointsTo"
        var aliasAlgorithm: String = "pointsTo"
        var client: String = "MethodInvocationTarget"

        val cgRegex = "-cg=(.*)".r
        val aliasRegex = "-alias=(.*)".r
        val clientRegex = "-client=(.*)".r

        parameters.foreach {
            case cgRegex(cg) => cgAlgorithm = cg
            case aliasRegex(alias) => aliasAlgorithm = alias
            case clientRegex(cl) => client = cl
            case p => throw new IllegalArgumentException("Unknown parameter: " + p)
        }

        val ps = project.get(PropertyStoreKey)

        var callGraphTime = Seconds.None
        var analysisTime = Seconds.None

        time {
            project.get(cgAlgorithm match {
                case "PointsTo" => AllocationSiteBasedPointsToCallGraphKey
                case "TypeBasedPointsTo" => TypeBasedPointsToCallGraphKey
                case "1-1-CFA" => CFA_1_1_CallGraphKey
                case "1-0-CFA" => CFA_1_0_CallGraphKey
                case _ => throw new IllegalArgumentException("Unknown call graph algorithm: " + cgAlgorithm)
            })
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
