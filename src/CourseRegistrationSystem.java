import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Main rules for course lookup, registration and prerequisites. */
public class CourseRegistrationSystem {
    private HashMap<String, Course> courses;
    private HashMap<String, HashSet<String>> completedCourses;
    private HashMap<String, String> studentNames;
    private PrerequisiteGraph prerequisiteGraph;

    public CourseRegistrationSystem() {
        courses = new HashMap<String, Course>();
        completedCourses = new HashMap<String, HashSet<String>>();
        studentNames = new HashMap<String, String>();
        prerequisiteGraph = new PrerequisiteGraph();
    }

    public void addCourse(Course course, List<String> prerequisites) {
        String courseId = course.getId().toUpperCase();
        if (courses.containsKey(courseId)) {
            throw new IllegalArgumentException("Duplicate course ID: " + courseId);
        }
        courses.put(courseId, course);
        prerequisiteGraph.addCourse(courseId);

        for (String prerequisite : prerequisites) {
            prerequisiteGraph.addPrerequisite(courseId, prerequisite);
        }
    }

    public Course findCourse(String courseId) {
        if (courseId == null) return null;
        return courses.get(courseId.trim().toUpperCase());
    }

    /** Returns courses sorted by ID using bubble sort. */
    public List<Course> getCoursesSorted() {
        ArrayList<Course> list = new ArrayList<Course>(courses.values());
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = 0; j < list.size() - 1 - i; j++) {
                String leftId = list.get(j).getId();
                String rightId = list.get(j + 1).getId();
                if (leftId.compareTo(rightId) > 0) {
                    Course temp = list.get(j);
                    list.set(j, list.get(j + 1));
                    list.set(j + 1, temp);
                }
            }
        }
        return list;
    }

    public List<String> getCompletedRecords() {
        ArrayList<String> records = new ArrayList<String>();
        for (String studentKey : completedCourses.keySet()) {
            HashSet<String> completed = completedCourses.get(studentKey);
            String studentName = studentNames.get(studentKey);
            for (String courseId : completed) {
                records.add("C|" + studentName + "|" + courseId);
            }
        }
        return records;
    }

    public void restoreEnrollment(String courseId, String studentName) {
        Course course = findCourse(courseId);
        if (course == null) throw new IllegalArgumentException("Unknown course: " + courseId);
        if (course.getAvailableSeats() == 0) throw new IllegalArgumentException("Course is already full: " + courseId);
        if (!course.enroll(studentName)) throw new IllegalArgumentException("Duplicate student: " + studentName);
    }

    public void restoreWaitlist(String courseId, String studentName) {
        Course course = findCourse(courseId);
        if (course == null) throw new IllegalArgumentException("Unknown course: " + courseId);
        if (course.getAvailableSeats() > 0) throw new IllegalArgumentException("Course has an available seat: " + courseId);
        if (course.hasStudent(studentName)) throw new IllegalArgumentException("Duplicate student: " + studentName);
        course.addToWaitlist(studentName);
    }

    public void restoreCompletedCourse(String studentName, String courseId) {
        if (findCourse(courseId) == null) throw new IllegalArgumentException("Unknown course: " + courseId);
        saveCompletedCourse(studentName, courseId);
    }

    public String register(String studentName, String courseId) {
        String name = cleanStudentName(studentName);
        Course course = findCourse(courseId);
        if (course == null) return "Course " + courseId.trim().toUpperCase() + " not found.";
        if (course.hasStudent(name)) return name + " is already registered for " + course.getId() + ".";

        HashSet<String> studentCompleted = completedCourses.get(name.toLowerCase());
        if (studentCompleted == null) studentCompleted = new HashSet<String>();
        List<String> unmet = prerequisiteGraph.findUnmetPrerequisites(course.getId(), studentCompleted);
        if (unmet.size() > 0) {
            return "Finish these prerequisites first: " + joinNames(unmet) + ".";
        }

        if (course.getAvailableSeats() > 0) {
            course.enroll(name);
            return "Registration successful: " + name + " -> " + course.getId()
                    + "\nAvailable seats updated: " + course.getAvailableSeats();
        }

        course.addToWaitlist(name);
        return "Course full. " + name + " added to waiting queue.";
    }

    public String markCourseCompleted(String studentName, String courseId) {
        String name = cleanStudentName(studentName);
        Course course = findCourse(courseId);
        if (course == null) return "Course " + courseId.trim().toUpperCase() + " not found.";
        if (!course.hasEnrolledStudent(name)) {
            return name + " must be enrolled in " + course.getId() + " before completing it.";
        }

        saveCompletedCourse(name, course.getId());
        return name + " marked " + course.getId() + " as completed.";
    }

    public String drop(String studentName, String courseId) {
        String name = cleanStudentName(studentName);
        Course course = findCourse(courseId);
        if (course == null) return "Course " + courseId.trim().toUpperCase() + " not found.";

        if (course.hasWaitingStudent(name)) {
            course.removeFromWaitlist(name);
            return name + " was removed from the waiting queue.";
        }
        if (!course.removeEnrolled(name)) return name + " is not registered for " + course.getId() + ".";

        String nextStudent = course.takeNextFromWaitlist();
        if (nextStudent == null) {
            return name + " dropped " + course.getId() + ". Available seats: " + course.getAvailableSeats();
        }

        course.enroll(nextStudent);
        return name + " dropped " + course.getId() + ". " + nextStudent + " got the seat.";
    }

    private void saveCompletedCourse(String studentName, String courseId) {
        String key = studentName.toLowerCase();
        studentNames.put(key, studentName);
        HashSet<String> completed = completedCourses.get(key);
        if (completed == null) {
            completed = new HashSet<String>();
            completedCourses.put(key, completed);
        }
        completed.add(courseId.toUpperCase());
    }

    public static String cleanStudentName(String name) {
        if (name == null || name.trim().length() == 0 || name.contains("|") || name.contains("\n")) {
            throw new IllegalArgumentException("Enter a student name without the | character.");
        }
        return name.trim();
    }

    private String joinNames(List<String> names) {
        String result = "";
        for (int i = 0; i < names.size(); i++) {
            if (i > 0) result += ", ";
            result += names.get(i);
        }
        return result;
    }
}
