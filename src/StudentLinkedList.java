import java.util.ArrayList;
import java.util.List;

/** A simple singly linked list of student names. */
public class StudentLinkedList {
    private Node head;
    private int count;

    public int size() {
        return count;
    }

    public boolean contains(String name) {
        Node current = head;
        while (current != null) {
            if (current.name.equalsIgnoreCase(name)) return true;
            current = current.next;
        }
        return false;
    }

    public boolean add(String name) {
        if (contains(name)) return false;

        Node newNode = new Node(name);
        if (head == null) {
            head = newNode;
        } else {
            Node current = head;
            while (current.next != null) current = current.next;
            current.next = newNode;
        }
        count++;
        return true;
    }

    public boolean remove(String name) {
        Node current = head;
        Node previous = null;

        while (current != null) {
            if (current.name.equalsIgnoreCase(name)) {
                if (previous == null) head = current.next;
                else previous.next = current.next;
                count--;
                return true;
            }
            previous = current;
            current = current.next;
        }
        return false;
    }

    public List<String> toList() {
        List<String> names = new ArrayList<String>();
        Node current = head;
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
