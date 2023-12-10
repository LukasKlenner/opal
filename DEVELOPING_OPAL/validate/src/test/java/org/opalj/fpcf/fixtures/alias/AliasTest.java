package org.opalj.fpcf.fixtures.alias;

import org.opalj.fpcf.properties.alias.Alias;
import org.opalj.fpcf.properties.alias.NoAlias;
import org.opalj.fpcf.properties.alias.MayAlias;

public class AliasTest {

    static int a = 0;

    public static void main(String[] args) {
        noAlias();
        mayAlias();
    }

    public static void noAlias() {

        Object o1 = new @Alias(noAlias = @NoAlias(reason = "o1 and o2 do not alias", testClass = AliasTest.class, id = "na")) Object();
        Object o2 = new @Alias(noAlias = @NoAlias(reason = "o1 and o2 do not alias", testClass = AliasTest.class, id = "na")) Object();

        if (a == 0) {
            Object o3 = o1;
        }

    }

    public static void mayAlias() {

        Object o1 = new @Alias(mayAlias = @MayAlias(reason = "o1 and o2 may alias", testClass = AliasTest.class, id = "ma")) Object();
        Object o2 = new @Alias(mayAlias = @MayAlias(reason = "o1 and o2 may alias", testClass = AliasTest.class, id = "ma")) Object();

        o2.hashCode();

        if (a == 0) {
            o2 = o1;
        }
        o2.hashCode();
    }
}
