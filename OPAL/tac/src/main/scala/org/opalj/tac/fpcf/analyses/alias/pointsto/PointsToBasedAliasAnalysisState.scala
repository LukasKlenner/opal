/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj
package tac
package fpcf
package analyses
package alias
package pointsto

import org.opalj.br.PC
import org.opalj.br.fpcf.properties.Context
import org.opalj.br.fpcf.properties.alias.AliasSourceElement
import org.opalj.fpcf.Entity
import org.opalj.fpcf.EOptionP
import org.opalj.fpcf.Property
import org.opalj.tac.fpcf.analyses.alias.AliasAnalysisContext
import org.opalj.tac.fpcf.analyses.alias.TacBasedAliasAnalysisState

/**
 * Encapsulates the current state of an points-to based alias analysis.
 *
 * It additionally contains the following information:
 *
 * - The current points-to sets for the two [[AliasSourceElement]]s.
 *
 * - The current dependees for each of the [[AliasSourceElement]].
 *
 * - The number of handled points-to elements for each [[AliasSourceElement]].
 *
 * - Whether each of the [[AliasSourceElement]]s can point to null.
 *
 * - The current field dependees for each [[AliasSourceElement]]. A field dependee is a definition site of an uVar that is
 *  used to access the field.
 */
class PointsToBasedAliasAnalysisState extends TacBasedAliasAnalysisState {

    private[this] var _pointsTo1: Set[(Context, PC)] = Set.empty[(Context, PC)]
    private[this] var _pointsTo2: Set[(Context, PC)] = Set.empty[(Context, PC)]

    private[this] var _element1Dependees = Set[Entity]()
    private[this] var _element2Dependees = Set[Entity]()

    private[this] var _pointsToElementsHandledElement1: Map[Entity, Int] = Map.empty[Entity, Int]
    private[this] var _pointsToElementsHandledElement2: Map[Entity, Int] = Map.empty[Entity, Int]

    private[this] var _pointsToNull1: Boolean = false

    private[this] var _pointsToNull2: Boolean = false

    private[this] var _field1Dependees = Set[Entity]()
    private[this] var _field2Dependees = Set[Entity]()

    /**
     * @return The current points-to set for the first [[AliasSourceElement]].
     */
    def pointsTo1: Set[(Context, PC)] = _pointsTo1

    /**
     * @return The current points-to set for the second [[AliasSourceElement]].
     */
    def pointsTo2: Set[(Context, PC)] = _pointsTo2

    /**
     * @return A set containing all elements that the first [[AliasSourceElement]] depends on.
     */
    def element1Dependees: Set[Entity] = _element1Dependees

    /**
     * @return A set containing all elements that the second [[AliasSourceElement]] depends on.
     */
    def element2Dependees: Set[Entity] = _element2Dependees

    /**
     * Adds the given entity property pair to the set of dependees of the given [[AliasSourceElement]]
     * and the set of all dependees.
     */
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

    /**
     * Removes the given entity property pair from element dependency set and the set of all dependencies.
     */
    def removeElementDependency(dependency: EOptionP[Entity, Property]): Unit = {

        removeDependency(dependency)

        _element1Dependees -= dependency.e
        _element2Dependees -= dependency.e
    }

    /**
     * @return `true` if the given [[AliasSourceElement]] has a dependency on the given entity property pair.
     */
    def element1HasDependency(dependency: EOptionP[Entity, Property]): Boolean = {
        element1Dependees.contains(dependency.e)
    }

    /**
     * @return `true` if the given [[AliasSourceElement]] has a dependency on the given entity property pair.
     */
    def element2HasDependency(dependency: EOptionP[Entity, Property]): Boolean = {
        element2Dependees.contains(dependency.e)
    }

    /**
     * adds the given points-to set to the points-to set of the given [[AliasSourceElement]].
     */
    def addPointsTo(ase: AliasSourceElement, pointsTo: (Context, PC))(implicit context: AliasAnalysisContext): Unit = {
        if (context.isElement1(ase)) {
            _pointsTo1 += pointsTo
        } else {
            _pointsTo2 += pointsTo
        }
    }

    /**
     * @return The number of handled points-to elements of the given entity for the given [[AliasSourceElement]].
     */
    def pointsToElementsHandled(ase: AliasSourceElement, e: Entity)(implicit context: AliasAnalysisContext): Int = {
        if (context.isElement1(ase)) _pointsToElementsHandledElement1.getOrElse(e, 0)
        else _pointsToElementsHandledElement2.getOrElse(e, 0)
    }

    /**
     * Increments the number of handled points-to elements of the given entity for the given [[AliasSourceElement]] by one.
     */
    def incPointsToElementsHandled(ase: AliasSourceElement, e: Entity)(implicit context: AliasAnalysisContext): Unit = {
        if (context.isElement1(ase)) _pointsToElementsHandledElement1 += e -> (pointsToElementsHandled(ase, e) + 1)
        else _pointsToElementsHandledElement2 += e -> (pointsToElementsHandled(ase, e) + 1)
    }

    /**
     * @return `true` iff the first [[AliasSourceElement]] can point to `null`.
     */
    def pointsToNull1: Boolean = _pointsToNull1

    /**
     * @return `true` iff the second [[AliasSourceElement]] can point to `null`.
     */
    def pointsToNull2: Boolean = _pointsToNull2

    /**
     * Stores that the given [[AliasSourceElement]] can point to `null`.
     */
    def setPointsToNull(ase: AliasSourceElement)(implicit context: AliasAnalysisContext): Unit = {
        if (context.isElement1(ase)) {
            _pointsToNull1 = true
        } else {
            _pointsToNull2 = true
        }
    }

    /**
     * adds the given entity property pair to the set of field dependees of the first [[AliasSourceElement]].
     */
    def addField1Dependency(dependency: EOptionP[Entity, Property]): Unit = {
        _field1Dependees += dependency.e
    }

    /**
     * adds the given entity property pair to the set of field dependees of the second [[AliasSourceElement]].
     */
    def addField2Dependency(dependency: EOptionP[Entity, Property]): Unit = {
        _field2Dependees += dependency.e
    }

    /**
     * Adds the given entity property pair to the set of field dependees of the given [[AliasSourceElement]].
     */
    def addFieldDependency(ase: AliasSourceElement, dependency: EOptionP[Entity, Property])(implicit
        context: AliasAnalysisContext
    ): Unit = {
        if (context.isElement1(ase)) _field1Dependees += dependency.e
        else _field2Dependees += dependency.e
    }

    /**
     * Removes the given entity property pair from the set of field dependees of all [[AliasSourceElement]]s.
     */
    def removeFieldDependency(dependency: EOptionP[Entity, Property]): Unit = {
        _field1Dependees -= dependency.e
        _field2Dependees -= dependency.e
    }

    /**
     * @return `true` if the first [[AliasSourceElement]] has a field dependency on the given entity property pair.
     */
    def field1HasDependency(dependency: EOptionP[Entity, Property]): Boolean = {
        _field1Dependees.contains(dependency.e)
    }

    /**
     * @return `true` if the second [[AliasSourceElement]] has a field dependency on the given entity property pair.
     */
    def field2HasDependency(dependency: EOptionP[Entity, Property]): Boolean = {
        _field2Dependees.contains(dependency.e)
    }

}
