/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias;

import org.opalj.fpcf.properties.alias.AliasMethodID;
import org.opalj.fpcf.properties.alias.u_var.MustAliasUVar;

public class MustAlias {

    public static void main(String[] args) {

    }

    @AliasMethodID(id = 0, clazz = MustAlias.class)
    @MustAliasUVar(reason = "same local variable with single defSite without loop used",
            lineNumber = 21, methodID = 0,
            secondLineNumber = 22, secondMethodID = 0,
            clazz = MustAlias.class)
    public static void mustAliasLocals() {
        Object o1 = new Object();

        o1.hashCode();
        o1.hashCode();
    }

}
