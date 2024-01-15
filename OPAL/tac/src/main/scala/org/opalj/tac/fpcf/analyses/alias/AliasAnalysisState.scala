/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org
package opalj
package tac
package fpcf
package analyses
package alias

import org.opalj.br.Method
import org.opalj.collection.immutable.IntTrieSet
import org.opalj.fpcf.EOptionP
import org.opalj.fpcf.Entity
import org.opalj.fpcf.Property
import org.opalj.fpcf.SomeEOptionP
import org.opalj.fpcf.SomeEPS
import org.opalj.tac.TACMethodParameter
import org.opalj.tac.TACode

class AliasAnalysisState {

    private[this] var _dependees = Map.empty[Entity, EOptionP[Entity, Property]]
    private[this] var _dependeesSet = Set.empty[SomeEOptionP]

    private[this] var _tacai1: Option[TACode[TACMethodParameter, V]] = None
    private[this] var _tacai2: Option[TACode[TACMethodParameter, V]] = None

    private[this] var _tacEPSToMethod: Map[SomeEPS, Method] = Map()

    private[this] var _uses1: IntTrieSet = _

    private[this] var _defSite1: Int = _

    private[this] var _uses2: IntTrieSet = _

    private[this] var _defSite2: Int = _

    /**
     * Adds an entity property pair (or epk) into the set of dependees.
     */
    @inline private[alias] final def addDependency(eOptionP: EOptionP[Entity, Property]): Unit = {
        assert(!_dependees.contains(eOptionP.e))
        _dependees += eOptionP.e -> eOptionP
        _dependeesSet += eOptionP
    }

    /**
     * Removes the entity property pair (or epk) that correspond to the given ep from the set of
     * dependees.
     */
    @inline private[alias] final def removeDependency(
        ep: EOptionP[Entity, Property]
    ): Unit = {
        assert(_dependees.contains(ep.e))
        val oldEOptionP = _dependees(ep.e)
        _dependees -= ep.e
        _dependeesSet -= oldEOptionP
    }

    /**
     * Do we already registered a dependency to that entity?
     */
    @inline private[alias] final def containsDependency(
        ep: EOptionP[Entity, Property]
    ): Boolean = {
        _dependees.contains(ep.e)
    }

    @inline private[alias] final def getDependency(e: Entity): EOptionP[Entity, Property] = {
        _dependees(e)
    }

    /**
     * The set of open dependees.
     */
    private[alias] final def getDependees: Set[SomeEOptionP] = {
        _dependeesSet
    }

    /**
     * Are there any dependees?
     */
    private[alias] final def hasDependees: Boolean = _dependees.nonEmpty

    private[alias] def updateTACAI(
        m:     Method,
        tacai: TACode[TACMethodParameter, V]
    )(implicit context: AliasAnalysisContext): Unit = {

        var anyMatch: Boolean = false

        if (context.element1.isMethodBound && m.equals(context.element1.method)) {
            this._tacai1 = Some(tacai)

            (context.element1) match {
                case (ds: AliasDS) =>
                    _defSite1 = tacai.properStmtIndexForPC(ds.element.pc)
                    _uses1 = ds.element.usedBy(tacai)
                case (fp: AliasFP) =>
                    val param = tacai.params.parameter(fp.element.origin)
                    _uses1 = param.useSites
                    _defSite1 = param.origin
                case _ =>
            }

            anyMatch = true
        }

        if (context.element2.isMethodBound && m.equals(context.element2.method)) {
            this._tacai2 = Some(tacai)

            (context.element2) match {
                case (ds: AliasDS) =>
                    _defSite2 = tacai.properStmtIndexForPC(ds.element.pc)
                    _uses2 = ds.element.usedBy(tacai)
                case (fp: AliasFP) =>
                    val param = tacai.params.parameter(fp.element.origin)
                    _uses2 = param.useSites
                    _defSite2 = param.origin
                case _ =>
            }

            anyMatch = true
        }

        if (!anyMatch) throw new IllegalArgumentException("Method not found")
    }

    def tacai1: Option[TACode[TACMethodParameter, V]] = _tacai1

    def tacai2: Option[TACode[TACMethodParameter, V]] = _tacai2

    def uses1: IntTrieSet = _uses1

    def defSite1: Int = _defSite1

    def uses2: IntTrieSet = _uses2

    def defSite2: Int = _defSite2

    def addTacEPSToMethod(eps: SomeEPS, m: Method): Unit = {
        _tacEPSToMethod += (eps -> m)
    }

    def getMethodForTacEPS(eps: SomeEPS): Method = {
        _tacEPSToMethod(eps)
    }
}
