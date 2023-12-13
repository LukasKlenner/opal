/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj
package tac
package fpcf
package analyses
package alias

import org.opalj.br.fpcf.properties.Context

class AliasEntity(val context: Context, private val e1: AliasSourceElement, private val e2: AliasSourceElement) {

    private val (_element1, _element2) = (e1, e2) match {
        case (e1: AliasReturnValue, e2) if !e2.isInstanceOf[AliasReturnValue] => (e1, e2)
        case (e1, e2: AliasReturnValue) if !e1.isInstanceOf[AliasReturnValue] => (e2, e1)
        case (e1: AliasNull, e2) => (e1, e2)
        case (e1, e2: AliasNull) => (e2, e1)
        case (e1, e2) if e1.hashCode() < e2.hashCode() => (e1, e2)
        case (e1, e2) => (e2, e1)
    }

    def element1: AliasSourceElement = _element1
    def element2: AliasSourceElement = _element2

    def bothElementsMethodBound: Boolean = element1.isMethodBound && element2.isMethodBound
    def elementsInSameMethod: Boolean = element1.method.eq(element2.method)

    //we can't use a case class because the order of the order of the two elements
    override def equals(other: Any): Boolean = other match {
        case that: AliasEntity =>
            that.isInstanceOf[AliasEntity] &&
                _element1 == that._element1 &&
                _element2 == that._element2
        case _ => false
    }

    override def hashCode(): Int = {
        val state = Seq(_element1, _element2)
        state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
    }
}

object AliasEntity {

    def apply(context: Context, e1: AliasSourceElement, e2: AliasSourceElement): AliasEntity = new AliasEntity(context, e1, e2)

}
