/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj
package br
package fpcf
package properties
package alias

import org.opalj.fpcf.Property

trait AliasSet[ElementType, AliasSetT, T <: AliasSet[ElementType, AliasSetT, T]]  extends Property {

    def elements: AliasSetT
}
