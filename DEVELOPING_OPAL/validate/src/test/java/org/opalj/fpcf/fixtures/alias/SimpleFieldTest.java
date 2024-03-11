/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias;

import org.opalj.fpcf.properties.alias.AliasMethodID;
import org.opalj.fpcf.properties.alias.MayAlias;
import org.opalj.fpcf.properties.alias.NoAlias;
import org.opalj.fpcf.properties.alias.u_var.MayAliasUVar;
import org.opalj.fpcf.properties.alias.u_var.NoAliasUVar;

public class SimpleFieldTest {

    @MayAliasUVar(reason = "mayAlias", lineNumber = 35, methodID = 0, clazz = SimpleFieldTest.class)
    @NoAlias(reason = "no alias with field", id = 1, methodID = 1, clazz = SimpleFieldTest.class)
    @NoAliasUVar(reason = "noAlias with field", lineNumber = 46, methodID = 1, clazz = SimpleFieldTest.class)
    @MayAlias(reason = "may alias with field", id = 2, methodID = 4, clazz = SimpleFieldTest.class)
    public static Object mayAliasField = new Object();

    @MayAliasUVar(reason = "may alias with field", lineNumber = 57, methodID = 3, clazz = SimpleFieldTest.class)
    public static Object nullField = null;

    public static void main(String[] args) {
        reassignField();
        noAlias(new Object());

        nullField();
        reassignNullField();
        nullField();

        returnMayAliasField();
    }

    @AliasMethodID(id = 0, clazz = SimpleFieldTest.class)
    public static void reassignField() {
        Object o = new Object();
        mayAliasField = o;
    }

    @AliasMethodID(id = 1, clazz = SimpleFieldTest.class)
    public static void noAlias(@NoAlias(reason = "no alias with field", id = 1, clazz = SimpleFieldTest.class) Object a) {
        Object o = new Object();

        if (Math.random() > 0.5) {
            o = a;
        }

        o.hashCode();
    }

    @AliasMethodID(id = 2, clazz = SimpleFieldTest.class)
    public static void nullField() {
        System.out.println(nullField);
    }

    @AliasMethodID(id = 3, clazz = SimpleFieldTest.class)
    public static void reassignNullField() {
        Object o = new Object();
        nullField = o;
    }

    @AliasMethodID(id = 4, clazz = SimpleFieldTest.class)
    @MayAlias(reason = "may alias with field", id = 2, clazz = SimpleFieldTest.class)
    public static Object returnMayAliasField() {
        return mayAliasField;
    }

}
