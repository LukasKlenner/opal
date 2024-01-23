/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.tac.fpcf.analyses.alias.pointsto

import org.opalj.br.PC
import org.opalj.br.fpcf.properties.Context
import org.opalj.fpcf.Entity
import org.opalj.tac.fpcf.analyses.alias.AliasAnalysisContext
import org.opalj.tac.fpcf.analyses.alias.AliasAnalysisState
import org.opalj.tac.fpcf.analyses.alias.AliasSourceElement

class PointsToBasedAliasAnalysisState extends AliasAnalysisState {

    var _pointsTo1: Set[(Context, PC, Int)] = Set[(Context, PC, Int)]()
    var _pointsTo2: Set[(Context, PC, Int)] = Set[(Context, PC, Int)]()

    var _pointsToElementsHandled: Map[Entity, Int] = Map[Entity, Int]()

    var _entityToAliasEntity: Map[Entity, AliasSourceElement] = Map[Entity, AliasSourceElement]()

    var _somePointsTo: Boolean = false

    def pointsTo1: Set[(Context, PC, Int)] = _pointsTo1

    def pointsTo2: Set[(Context, PC, Int)] = _pointsTo2

    def addPointsTo1(pointsTo: (Context, PC, Int)): Unit = {
        _pointsTo1 += pointsTo
    }

    def addPointsTo2(pointsTo: (Context, PC, Int)): Unit = {
        _pointsTo2 += pointsTo
    }

    def addPointsTo(e: AliasSourceElement, pointsTo: (Context, PC, Int))(implicit context: AliasAnalysisContext) = {
        if (context.isElement1(e)) {
            addPointsTo1(pointsTo)
        } else {
            addPointsTo2(pointsTo)
        }
    }

    def pointsToElementsHandled(e: Entity): Int = {
        _pointsToElementsHandled.getOrElse(e, 0)
    }

    def incPointsToElementsHandled(e: Entity): Unit = {
        _pointsToElementsHandled += e -> (pointsToElementsHandled(e) + 1)
    }

    def entityToAliasSourceElement(e: Entity): AliasSourceElement = {
        _entityToAliasEntity(e)
    }

    def addEntityToAliasSourceElement(e: Entity, ase: AliasSourceElement): Unit = {
        _entityToAliasEntity += e -> ase
    }

    def somePointsTo: Boolean = _somePointsTo

    def setSomePointsTo(): Unit = {
        _somePointsTo = true
    }

}
