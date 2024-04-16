/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias.eval.generalJava;

import org.opalj.fpcf.fixtures.alias.eval.objects.A;
import org.opalj.fpcf.fixtures.alias.eval.objects.P;
import org.opalj.fpcf.properties.alias.AliasMethodID;
import org.opalj.fpcf.properties.alias.line.MayAliasLine;
import org.opalj.fpcf.properties.alias.line.NoAliasLine;

/*
 * Testcases taken from: https://github.com/secure-software-engineering/PointerBench
 *
 * @testcase SuperClass1
 *
 * @version 1.0
 *
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 *
 * @description Alias from method in super class
 */
public class SuperClasses1 {

    @AliasMethodID(id = 0, clazz = SuperClasses1.class)
    @NoAliasLine(reason = "SuperClasses1 h may a",
            lineNumber = 52, methodID = 0,
            secondLineNumber = 50, secondMethodID = 0,
            clazz = SuperClasses1.class)
    @MayAliasLine(reason = "SuperClasses1 h may b",
            lineNumber = 52, methodID = 0,
            secondLineNumber = 51, secondMethodID = 0,
            clazz = SuperClasses1.class)
    @MayAliasLine(reason = "SuperClasses1 h may h",
            lineNumber = 52, methodID = 0,
            secondLineNumber = 53, secondMethodID = 0,
            clazz = SuperClasses1.class)
    @NoAliasLine(reason = "SuperClasses1 h not p",
            lineNumber = 52, methodID = 0,
            secondLineNumber = 54, secondMethodID = 0,
            clazz = SuperClasses1.class)
    public static void main(String[] args) {
        //Benchmark.alloc(1);
        A a = new A();
        A b = new A();

        P p = new P(a);
        p.alias(b);
        A h = p.getA();

        a.hashCode();
        b.hashCode();
        h.hashCode();
        h.hashCode();
        p.hashCode();

        //Benchmark.test("h",
        //    "{allocId:1, mayAlias:[h,b], notMayAlias:[a,p], mustAlias:[b,a], notMustAlias:[p]}");
        //Benchmark.use(h);
    }

}
