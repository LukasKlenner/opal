/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias.eval.cornerCases;

import org.opalj.fpcf.fixtures.alias.eval.objects.A;
import org.opalj.fpcf.fixtures.alias.eval.objects.B;
import org.opalj.fpcf.properties.alias.AliasMethodID;
import org.opalj.fpcf.properties.alias.line.MayAliasLine;
import org.opalj.fpcf.properties.alias.line.NoAliasLine;

/*
 * Testcases taken from: https://github.com/secure-software-engineering/PointerBench
 *
 * @testcase StrongUpdate1
 * 
 * @version 1.0
 * 
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 * 
 * @description Indirect alias of a.f and b.f through alias of a and b
 */
public class StrongUpdate2 {

  @AliasMethodID(id = 0, clazz = StrongUpdate2.class)
  @MayAliasLine(reason = "StrongUpdate2 y may y",
          lineNumber = 43, methodID = 0,
          secondLineNumber = 44, secondMethodID = 0,
          clazz = StrongUpdate2.class)
  @NoAliasLine(reason = "StrongUpdate2 y no x",
          lineNumber = 43, methodID = 0,
          secondLineNumber = 42, secondMethodID = 0,
          clazz = StrongUpdate2.class)
  public static void main(String[] args) {

    A a = new A();
    A b = a;
    B x = b.f;
    //Benchmark.alloc(1);
    a.f = new B();
    B y = b.f;

    x.hashCode();
    y.hashCode();
    y.hashCode();

    //Benchmark.test("y",
    //    "{allocId:1, mayAlias:[y], notMayAlias:[x], mustAlias:[y], notMustAlias:[x]}");
  }
}
