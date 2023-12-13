/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org
package opalj
package tac
package fpcf
package analyses
package alias

import org.opalj.br.DeclaredMethod
import org.opalj.br.Method
import org.opalj.br.analyses.DeclaredMethodsKey
import org.opalj.br.analyses.SomeProject
import org.opalj.br.analyses.VirtualFormalParameter
import org.opalj.tac.common.DefinitionSite

sealed trait AliasSourceElement {

    def element: AnyRef

    def method: Method

    def declaredMethod: DeclaredMethod

    def definitionSite: Int

    def isMethodBound: Boolean
}

object AliasSourceElement {

    def apply(element: AnyRef)(implicit project: SomeProject): AliasSourceElement = {
        element match {
            case fp: VirtualFormalParameter => AliasFP(fp)
            case ds: DefinitionSite         => AliasDS(ds, project)
            case dm: Method                 => AliasReturnValue(dm, project)
            case _                          => throw new UnknownError("unhandled entity type")
        }
    }
}

//case class AliasField(field: Field) extends AliasEntity {
//
//    override def entity: Field = field
//}
//

case class AliasNull() extends AliasSourceElement {
    override def element: AnyRef = throw new UnsupportedOperationException()

    override def method: Method = throw new UnsupportedOperationException()

    override def declaredMethod: DeclaredMethod = throw new UnsupportedOperationException()

    override def definitionSite: UByte = throw new UnsupportedOperationException()

    override def isMethodBound: Boolean = false
}

case class AliasReturnValue(method: Method, project: SomeProject) extends AliasSourceElement {
    override def element: AnyRef = method

    override def definitionSite: Int = throw new UnsupportedOperationException("No definition site for return value")

    override def declaredMethod: DeclaredMethod = project.get(DeclaredMethodsKey)(method)

    override def isMethodBound: Boolean = true
}

case class AliasFP(fp: VirtualFormalParameter) extends AliasSourceElement {

    override def element: VirtualFormalParameter = fp

    override def method: Method = fp.method.definedMethod

    override def definitionSite: Int = fp.origin

    override def declaredMethod: DeclaredMethod = fp.method

    override def isMethodBound: Boolean = true
}

case class AliasDS(ds: DefinitionSite, project: SomeProject) extends AliasSourceElement {

    override def element: DefinitionSite = ds

    override def method: Method = ds.method

    override def definitionSite: Int = ds.pc

    override def declaredMethod: DeclaredMethod = project.get(DeclaredMethodsKey)(method)

    override def isMethodBound: Boolean = true
}
