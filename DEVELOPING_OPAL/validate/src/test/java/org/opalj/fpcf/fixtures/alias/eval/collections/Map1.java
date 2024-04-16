/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias.eval.collections;

import org.opalj.fpcf.fixtures.alias.eval.objects.A;
import org.opalj.fpcf.fixtures.alias.eval.stdBib.MyHashMap;
import org.opalj.fpcf.properties.alias.AliasMethodID;
import org.opalj.fpcf.properties.alias.line.MayAliasLine;
import org.opalj.fpcf.properties.alias.line.NoAliasLine;

/*
 * Testcases taken from: https://github.com/secure-software-engineering/PointerBench
 *
 * @testcase Map1
 * 
 * @version 1.0
 * 
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 * 
 * @description HashMap
 */
public class Map1 {

  @AliasMethodID(id = 0, clazz = Map1.class)
  @MayAliasLine(reason = "Map1 c may c",
          lineNumber = 53, methodID = 0,
          secondLineNumber = 54, secondMethodID = 0,
          clazz = Map1.class)
  @MayAliasLine(reason = "Map1 c may b",
          lineNumber = 53, methodID = 0,
          secondLineNumber = 52, secondMethodID = 0,
          clazz = Map1.class)
  @NoAliasLine(reason = "Map1 c no a",
          lineNumber = 53, methodID = 0,
          secondLineNumber = 51, secondMethodID = 0,
          clazz = Map1.class)
  @NoAliasLine(reason = "Map1 c no map",
          lineNumber = 53, methodID = 0,
          secondLineNumber = 55, secondMethodID = 0,
          clazz = Map1.class)
  public static void main(String[] args) {

    MyHashMap<String, A> map = new MyHashMap<>(10); // use custom implementation, which works identical to HashMap, to avoid analyzing the standard library
    A a = new A();
    //Benchmark.alloc(1);
    A b = new A();
    map.put("first", a);
    map.put("second", b);
    A c = map.get("second");

    a.hashCode();
    b.hashCode();
    c.hashCode();
    c.hashCode();
    map.hashCode();

    //Benchmark.test("c",
    //    "{allocId:1, mayAlias:[c,b], notMayAlias:[a,map], mustAlias:[c,b], notMustAlias:[a,map]}");
  }
}
