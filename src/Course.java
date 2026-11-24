import java.util.ArrayList;
import java.util.List;

/** A course and the data structures that hold its enrolled and waiting students. */
public final class Course {
    private final String id;
    private final String title;
    private final int capacity;
    private final StudentLinkedList enrolledStudents = new StudentLinkedList();
    private final StudentQueue waitingStudents = new StudentQueue();

    public Course(String id, String title, int capacity) {
        this.id = id.trim().toUpperCase();
        this.title = title.trim();
        if (capacity < 1) throw new IllegalArgumentException("Course capacity must be at least 1.");
        this.capacity = capacity;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public int getCapacity() { return capacity; }
    public int getAvailableSeats() { return capacity - enrolledStudents.size(); }
    public List<String> getEnrolledStudents() { return enrolledStudents.toList(); }
    public List<String> getWaitingStudents() { return waitingStudents.toList(); }
    public boolean hasStudent(String name) {
        return enrolledStudents.contains(name) || waitingStudents.contains(name);
    }
    public boolean hasEnrolledStudent(String name) { return enrolledStudents.contains(name); }
    public boolean hasWaitingStudent(String name) { return waitingStudents.contains(name); }
    public boolean removeWaitingStudent(String name) { return waitingStudents.remove(name); }
    public boolean enroll(String name) { return enrolledStudents.add(name); }
    public boolean removeEnrolled(String name) { return enrolledStudents.remove(name); }
    public boolean addToWaitlist(String name) { return waitingStudents.enqueue(name); }
    public String takeNextFromWaitlist() { return waitingStudents.dequeue(); }

    /** Custom singly linked list: keeps the course roster in registration order. */
    private static final class StudentLinkedList {
        private Node head;
        private int size;

        boolean contains(String name) {
            for (Node node = head; node != null; node = node.next) {
                if (node.value.equalsIgnoreCase(name)) return true;
            }
            return false;
        }

        boolean add(String name) {
            if (contains(name)) return false;
            Node added = new Node(name);
            if (head == null) head = added;
            else {
                Node tail = head;
                while (tail.next != null) tail = tail.next;
                tail.next = added;
            }
            size++;
            return true;
        }

        boolean remove(String name) {
            Node previous = null;
            Node current = head;
            while (current != null) {
                if (current.value.equalsIgnoreCase(name)) {
                    if (previous == null) head = current.next;
                    else previous.next = current.next;
                    size--;
                    return true;
                }
                previous = current;
                current = current.next;
            }
            return false;
        }

        int size() { return size; }

        List<String> toList() {
            List<String> result = new ArrayList<>();
            for (Node node = head; node != null; node = node.next) result.add(node.value);
            return result;
        }

        private static final class Node {
            final String value;
            Node next;
            Node(String value) { this.value = value; }
        }
    }

    /** Custom FIFO linked queue for students waiting for a seat. */
    private static final class StudentQueue {
        private Node front;
        private Node rear;

        boolean contains(String name) {
            for (Node node = front; node != null; node = node.next) {
                if (node.value.equalsIgnoreCase(name)) return true;
            }
            return false;
        }

        boolean enqueue(String name) {
            if (contains(name)) return false;
            Node added = new Node(name);
            if (rear == null) front = rear = added;
            else {
                rear.next = added;
                rear = added;
            }
            return true;
        }

        String dequeue() {
            if (front == null) return null;
            String first = front.value;
            front = front.next;
            if (front == null) rear = null;
            return first;
        }

        boolean remove(String name) {
            Node previous = null;
            Node current = front;
            while (current != null) {
                if (current.value.equalsIgnoreCase(name)) {
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

        List<String> toList() {
            List<String> result = new ArrayList<>();
            for (Node node = front; node != null; node = node.next) result.add(node.value);
            return result;
        }

        private static final class Node {
            final String value;
            Node next;
            Node(String value) { this.value = value; }
        }
    }
}
