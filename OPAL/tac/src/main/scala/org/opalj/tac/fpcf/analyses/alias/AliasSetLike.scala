/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.tac.fpcf.analyses.alias

import scala.collection.mutable

trait AliasSetLike[ElementType, T <: AliasSetLike[ElementType, T]] {

    protected var _pointsToAny: Boolean = false

    def addPointsTo(pointsTo: ElementType): Unit = allPointsTo.add(pointsTo)

    def pointsTo(element: ElementType): Boolean = allPointsTo.contains(element)

    def setPointsToAny(): Unit = _pointsToAny = true

    def isEmpty: Boolean = allPointsTo.isEmpty

    def size: Int = allPointsTo.size

    def allPointsTo: mutable.Set[ElementType]

    def intersection(other: T): T

}
