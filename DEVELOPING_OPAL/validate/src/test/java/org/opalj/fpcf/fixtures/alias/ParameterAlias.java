/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias;

import org.opalj.fpcf.properties.alias.AliasMethodID;
import org.opalj.fpcf.properties.alias.MayAlias;
import org.opalj.fpcf.properties.alias.NoAlias;
import org.opalj.fpcf.properties.alias.u_var.MayAliasUVar;
import org.opalj.fpcf.properties.alias.u_var.NoAliasUVar;

public class ParameterAlias {

    public static void main(String[] args) {
        Object o1 = new Object();
        Object o2 = new Object();

        noAliasWithLocal(o1);

        noAliasWithParam(o1, o2);

        mayAliasWithLocal(o1);

        mayAliasWithParam1(o1, o2);
        mayAliasWithParam1(o1, o1);

        mayAliasWithParam2(o1, o1);
    }

    @AliasMethodID(id = 0, clazz = ParameterAlias.class)
    public static void noAliasWithLocal(@NoAliasUVar(reason = "noAlias with uVar", lineNumber = 32, methodID = 0, clazz = ParameterAlias.class) Object o1) {

        Object o2 = new Object();
        o2.hashCode();
    }

    public static void noAliasWithParam(@NoAlias(reason = "noAlias with other parameter", id = 0, clazz = ParameterAlias.class) Object o1,
                                        @NoAlias(reason = "noAlias with other parameter", id = 0, clazz = ParameterAlias.class) Object o2) {

    }

    @AliasMethodID(id = 1, clazz = ParameterAlias.class)
    public static void mayAliasWithLocal(@MayAliasUVar(reason = "mayAlias with uVar", lineNumber = 49, methodID = 1, clazz = ParameterAlias.class) Object o1) {

        Object o2 = new Object();

        if (Math.random() > 0.5) {
            o2 = o1;
        }

        o2.hashCode();
    }

    public static void mayAliasWithParam1(@MayAlias(reason = "mayAlias with other parameter 1", id = 1, clazz = ParameterAlias.class) Object o1,
                                          @MayAlias(reason = "mayAlias with other parameter 1", id = 1, clazz = ParameterAlias.class) Object o2) {

    }

    public static void mayAliasWithParam2(@MayAlias(reason = "mayAlias with other parameter 2", id = 2, clazz = ParameterAlias.class) Object o1,
                                          @MayAlias(reason = "mayAlias with other parameter 2", id = 2, clazz = ParameterAlias.class) Object o2) {

    }

}
