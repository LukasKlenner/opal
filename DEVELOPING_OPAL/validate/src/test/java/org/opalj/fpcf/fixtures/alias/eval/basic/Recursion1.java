/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias.eval.basic;

import org.opalj.fpcf.properties.alias.AliasMethodID;
import org.opalj.fpcf.properties.alias.line.MayAliasLine;
import org.opalj.fpcf.properties.alias.line.NoAliasLine;

/*
 * Testcases taken from: https://github.com/secure-software-engineering/PointerBench
 *
 * @testcase Recursion1
 * 
 * @version 1.0
 * 
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 * 
 * @description The analysis must support recursion
 */
public class Recursion1 {

  public Recursion1() {}

  public class N {
    public String value;
    public N next;

    public N(String value) {
      this.value = value;
      next = null;
    }
  }

  public N recursive(int i, N m) {
    if (i < 10) {
      int j = i + 1;
      return recursive(j, m.next);
    }
    return m;
  }

  @AliasMethodID(id = 0, clazz = Recursion1.class)
  @NoAliasLine(reason = "Recursion1 n no o",
          lineNumber = 70, methodID = 0,
          secondLineNumber = 72, secondMethodID = 0,
          clazz = Recursion1.class)
  @NoAliasLine(reason = "Recursion1 n no p",
          lineNumber = 70, methodID = 0,
          secondLineNumber = 73, secondMethodID = 0,
          clazz = Recursion1.class)
  @NoAliasLine(reason = "Recursion1 n no q",
          lineNumber = 70, methodID = 0,
          secondLineNumber = 74, secondMethodID = 0,
          clazz = Recursion1.class)
  @MayAliasLine(reason = "Recursion1 n may n",
          lineNumber = 70, methodID = 0,
          secondLineNumber = 71, secondMethodID = 0,
          clazz = Recursion1.class)
  public void test() {
    //Benchmark.alloc(1);
    N node = new N("");

    Recursion1 r1 = new Recursion1();
    N n = r1.recursive(0, node);

    N o = node.next;
    N p = node.next.next;
    N q = node.next.next.next;

    n.hashCode();
    n.hashCode();
    o.hashCode();
    p.hashCode();
    q.hashCode();

    //Benchmark.test("n",
    //    "{allocId:1, mayAlias:[n], notMayAlias:[o,p,q], mustAlias:[n], notMustAlias:[o,p,q]}");
  }

  public static void main(String[] args) {
    Recursion1 r1 = new Recursion1();
    r1.test();
  }
}
