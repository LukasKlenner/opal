/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.tac.fpcf.analyses.alias

import org.opalj.collection.immutable.IntTrieSet
import org.opalj.fpcf.EOptionP
import org.opalj.fpcf.Entity
import org.opalj.fpcf.Property
import org.opalj.fpcf.SomeEOptionP
import org.opalj.tac.TACMethodParameter
import org.opalj.tac.TACode
import org.opalj.tac.common.DefinitionSiteLike

import scala.collection.mutable

class AliasAnalysisState {

  private[this] var dependees = Map.empty[Entity, EOptionP[Entity, Property]]
  private[this] var dependeesSet = Set.empty[SomeEOptionP]

  private[this] var tacai: Option[TACode[TACMethodParameter, V]] = None

  private[this] var uses1: IntTrieSet = _

  private[this] var defSite1: Int = _

  private[this] var uses2: IntTrieSet = _

  private[this] var defSite2: Int = _

  private[this] val pointsTo = scala.collection.mutable.Map.empty[V, mutable.Set[V]]

  private[this] var mayAlias: Boolean = false

  /**
   * Adds an entity property pair (or epk) into the set of dependees.
   */
  @inline private[alias] final def addDependency(eOptionP: EOptionP[Entity, Property]): Unit = {
    assert(!dependees.contains(eOptionP.e))
    dependees += eOptionP.e -> eOptionP
    dependeesSet += eOptionP
  }

  /**
   * Removes the entity property pair (or epk) that correspond to the given ep from the set of
   * dependees.
   */
  @inline private[alias] final def removeDependency(
                                                      ep: EOptionP[Entity, Property]
                                                    ): Unit = {
    assert(dependees.contains(ep.e))
    val oldEOptionP = dependees(ep.e)
    dependees -= ep.e
    dependeesSet -= oldEOptionP
  }

  /**
   * Do we already registered a dependency to that entity?
   */
  @inline private[alias] final def containsDependency(
                                                        ep: EOptionP[Entity, Property]
                                                      ): Boolean = {
    dependees.contains(ep.e)
  }

  @inline private[alias] final def getDependency(e: Entity): EOptionP[Entity, Property] = {
    dependees(e)
  }

  /**
   * The set of open dependees.
   */
  private[alias] final def getDependees: Set[SomeEOptionP] = {
    dependeesSet
  }

  /**
   * Are there any dependees?
   */
  private[alias] final def hasDependees: Boolean = dependees.nonEmpty

  private[alias] def updateTACAI(
                                   tacai: TACode[TACMethodParameter, V]
                                 )(implicit context: AliasAnalysisContext): Unit = {
    this.tacai = Some(tacai)

    (context.entity1, context.entity2) match {
      case (ds1: DefinitionSiteLike, ds2: DefinitionSiteLike) =>
        defSite1 = tacai.properStmtIndexForPC(ds1.pc)
        uses1 = ds1.usedBy(tacai)

        defSite2 = tacai.properStmtIndexForPC(ds2.pc)
        uses2 = ds2.usedBy(tacai)
    }
  }

  def getTacai: Option[TACode[TACMethodParameter, V]] = tacai

  def getUses1: IntTrieSet = uses1

  def getDefSite1: Int = defSite1

  def getUses2: IntTrieSet = uses2

  def getDefSite2: Int = defSite2

  def getPointsTo: mutable.Map[V, mutable.Set[V]] = pointsTo

  def getMayAlias: Boolean = mayAlias

  def setMayAlias(mayAlias: Boolean): Unit = {
    this.mayAlias = mayAlias
  }
}
