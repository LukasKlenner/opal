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
import org.opalj.tac.common.DefinitionSiteLike

sealed trait AliasSourceElement {

    def element: AnyRef

    def method: Method

    def declaredMethod: DeclaredMethod

    def definitionSite: Int
}

object AliasSourceElement {

    def apply(element: AnyRef)(implicit project: SomeProject): AliasSourceElement = {
        element match {
            case fp: VirtualFormalParameter => AliasFP(fp)
            case ds: DefinitionSiteLike     => AliasDS(ds, project)
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
case class AliasReturnValue(method: Method, project: SomeProject) extends AliasSourceElement {
    override def element: AnyRef = method

    override def definitionSite: Int = throw new UnsupportedOperationException("No definition site for return value")

    override def declaredMethod: DeclaredMethod = project.get(DeclaredMethodsKey)(method)
}

case class AliasFP(fp: VirtualFormalParameter) extends AliasSourceElement {

    override def element: VirtualFormalParameter = fp

    override def method: Method = fp.method.definedMethod

    override def definitionSite: Int = fp.origin

    override def declaredMethod: DeclaredMethod = fp.method
}

case class AliasDS(ds: DefinitionSiteLike, project: SomeProject) extends AliasSourceElement { //TODO nur DefSite ohne only

    override def element: DefinitionSiteLike = ds

    override def method: Method = ds.method

    override def definitionSite: Int = ds.pc

    override def declaredMethod: DeclaredMethod = project.get(DeclaredMethodsKey)(method)
}
