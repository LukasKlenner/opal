/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias.eval.objects;

public class H implements I {
	// G and H implement I

	A a;

	public A foo(A a) {
		this.a = a;
		return a;
	}
}
