/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org
package opalj
package tac
package fpcf
package analyses
package alias

import org.opalj.br.analyses.SomeProject
import org.opalj.br.fpcf.properties.Context
import org.opalj.fpcf.PropertyStore

class AliasAnalysisContext(
        val entity:        (Context, AliasEntity, AliasEntity),
        val project:       SomeProject,
        val propertyStore: PropertyStore
) {

    def context: Context = entity._1

    def entity1: AliasEntity = entity._2

    def entity2: AliasEntity = entity._3

}
