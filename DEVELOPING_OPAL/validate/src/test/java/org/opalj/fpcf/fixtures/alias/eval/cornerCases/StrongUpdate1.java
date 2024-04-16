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
public class StrongUpdate1 {

  @AliasMethodID(id = 0, clazz = StrongUpdate1.class)
  @MayAliasLine(reason = "StrongUpdate1 x may x",
          lineNumber = 52, methodID = 0,
          secondLineNumber = 53, secondMethodID = 0,
          clazz = StrongUpdate1.class)
  @MayAliasLine(reason = "StrongUpdate1 x may y",
          lineNumber = 52, methodID = 0,
          secondLineNumber = 54, secondMethodID = 0,
          clazz = StrongUpdate1.class)
  @NoAliasLine(reason = "StrongUpdate1 x no a",
          lineNumber = 52, methodID = 0,
          secondLineNumber = 50, secondMethodID = 0,
          clazz = StrongUpdate1.class)
  @NoAliasLine(reason = "StrongUpdate1 x no b",
          lineNumber = 52, methodID = 0,
          secondLineNumber = 51, secondMethodID = 0,
          clazz = StrongUpdate1.class)
  public static void main(String[] args) {

    A a = new A();
    A b = a;
    //Benchmark.alloc(1);
    a.f = new B();
    B y = a.f;
    B x = b.f;

    a.hashCode();
    b.hashCode();
    x.hashCode();
    x.hashCode();
    y.hashCode();

    //Benchmark.test("x",
    //    "{allocId:1, mayAlias:[x,y], notMayAlias:[a,b], mustAlias:[x,y], notMustAlias:[a,b]}");
  }
}
