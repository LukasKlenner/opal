/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias.eval.basic;

import org.opalj.fpcf.fixtures.alias.eval.objects.A;
import org.opalj.fpcf.properties.alias.AliasMethodID;
import org.opalj.fpcf.properties.alias.line.MayAliasLine;

/*
 * Testcases taken from: https://github.com/secure-software-engineering/PointerBench
 *
 * @testcase ParameterAlias1
 * 
 * @version 1.0
 * 
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 * 
 * @description Aliasing through static method parameter
 */
public class Parameter1 {

  @AliasMethodID(id = 0, clazz = Parameter1.class)
  @MayAliasLine(reason = "Parameter1 b may b",
          lineNumber = 34, methodID = 0,
          secondLineNumber = 35, secondMethodID = 0,
          clazz = Parameter1.class)
  @MayAliasLine(reason = "Parameter1 b may x",
          lineNumber = 34, methodID = 0,
          secondLineNumber = 36, secondMethodID = 0,
          clazz = Parameter1.class)
  public static void test(A x) {
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
    test(a);
  }
}
