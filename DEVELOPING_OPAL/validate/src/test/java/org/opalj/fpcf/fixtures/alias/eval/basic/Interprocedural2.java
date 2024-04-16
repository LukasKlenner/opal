/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias.eval.basic;

import org.opalj.fpcf.fixtures.alias.eval.objects.A;
import org.opalj.fpcf.fixtures.alias.eval.objects.B;
import org.opalj.fpcf.properties.alias.AliasMethodID;
import org.opalj.fpcf.properties.alias.line.MayAliasLine;
import org.opalj.fpcf.properties.alias.line.NoAliasLine;

/*
 * Testcases taken from: https://github.com/secure-software-engineering/PointerBench
 *
 * @testcase Method2
 * 
 * @version 1.0
 * 
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 * 
 * @description Alias in a method
 */
public class Interprocedural2 {

  public Interprocedural2() {}

  public void alloc(A x, A y) {
    x.f = y.f;
  }

  @AliasMethodID(id = 0, clazz = Interprocedural2.class)
  @NoAliasLine(reason = "Interprocedural2 x no a",
          lineNumber = 64, methodID = 0,
          secondLineNumber = 67, secondMethodID = 0,
          clazz = Interprocedural2.class)
  @NoAliasLine(reason = "Interprocedural2 x no b",
          lineNumber = 64, methodID = 0,
          secondLineNumber = 68, secondMethodID = 0,
          clazz = Interprocedural2.class)
  @NoAliasLine(reason = "Interprocedural2 x no m2",
          lineNumber = 64, methodID = 0,
          secondLineNumber = 69, secondMethodID = 0,
          clazz = Interprocedural2.class)
  @MayAliasLine(reason = "Interprocedural2 x may x",
          lineNumber = 64, methodID = 0,
          secondLineNumber = 65, secondMethodID = 0,
          clazz = Interprocedural2.class)
  @MayAliasLine(reason = "Interprocedural2 x may y",
          lineNumber = 64, methodID = 0,
          secondLineNumber = 66, secondMethodID = 0,
          clazz = Interprocedural2.class)
  public static void main(String[] args) {

    A a = new A();
    A b = new A();

    //Benchmark.alloc(1);
    b.f = new B();
    Interprocedural2 m2 = new Interprocedural2();
    m2.alloc(a, b);

    B x = a.f;
    B y = b.f;

    x.hashCode();
    x.hashCode();
    y.hashCode();
    a.hashCode();
    b.hashCode();
    m2.hashCode();

    //Benchmark
    //    .test("x",
    //        "{allocId:1, mayAlias:[x,y], notMayAlias:[a,b,m2], mustAlias:[x,y], notMustAlias:[a,b,m2]}");
  }
}
