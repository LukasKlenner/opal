/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias.eval.cornerCases;

import org.opalj.fpcf.fixtures.alias.eval.objects.A;
import org.opalj.fpcf.properties.alias.AliasMethodID;
import org.opalj.fpcf.properties.alias.line.MayAliasLine;
import org.opalj.fpcf.properties.alias.line.NoAliasLine;

/*
 * Testcases taken from: https://github.com/secure-software-engineering/PointerBench
 *
 * @testcase ContextSensitivity1
 *
 * @version 1.0
 *
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 *
 * @description Object sensitive alias from caller object (1-CS)
 */
public class ContextSensitivity3 {

    public ContextSensitivity3() {
    }

    @AliasMethodID(id = 0, clazz = ContextSensitivity3.class)
    @MayAliasLine(reason = "ContextSensitivity3 b may a",
            lineNumber = 41, methodID = 0,
            secondLineNumber = 40, secondMethodID = 0,
            clazz = ContextSensitivity3.class)
    @MayAliasLine(reason = "ContextSensitivity3 b may b",
            lineNumber = 41, methodID = 0,
            secondLineNumber = 42, secondMethodID = 0,
            clazz = ContextSensitivity3.class)
    @NoAliasLine(reason = "ContextSensitivity3 b no a",
            lineNumber = 41, methodID = 0,
            secondLineNumber = 43, secondMethodID = 0,
            clazz = ContextSensitivity3.class)
    public void callee(A a, A b) {
        a.hashCode();
        b.hashCode();
        b.hashCode();
        a.hashCode();
        //Benchmark.test("b",
        //    "{allocId:1, mayAlias:[a,b], notMayAlias:[], mustAlias:[a,b], notMustAlias:[]},"
        //        + "{allocId:2, mayAlias:[a], notMayAlias:[b], mustAlias:[a], notMustAlias:[b]}");
    }

    public void test1() {
        //Benchmark.alloc(1);
        A a1 = new A();
        A b1 = a1;
        test11(a1, b1);
    }

    private void test11(A a1, A b1) {
        test111(a1, b1);
    }

    private void test111(A a1, A b1) {
        callee(a1, b1);
    }

    public void test2() {
        A a2 = new A();
        //Benchmark.alloc(2);
        A b2 = new A();
        test22(a2, b2);
    }

    private void test22(A a2, A b2) {
        test222(a2, b2);
    }

    private void test222(A a2, A b2) {
        callee(a2, b2);
    }

    public static void main(String[] args) {
        ContextSensitivity3 cs1 = new ContextSensitivity3();
        cs1.test1();
        cs1.test2();
    }
}
