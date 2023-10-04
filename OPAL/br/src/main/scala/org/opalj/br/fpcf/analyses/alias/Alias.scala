/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.br.fpcf.analyses.alias

import org.opalj.fpcf.{Property, PropertyKey, PropertyMetaInformation}

sealed trait AliasPropertyMetaInformation extends PropertyMetaInformation {
    type Self = Alias
}

sealed trait Alias extends AliasPropertyMetaInformation with Property {

    final def key: PropertyKey[Alias] = Alias.key
}

object Alias extends AliasPropertyMetaInformation {
    final val PropertyKeyName = "opalj.Alias"

    final val key: PropertyKey[Alias] = {
        PropertyKey.create(
            PropertyKeyName,
            MayAlias
        )
    }
}

case object MustAlias extends Alias

case object MayAlias extends Alias

case object NoAlias extends Alias