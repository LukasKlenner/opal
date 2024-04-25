package org.opalj.tac.fpcf.analyses.alias

import org.opalj.br.ReferenceType

import scala.collection.mutable

class TypeBasedAliasSet extends AliasSetLike[ReferenceType, TypeBasedAliasSet] {

    private var _pointsTo: mutable.Set[ReferenceType] = mutable.Set.empty[ReferenceType]

    override def allPointsTo: mutable.Set[ReferenceType] = _pointsTo

    override def intersection(other: TypeBasedAliasSet): TypeBasedAliasSet =  {
        val intersection = new TypeBasedAliasSet

        // we could theoretically optimize this by stopping once two intersecting elements have been found
        // because more than two intersecting elements won't change the behaviour of an alias analysis
        intersection._pointsTo = _pointsTo.intersect(other._pointsTo)
        intersection._pointsToAny = _pointsToAny || other._pointsToAny

        intersection
    }
}
