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

sealed trait AliasEntity {

    def entity: AnyRef

    def method: Method
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

case class AliasFP(fp: VirtualFormalParameter) extends AliasEntity {

    override def entity: VirtualFormalParameter = fp

    override def method: Method = fp.method.definedMethod
}

case class AliasDS(ds: DefinitionSiteLike) extends AliasEntity {

    override def entity: DefinitionSiteLike = ds

    override def method: Method = ds.method
}
