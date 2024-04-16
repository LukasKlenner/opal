/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias.eval.collections;

import org.opalj.fpcf.fixtures.alias.eval.objects.A;
import org.opalj.fpcf.properties.alias.AliasMethodID;
import org.opalj.fpcf.properties.alias.line.MayAliasLine;
import org.opalj.fpcf.properties.alias.line.NoAliasLine;

/*
 * Testcases taken from: https://github.com/secure-software-engineering/PointerBench
 *
 * @testcase Array1
 *
 * @version 1.0
 *
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 *
 * @description Array alias
 */
public class Array1 {


    @AliasMethodID(id = 0, clazz = Array1.class)
    @MayAliasLine(reason = "Array1 c may c",
            lineNumber = 53, methodID = 0,
            secondLineNumber = 54, secondMethodID = 0,
            clazz = Array1.class)
    @MayAliasLine(reason = "Array1 c may b",
            lineNumber = 53, methodID = 0,
            secondLineNumber = 52, secondMethodID = 0,
            clazz = Array1.class)
    @NoAliasLine(reason = "Array1 c no a",
            lineNumber = 53, methodID = 0,
            secondLineNumber = 51, secondMethodID = 0,
            clazz = Array1.class)
    @NoAliasLine(reason = "Array1 c no array",
            lineNumber = 53, methodID = 0,
            secondLineNumber = 55, secondMethodID = 0,
            clazz = Array1.class)
    public static void main(String[] args) {

        A[] array = new A[2];
        A a = new A();
        //Benchmark.alloc(1);
        A b = new A();
        array[0] = a;
        array[1] = b;
        A c = array[1];

        a.hashCode();
        b.hashCode();
        c.hashCode();
        c.hashCode();
        array.hashCode();
        //Benchmark
        //    .test("c",
        //        "{allocId:1, mayAlias:[c,b], notMayAlias:[a,array], mustAlias:[c,b], notMustAlias:[a,array]}");

        A d = new A();
        d.hashCode();
    }
}
