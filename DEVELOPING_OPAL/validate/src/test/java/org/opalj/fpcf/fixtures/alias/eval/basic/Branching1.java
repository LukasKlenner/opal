/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias.eval.basic;

import org.opalj.fpcf.fixtures.alias.eval.objects.A;
import org.opalj.fpcf.properties.alias.AliasMethodID;
import org.opalj.fpcf.properties.alias.line.MayAliasLine;
import org.opalj.fpcf.properties.alias.line.NoAliasLine;

/*
 * testcases taken from: https://github.com/secure-software-engineering/PointerBench
 *
 * @testcase Branching1
 * 
 * @version 1.0
 * 
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 * 
 * @description Condition. a and b alias on one path, not on the other
 */
public class Branching1 {

  @AliasMethodID(id = 0, clazz = Branching1.class)
  @NoAliasLine(reason = "branching1 a no i",
          lineNumber = 47, methodID = 0, parameterIndex = 0,
          secondLineNumber = 48, secondMethodID = 0,
          clazz = Branching1.class)
  @MayAliasLine(reason = "branching1 a may a",
          lineNumber = 48, methodID = 0,
          secondLineNumber = 49, secondMethodID = 0,
          clazz = Branching1.class)
  @MayAliasLine(reason = "branching1 a may b",
          lineNumber = 49, methodID = 0,
          secondLineNumber = 50, secondMethodID = 0,
          clazz = Branching1.class)
  public static void main(String[] args) {
    int i = 0;

    //Benchmark.alloc(1);
    A a = new A();
    //Benchmark.alloc(2);
    A b = new A();

    if (i < Math.random())
      a = b;

    useInt(i);
    a.hashCode();
    a.hashCode();
    b.hashCode();

    //Benchmark.test("a",
    //    "{allocId:1, mayAlias:[a], notMayAlias:[i,b], mustAlias:[a], notMustAlias:[i,b]},"
    //        + "{allocId:2, mayAlias:[a,b], notMayAlias:[i], mustAlias:[a], notMustAlias:[i,b]}");
  }

  private static void useInt(int i) {

  }
}
