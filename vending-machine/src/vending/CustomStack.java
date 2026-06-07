package vending;

/** Custom stack used for recent manager/audit messages. */
public class CustomStack<T> {
    private Node<T> top;
    private int size;
    private static class Node<T> { T value; Node<T> next; Node(T value) { this.value = value; } }
    public void push(T value) { Node<T> node = new Node<>(value); node.next = top; top = node; size++; }
    public T pop() { if (top == null) return null; T value = top.value; top = top.next; size--; return value; }
    public T peek() { return top == null ? null : top.value; }
    public int size() { return size; }
}
