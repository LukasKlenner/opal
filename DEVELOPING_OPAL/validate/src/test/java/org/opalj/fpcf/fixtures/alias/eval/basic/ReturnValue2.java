/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias.eval.basic;

import org.opalj.fpcf.fixtures.alias.eval.objects.A;
import org.opalj.fpcf.properties.alias.AliasMethodID;
import org.opalj.fpcf.properties.alias.line.MayAliasLine;
import org.opalj.fpcf.properties.alias.line.MustAliasLine;
import org.opalj.fpcf.properties.alias.line.NoAliasLine;

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
public class ReturnValue2 {

  public ReturnValue2() {}

  public A id(A x) {
    return x;
  }

  @AliasMethodID(id = 0, clazz = ReturnValue2.class)
  @MustAliasLine(reason = "ReturnValue2 b may b",
          lineNumber = 51, methodID = 0,
          secondLineNumber = 52, secondMethodID = 0,
          clazz = ReturnValue2.class)
  @MayAliasLine(reason = "ReturnValue2 b may a",
          lineNumber = 51, methodID = 0,
          secondLineNumber = 50, secondMethodID = 0,
          clazz = ReturnValue2.class)
  @NoAliasLine(reason = "ReturnValue2 b no rv2",
          lineNumber = 51, methodID = 0,
          secondLineNumber = 53, secondMethodID = 0,
          clazz = ReturnValue2.class)
  public static void main(String[] args) {

    //Benchmark.alloc(1);
    A a = new A();
    ReturnValue2 rv2 = new ReturnValue2();
    A b = rv2.id(a);

    a.hashCode();
    b.hashCode();
    b.hashCode();
    rv2.hashCode();

    //Benchmark.test("b",
    //    "{allocId:1, mayAlias:[a,b], notMayAlias:[rv2], mustAlias:[a,b], notMustAlias:[rv2]}");
  }
}
