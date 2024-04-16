/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias.eval.cornerCases;

import org.opalj.fpcf.fixtures.alias.eval.objects.A;
import org.opalj.fpcf.fixtures.alias.eval.objects.B;
import org.opalj.fpcf.properties.alias.AliasMethodID;
import org.opalj.fpcf.properties.alias.line.MayAliasLine;
import org.opalj.fpcf.properties.alias.line.NoAliasLine;

/*
 * Testcases taken from: https://github.com/secure-software-engineering/PointerBench
 *
 * @testcase FieldSensitivity1
 * 
 * @version 1.0
 * 
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 * 
 * @description Field Sensitivity with static method
 */
public class FieldSensitivity1 {

  private static void assign(A x, A y) {
    y.f = x.f;
  }

  @AliasMethodID(id = 0, clazz = FieldSensitivity1.class)
  @MayAliasLine(reason = "FieldSensitivity1 d may b",
          lineNumber = 57, methodID = 0,
          secondLineNumber = 55, secondMethodID = 0,
          clazz = FieldSensitivity1.class)
  @MayAliasLine(reason = "FieldSensitivity1 d may d",
          lineNumber = 57, methodID = 0,
          secondLineNumber = 58, secondMethodID = 0,
          clazz = FieldSensitivity1.class)
  @NoAliasLine(reason = "FieldSensitivity1 d no a",
          lineNumber = 57, methodID = 0,
          secondLineNumber = 54, secondMethodID = 0,
          clazz = FieldSensitivity1.class)
  @NoAliasLine(reason = "FieldSensitivity1 d no c",
          lineNumber = 57, methodID = 0,
          secondLineNumber = 56, secondMethodID = 0,
          clazz = FieldSensitivity1.class)
  public static void main(String[] args) {

    //Benchmark.alloc(1);
    B b = new B();
    A a = new A(b);
    A c = new A();
    assign(a, c);
    B d = c.f;

    a.hashCode();
    b.hashCode();
    c.hashCode();
    d.hashCode();
    d.hashCode();

    //Benchmark.test("d",
    //    "{allocId:1, mayAlias:[d,b], notMayAlias:[a,c], mustAlias:[d,b], notMustAlias:[a,c]}");

  }

}
