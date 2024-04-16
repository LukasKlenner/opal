/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias.eval.objects;

import org.opalj.fpcf.properties.alias.AliasFieldID;

public class A {

	// Object A with attributes of type B

	public int i = 5;

	@AliasFieldID(id = 0, clazz = A.class)
	public B f = new B();
	public B g = new B();
	public B h;

	public A() {
	}

	public A(B b) {
		this.f = b;
	}

	public B getF() {
		return f;
	}
	public B getH() {
		return h;
	}
	public B id(B b) {
		return b;
	}

}
