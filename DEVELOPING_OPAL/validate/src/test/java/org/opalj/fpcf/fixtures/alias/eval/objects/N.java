/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias.eval.objects;


public class N {
	public String value = "";
	public N next;

	public N() {
		//Benchmark.alloc(2);
		next = new N();
	}
}
