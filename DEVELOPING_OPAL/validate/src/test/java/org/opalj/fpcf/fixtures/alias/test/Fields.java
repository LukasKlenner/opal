/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias.test;

import org.opalj.fpcf.properties.alias.AliasMethodID;
import org.opalj.fpcf.properties.alias.line.MayAliasLine;

public class Fields {

    @AliasMethodID(id = 0, clazz = Fields.class)
    @MayAliasLine(reason = "Fields a may b",
            lineNumber = 19, methodID = 0,
            secondLineNumber = 20, secondMethodID = 0,
            clazz = Fields.class)
    public void test() {

        FieldClass a = new FieldClass();
        FieldClass b = new FieldClass();

        a.hashCode();
        b.hashCode();

        a.fc = new FieldClass();

        m(a.f);
        b.f.hashCode();
        a.fc.f.hashCode();

        m(FieldClass.sf);
    }

    public void m(Object o) {}
}

class FieldClass {
    public Object f = new Object();
    public FieldClass fc;

    static Object sf = new Object();
}
