/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.br.fpcf.properties.alias

import org.opalj.br.DeclaredMethod
import org.opalj.br.Field
import org.opalj.br.Method
import org.opalj.br.analyses.DeclaredMethodsKey
import org.opalj.br.analyses.SomeProject
import org.opalj.br.analyses.VirtualFormalParameter
import org.opalj.br.fpcf.properties.Context
import org.opalj.br.fpcf.properties.SimpleContextsKey
import org.opalj.collection.immutable.IntTrieSet
import org.opalj.value.ValueInformation

/**
 * Represents a source code element which can be part of an alias relation.
 *
 * Valid elements are:
 * - [[AliasFP]]: A formal parameter
 * - [[AliasDS]]: A definition site
 * - [[AliasReturnValue]]: A method return value
 * - [[AliasNull]]: The null value
 */
sealed trait AliasSourceElement {

    /**
     * The underlying element that is represented.
     */
    def element: AnyRef

    /**
     * Returns the [[Method]] this element is associated with.
     * If such a method does not exist, an exception is thrown.
     *
     * @throws UnsupportedOperationException if the element is not associated with a method
     * @return The [[Method]] this element is associated with.
     *
     * @see [[isMethodBound]]
     */
    def method: Method = throw new UnsupportedOperationException()

    /**
     * Returns the [[DeclaredMethod]] this element is associated with.
     * If such a method does not exist, an exception is thrown.
     *
     * @throws UnsupportedOperationException if the element is not associated with a method
     * @return The [[DeclaredMethod]] this element is associated with.
     *
     * @see [[isMethodBound]]
     */
    def declaredMethod: DeclaredMethod = throw new UnsupportedOperationException()

    /**
     * Returns the definition site of this element.
     * If such a definition site does not exist, an exception is thrown.
     *
     * @throws UnsupportedOperationException if the element is not associated with a definition site
     * @return The definition site of this element.
     */
    def definitionSite: Int = throw new UnsupportedOperationException() // TODO remove?

    /**
     * Returns `true` if this element is associated with a method.
     * If this method returns `true`, [[method]] and [[declaredMethod]] can be safely called.
     *
     * @return `true` if this element is associated with a method.
     */
    def isMethodBound: Boolean

    // conversion methods

    def isAliasField: Boolean = false

    def asAliasField: AliasField = throw new UnsupportedOperationException()

    def isAliasNull: Boolean = false

    def isAliasReturnValue: Boolean = false

    def asAliasReturnValue: AliasReturnValue = throw new UnsupportedOperationException()

    def isAliasFP: Boolean = false

    def asAliasFP: AliasFP = throw new UnsupportedOperationException()

    def isAliasUVar: Boolean = false

    def asAliasUVar: AliasUVar = throw new UnsupportedOperationException()

}

object AliasSourceElement {

    /**
     * Creates an [[AliasSourceElement]] that represents the given element.
     *
     * @param element The element to represent
     * @param project The project the element is part of
     * @return An [[AliasSourceElement]] that represents the given element
     */
    def apply(element: AnyRef)(implicit project: SomeProject): AliasSourceElement = {
        element match {
            case fp: VirtualFormalParameter        => AliasFP(fp)
            case dm: Method                        => AliasReturnValue(dm, project)
            case f: Field                          => AliasField(f)
            case null                              => AliasNull
            case (uVar: PersistentUVar, m: Method) => AliasUVar(uVar, m, project)
            case _                                 => throw new UnknownError("unhandled entity type")
        }
    }
}

/**
 * Represents a field that is part of an alias relation.
 */
case class AliasField(field: Field) extends AliasSourceElement {

    override def element: Field = field

    override def isMethodBound: Boolean = false

    override def isAliasField: Boolean = true

    override def asAliasField: AliasField = this
}

/**
 * Represents the null value that is part of an alias relation.
 */
object AliasNull extends AliasSourceElement {

    override def element: AnyRef = throw new UnsupportedOperationException()

    override def isMethodBound: Boolean = false

    override def isAliasNull: Boolean = true
}

/**
 * Represents a method return value of a method that is part of an alias relation.
 */
case class AliasReturnValue(override val method: Method, project: SomeProject) extends AliasSourceElement {

    private[this] val dm = project.get(DeclaredMethodsKey)(method)

    private[this] val context = project.get(SimpleContextsKey)(dm)

    override def element: AnyRef = method

    override def declaredMethod: DeclaredMethod = dm

    override def isMethodBound: Boolean = true

    override def isAliasReturnValue: Boolean = true

    override def asAliasReturnValue: AliasReturnValue = this

    def callContext: Context = context
}

/**
 * Represents a formal parameter of a method that is part of an alias relation.
 */
case class AliasFP(fp: VirtualFormalParameter) extends AliasSourceElement {

    override def element: VirtualFormalParameter = fp

    override def method: Method = fp.method.definedMethod

    override def definitionSite: Int = fp.origin

    override def declaredMethod: DeclaredMethod = fp.method

    override def isMethodBound: Boolean = true

    override def isAliasFP: Boolean = true

    override def asAliasFP: AliasFP = this
}

/**
 * A persistent representation (using pcs instead of TAC value origins) for a UVar.
 *
 * @see [[org.opalj.tac.fpcf.analyses.cg.persistentUVar]]
 */
case class PersistentUVar(valueInformation: ValueInformation, defSites: IntTrieSet)

case class AliasUVar(
    uVar:                PersistentUVar,
    override val method: Method,
    project:             SomeProject
) extends AliasSourceElement {

    private[this] val dm = project.get(DeclaredMethodsKey)(method)

    override def element: (PersistentUVar, Method) = (uVar, method)

    override def isMethodBound: Boolean = true

    override def declaredMethod: DeclaredMethod = dm

    override def isAliasUVar: Boolean = true

    override def asAliasUVar: AliasUVar = this

}
