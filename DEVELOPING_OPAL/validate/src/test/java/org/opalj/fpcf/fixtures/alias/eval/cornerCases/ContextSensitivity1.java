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
public class ContextSensitivity1 {

    public ContextSensitivity1() {
    }

    @AliasMethodID(id = 0, clazz = ContextSensitivity1.class)
    @MayAliasLine(reason = "ContextSensitivity1 b may a test1 context",
            lineNumber = 45, methodID = 0, callerContext = "test1",
            secondLineNumber = 44, secondMethodID = 0, secondCallerContext = "test1",
            clazz = ContextSensitivity1.class)
    @MayAliasLine(reason = "ContextSensitivity1 b may b test1 context",
            lineNumber = 45, methodID = 0, callerContext = "test1",
            secondLineNumber = 46, secondMethodID = 0, secondCallerContext = "test1",
            clazz = ContextSensitivity1.class)
    @NoAliasLine(reason = "ContextSensitivity1 b no a test2 context",
            lineNumber = 45, methodID = 0, callerContext = "test2",
            secondLineNumber = 44, secondMethodID = 0, secondCallerContext = "test2",
            clazz = ContextSensitivity1.class)
    @MayAliasLine(reason = "ContextSensitivity1 b may b test2 context",
            lineNumber = 45, methodID = 0, callerContext = "test2",
            secondLineNumber = 46, secondMethodID = 0, secondCallerContext = "test2",
            clazz = ContextSensitivity1.class)
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
        callee(a1, b1);
    }

    public void test2() {
        A a2 = new A();
        //Benchmark.alloc(2);
        A b2 = new A();
        callee(a2, b2);
    }

    public static void main(String[] args) {
        ContextSensitivity1 cs1 = new ContextSensitivity1();
        cs1.test1();
        cs1.test2();
    }
}
