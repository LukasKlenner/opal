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
 * @testcase Method1
 * 
 * @version 1.0
 * 
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 * 
 * @description Alias in a static method
 */
public class Interprocedural1 {

  public static void alloc(A x, A y) {
    x.f = y.f;
  }

  @AliasMethodID(id = 0, clazz = Interprocedural1.class)
  @NoAliasLine(reason = "Interprocedural1 x no a",
          lineNumber = 57, methodID = 0,
          secondLineNumber = 60, secondMethodID = 0,
          clazz = Interprocedural1.class)
  @NoAliasLine(reason = "Interprocedural1 x no b",
          lineNumber = 57, methodID = 0,
          secondLineNumber = 61, secondMethodID = 0,
          clazz = Interprocedural1.class)
  @MayAliasLine(reason = "Interprocedural1 x may x",
          lineNumber = 57, methodID = 0,
          secondLineNumber = 58, secondMethodID = 0,
          clazz = Interprocedural1.class)
  @MayAliasLine(reason = "Interprocedural1 x may y",
          lineNumber = 57, methodID = 0,
          secondLineNumber = 59, secondMethodID = 0,
          clazz = Interprocedural1.class)
  public static void main(String[] args) {

    A a = new A();
    A b = new A();

    //Benchmark.alloc(1);
    b.f = new B();
    alloc(a, b);

    B x = a.f;
    B y = b.f;

    x.hashCode();
    x.hashCode();
    y.hashCode();
    a.hashCode();
    b.hashCode();
    //Benchmark.test("x",
    //    "{allocId:1, mayAlias:[x,y], notMayAlias:[a,b], mustAlias:[x,y], notMustAlias:[a,b]}");
  }
}
