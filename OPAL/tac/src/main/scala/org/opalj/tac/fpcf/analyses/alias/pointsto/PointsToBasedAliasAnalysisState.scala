/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.tac.fpcf.analyses.alias.pointsto

import org.opalj.br.PC
import org.opalj.br.fpcf.properties.Context
import org.opalj.br.fpcf.properties.alias.AliasSourceElement
import org.opalj.fpcf.Entity
import org.opalj.fpcf.EOptionP
import org.opalj.fpcf.Property
import org.opalj.tac.fpcf.analyses.alias.AliasAnalysisContext
import org.opalj.tac.fpcf.analyses.alias.AliasAnalysisState

class PointsToBasedAliasAnalysisState extends AliasAnalysisState {

    private[this] var _pointsTo1: Set[(Context, PC)] = Set.empty[(Context, PC)]
    private[this] var _pointsTo2: Set[(Context, PC)] = Set.empty[(Context, PC)]

    private[this] var _element1Dependees = Set[Entity]()
    private[this] var _element2Dependees = Set[Entity]()

    private[this] var _pointsToElementsHandledElement1: Map[Entity, Int] = Map.empty[Entity, Int]
    private[this] var _pointsToElementsHandledElement2: Map[Entity, Int] = Map.empty[Entity, Int]

    private[this] var _pointsToNull1: Boolean = false

    private[this] var _pointsToNull2: Boolean = false

    def pointsTo1: Set[(Context, PC)] = _pointsTo1

    def pointsTo2: Set[(Context, PC)] = _pointsTo2

    def element1Dependees: Set[Entity] = _element1Dependees

    def element2Dependees: Set[Entity] = _element2Dependees

    def addElementDependency(ase: AliasSourceElement, dependency: EOptionP[Entity, Property])(implicit
        context: AliasAnalysisContext
    ): Unit = {

        addDependency(dependency)

        if (context.isElement1(ase)) {
            _element1Dependees += dependency.e
        } else {
            _element2Dependees += dependency.e
        }
    }

    def removeElementDependency(dependency: EOptionP[Entity, Property]): Unit = {

        removeDependency(dependency)

        _element1Dependees -= dependency.e
        _element2Dependees -= dependency.e
    }

    def element1HasDependency(dependency: EOptionP[Entity, Property]): Boolean = {
        element1Dependees.contains(dependency.e)
    }

    def element2HasDependency(dependency: EOptionP[Entity, Property]): Boolean = {
        element2Dependees.contains(dependency.e)
    }

    def addPointsTo(e: AliasSourceElement, pointsTo: (Context, PC))(implicit context: AliasAnalysisContext): Unit = {
        if (context.isElement1(e)) {
            _pointsTo1 += pointsTo
        } else {
            _pointsTo2 += pointsTo
        }
    }

    def pointsToElementsHandled(ase: AliasSourceElement, e: Entity)(implicit context: AliasAnalysisContext): Int = {
        if (context.isElement1(ase)) _pointsToElementsHandledElement1.getOrElse(e, 0)
        else _pointsToElementsHandledElement2.getOrElse(e, 0)
    }

    def incPointsToElementsHandled(ase: AliasSourceElement, e: Entity)(implicit context: AliasAnalysisContext): Unit = {
        if (context.isElement1(ase)) _pointsToElementsHandledElement1 += e -> (pointsToElementsHandled(ase, e) + 1)
        else _pointsToElementsHandledElement2 += e -> (pointsToElementsHandled(ase, e) + 1)
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
