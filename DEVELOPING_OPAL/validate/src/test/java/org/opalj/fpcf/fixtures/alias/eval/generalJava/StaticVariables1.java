/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias.eval.generalJava;

import org.opalj.fpcf.fixtures.alias.eval.objects.A;
import org.opalj.fpcf.properties.alias.AliasMethodID;
import org.opalj.fpcf.properties.alias.line.MayAliasLine;

/*
 * Testcases taken from: https://github.com/secure-software-engineering/PointerBench
 *
 * @testcase StaticVariables1
 * 
 * @version 1.0
 * 
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 * 
 * @description Alias to a static variable, allocation site at the static variable site
 */
public class StaticVariables1 {

  private static A a;

  @AliasMethodID(id = 0, clazz = StaticVariables1.class)
  @MayAliasLine(reason = "StaticVariables1 b may b",
          lineNumber = 39, methodID = 0,
          secondLineNumber = 40, secondMethodID = 0,
          clazz = StaticVariables1.class)
  @MayAliasLine(reason = "StaticVariables1 b may c",
          lineNumber = 40, methodID = 0,
          secondLineNumber = 41, secondMethodID = 0,
          clazz = StaticVariables1.class)
  public static void main(String[] args) {
    //Benchmark.alloc(1);
    a = new A();
    A b = a;
    A c = a;

    b.hashCode();
    b.hashCode();
    c.hashCode();
    //Benchmark.test("b",
    //    "{allocId:1, mayAlias:[b,c], notMayAlias:[], mustAlias:[b,c], notMustAlias:[]}");
  }
}
