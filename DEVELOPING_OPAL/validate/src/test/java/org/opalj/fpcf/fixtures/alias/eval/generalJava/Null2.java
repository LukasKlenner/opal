/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias.eval.generalJava;

import org.opalj.fpcf.fixtures.alias.eval.objects.A;
import org.opalj.fpcf.fixtures.alias.eval.objects.B;
import org.opalj.fpcf.properties.alias.AliasMethodID;
import org.opalj.fpcf.properties.alias.line.NoAliasLine;

/*
 * Testcases taken from: https://github.com/secure-software-engineering/PointerBench
 *
 * @testcase Null2
 * @version 1.0
 * @author Johannes Sp�th, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer Institute SIT)
 * 
 * @description Implicit alias to null
 * 
 */
public class Null2 {

	@AliasMethodID(id = 0, clazz = Null2.class)
	@NoAliasLine(reason = "Null2 x no a",
			lineNumber = 39, methodID = 0,
			secondLineNumber = 37, secondMethodID = 0,
			clazz = Null2.class)
	@NoAliasLine(reason = "Null2 x no b",
			lineNumber = 39, methodID = 0,
			secondLineNumber = 38, secondMethodID = 0,
			clazz = Null2.class)
	public static void main(String[] args) {

		// No allocation site
		A a = new A();
		A b = a;
		B x = b.h; // a.h is null

		a.hashCode();
		b.hashCode();
		x.hashCode();

		//Benchmark
		//		.test("x",
		//				"{NULLALLOC, mayAlias:[], notMayAlias:[b,a], mustAlias:[b,a], notMustAlias:[i]}");
	}
}
