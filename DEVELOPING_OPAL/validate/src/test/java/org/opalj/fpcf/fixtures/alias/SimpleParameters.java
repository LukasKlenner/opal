/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias;

import org.opalj.fpcf.properties.alias.AliasMethodID;
import org.opalj.fpcf.properties.alias.MayAlias;
import org.opalj.fpcf.properties.alias.MustAlias;
import org.opalj.fpcf.properties.alias.NoAlias;
import org.opalj.fpcf.properties.alias.u_var.MayAliasUVar;
import org.opalj.fpcf.properties.alias.u_var.NoAliasUVar;

public class SimpleParameters {

//    public static void main(String[] args) {
//        noAliasWithLocal(new Object());
//        noAliasWithParam(new Object(), new Object());
//        mayAliasWithLocal(new Object());
//        mayAliasWithParam(new Object(), new Object());
//
//        Object o1 = new Object();
//        mustAliasWithParam(o1, o1);
//        mayAliasWithParam(o1, o1);
//    }
//
//    @AliasMethodID(id = 0, clazz = SimpleParameters.class)
//    public static void noAliasWithLocal(@NoAliasUVar(reason = "noAlias", lineNumber = 30, methodID = 0, clazz = SimpleParameters.class) Object o1) {
//
//        Object o2 = new Object();
//
//        o1.hashCode();
//        o2.hashCode();
//    }
//
//    public static void noAliasWithParam(@NoAlias(reason = "noAlias", id = 0, clazz = SimpleParameters.class) Object o1,
//                                        @NoAlias(reason = "noAlias", id = 0, clazz = SimpleParameters.class) Object o2) { //TODO doppelt in properties in AliasTests
//
//        o1.hashCode();
//        o2.hashCode();
//    }
//
//    @AliasMethodID(id = 1, clazz = SimpleParameters.class)
//    public static void mayAliasWithLocal(@MayAliasUVar(reason = "mayAlias", lineNumber = 50, methodID = 1, clazz = SimpleParameters.class) Object o1) {
//
//        Object o2 = new Object();
//        o2.hashCode();
//
//        if (Math.random() > 0.5) {
//            o2 = o1;
//        }
//
//        o2.hashCode();
//    }
//
//    public static void mayAliasWithParam(@MayAlias(reason = "mayAlias", id = 1, clazz = SimpleParameters.class) Object o1,
//                                         @MayAlias(reason = "mayAlias", id = 1, clazz = SimpleParameters.class) Object o2) {
//
//        o1.hashCode();
//        o2.hashCode();
//
//        if (Math.random() > 0.5) {
//            o2 = o1;
//        }
//
//        o2.hashCode();
//    }
//
//    public static void mustAliasWithParam(@MustAlias(reason = "identical variable used at call", id = 2, clazz = SimpleParameters.class) Object o1,
//                                 @MustAlias(reason = "identical variable used at call", id = 2, clazz = SimpleParameters.class) Object o2) {
//
//        o1.hashCode();
//        o2.hashCode();
//    }

}
