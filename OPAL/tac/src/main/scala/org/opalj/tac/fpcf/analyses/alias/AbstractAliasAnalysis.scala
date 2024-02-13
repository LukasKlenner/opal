/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org
package opalj
package tac
package fpcf
package analyses
package alias

import org.opalj.br.fpcf.FPCFAnalysis
import org.opalj.br.fpcf.properties.alias.AliasEntity
import org.opalj.fpcf.Entity
import org.opalj.fpcf.ProperPropertyComputationResult

trait AbstractAliasAnalysis extends FPCFAnalysis {

    type AnalysisContext <: AliasAnalysisContext
    type AnalysisState <: AliasAnalysisState

    def doDetermineAlias(
        implicit
        context: AnalysisContext,
        state:   AnalysisState
    ): ProperPropertyComputationResult

    def determineAlias(e: Entity): ProperPropertyComputationResult = {
        e match {
            case entity: AliasEntity =>
                doDetermineAlias(createContext(entity), createState)
            case _ => throw new UnknownError("unhandled entity type")
        }
    }

    protected[this] def createState: AnalysisState

    protected[this] def createContext(
        entity: AliasEntity
    ): AnalysisContext

}
