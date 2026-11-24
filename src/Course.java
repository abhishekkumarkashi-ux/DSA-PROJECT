import java.util.List;

/** Stores one course, its enrolled students and its waiting students. */
public class Course {
    private String id;
    private String title;
    private int capacity;
    private StudentLinkedList enrolledStudents;
    private StudentQueue waitingStudents;

    public Course(String id, String title, int capacity) {
        if (capacity < 1) throw new IllegalArgumentException("Capacity must be at least 1.");
        this.id = id.trim().toUpperCase();
        this.title = title.trim();
        this.capacity = capacity;
        enrolledStudents = new StudentLinkedList();
        waitingStudents = new StudentQueue();
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public int getCapacity() { return capacity; }
    public int getAvailableSeats() { return capacity - enrolledStudents.size(); }

    public boolean hasStudent(String name) {
        return hasEnrolledStudent(name) || hasWaitingStudent(name);
    }

    public boolean hasEnrolledStudent(String name) {
        return enrolledStudents.contains(name);
    }

    public boolean hasWaitingStudent(String name) {
        return waitingStudents.contains(name);
    }

    public boolean enroll(String name) {
        return enrolledStudents.add(name);
    }

    public boolean removeEnrolled(String name) {
        return enrolledStudents.remove(name);
    }

    public void addToWaitlist(String name) {
        waitingStudents.enqueue(name);
    }

    public boolean removeFromWaitlist(String name) {
        return waitingStudents.remove(name);
    }

    public String takeNextFromWaitlist() {
        return waitingStudents.dequeue();
    }

    public List<String> getEnrolledStudents() {
        return enrolledStudents.toList();
    }

    public List<String> getWaitingStudents() {
        return waitingStudents.toList();
    }
}
