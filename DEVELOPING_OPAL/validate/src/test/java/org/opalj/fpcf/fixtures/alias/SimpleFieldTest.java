/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias;

import org.opalj.fpcf.properties.alias.AliasMethodID;
import org.opalj.fpcf.properties.alias.NoAlias;
import org.opalj.fpcf.properties.alias.u_var.MayAliasUVar;
import org.opalj.fpcf.properties.alias.u_var.NoAliasUVar;

public class SimpleFieldTest {

    @MayAliasUVar(reason = "mayAlias", lineNumber = 32, methodID = 0, clazz = SimpleFieldTest.class)
    @NoAlias(reason = "no alias with field", id = 1, clazz = SimpleFieldTest.class, methodID = 2)
    @NoAliasUVar(reason = "noAlias with field", lineNumber = 42, methodID = 2, clazz = SimpleFieldTest.class)
    public static Object mayAliasStaticField = new Object();

    @MayAliasUVar(reason = "mayAlias", lineNumber = 38, methodID = 1, clazz = SimpleFieldTest.class)
    @NoAlias(reason = "no alias with field", id = 1, clazz = SimpleFieldTest.class, methodID = 2)
    @NoAliasUVar(reason = "noAlias with field", lineNumber = 42, methodID = 2, clazz = SimpleFieldTest.class)
    public Object mayAliasField = new Object();

    public static Object finalField = new Object();

    public static void main(String[] args) {
        reassignStaticField();
        SimpleFieldTest sft = new SimpleFieldTest();
        sft.reassignField();
        sft.noAlias(new Object());
    }

    @AliasMethodID(id = 0, clazz = SimpleFieldTest.class)
    public static void reassignStaticField() {
        Object o = new Object();
        mayAliasStaticField = o;
    }

    @AliasMethodID(id = 1, clazz = SimpleFieldTest.class)
    public void reassignField() {
        Object o = new Object();
        mayAliasField = o;
    }

    @AliasMethodID(id = 2, clazz = SimpleFieldTest.class)
    public void noAlias(@NoAlias(reason = "no alias with field", id = 1, clazz = SimpleFieldTest.class) Object a) {
        Object o = new Object();

        if (Math.random() > 0.5) {
            o = a;
        }

        o.hashCode();
    }

}
