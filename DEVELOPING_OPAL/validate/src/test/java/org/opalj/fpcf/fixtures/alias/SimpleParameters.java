package org.opalj.fpcf.fixtures.alias;

import org.opalj.fpcf.properties.alias.Alias;
import org.opalj.fpcf.properties.alias.MayAlias;
import org.opalj.fpcf.properties.alias.NoAlias;

public class SimpleParameters {

    static int a = 0;

    public static void main(String[] args) {
        noAlias(new Object());
        mayAlias(new Object());
    }

    public static void noAlias(@Alias(noAlias = @NoAlias(reason = "noAlias", testClass = SimpleParameters.class, id = "na")) Object o1) {

        Object o2 = new @Alias(noAlias = @NoAlias(reason = "noAlias", testClass = SimpleParameters.class, id = "na")) Object();
        o1.hashCode();
        o2.hashCode();
    }

    public static void mayAlias(@Alias(mayAlias = @MayAlias(reason = "mayAlias", testClass = SimpleParameters.class, id = "ma")) Object o1) {

        Object o2 = new @Alias(mayAlias = @MayAlias(reason = "mayAlias", testClass = SimpleParameters.class, id = "ma")) Object();
        o2.hashCode();

        if (a == 1) {
            o2 = o1;
        }

        o2.hashCode();
    }

}
