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

    @MayAliasUVar(reason = "may alias with field and assigned uVar", lineNumber = 69, methodID = 0, clazz = FieldAlias.class)
    @NoAlias(reason = "no alias with field and parameter", id = 1, methodID = 1, clazz = FieldAlias.class)
    @NoAlias(reason = "no alias with field and return value", id = 3, methodID = 1, clazz = FieldAlias.class)
    @NoAliasUVar(reason = "noAlias with field and unrelated uVar", lineNumber = 83, methodID = 1, clazz = FieldAlias.class)
    @MayAlias(reason = "may alias with field and return value", id = 2, methodID = 4, clazz = FieldAlias.class)
    @MayAliasUVar(reason = "may alias with field and returned uVar", lineNumber = 96, methodID = 4, clazz = FieldAlias.class)
    @MayAlias(reason = "may alias with field and return value via parameter", id = 5, methodID = 6, clazz = FieldAlias.class)
    @MayAliasUVar(reason = "may alias with field and returned uVar via parameter", lineNumber = 113, methodID = 6, clazz = FieldAlias.class)
    @MayAlias(reason = "may alias with field and parameter", id = 6, methodID = 6, clazz = FieldAlias.class)
    public static Object mayAliasField = new Object();

    @MayAliasUVar(reason = "may alias with non-static field and assigned uVar", lineNumber = 127, methodID = 8, clazz = FieldAlias.class)
    @NoAlias(reason = "no alias with non-static field and parameter", id = 10, methodID = 9, clazz = FieldAlias.class)
    @NoAlias(reason = "no alias with non-static field and return value", id = 9, methodID = 9, clazz = FieldAlias.class)
    @NoAliasUVar(reason = "noAlias with non-static field and unrelated uVar", lineNumber = 141, methodID = 9, clazz = FieldAlias.class)
    @MayAlias(reason = "may alias with non-static field and return value", id = 11, methodID = 10, clazz = FieldAlias.class)
    @MayAliasUVar(reason = "may alias with non-static field and returned uVar", lineNumber = 148, methodID = 10, clazz = FieldAlias.class)
    @MayAlias(reason = "may alias with non-static field and return value via parameter", id = 12, clazz = FieldAlias.class)
    @MayAliasUVar(reason = "may alias with non-static field and returned uVar via parameter", lineNumber = 158, methodID = 11, clazz = FieldAlias.class)
    @MayAlias(reason = "may alias with non-static field and parameter", id = 13, clazz = FieldAlias.class)
    public Object nonStaticField = new Object();

    @MayAliasUVar(reason = "may alias with field and assigned uVar", lineNumber = 89, methodID = 3, clazz = FieldAlias.class)
    public static Object nullField = null;

    @MustAlias(reason = "must alias with static final field and return value", id = 4, methodID = 5, clazz = FieldAlias.class)
    @MustAliasUVar(reason = "must alias with static final field and returned uVar", lineNumber = 103, methodID = 5, clazz = FieldAlias.class)
    @MustAlias(reason = "must alias with static final field and return value via parameter", id = 7, methodID = 7, clazz = FieldAlias.class)
    @MustAliasUVar(reason = "must alias with static final field and returned uVar via parameter", lineNumber = 121, methodID = 7, clazz = FieldAlias.class)
    @MustAlias(reason = "must alias with static final field and parameter", id = 8, methodID = 7, clazz = FieldAlias.class)
    public static Object staticFinalField = new Object();

    @MayAlias(reason = "may alias with final field and return value", id = 14, clazz = FieldAlias.class)
    @MayAliasUVar(reason = "may alias with final field and returned uVar", lineNumber = 165, methodID = 12, clazz = FieldAlias.class)
    @MayAlias(reason = "may alias with final field and return value via parameter", id = 15, clazz = FieldAlias.class)
    @MayAliasUVar(reason = "may alias with final field and returned uVar via parameter", lineNumber = 173, methodID = 13, clazz = FieldAlias.class)
    @MayAlias(reason = "may alias with final field and parameter", id = 16, clazz = FieldAlias.class)
    public Object finalField = new Object();

    public static void main(String[] args) {
        noAlias(new Object());
        noAlias(staticFinalField);

        parameterIsMayAliasField(mayAliasField);
        parameterIsStaticFinalField(staticFinalField);

        FieldAlias fa = new FieldAlias();
        fa.noAliasNonStatic(mayAliasField);
        fa.parameterIsNonStaticField(fa.nonStaticField);
        fa.parameterIsFinalField(fa.finalField);fa.reassignNonStaticField();
    }

    @AliasMethodID(id = 0, clazz = FieldAlias.class)
    public static void reassignField() {
        Object o = new Object();
        mayAliasField = o;
    }

    @AliasMethodID(id = 1, clazz = FieldAlias.class)
    @NoAlias(reason = "no alias with field and return value", id = 3, clazz = FieldAlias.class)
    public static Object noAlias(
            @NoAlias(reason = "no alias with field and parameter", id = 1, clazz = FieldAlias.class)
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
        Object o = mayAliasField;
        return o;
    }

    @AliasMethodID(id = 5, clazz = FieldAlias.class)
    @MustAlias(reason = "must alias with static final field and return value", id = 4, clazz = FieldAlias.class)
    public static Object returnStaticFinalField() {
        Object o = staticFinalField;
        return o;
    }

    @AliasMethodID(id = 6, clazz = FieldAlias.class)
    @MayAlias(reason = "may alias with field and return value via parameter", id = 5, clazz = FieldAlias.class)
    @MayAliasUVar(reason = "may alias with return value and uVar via parameter and field", lineNumber = 69, methodID = 0, clazz = FieldAlias.class)
    public static Object parameterIsMayAliasField(
            @MayAlias(reason = "may alias with field and parameter", id = 6, clazz = FieldAlias.class)
            @MayAliasUVar(reason = "may alias with parameter and UVar via field", lineNumber = 69, methodID = 0, clazz = FieldAlias.class)
            Object a) {
        return a;
    }

    @AliasMethodID(id = 7, clazz = FieldAlias.class)
    @MustAlias(reason = "must alias with static final field and return value via parameter", id = 7, clazz = FieldAlias.class)
    public static Object parameterIsStaticFinalField(
            @MustAlias(reason = "must alias with static final field and parameter", id = 8, clazz = FieldAlias.class)
            Object a) {
        return a;
    }

    @AliasMethodID(id = 8, clazz = FieldAlias.class)
    public void reassignNonStaticField() {
        Object o = new Object();
        nonStaticField = o;
    }

    @AliasMethodID(id = 9, clazz = FieldAlias.class)
    @NoAlias(reason = "no alias with non-static field and return value", id = 9, clazz = FieldAlias.class)
    public Object noAliasNonStatic(
            @NoAlias(reason = "no alias with non-static field and parameter", id = 10, clazz = FieldAlias.class)
            Object a) {
        Object o = new Object();

        if (Math.random() > 0.5) {
            o = a;
        }

        return o;
    }

    @AliasMethodID(id = 10, clazz = FieldAlias.class)
    @MayAlias(reason = "may alias with non-static field and return value", id = 11, clazz = FieldAlias.class)
    public Object returnNonStaticField() {
        Object o = nonStaticField;
        return o;
    }

    @AliasMethodID(id = 11, clazz = FieldAlias.class)
    @MayAlias(reason = "may alias with non-static field and return value via parameter", id = 12, clazz = FieldAlias.class)
    @MayAliasUVar(reason = "may alias with return value and uVar via parameter and non-static field", lineNumber = 127, methodID = 8, clazz = FieldAlias.class)
    public Object parameterIsNonStaticField(
            @MayAlias(reason = "may alias with non-static field and parameter", id = 13, clazz = FieldAlias.class)
            @MayAliasUVar(reason = "may alias with parameter and UVar via non-static field", lineNumber = 127, methodID = 8, clazz = FieldAlias.class)
            Object a) {
        return a;
    }

    @AliasMethodID(id = 12, clazz = FieldAlias.class)
    @MayAlias(reason = "may alias with final field and return value", id = 14, clazz = FieldAlias.class)
    public Object returnFinalField() {
        Object o = finalField;
        return o;
    }

    @AliasMethodID(id = 13, clazz = FieldAlias.class)
    @MayAlias(reason = "may alias with final field and return value via parameter", id = 15, clazz = FieldAlias.class)
    public Object parameterIsFinalField(
            @MayAlias(reason = "must alias with final field and parameter", id = 16, clazz = FieldAlias.class)
            Object a) {
        return a;
    }

}
