package org.opalj.fpcf.fixtures.alias;

import org.opalj.fpcf.properties.alias.NoAlias;
import org.opalj.fpcf.properties.alias.MayAlias;

public class AliasTest {

    static int a = 0;

    public static void main(String[] args) {
        noAlias();
        mayAlias();
    }

    public static void noAlias() {

        Object o1 = new @NoAlias(value = "o1 and o2 do not alias", thiz = "na_o1", other = "na_o2") Object();
        Object o2 = new @NoAlias(value = "o1 and o2 do not alias", thiz = "na_o2", other = "na_o1") Object();

        if (a == 0) {
            Object o3 = o1;
        }

    }

    public static void mayAlias() {

        Object o1 = new @MayAlias(value = "o1 and o2 may alias", thiz = "ma_o1", other = "ma_o2") Object();
        Object o2 = new @MayAlias(value = "o1 and o2 may alias", thiz = "ma_o2", other = "ma_o1") Object();

        o2.hashCode();

        if (a == 0) {
            o2 = o1;
        }
        o2.hashCode();
    }
}
