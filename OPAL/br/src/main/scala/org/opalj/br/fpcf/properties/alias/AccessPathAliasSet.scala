/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj
package br
package fpcf
package properties
package alias

import org.opalj.fpcf.{Entity, FallbackReason, OrderedProperty, PropertyIsNotDerivedByPreviouslyExecutedAnalysis, PropertyKey, PropertyMetaInformation, PropertyStore}

import scala.collection.immutable.HashSet

sealed trait AccessPathAliasSetPropertyMetaInformation extends PropertyMetaInformation {

    final type Self = AccessPathAliasSet
}

sealed trait AccessPathAliasSet
    extends AliasSet[AccessPath, HashSet[AccessPath], AccessPathAliasSet]
    with OrderedProperty
    with AccessPathAliasSetPropertyMetaInformation {

    final def key: PropertyKey[AccessPathAliasSet] = AccessPathAliasSet.key

    override def checkIsEqualOrBetterThan(e: Entity, other: AccessPathAliasSet): Unit = {
      if (!elements.subsetOf(other.elements)) {
        throw new IllegalArgumentException(s"$e: illegal refinement of property $other to $this")
      }
    }
}

object AccessPathAliasSet extends AccessPathAliasSetPropertyMetaInformation {
    final val key: PropertyKey[AccessPathAliasSet] = {
      val name = "opalj.AccessPathAliasSet"
      PropertyKey.create(
            name,
            (_: PropertyStore, reason: FallbackReason, _: Entity) => reason match {
              case PropertyIsNotDerivedByPreviouslyExecutedAnalysis => NoAccessPaths
              case _ => throw new IllegalStateException(s"No analysis is scheduled to property $name.")
            }
        )
    }
}

case class AccessPathAliasSetN() extends AccessPathAliasSet {
  override def elements: HashSet[AccessPath] = HashSet.empty
}

object NoAccessPaths extends AccessPathAliasSet {
    override def elements: HashSet[AccessPath] = HashSet.empty
}
