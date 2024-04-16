/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias.eval.cornerCases;

import org.opalj.fpcf.fixtures.alias.eval.objects.A;
import org.opalj.fpcf.fixtures.alias.eval.objects.B;
import org.opalj.fpcf.properties.alias.AliasMethodID;
import org.opalj.fpcf.properties.alias.line.MayAliasLine;
import org.opalj.fpcf.properties.alias.line.NoAliasLine;

/*
 * @testcase ObjectSensitivity2
 * 
 * @version 1.0
 * 
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 * 
 * @description Object sensitive alias from parameter object
 */
public class ObjectSensitivity2 {

  @AliasMethodID(id = 0, clazz = ObjectSensitivity2.class)
  @MayAliasLine(reason = "ObjectSensitivity2 b4 may b4",
          lineNumber = 58, methodID = 0,
          secondLineNumber = 59, secondMethodID = 0,
          clazz = ObjectSensitivity2.class)
  @MayAliasLine(reason = "ObjectSensitivity2 b4 may b2",
          lineNumber = 58, methodID = 0,
          secondLineNumber = 56, secondMethodID = 0,
          clazz = ObjectSensitivity2.class)
  @NoAliasLine(reason = "ObjectSensitivity2 b4 no a",
          lineNumber = 58, methodID = 0,
          secondLineNumber = 54, secondMethodID = 0,
          clazz = ObjectSensitivity2.class)
  @NoAliasLine(reason = "ObjectSensitivity2 b4 no b1",
          lineNumber = 58, methodID = 0,
          secondLineNumber = 55, secondMethodID = 0,
          clazz = ObjectSensitivity2.class)
  @NoAliasLine(reason = "ObjectSensitivity2 b4 no b3",
          lineNumber = 58, methodID = 0,
          secondLineNumber = 57, secondMethodID = 0,
          clazz = ObjectSensitivity2.class)
  public static void main(String[] args) {

    B b1 = new B();
    //Benchmark.alloc(1);
    B b2 = new B();

    A a = new A();

    B b3 = a.id(b1);
    B b4 = a.id(b2);

    a.hashCode();
    b1.hashCode();
    b2.hashCode();
    b3.hashCode();
    b4.hashCode();
    b4.hashCode();

    //Benchmark
    //    .test("b4",
    //        "{allocId:1, mayAlias:[b4,b2], notMayAlias:[a,b1,b3], mustAlias:[b4,b2], notMustAlias:[a,b1,b3]}");
  }
}
