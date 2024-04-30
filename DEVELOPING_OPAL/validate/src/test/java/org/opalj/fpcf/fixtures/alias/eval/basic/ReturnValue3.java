/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias.eval.basic;

import org.opalj.fpcf.fixtures.alias.eval.objects.B;
import org.opalj.fpcf.fixtures.alias.eval.objects.A;
import org.opalj.fpcf.properties.alias.AliasMethodID;
import org.opalj.fpcf.properties.alias.line.MayAliasLine;
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
public class ReturnValue3 {

  public static A id(A x) {
    A y = new A();
    //Benchmark.alloc(1);
    y.f = new B();
    return y;
  }

  @AliasMethodID(id = 0, clazz = ReturnValue3.class)
  @MayAliasLine(reason = "ReturnValue3 x may x",
          lineNumber = 55, methodID = 0,
          secondLineNumber = 56, secondMethodID = 0,
          clazz = ReturnValue3.class)
  @NoAliasLine(reason = "ReturnValue3 x no a",
          lineNumber = 55, methodID = 0,
          secondLineNumber = 58, secondMethodID = 0,
          clazz = ReturnValue3.class)
  @NoAliasLine(reason = "ReturnValue3 x no b",
          lineNumber = 55, methodID = 0,
          secondLineNumber = 59, secondMethodID = 0,
          clazz = ReturnValue3.class)
  @NoAliasLine(reason = "ReturnValue3 x no y",
          lineNumber = 55, methodID = 0,
          secondLineNumber = 57, secondMethodID = 0,
          clazz = ReturnValue3.class)
  public static void main(String[] args) {

    A a = new A();
    A b = id(a);
    B x = b.f;
    B y = a.f;

    x.hashCode();
    x.hashCode();
    y.hashCode();
    a.hashCode();
    b.hashCode();

    //Benchmark.test("x",
    //    "{allocId:1, mayAlias:[x], notMayAlias:[a,b,y], mustAlias:[x], notMustAlias:[a,b,y]}");
  }
}
