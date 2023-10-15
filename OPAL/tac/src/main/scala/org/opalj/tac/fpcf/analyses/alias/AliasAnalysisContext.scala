/* BSD 2-Clause License - see OPAL/LICENSE for details. */

package org.opalj.tac.fpcf.analyses.alias

import org.opalj.br.Method
import org.opalj.br.analyses.SomeProject
import org.opalj.br.fpcf.properties.Context
import org.opalj.fpcf.Entity
import org.opalj.fpcf.PropertyStore

class AliasAnalysisContext(
                            val entity: (Context, Entity, Entity),
                            val targetMethod1: Method,
                            val targetMethod2: Method,
                            val project: SomeProject,
                            val propertyStore: PropertyStore,
                          ) {

  def context: Context = entity._1

  def entity1: Entity = entity._2

  def entity2: Entity = entity._3

}
