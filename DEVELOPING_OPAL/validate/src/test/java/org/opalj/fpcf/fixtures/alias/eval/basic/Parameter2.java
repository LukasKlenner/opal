/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias.eval.basic;

import org.opalj.fpcf.fixtures.alias.eval.objects.A;
import org.opalj.fpcf.properties.alias.AliasMethodID;
import org.opalj.fpcf.properties.alias.line.MayAliasLine;

/*
 * Testcases taken from: https://github.com/secure-software-engineering/PointerBench
 *
 * @testcase ParameterAlias2
 * 
 * @version 1.0
 * 
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 * 
 * @description Aliasing through non static method parameter
 */
public class Parameter2 {

  public Parameter2() {}

  @AliasMethodID(id = 0, clazz = Parameter2.class)
  @MayAliasLine(reason = "Parameter2 b may b",
          lineNumber = 36, methodID = 0,
          secondLineNumber = 37, secondMethodID = 0,
          clazz = Parameter2.class)
  @MayAliasLine(reason = "Parameter2 b may x",
          lineNumber = 36, methodID = 0,
          secondLineNumber = 38, secondMethodID = 0,
          clazz = Parameter2.class)
  public void test(A x) {
    A b = x;

    b.hashCode();
    b.hashCode();
    x.hashCode();

    //Benchmark.test("b",
    //    "{allocId:1, mayAlias:[b,x], notMayAlias:[], mustAlias:[b,x], notMustAlias:[]}");
  }

  public static void main(String[] args) {

    //Benchmark.alloc(1);
    A a = new A();
    Parameter2 p2 = new Parameter2();
    p2.test(a);
  }
}
