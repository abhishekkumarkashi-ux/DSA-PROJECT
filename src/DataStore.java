import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/** Loads and saves courses and registrations in text files. */
public class DataStore {
    private static final Path COURSE_FILE = Paths.get("courses.txt");
    private static final Path REGISTRATION_FILE = Paths.get("registrations.txt");

    public static CourseRegistrationSystem load() throws IOException {
        makeStarterFiles();
        CourseRegistrationSystem system = new CourseRegistrationSystem();
        loadCourses(system);
        loadRegistrations(system);
        return system;
    }

    private static void loadCourses(CourseRegistrationSystem system) throws IOException {
        List<String> lines = Files.readAllLines(COURSE_FILE, StandardCharsets.UTF_8);

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.length() == 0 || line.startsWith("#")) continue;

            String[] fields = line.split("\\|", -1);
            if (fields.length != 4) {
                throw new IOException("Bad line " + (i + 1) + " in courses.txt.");
            }

            try {
                String id = fields[0].trim();
                String title = fields[1].trim();
                int capacity = Integer.parseInt(fields[2].trim());
                List<String> prerequisites = parsePrerequisites(fields[3]);
                system.addCourse(new Course(id, title, capacity), prerequisites);
            } catch (RuntimeException error) {
                throw new IOException("Check line " + (i + 1) + " in courses.txt: " + error.getMessage());
            }
        }
    }

    private static List<String> parsePrerequisites(String text) {
        ArrayList<String> prerequisites = new ArrayList<String>();
        if (text.trim().length() == 0) return prerequisites;

        String[] ids = text.split(",");
        for (String id : ids) {
            if (id.trim().length() > 0) prerequisites.add(id.trim());
        }
        return prerequisites;
    }

    private static void loadRegistrations(CourseRegistrationSystem system) throws IOException {
        List<String> lines = Files.readAllLines(REGISTRATION_FILE, StandardCharsets.UTF_8);

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.length() == 0 || line.startsWith("#")) continue;

            String[] fields = line.split("\\|", -1);
            if (fields.length != 3) {
                throw new IOException("Bad line " + (i + 1) + " in registrations.txt.");
            }

            String type = fields[0].trim().toUpperCase();
            String student = fields[1].trim();
            String course = fields[2].trim();
            try {
                if (type.equals("E")) {
                    system.restoreEnrollment(course, student);
                } else if (type.equals("W")) {
                    system.restoreWaitlist(course, student);
                } else if (type.equals("C")) {
                    system.restoreCompletedCourse(student, course);
                } else {
                    throw new IllegalArgumentException("Unknown record type: " + type);
                }
            } catch (RuntimeException error) {
                throw new IOException("Check line " + (i + 1) + " in registrations.txt: " + error.getMessage());
            }
        }
    }

    public static void save(CourseRegistrationSystem system) throws IOException {
        ArrayList<String> lines = new ArrayList<String>();
        lines.add("# E|student|course = enrolled, W|student|course = waiting, C|student|course = completed");

        List<Course> courses = system.getCoursesSorted();
        for (Course course : courses) {
            for (String student : course.getEnrolledStudents()) {
                lines.add("E|" + student + "|" + course.getId());
            }
            for (String student : course.getWaitingStudents()) {
                lines.add("W|" + student + "|" + course.getId());
            }
        }

        lines.addAll(system.getCompletedRecords());
        Files.write(REGISTRATION_FILE, lines, StandardCharsets.UTF_8);
    }

    private static void makeStarterFiles() throws IOException {
        if (!Files.exists(COURSE_FILE)) {
            ArrayList<String> courses = new ArrayList<String>();
            courses.add("# ID|title|capacity|prerequisite IDs");
            courses.add("CS101|Introduction to Programming|2|");
            courses.add("CS102|Data Structures|2|CS101");
            courses.add("CS201|Algorithms|2|CS102");
            courses.add("MA101|Discrete Mathematics|2|");
            Files.write(COURSE_FILE, courses, StandardCharsets.UTF_8);
        }

        if (!Files.exists(REGISTRATION_FILE)) {
            ArrayList<String> registrations = new ArrayList<String>();
            registrations.add("# E|student|course = enrolled, W|student|course = waiting, C|student|course = completed");
            Files.write(REGISTRATION_FILE, registrations, StandardCharsets.UTF_8);
        }
    }
}
