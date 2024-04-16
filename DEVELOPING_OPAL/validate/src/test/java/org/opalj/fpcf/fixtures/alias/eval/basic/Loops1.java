/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias.eval.basic;

import org.opalj.fpcf.properties.alias.AliasMethodID;
import org.opalj.fpcf.properties.alias.line.MayAliasLine;
import org.opalj.fpcf.properties.alias.line.NoAliasLine;

/*
 * Testcases taken from: https://github.com/secure-software-engineering/PointerBench
 *
 * @testcase Loops1
 * 
 * @version 1.0
 * 
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 * 
 * @description The analysis must support loop constructs. No allocation site in N
 */
public class Loops1 {

//  public class N {
//    public String value = "";
//    public N next;
//
//    public N() {
//      next = null;
//    }
//  }
//
//  @AliasMethodID(id = 0, clazz = Loops1.class)
//  @MayAliasLine(reason = "Loops1 node may node",
//          lineNumber = 66, methodID = 0,
//          secondLineNumber = 67, secondMethodID = 0,
//          clazz = Loops1.class)
//  @NoAliasLine(reason = "Loops1 node no i",
//          lineNumber = 66, methodID = 0,
//          secondLineNumber = 68, secondMethodID = 0,
//          clazz = Loops1.class)
//  @NoAliasLine(reason = "Loops1 node no o",
//          lineNumber = 66, methodID = 0,
//          secondLineNumber = 69, secondMethodID = 0,
//          clazz = Loops1.class)
//  @NoAliasLine(reason = "Loops1 node no p",
//          lineNumber = 66, methodID = 0,
//          secondLineNumber = 70, secondMethodID = 0,
//          clazz = Loops1.class)
//  @NoAliasLine(reason = "Loops1 node no q",
//          lineNumber = 66, methodID = 0,
//          secondLineNumber = 71, secondMethodID = 0,
//          clazz = Loops1.class)
//  private void test() {
//    //Benchmark.alloc(1);
//    N node = new N();
//
//    Integer i = 0;
//    while (i < 10) {
//      node = node.next;
//      i++;
//    }
//
//    N o = node.next;
//    N p = node.next.next;
//    N q = node.next.next.next;
//
//    node.hashCode();
//    node.hashCode();
//    i.hashCode();
//    o.hashCode();
//    p.hashCode();
//    q.hashCode();
//
//    //Benchmark
//    //    .test("node",
//    //        "{allocId:1, mayAlias:[node], notMayAlias:[i,o,p,q], mustAlias:[node], notMustAlias:[i,o,p,q]}");
//  }
//
//  public static void main(String[] args) {
//    Loops1 l1 = new Loops1();
//    l1.test();
//  }
}
