/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias.eval.collections;


import org.opalj.fpcf.fixtures.alias.eval.objects.A;
import org.opalj.fpcf.fixtures.alias.eval.stdBib.MyHashSet;
import org.opalj.fpcf.properties.alias.AliasMethodID;
import org.opalj.fpcf.properties.alias.line.MayAliasLine;
import org.opalj.fpcf.properties.alias.line.NoAliasLine;

/*
 * Testcases taken from: https://github.com/secure-software-engineering/PointerBench
 *
 * @testcase Set1
 *
 * @version 1.0
 *
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 *
 * @description HashSet
 */
public class Set1 {

    @AliasMethodID(id = 0, clazz = Set1.class)
    @MayAliasLine(reason = "Set1 c may c",
            lineNumber = 59, methodID = 0,
            secondLineNumber = 60, secondMethodID = 0,
            clazz = Set1.class)
    @NoAliasLine(reason = "Set1 c no a",
            lineNumber = 59, methodID = 0,
            secondLineNumber = 57, secondMethodID = 0,
            clazz = Set1.class)
    @NoAliasLine(reason = "Set1 c no b",
            lineNumber = 59, methodID = 0,
            secondLineNumber = 58, secondMethodID = 0,
            clazz = Set1.class)
    @NoAliasLine(reason = "Set1 c no set",
            lineNumber = 59, methodID = 0,
            secondLineNumber = 61, secondMethodID = 0,
            clazz = Set1.class)
    public static void main(String[] args) {

        MyHashSet<A> set = new MyHashSet<>(10); // use custom implementation, which works identical to HashSet, to avoid analyzing the standard library
        A a = new A();
        A c = null;
        //Benchmark.alloc(1);
        A b = new A();
        set.add(a);
        set.add(b);
        for (A i : set) {
            c = i;
            break;
        }
        //a = null;

        a.hashCode();
        b.hashCode();
        c.hashCode();
        c.hashCode();
        set.hashCode();

        //Benchmark.test("c",
        //    "{allocId:1, mayAlias:[c], notMayAlias:[a,b,set], mustAlias:[c], notMustAlias:[a,b,set]}");
        //Benchmark.use(c);
    }
}
