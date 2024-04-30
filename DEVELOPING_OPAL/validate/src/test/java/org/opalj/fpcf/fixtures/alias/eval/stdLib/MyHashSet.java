/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias.eval.stdLib;

import java.util.Iterator;

/**
 * A simple hash map implementation similar to the one in the standard library.
 */
public class MyHashSet<E> implements Iterable<E> {

    private final MyHashMap<E, Object> map;

    private static final Object DUMMY = new Object();

    public MyHashSet(int capacity) {
        map = new MyHashMap<>(capacity);
    }

    public void add(E e) {
        map.put(e, DUMMY);
    }

    @Override
    public Iterator<E> iterator() {
        return map.keyIterator();
    }
}
