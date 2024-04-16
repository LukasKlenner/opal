/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias.eval.stdBib;

import java.util.Iterator;

/**
 * A simple hash map implementation similar to the one in the standard library.
 */
public class MyHashMap<K, V> {

    static class Node<K, V> {
        K key;
        V value;

        Node<K, V> next;
    }

    private final Node<K, V>[] table;

    @SuppressWarnings("unchecked")
    public MyHashMap(int capacity) {
        table = (Node<K, V>[]) new Node[capacity];
    }

    public void put(K key, V value) {
        int index = key.hashCode() % table.length;
        Node<K, V> node = new Node<>();
        node.key = key;
        node.value = value;
        if (table[index] != null) {
            Node<K, V> current = table[index];
            while (current != null) {
                if (current.key.equals(key)) {
                    current.value = value;
                    return;
                }
                current = current.next;
            }
            node.next = table[index];
        }
        table[index] = node;
    }

    public V get(K key) {
        int index = key.hashCode() % table.length;
        Node<K, V> current = table[index];
        while (current != null) {
            if (current.key.equals(key)) {
                return current.value;
            }
            current = current.next;
        }
        return null;
    }

    public Iterator<K> keyIterator() {
        return new Iterator<K>() {
            private int index = 0;
            private Node<K, V> current = table[index];

            @Override
            public boolean hasNext() {
                while (index < table.length && current == null) {
                    current = table[index++];
                }
                return index < table.length;
            }

            @Override
            public K next() {
                K key = current.key;
                current = current.next;
                return key;
            }
        };
    }

}
