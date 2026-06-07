package vending;

/** Custom queue used for pending socket synchronization events. */
public class CustomQueue<T> {
    private Node<T> head;
    private Node<T> tail;
    private int size;
    private static class Node<T> { T value; Node<T> next; Node(T value) { this.value = value; } }
    public synchronized void enqueue(T value) { Node<T> node = new Node<>(value); if (tail == null) head = node; else tail.next = node; tail = node; size++; notifyAll(); }
    public synchronized T dequeue() { if (head == null) return null; T value = head.value; head = head.next; if (head == null) tail = null; size--; return value; }
    public synchronized T waitAndDequeue() throws InterruptedException { while (head == null) wait(); return dequeue(); }
    public synchronized int size() { return size; }
}
