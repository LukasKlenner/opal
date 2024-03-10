/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias;

import org.opalj.fpcf.properties.alias.AliasMethodID;
import org.opalj.fpcf.properties.alias.u_var.MayAliasUVar;
import org.opalj.fpcf.properties.alias.u_var.NoAliasUVar;

public class SimpleDefinitionSites {

//    public static void main(String[] args) {
//        noAlias();
//        mayAlias();
//    }
//
//    @AliasMethodID(id = 0, clazz = SimpleDefinitionSites.class)
//    public static void noAlias() {
//
//        Object o1 = new Object();
//        Object o2 = new @NoAliasUVar(reason = "noAlias", lineNumber = 27, methodID = 0, clazz = SimpleDefinitionSites.class) Object();
//        Object o3 = new Object();
//
//        if (Math.random() > 0.5) {
//            o3 = o1;
//        }
//
//        o2.hashCode();
//        o3.hashCode();
//
//    }
//
//    @AliasMethodID(id = 1, clazz = SimpleDefinitionSites.class)
//    public static void mayAlias() {
//
//        Object o1 = new @MayAliasUVar(reason = "noAlias", lineNumber = 42, methodID = 1, clazz = SimpleDefinitionSites.class) Object();
//        Object o2 = new Object();
//
//        o2.hashCode();
//
//        if (Math.random() > 0.5) {
//            o2 = o1;
//        }
//        o2.hashCode();
//    }

}
