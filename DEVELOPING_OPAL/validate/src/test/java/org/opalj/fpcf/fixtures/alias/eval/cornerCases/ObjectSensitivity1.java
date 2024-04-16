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
 * @testcase ObjectSensitivity1
 * 
 * @version 1.0
 * 
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 * 
 * @description Object sensitive alias from caller object
 */
public class ObjectSensitivity1 {

  @AliasMethodID(id = 0, clazz = ObjectSensitivity1.class)
  @MayAliasLine(reason = "ObjectSensitivity1 b4 may b4",
          lineNumber = 66, methodID = 0,
          secondLineNumber = 67, secondMethodID = 0,
          clazz = ObjectSensitivity1.class)
  @MayAliasLine(reason = "ObjectSensitivity1 b4 may b2",
          lineNumber = 66, methodID = 0,
          secondLineNumber = 64, secondMethodID = 0,
          clazz = ObjectSensitivity1.class)
  @NoAliasLine(reason = "ObjectSensitivity1 b4 no a1",
          lineNumber = 66, methodID = 0,
          secondLineNumber = 61, secondMethodID = 0,
          clazz = ObjectSensitivity1.class)
  @NoAliasLine(reason = "ObjectSensitivity1 b4 no a2",
          lineNumber = 66, methodID = 0,
          secondLineNumber = 62, secondMethodID = 0,
          clazz = ObjectSensitivity1.class)
  @NoAliasLine(reason = "ObjectSensitivity1 b4 no b1",
          lineNumber = 66, methodID = 0,
          secondLineNumber = 63, secondMethodID = 0,
          clazz = ObjectSensitivity1.class)
  @NoAliasLine(reason = "ObjectSensitivity1 b4 no b3",
          lineNumber = 66, methodID = 0,
          secondLineNumber = 65, secondMethodID = 0,
          clazz = ObjectSensitivity1.class)
  public static void main(String[] args) {

    B b1 = new B();
    //Benchmark.alloc(1);
    B b2 = new B();

    A a1 = new A(b1);
    A a2 = new A(b2);

    B b3 = a1.getF();
    B b4 = a2.getF();

    a1.hashCode();
    a2.hashCode();
    b1.hashCode();
    b2.hashCode();
    b3.hashCode();
    b4.hashCode();
    b4.hashCode();

    //Benchmark
    //    .test(
    //        "b4",
    //        "{allocId:1, mayAlias:[b4,b2], notMayAlias:[a1,a2,b1,b3], mustAlias:[b4,b2], notMustAlias:[a1,a2,b1,b3]}");
  }
}
