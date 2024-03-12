/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias;

import org.opalj.fpcf.properties.alias.AliasMethodID;
import org.opalj.fpcf.properties.alias.MayAlias;
import org.opalj.fpcf.properties.alias.MustAlias;
import org.opalj.fpcf.properties.alias.NoAlias;
import org.opalj.fpcf.properties.alias.u_var.MayAliasUVar;
import org.opalj.fpcf.properties.alias.u_var.MustAliasUVar;
import org.opalj.fpcf.properties.alias.u_var.NoAliasUVar;

public class FieldAlias {

    @MayAliasUVar(reason = "may alias with field and assigned uVar", lineNumber = 49, methodID = 0, clazz = FieldAlias.class)
    @NoAlias(reason = "no alias with field and parameter", id = 1, methodID = 1, clazz = FieldAlias.class)
    @NoAlias(reason = "no alias with field and return value", id = 3, methodID = 1, clazz = FieldAlias.class)
    @NoAliasUVar(reason = "noAlias with field and unrelated uVar", lineNumber = 63, methodID = 1, clazz = FieldAlias.class)
    @MayAlias(reason = "may alias with field and return value", id = 2, methodID = 4, clazz = FieldAlias.class)
    @MayAlias(reason = "may alias with field and return value via parameter", id = 5, methodID = 6, clazz = FieldAlias.class)
    @MayAlias(reason = "may alias with field and parameter", id = 6, methodID = 6, clazz = FieldAlias.class)
    public static Object mayAliasField = new Object();

    @MayAliasUVar(reason = "may alias with field and assigned uVar", lineNumber = 69, methodID = 3, clazz = FieldAlias.class)
    public static Object nullField = null;

    @MustAlias(reason = "must alias with field and return value", id = 4, methodID = 5, clazz = FieldAlias.class)
    @MustAliasUVar(reason = "must alias with field and returned uVar", lineNumber = 82, methodID = 5, clazz = FieldAlias.class)
    @MustAlias(reason = "must alias with field and return value via parameter", id = 7, methodID = 7, clazz = FieldAlias.class)
    @MustAlias(reason = "must alias with field and parameter", id = 8, methodID = 7, clazz = FieldAlias.class)
    public static Object finalField = new Object();

    public static void main(String[] args) {
        reassignField();
        noAlias(new Object());
        noAlias(finalField);

        reassignNullField();

        returnMayAliasField();
        returnFinalField();

        parameterIsMayAliasField(mayAliasField);
        parameterIsFinalField(finalField);
    }

    @AliasMethodID(id = 0, clazz = FieldAlias.class)
    public static void reassignField() {
        Object o = new Object();
        mayAliasField = o;
    }

    @AliasMethodID(id = 1, clazz = FieldAlias.class)
    @NoAlias(reason = "no alias with field and return value", id = 3, clazz = FieldAlias.class)
    public static Object noAlias(
            @NoAlias(reason = "no alias with field", id = 1, clazz = FieldAlias.class)
            Object a) {
        Object o = new Object();

        if (Math.random() > 0.5) {
            o = a;
        }

        return o;
    }

    @AliasMethodID(id = 3, clazz = FieldAlias.class)
    public static void reassignNullField() {
        Object o = new Object();
        nullField = o;
    }

    @AliasMethodID(id = 4, clazz = FieldAlias.class)
    @MayAlias(reason = "may alias with field and return value", id = 2, clazz = FieldAlias.class)
    public static Object returnMayAliasField() {
        return mayAliasField;
    }

    @AliasMethodID(id = 5, clazz = FieldAlias.class)
    @MustAlias(reason = "must alias with field and return value", id = 4, clazz = FieldAlias.class)
    public static Object returnFinalField() {
        Object o = finalField;
        return o;
    }

    @AliasMethodID(id = 6, clazz = FieldAlias.class)
    @MayAlias(reason = "may alias with field and return value via parameter", id = 5, clazz = FieldAlias.class)
    @MayAliasUVar(reason = "may alias with return value and uVar via parameter and field", lineNumber = 49, methodID = 0, clazz = FieldAlias.class)
    public static Object parameterIsMayAliasField(
            @MayAlias(reason = "may alias with field and parameter", id = 6, clazz = FieldAlias.class)
            @MayAliasUVar(reason = "may alias with parameter and UVar via field", lineNumber = 49, methodID = 0, clazz = FieldAlias.class)
            Object a) {
        return a;
    }

    @AliasMethodID(id = 7, clazz = FieldAlias.class)
    @MustAlias(reason = "must alias with field and return value via parameter", id = 7, clazz = FieldAlias.class)
    public static Object parameterIsFinalField(
            @MustAlias(reason = "must alias with field and parameter", id = 8, clazz = FieldAlias.class)
            Object a) {
        return a;
    }

}
