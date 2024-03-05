/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.tac.fpcf.analyses.alias.pointsto

import org.opalj.br.analyses.SomeProject
import org.opalj.br.analyses.VirtualFormalParametersKey
import org.opalj.br.fpcf.properties.alias.AliasEntity
import org.opalj.fpcf.PropertyStore
import org.opalj.tac.cg.TypeIteratorKey
import org.opalj.tac.common.DefinitionSitesKey
import org.opalj.tac.fpcf.analyses.alias.AliasAnalysisContext

class PointsToBasedAliasAnalysisContext(
    override val entity:        AliasEntity,
    override val project:       SomeProject,
    override val propertyStore: PropertyStore
) extends AliasAnalysisContext(entity, project, propertyStore) {

    private[this] val _virtualFormalParameters = project.get(VirtualFormalParametersKey)

    private[this] val _definitionSites = project.get(DefinitionSitesKey)

    private[this] val _typeIterator = project.get(TypeIteratorKey)

    def virtualFormalParameters = _virtualFormalParameters

    def definitionSites = _definitionSites

    def typeIterator = _typeIterator

}
