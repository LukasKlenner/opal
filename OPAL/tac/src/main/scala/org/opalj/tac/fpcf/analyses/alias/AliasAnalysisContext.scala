/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org
package opalj
package tac
package fpcf
package analyses
package alias

import org.opalj.br.analyses.SomeProject
import org.opalj.br.fpcf.properties.Context
import org.opalj.br.fpcf.properties.alias.AliasEntity
import org.opalj.br.fpcf.properties.alias.AliasSourceElement
import org.opalj.fpcf.PropertyStore

class AliasAnalysisContext(
    val entity:        AliasEntity,
    val project:       SomeProject,
    val propertyStore: PropertyStore
) {

    def context1: Context = entity.context1

    def context2: Context = entity.context2

    def contextOf(ase: AliasSourceElement): Context = {
        if (isElement1(ase)) context1 else context2
    }

    def element1: AliasSourceElement = entity.element1

    def element2: AliasSourceElement = entity.element2

    def isElement1(e: AliasSourceElement): Boolean = element1 eq e

    def isElement2(e: AliasSourceElement): Boolean = element2 eq e

}
