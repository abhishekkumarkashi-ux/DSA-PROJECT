import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Application logic and HashMap-based course lookup. */
public final class CourseRegistrationSystem {
    private final Map<String, Course> courses = new HashMap<>();
    private final Map<String, Set<String>> completedByStudent = new HashMap<>();
    private final Map<String, String> studentNames = new HashMap<>();
    private final PrerequisiteGraph prerequisiteGraph = new PrerequisiteGraph();

    public void addCourse(Course course, List<String> prerequisites) {
        String id = course.getId().toUpperCase();
        if (courses.putIfAbsent(id, course) != null) {
            throw new IllegalArgumentException("Duplicate course ID: " + id);
        }
        prerequisiteGraph.addCourse(id);
        for (String prerequisite : prerequisites) prerequisiteGraph.addPrerequisite(id, prerequisite);
    }

    public Course getCourse(String courseId) { return courses.get(courseId.trim().toUpperCase()); }

    public List<Course> getCoursesSorted() {
        List<Course> result = new ArrayList<>(courses.values());
        result.sort(Comparator.comparing(Course::getId));
        return result;
    }

    public List<String> getCompletedRecords() {
        List<String> records = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : completedByStudent.entrySet()) {
            String name = studentNames.getOrDefault(entry.getKey(), entry.getKey());
            for (String courseId : entry.getValue()) records.add("C|" + name + "|" + courseId);
        }
        records.sort(String::compareToIgnoreCase);
        return records;
    }

    public void restoreEnrollment(String courseId, String studentName) {
        Course course = getCourse(courseId);
        if (course == null) throw new IllegalArgumentException("Registration references unknown course " + courseId);
        if (!course.enroll(studentName)) throw new IllegalArgumentException("Duplicate enrollment in " + courseId);
        if (course.getAvailableSeats() < 0) throw new IllegalArgumentException("Capacity exceeded for " + courseId);
    }

    public void restoreWaitlist(String courseId, String studentName) {
        Course course = getCourse(courseId);
        if (course == null) throw new IllegalArgumentException("Waitlist references unknown course " + courseId);
        if (course.getAvailableSeats() > 0) throw new IllegalArgumentException("Waitlist exists while seats are available for " + courseId);
        if (course.hasStudent(studentName) || !course.addToWaitlist(studentName)) {
            throw new IllegalArgumentException("Duplicate student in course " + courseId);
        }
    }

    public void restoreCompletedCourse(String studentName, String courseId) {
        if (getCourse(courseId) == null) throw new IllegalArgumentException("Completion references unknown course " + courseId);
        String key = studentName.toLowerCase();
        studentNames.putIfAbsent(key, studentName);
        completedByStudent.computeIfAbsent(key, ignored -> new HashSet<>())
                .add(courseId.toUpperCase());
    }

    public String register(String studentName, String courseId) {
        String name = cleanStudentName(studentName);
        Course course = getCourse(courseId);
        if (course == null) return "Course " + courseId.trim().toUpperCase() + " not found.";
        if (course.hasStudent(name)) return name + " is already registered for " + course.getId() + ".";

        List<String> unmet = prerequisiteGraph.findUnmetPrerequisites(
                course.getId(), completedByStudent.getOrDefault(name.toLowerCase(), Set.of()));
        if (!unmet.isEmpty()) {
            return "Prerequisites not completed for " + course.getId() + ": " + String.join(", ", unmet) + ".";
        }

        if (course.getAvailableSeats() > 0) {
            course.enroll(name);
            return "Registration successful: " + name + " -> " + course.getId()
                    + System.lineSeparator() + "Available seats updated: " + course.getAvailableSeats();
        }
        course.addToWaitlist(name);
        return "Course full. " + name + " added to waiting queue.";
    }

    public String markCourseCompleted(String studentName, String courseId) {
        String name = cleanStudentName(studentName);
        Course course = getCourse(courseId);
        if (course == null) return "Course " + courseId.trim().toUpperCase() + " not found.";
        if (!course.hasEnrolledStudent(name)) return name + " must be enrolled in " + course.getId() + " before completing it.";
        String key = name.toLowerCase();
        studentNames.putIfAbsent(key, name);
        completedByStudent.computeIfAbsent(key, ignored -> new HashSet<>()).add(course.getId());
        return name + " marked " + course.getId() + " as completed.";
    }

    public String drop(String studentName, String courseId) {
        String name = cleanStudentName(studentName);
        Course course = getCourse(courseId);
        if (course == null) return "Course " + courseId.trim().toUpperCase() + " not found.";
        if (course.hasWaitingStudent(name)) {
            course.removeWaitingStudent(name);
            return course.getId() + ": " + name + " removed from the waiting queue.";
        }
        if (!course.removeEnrolled(name)) return name + " is not registered for " + course.getId() + ".";
        String promoted = course.takeNextFromWaitlist();
        if (promoted == null) return name + " dropped " + course.getId() + ". Available seats: " + course.getAvailableSeats();
        course.enroll(promoted);
        return name + " dropped " + course.getId() + ". " + promoted + " promoted from the waiting queue.";
    }

    public void addRestoredCompleted(String studentName, String courseId) { restoreCompletedCourse(studentName, courseId); }

    public static String cleanStudentName(String name) {
        if (name == null || name.trim().isEmpty() || name.contains("|") || name.contains("\n")) {
            throw new IllegalArgumentException("Enter a student name without the | character.");
        }
        return name.trim();
    }
}
