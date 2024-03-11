/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.tac.fpcf.analyses.alias.pointsto

import org.opalj.br.PC
import org.opalj.br.fpcf.properties.Context
import org.opalj.br.fpcf.properties.alias.AliasSourceElement
import org.opalj.fpcf.Entity
import org.opalj.tac.fpcf.analyses.alias.AliasAnalysisContext
import org.opalj.tac.fpcf.analyses.alias.AliasAnalysisState

class PointsToBasedAliasAnalysisState extends AliasAnalysisState {

    private[this] var _pointsTo1: Set[(Context, PC)] = Set[(Context, PC)]()
    private[this] var _pointsTo2: Set[(Context, PC)] = Set[(Context, PC)]()

    private[this] var _pointsToFinal: Map[Entity, Boolean] = Map[Entity, Boolean]()

    private[this] var _pointsToElementsHandled: Map[Entity, Int] = Map[Entity, Int]()

    private[this] var _entityToAliasEntity: Map[Entity, AliasSourceElement] = Map[Entity, AliasSourceElement]()

    private[this] var _somePointsTo: Boolean = false

    private[this] var _pointsToNull1: Boolean = false

    private[this] var _pointsToNull2: Boolean = false

    def pointsTo1: Set[(Context, PC)] = _pointsTo1

    def pointsTo2: Set[(Context, PC)] = _pointsTo2

    def addPointsTo(e: AliasSourceElement, pointsTo: (Context, PC))(implicit context: AliasAnalysisContext): Unit = {
        if (context.isElement1(e)) {
            _pointsTo1 += pointsTo
        } else {
            _pointsTo2 += pointsTo
        }
    }

    def pointsToElementsHandled(e: (Entity, AliasSourceElement)): Int = {
        _pointsToElementsHandled.getOrElse(e, 0)
    }

    def incPointsToElementsHandled(e: (Entity, AliasSourceElement)): Unit = {
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

    def allPointsToFinal(): Boolean = {
        _pointsToFinal.values.forall(identity)
    }

    def setPointsToFinal(entity: Entity, boolean: Boolean): Unit = {
        _pointsToFinal += entity -> boolean
    }

    def pointsToNull1: Boolean = _pointsToNull1

    def pointsToNull2: Boolean = _pointsToNull2

    def setPointsToNull(ase: AliasSourceElement)(implicit context: AliasAnalysisContext): Unit = {
        if (context.isElement1(ase)) {
            _pointsToNull1 = true
        } else {
            _pointsToNull2 = true
        }
    }

}
