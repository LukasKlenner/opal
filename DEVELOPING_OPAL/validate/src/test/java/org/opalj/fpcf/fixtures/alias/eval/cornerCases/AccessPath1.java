/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias.eval.cornerCases;

import org.opalj.fpcf.fixtures.alias.eval.objects.A;
import org.opalj.fpcf.properties.alias.AliasMethodID;
import org.opalj.fpcf.properties.alias.line.MayAliasLine;

/*
 * Testcases taken from: https://github.com/secure-software-engineering/PointerBench
 *
 * @version 1.0
 * 
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 * 
 * @description Query for access paths
 */
public class AccessPath1 {

  @AliasMethodID(id = 0, clazz = AccessPath1.class)
  @MayAliasLine(reason = "AccessPath1 a.f may a.f",
          lineNumber = 37, fieldReference = true, fieldID = 0, fieldClass = A.class, methodID = 0,
          secondLineNumber = 38, secondFieldReference = true, secondFieldID = 0, secondFieldClass = A.class, secondMethodID = 0,
          clazz = AccessPath1.class)
  @MayAliasLine(reason = "AccessPath1 a.f may b.f",
          lineNumber = 37, fieldReference = true, fieldID = 0, fieldClass = A.class, methodID = 0,
          secondLineNumber = 39, secondFieldReference = true, secondFieldID = 0, secondFieldClass = A.class, secondMethodID = 0,
          clazz = AccessPath1.class)
  public static void main(String[] args) {

    //Benchmark.alloc(1);
    A a = new A();
    A b = new A();

    a.f = b.f;

    a.f.hashCode();
    a.f.hashCode();
    b.f.hashCode();

    //Benchmark
    //    .test("a.f",
    //        "{allocId:1, mayAlias:[a.f,b.f], notMayAlias:[a,b], mustAlias:[a.f,b.f], notMustAlias:[a,b]}");
  }
}
