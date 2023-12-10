/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj
package tac
package fpcf
package analyses
package alias

import org.opalj.br.analyses.{SomeProject, VirtualFormalParametersKey}
import org.opalj.fpcf.PropertyStore
import org.opalj.tac.cg.TypeIteratorKey
import org.opalj.tac.common.DefinitionSitesKey

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
