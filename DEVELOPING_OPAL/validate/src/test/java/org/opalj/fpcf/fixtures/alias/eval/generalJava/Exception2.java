/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias.eval.generalJava;

import org.opalj.fpcf.fixtures.alias.eval.objects.A;
import org.opalj.fpcf.properties.alias.AliasMethodID;
import org.opalj.fpcf.properties.alias.line.MustAliasLine;
import org.opalj.fpcf.properties.alias.line.NoAliasLine;

/*
 * Testcases taken from: https://github.com/secure-software-engineering/PointerBench
 *
 * @testcase Exception2
 * 
 * @version 1.0
 * 
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 * 
 * @description No alias in exception (exception never triggered)
 */
public class Exception2 {

  @AliasMethodID(id = 0, clazz = Exception2.class)
  @MustAliasLine(reason = "Exception2 b may b",
          lineNumber = 45, methodID = 0,
          secondLineNumber = 46, secondMethodID = 0,
          clazz = Exception2.class)
  @NoAliasLine(reason = "Exception2 b no a",
          lineNumber = 45, methodID = 0,
          secondLineNumber = 44, secondMethodID = 0,
          clazz = Exception2.class)
  public static void main(String[] args) {

    A a = new A();
    //Benchmark.alloc(1);
    A b = new A();

    try {
      Integer.parseInt("abc");
      a = b;

    } catch (RuntimeException e) {

      a.hashCode();
      b.hashCode();
      b.hashCode();

      //Benchmark.test("b",
      //    "{allocId:1, mayAlias:[b], notMayAlias:[a], mustAlias:[b], notMustAlias:[a]}");
    }

  }
}
