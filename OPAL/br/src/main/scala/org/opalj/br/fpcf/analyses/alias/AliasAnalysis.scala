/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.br.fpcf.analyses.alias

import org.opalj.br.analyses.SomeProject
import org.opalj.br.fpcf.FPCFAnalysis
import org.opalj.br.fpcf.properties.Context

class AliasAnalysis(final val project: SomeProject) extends FPCFAnalysis {

  def determineAlias(context: Context, e1: AliasEntity, e2: AliasEntity): Alias = {
    if (e1 == e2) {
      MustAlias
    } else {
      MayAlias
    }
  }

}
