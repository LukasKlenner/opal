package org.opalj.fpcf.fixtures.alias;

import org.opalj.fpcf.properties.alias.AliasMethodID;
import org.opalj.fpcf.properties.alias.u_var.MayAliasUVar;

public class SimpleFieldTest {

    @MayAliasUVar(lineNumber = 16, methodID = 0, clazz = SimpleFieldTest.class)
    private static Object mayAliasField;

    public static void main(String[] args) {
        reassignField();
    }

    @AliasMethodID(id = 0, clazz = SimpleFieldTest.class)
    public static void reassignField() {
        Object o = new Object();
        mayAliasField = o;
    }

}
