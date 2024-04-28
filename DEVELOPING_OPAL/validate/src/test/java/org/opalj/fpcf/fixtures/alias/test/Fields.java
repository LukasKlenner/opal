/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias.test;

import org.opalj.fpcf.properties.alias.AliasFieldID;

public class Fields {

    /*@MayAliasLine(reason = "Test",
            lineNumber = 20, fieldReference = true, fieldID = 0, fieldClass = Field.class, methodID = 0,
            secondLineNumber =23, secondFieldReference = true, secondFieldID = 0, secondFieldClass = Field.class, secondMethodID = 0,
            clazz = Fields.class)
    @AliasMethodID(id = 0, clazz = Fields.class)
    public void main() {
        //Object o = new Object();
        Field f1 = new Field();
        f1.a = new Object();

        f1.a.hashCode();

        f1.a = new Object();
        f1.a.hashCode();
        //f2.a.hashCode();

    }*/

}

class Field {

    @AliasFieldID(id = 0, clazz = Field.class)
    public Object a;

    /*public Field(Object a) {
        this.a = a;
    }*/

}
