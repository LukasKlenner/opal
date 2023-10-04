/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.br.fpcf.analyses.alias

import org.opalj.br.{Field, LocalVariable, Method, MethodParameter}

sealed trait AliasEntity {

  def getEntity: AnyRef
}

case class AliasField(field: Field) extends AliasEntity {

    override def getEntity: Field = field
}

case class AliasReturnValue(method: Method) extends AliasEntity {

    override def getEntity: Method = method
}

case class AliasParameter(parameter: MethodParameter) extends AliasEntity {

    override def getEntity: MethodParameter = parameter
}

case class AliasLocalVariable(variable: LocalVariable) extends AliasEntity {

    override def getEntity: LocalVariable = variable
}
