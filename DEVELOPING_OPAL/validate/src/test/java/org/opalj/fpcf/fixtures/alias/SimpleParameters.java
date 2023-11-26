package org.opalj.fpcf.fixtures.alias;

import org.opalj.fpcf.properties.alias.MayAlias;
import org.opalj.fpcf.properties.alias.NoAlias;

public class SimpleParameters {

    static int a = 0;

    public static void main(String[] args) {
        noAlias(new Object());
        mayAlias(new Object());
    }

    public static void noAlias(@NoAlias(value = "noAlias", other = "NoFpDso2", thiz = "NoFpDso1") Object o1) {

        Object o2 = new @NoAlias(value = "noAlias", other = "NoFpDso1", thiz = "NoFpDso2") Object();
        o1.hashCode();
        o2.hashCode();
    }

    public static void mayAlias(@MayAlias(value = "mayAlias", other = "MayFpDso2", thiz = "MayFpDso1") Object o1) {

        Object o2 = new @MayAlias(value = "mayAlias", other = "MayFpDso1", thiz = "MayFpDso2") Object();
        o2.hashCode();

        if (a == 1) {
            o2 = o1;
        }

        o2.hashCode();
    }

}
