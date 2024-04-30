/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package org.opalj.fpcf.fixtures.alias.eval.stdLib;

/**
 * A simple linked list implementation similar to the one in the standard library.
 */
public class MyLinkedList<E> {

    class Node {
        E data;
        Node next;
    }

    Node head = null;

    public void add(E data) {
        Node newNode = new Node();
        newNode.data = data;
        newNode.next = head;

        if (head == null) {
            head = newNode;
            return;
        }

        Node current = head;
        while (current.next != null) {
            current = current.next;
        }

        current.next = newNode;
    }

    public E get(int index) {
        Node current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.data;
    }

}
