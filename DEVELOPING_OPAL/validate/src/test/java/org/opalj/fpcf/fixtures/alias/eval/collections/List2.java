/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias.eval.collections;

import org.opalj.fpcf.fixtures.alias.eval.objects.A;
import org.opalj.fpcf.fixtures.alias.eval.stdLib.MyLinkedList;
import org.opalj.fpcf.properties.alias.AliasMethodID;
import org.opalj.fpcf.properties.alias.line.MayAliasLine;
import org.opalj.fpcf.properties.alias.line.MustAliasLine;
import org.opalj.fpcf.properties.alias.line.NoAliasLine;

/*
 * Testcases taken from: https://github.com/secure-software-engineering/PointerBench
 *
 * @testcase List2
 * 
 * @version 1.0
 * 
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 * 
 * @description LinkedList
 */
public class List2 {

  @AliasMethodID(id = 0, clazz = List2.class)
  @MayAliasLine(reason = "List2 b may c",
          lineNumber = 53, methodID = 0,
          secondLineNumber = 55, secondMethodID = 0,
          clazz = List2.class)
  @MustAliasLine(reason = "List2 b may b",
          lineNumber = 53, methodID = 0,
          secondLineNumber = 54, secondMethodID = 0,
          clazz = List2.class)
  @NoAliasLine(reason = "List2 b no a",
          lineNumber = 53, methodID = 0,
          secondLineNumber = 52, secondMethodID = 0,
          clazz = List2.class)
  @NoAliasLine(reason = "List2 b no list",
          lineNumber = 53, methodID = 0,
          secondLineNumber = 56, secondMethodID = 0,
          clazz = List2.class)
  public static void main(String[] args) {

    MyLinkedList<A> list = new MyLinkedList<>(); // use custom implementation, which works identical to LinkedList  , to avoid analyzing the standard library
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
