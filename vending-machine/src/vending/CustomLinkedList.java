package vending;

import java.util.Iterator;
import java.util.NoSuchElementException;

/** Custom singly linked list used for beverage inventory storage. */
public class CustomLinkedList<T> implements Iterable<T> {
    private Node<T> head;
    private Node<T> tail;
    private int size;

    private static class Node<T> {
        T value;
        Node<T> next;
        Node(T value) { this.value = value; }
    }

    public void add(T value) {
        Node<T> node = new Node<>(value);
        if (head == null) head = node; else tail.next = node;
        tail = node;
        size++;
    }

    public T get(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException("Invalid index: " + index);
        Node<T> current = head;
        for (int i = 0; i < index; i++) current = current.next;
        return current.value;
    }

    public int size() { return size; }

    public T findById(int id) {
        for (T value : this) {
            if (value instanceof Beverage b && b.getId() == id) return value;
        }
        return null;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private Node<T> current = head;
            public boolean hasNext() { return current != null; }
            public T next() {
                if (current == null) throw new NoSuchElementException();
                T value = current.value;
                current = current.next;
                return value;
            }
        };
    }
}
