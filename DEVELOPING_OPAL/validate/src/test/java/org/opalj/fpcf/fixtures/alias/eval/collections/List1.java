/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias.eval.collections;

import org.opalj.fpcf.fixtures.alias.eval.objects.A;
import org.opalj.fpcf.fixtures.alias.eval.stdBib.MyArrayList;
import org.opalj.fpcf.properties.alias.AliasMethodID;
import org.opalj.fpcf.properties.alias.line.MayAliasLine;
import org.opalj.fpcf.properties.alias.line.MustAliasLine;
import org.opalj.fpcf.properties.alias.line.NoAliasLine;

/*
 * Testcases taken from: https://github.com/secure-software-engineering/PointerBench
 *
 * @testcase List1
 * 
 * @version 1.0
 * 
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 * 
 * @description ArrayList
 */
public class List1 {

  @AliasMethodID(id = 0, clazz = List1.class)
  @MayAliasLine(reason = "List1 b may c",
          lineNumber = 53, methodID = 0,
          secondLineNumber = 55, secondMethodID = 0,
          clazz = List1.class)
  @MustAliasLine(reason = "List1 b may b",
          lineNumber = 53, methodID = 0,
          secondLineNumber = 54, secondMethodID = 0,
          clazz = List1.class)
  @NoAliasLine(reason = "List1 b no a",
          lineNumber = 53, methodID = 0,
          secondLineNumber = 52, secondMethodID = 0,
          clazz = List1.class)
  @NoAliasLine(reason = "List1 b no list",
          lineNumber = 53, methodID = 0,
          secondLineNumber = 56, secondMethodID = 0,
          clazz = List1.class)
  public static void main(String[] args) {

    MyArrayList<A> list = new MyArrayList<>(10); // use custom implementation, which works identical to ArrayList, to avoid analyzing the standard library
    A a = new A();
    //Benchmark.alloc(1);
    A b = new A();
    list.add(a);
    list.add(b);
    A c = list.get(1);

    a.hashCode();
    b.hashCode();
    b.hashCode();
    c.hashCode();
    list.hashCode();

    //Benchmark
    //    .test("b",
    //        "{allocId:1, mayAlias:[c,b], notMayAlias:[a,list], mustAlias:[c,b], notMustAlias:[a,list]}");
  }
}
