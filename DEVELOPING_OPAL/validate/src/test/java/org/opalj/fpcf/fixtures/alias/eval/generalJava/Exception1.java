/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias.eval.generalJava;

import org.opalj.fpcf.fixtures.alias.eval.objects.A;
import org.opalj.fpcf.properties.alias.AliasMethodID;
import org.opalj.fpcf.properties.alias.line.MayAliasLine;

/*
 * Testcases taken from: https://github.com/secure-software-engineering/PointerBench
 *
 * @testcase Exception1
 * 
 * @version 1.0
 * 
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 * 
 * @description Alias in exception
 */
public class Exception1 {

  @AliasMethodID(id = 0, clazz = Exception1.class)
  @MayAliasLine(reason = "Exception1 b may b",
          lineNumber = 44, methodID = 0,
          secondLineNumber = 45, secondMethodID = 0,
          clazz = Exception1.class)
  @MayAliasLine(reason = "Exception1 b may a",
          lineNumber = 44, methodID = 0,
          secondLineNumber = 43, secondMethodID = 0,
          clazz = Exception1.class)
  public static void main(String[] args) {

    //Benchmark.alloc(1);
    A a = new A();
    A b = new A();

    try {
      b = a;
      throw new RuntimeException();

    } catch (RuntimeException e) {

        a.hashCode();
        b.hashCode();
        b.hashCode();

      //Benchmark.test("b",
      //    "{allocId:1, mayAlias:[a,b], notMayAlias:[], mustAlias:[a,b], notMustAlias:[]}");
    }

  }
}
