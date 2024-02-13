/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org
package opalj
package tac
package fpcf
package analyses

import org.opalj.ai.ValueOrigin
import org.opalj.br.PC
import org.opalj.br.fpcf.properties.alias.PersistentUVar
import org.opalj.tac.DUVar
import org.opalj.value.ValueInformation

package object alias {

    type V = DUVar[ValueInformation]

    final def persistentUVar(uVar: UVar[ValueInformation])(implicit stmts: Array[Stmt[V]]): PersistentUVar = {
        PersistentUVar(uVar.value, uVar.definedBy.map(pcOfDefSite _))
    }

    final def pcOfDefSite(valueOrigin: ValueOrigin)(implicit stmts: Array[Stmt[V]]): PC = {
        org.opalj.tac.fpcf.analyses.cg.pcOfDefSite(valueOrigin)
    }

}
