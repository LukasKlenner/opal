/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias.eval.generalJava;

import org.opalj.fpcf.fixtures.alias.eval.objects.B;
import org.opalj.fpcf.fixtures.alias.eval.objects.A;
import org.opalj.fpcf.properties.alias.AliasMethodID;
import org.opalj.fpcf.properties.alias.line.NoAliasLine;

/*
 * Testcases taken from: https://github.com/secure-software-engineering/PointerBench
 *
 * @testcase Null1
 * 
 * @version 1.0
 * 
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 * 
 * @description Direct alias to null
 */
public class Null1 {

  @AliasMethodID(id = 0, clazz = Null1.class)
  @NoAliasLine(reason = "Null1 b no b",
          lineNumber = 40, methodID = 0,
          secondLineNumber = 41, secondMethodID = 0,
          clazz = Null1.class)
  @NoAliasLine(reason = "Null1 b no a",
          lineNumber = 40, methodID = 0,
          secondLineNumber = 39, secondMethodID = 0,
          clazz = Null1.class)
  public static void main(String[] args) {

    // No allocation site
    A h = new A();
    B a = h.getH();
    B b = a;

    a.hashCode();
    b.hashCode();
    b.hashCode();

    //Benchmark.test("b",
    //    "{NULLALLOC, mayAlias:[], notMayAlias:[b,a], mustAlias:[b,a], notMustAlias:[i]}");
    //Benchmark.use(b);
  }
}
