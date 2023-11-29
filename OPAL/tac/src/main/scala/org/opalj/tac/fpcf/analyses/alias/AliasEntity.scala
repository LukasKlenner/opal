/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org
package opalj
package tac
package fpcf
package analyses
package alias

import org.opalj.br.fpcf.properties.Context

case class AliasEntity(context: Context, var element1: AliasSourceElement, var element2: AliasSourceElement) {

    if (element1.hashCode() > element2.hashCode()) {
        val tmp = element1
        element1 = element2
        element2 = tmp
    }

}
