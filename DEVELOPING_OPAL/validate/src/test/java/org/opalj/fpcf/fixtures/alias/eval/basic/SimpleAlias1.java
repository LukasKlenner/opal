/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias.eval.basic;

import org.opalj.fpcf.fixtures.alias.eval.objects.A;
import org.opalj.fpcf.properties.alias.AliasMethodID;
import org.opalj.fpcf.properties.alias.line.MustAliasLine;

/*
 * Testcases taken from: https://github.com/secure-software-engineering/PointerBench
 *
 * @testcase SimpleAlias1
 * 
 * @version 1.0
 * 
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 * 
 * @description Direct alias
 */
public class SimpleAlias1 {

  @AliasMethodID(id = 0, clazz = SimpleAlias1.class)
  @MustAliasLine(reason = "SimpleAlias1 b may b",
          lineNumber = 39, methodID = 0,
          secondLineNumber = 40, secondMethodID = 0,
          clazz = SimpleAlias1.class)
  @MustAliasLine(reason = "SimpleAlias1 b may a",
          lineNumber = 39, methodID = 0,
          secondLineNumber = 38, secondMethodID = 0,
          clazz = SimpleAlias1.class)
  public static void main(String[] args) {

    //Benchmark.alloc(1);
    A a = new A();

    A b = a;

    a.hashCode();
    b.hashCode();
    b.hashCode();

    //Benchmark.test("b",
    //    "{allocId:1, mayAlias:[a,b], notMayAlias:[], mustAlias:[a,b], notMustAlias:[]}");
  }
}
