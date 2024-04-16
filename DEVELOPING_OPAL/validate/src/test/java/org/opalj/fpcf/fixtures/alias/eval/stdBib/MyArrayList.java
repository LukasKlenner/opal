/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias.eval.stdBib;

public class MyArrayList<E> {

    private E[] elementData;

    private int size;

    public MyArrayList(int size) {
        elementData = (E[]) new Object[size];
    }

    public void add(E e) {
        elementData[size++] = e;
    }

    public E get(int index) {
        return elementData[index];
    }

}
