/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias.eval.generalJava;

import org.opalj.fpcf.fixtures.alias.eval.objects.A;
import org.opalj.fpcf.fixtures.alias.eval.objects.G;
import org.opalj.fpcf.fixtures.alias.eval.objects.H;
import org.opalj.fpcf.properties.alias.AliasMethodID;
import org.opalj.fpcf.properties.alias.line.MayAliasLine;
import org.opalj.fpcf.properties.alias.line.NoAliasLine;


/*
 * Testcases taken from: https://github.com/secure-software-engineering/PointerBench
 *
 * @testcase Interface1
 * 
 * @version 1.0
 * 
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 * 
 * @description Alias from method in interface
 */
public class Interface1 {

  @AliasMethodID(id = 0, clazz = Interface1.class)
  @NoAliasLine(reason = "Interface1 c no a",
          lineNumber = 60, methodID = 0,
          secondLineNumber = 58, secondMethodID = 0,
          clazz = Interface1.class)
  @NoAliasLine(reason = "Interface1 c no g",
          lineNumber = 60, methodID = 0,
          secondLineNumber = 62, secondMethodID = 0,
          clazz = Interface1.class)
  @NoAliasLine(reason = "Interface1 c no h",
          lineNumber = 60, methodID = 0,
          secondLineNumber = 63, secondMethodID = 0,
          clazz = Interface1.class)
  @MayAliasLine(reason = "Interface1 c may c",
          lineNumber = 60, methodID = 0,
          secondLineNumber = 61, secondMethodID = 0,
          clazz = Interface1.class)
  @MayAliasLine(reason = "Interface1 c may b",
          lineNumber = 60, methodID = 0,
          secondLineNumber = 59, secondMethodID = 0,
          clazz = Interface1.class)
  public static void main(String[] args) {

    A a = new A();
    //Benchmark.alloc(1);
    A b = new A();

    G g = new G();
    H h = new H();
    g.foo(a);
    A c = h.foo(b);

    a.hashCode();
    b.hashCode();
    c.hashCode();
    c.hashCode();
    g.hashCode();
    h.hashCode();

    //Benchmark.test("c",
    //    "{allocId:1, mayAlias:[c,b], notMayAlias:[a,g,h], mustAlias:[c,b], notMustAlias:[a,g,h]}");

    //Benchmark.use(c);
  }

}
