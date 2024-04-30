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
public class ContextSensitivity2 {

    public ContextSensitivity2() {
    }

    @AliasMethodID(id = 0, clazz = ContextSensitivity2.class)
    @MayAliasLine(reason = "ContextSensitivity2 b may a context test11",
            lineNumber = 45, methodID = 0, callerContext = "test11",
            secondLineNumber = 44, secondMethodID = 0, secondCallerContext = "test11",
            clazz = ContextSensitivity2.class)
    @MayAliasLine(reason = "ContextSensitivity2 b may b context test11",
            lineNumber = 45, methodID = 0, callerContext = "test11",
            secondLineNumber = 46, secondMethodID = 0, secondCallerContext = "test11",
            clazz = ContextSensitivity2.class)
    @NoAliasLine(reason = "ContextSensitivity2 b no a context test22",
            lineNumber = 45, methodID = 0, callerContext = "test22",
            secondLineNumber = 44, secondMethodID = 0, secondCallerContext = "test22",
            clazz = ContextSensitivity2.class)
    @MayAliasLine(reason = "ContextSensitivity2 b may b context test22",
            lineNumber = 45, methodID = 0, callerContext = "test22",
            secondLineNumber = 46, secondMethodID = 0, secondCallerContext = "test22",
            clazz = ContextSensitivity2.class)
    public void callee(A a, A b) {
        a.hashCode();
        b.hashCode();
        b.hashCode();
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
        callee(a1, b1);
    }

    public void test2() {
        A a2 = new A();
        //Benchmark.alloc(2);
        A b2 = new A();
        test22(a2, b2);
    }

    private void test22(A a2, A b2) {
        callee(a2, b2);
    }

    public static void main(String[] args) {
        ContextSensitivity2 cs1 = new ContextSensitivity2();
        cs1.test1();
        cs1.test2();
    }
}
