/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias.eval.cornerCases;

import org.opalj.fpcf.fixtures.alias.eval.objects.A;
import org.opalj.fpcf.properties.alias.AliasMethodID;
import org.opalj.fpcf.properties.alias.line.MayAliasLine;
import org.opalj.fpcf.properties.alias.line.NoAliasLine;

/*
 * Testcases taken from: https://github.com/secure-software-engineering/PointerBench
 *
 * @testcase FlowSensitivity1
 * 
 * @version 1.0
 * 
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 * 
 * @description Is the analysis flow-sensitive?
 */
public class FlowSensitivity1 {

  @AliasMethodID(id = 0, clazz = FlowSensitivity1.class)
  @MayAliasLine(reason = "FlowSensitivity1 b must b",
          lineNumber = 39, methodID = 0,
          secondLineNumber = 40, secondMethodID = 0,
          clazz = FlowSensitivity1.class)
  @NoAliasLine(reason = "FlowSensitivity1 b no a",
          lineNumber = 39, methodID = 0,
          secondLineNumber = 38, secondMethodID = 0,
          clazz = FlowSensitivity1.class)
  public static void main(String[] args) {

    A a = new A();
    //Benchmark.alloc(1);
    A b = new A();

    a.hashCode();
    b.hashCode();
    b.hashCode();

    //Benchmark.test("b",
    //    "{allocId:1, mayAlias:[b], notMayAlias:[a], mustAlias:[b], notMustAlias:[a]}");

    b = a;

    b.hashCode();
  }
}
