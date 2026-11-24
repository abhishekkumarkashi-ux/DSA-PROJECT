import java.util.ArrayList;
import java.util.List;

/** A simple linked FIFO queue for students waiting for a seat. */
public class StudentQueue {
    private Node front;
    private Node rear;

    public boolean contains(String name) {
        Node current = front;
        while (current != null) {
            if (current.name.equalsIgnoreCase(name)) return true;
            current = current.next;
        }
        return false;
    }

    public void enqueue(String name) {
        Node newNode = new Node(name);
        if (rear == null) {
            front = newNode;
            rear = newNode;
        } else {
            rear.next = newNode;
            rear = newNode;
        }
    }

    public String dequeue() {
        if (front == null) return null;

        String firstName = front.name;
        front = front.next;
        if (front == null) rear = null;
        return firstName;
    }

    public boolean remove(String name) {
        Node current = front;
        Node previous = null;
        while (current != null) {
            if (current.name.equalsIgnoreCase(name)) {
                if (previous == null) front = current.next;
                else previous.next = current.next;
                if (current == rear) rear = previous;
                if (front == null) rear = null;
                return true;
            }
            previous = current;
            current = current.next;
        }
        return false;
    }

    public List<String> toList() {
        List<String> names = new ArrayList<String>();
        Node current = front;
        while (current != null) {
            names.add(current.name);
            current = current.next;
        }
        return names;
    }

    private static class Node {
        String name;
        Node next;

        Node(String name) {
            this.name = name;
        }
    }
}
