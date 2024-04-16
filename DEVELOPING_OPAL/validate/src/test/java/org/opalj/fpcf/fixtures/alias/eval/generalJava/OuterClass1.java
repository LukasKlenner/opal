/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias.eval.generalJava;

import org.opalj.fpcf.fixtures.alias.eval.objects.A;
import org.opalj.fpcf.properties.alias.AliasMethodID;
import org.opalj.fpcf.properties.alias.line.MayAliasLine;
import org.opalj.fpcf.properties.alias.line.NoAliasLine;

/*
 * Testcases taken from: https://github.com/secure-software-engineering/PointerBench
 *
 * @testcase OuterClass1
 * 
 * @version 1.0
 * 
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 * 
 * @description Alias from method in inner class
 */
public class OuterClass1 {

  public OuterClass1() {}

  public class InnerClass {
    private A a;

    public InnerClass(A a) {
      this.a = a;
    }

    public void alias(A x) {
      this.a = x;
    }
  }

  @AliasMethodID(id = 0, clazz = OuterClass1.class)
  @MayAliasLine(reason = "OuterClass1 h may b",
          lineNumber = 65, methodID = 0,
          secondLineNumber = 64, secondMethodID = 0,
          clazz = OuterClass1.class)
  @MayAliasLine(reason = "OuterClass1 h may h",
          lineNumber = 65, methodID = 0,
          secondLineNumber = 65, secondMethodID = 0,
          clazz = OuterClass1.class)
  @NoAliasLine(reason = "OuterClass1 h no i",
          lineNumber = 65, methodID = 0,
          secondLineNumber = 66, secondMethodID = 0,
          clazz = OuterClass1.class)
  @NoAliasLine(reason = "OuterClass1 h no a",
          lineNumber = 65, methodID = 0,
          secondLineNumber = 63, secondMethodID = 0,
          clazz = OuterClass1.class)
  private void test() {
    //Benchmark.alloc(1);
    A a = new A();
    A b = new A();

    InnerClass i = new InnerClass(a);
    i.alias(b);
    A h = i.a;

    a.hashCode();
    b.hashCode();
    h.hashCode();
    i.hashCode();

    //Benchmark.test("h",
    //    "{allocId:1, mayAlias:[b,h], notMayAlias:[i,a], mustAlias:[b,a], notMustAlias:[i]}");
  }

  private static void main(String[] args) {
    OuterClass1 oc1 = new OuterClass1();
    oc1.test();
  }

}
