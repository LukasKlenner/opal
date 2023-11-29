/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org
package opalj
package tac
package fpcf
package analyses
package alias

import org.opalj.br.Method
import org.opalj.br.analyses.VirtualFormalParameter
import org.opalj.tac.common.DefinitionSiteLike

sealed trait AliasSourceElement {

    def element: AnyRef

    def method: Method
}

object AliasSourceElement {

        def apply(element: AnyRef): AliasSourceElement = {
            element match {
                case fp: VirtualFormalParameter => AliasFP(fp)
                case ds: DefinitionSiteLike     => AliasDS(ds)
                case _                          => throw new UnknownError("unhandled entity type")
            }
        }
}



//case class AliasField(field: Field) extends AliasEntity {
//
//    override def entity: Field = field
//}
//
//case class AliasReturnValue(method: Method) extends AliasEntity {
//
//    override def entity: Method = method
//}

case class AliasFP(fp: VirtualFormalParameter) extends AliasSourceElement {

    override def element: VirtualFormalParameter = fp

    override def method: Method = fp.method.definedMethod
}

case class AliasDS(ds: DefinitionSiteLike) extends AliasSourceElement {

    override def element: DefinitionSiteLike = ds

    override def method: Method = ds.method
}
