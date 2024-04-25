/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias.eval.basic;

import org.opalj.fpcf.fixtures.alias.eval.objects.A;
import org.opalj.fpcf.properties.alias.AliasMethodID;
import org.opalj.fpcf.properties.alias.line.MayAliasLine;

/*
 * Testcases taken from: https://github.com/secure-software-engineering/PointerBench
 *
 * @testcase ReturnValue1
 * 
 * @version 1.0
 * 
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 * 
 * @description Alias to a return value from a static method
 */
public class ReturnValue1 {

  public static A id(A x) {
    return x;
  }

  @AliasMethodID(id = 0, clazz = ReturnValue1.class)
  @MayAliasLine(reason = "ReturnValue1 b may b",
          lineNumber = 42, methodID = 0,
          secondLineNumber = 43, secondMethodID = 0,
          clazz = ReturnValue1.class)
  @MayAliasLine(reason = "ReturnValue1 b may a",
          lineNumber = 42, methodID = 0,
          secondLineNumber = 41, secondMethodID = 0,
          clazz = ReturnValue1.class)
  public static void main(String[] args) {

    //Benchmark.alloc(1);
    A a = new A();
    A b = id(a);

    a.hashCode();
    b.hashCode();
    b.hashCode();

    //Benchmark.test("b",
    //    "{allocId:1, mayAlias:[a,b], notMayAlias:[], mustAlias:[a,b], notMustAlias:[]}");
  }
}
