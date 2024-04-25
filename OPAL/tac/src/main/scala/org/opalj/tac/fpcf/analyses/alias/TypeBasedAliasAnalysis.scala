package org.opalj.tac.fpcf.analyses.alias

trait TypeBasedAliasAnalysis extends SetBasedAliasAnalysis {

    override protected[this] type AliasSet = TypeBasedAliasSet
    override protected[this] type AnalysisState <: TypeBasedAliasAnalysisState

}
