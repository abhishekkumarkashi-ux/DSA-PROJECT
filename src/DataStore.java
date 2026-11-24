import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Reads and writes the plain-text course and registration data files. */
public final class DataStore {
    private static final Path COURSE_FILE = Path.of("courses.txt");
    private static final Path REGISTRATION_FILE = Path.of("registrations.txt");

    private DataStore() { }

    public static CourseRegistrationSystem load() throws IOException {
        createStarterFilesIfMissing();
        CourseRegistrationSystem system = new CourseRegistrationSystem();
        int lineNumber = 0;
        for (String line : Files.readAllLines(COURSE_FILE, StandardCharsets.UTF_8)) {
            lineNumber++;
            if (line.isBlank() || line.stripLeading().startsWith("#")) continue;
            String[] fields = line.split("\\|", -1);
            if (fields.length != 4) throw new IOException("Invalid courses.txt line " + lineNumber + ". Expected ID|title|capacity|prerequisites.");
            try {
                int capacity = Integer.parseInt(fields[2].trim());
                List<String> prerequisites = fields[3].isBlank() ? List.of()
                        : java.util.Arrays.stream(fields[3].split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
                system.addCourse(new Course(fields[0], fields[1], capacity), prerequisites);
            } catch (RuntimeException error) {
                throw new IOException("Invalid courses.txt line " + lineNumber + ": " + error.getMessage(), error);
            }
        }

        lineNumber = 0;
        for (String line : Files.readAllLines(REGISTRATION_FILE, StandardCharsets.UTF_8)) {
            lineNumber++;
            if (line.isBlank() || line.stripLeading().startsWith("#")) continue;
            String[] fields = line.split("\\|", -1);
            if (fields.length != 3) throw new IOException("Invalid registrations.txt line " + lineNumber + ". Expected E|student|course, W|student|course, or C|student|course.");
            try {
                switch (fields[0].trim().toUpperCase()) {
                    case "E" -> system.restoreEnrollment(fields[2], fields[1]);
                    case "W" -> system.restoreWaitlist(fields[2], fields[1]);
                    case "C" -> system.restoreCompletedCourse(fields[1], fields[2]);
                    default -> throw new IllegalArgumentException("Unknown record type " + fields[0]);
                }
            } catch (RuntimeException error) {
                throw new IOException("Invalid registrations.txt line " + lineNumber + ": " + error.getMessage(), error);
            }
        }
        return system;
    }

    public static void save(CourseRegistrationSystem system) throws IOException {
        List<String> lines = new java.util.ArrayList<>();
        lines.add("# E|student|course = enrolled, W|student|course = waiting, C|student|course = completed");
        for (Course course : system.getCoursesSorted()) {
            for (String student : course.getEnrolledStudents()) lines.add("E|" + student + "|" + course.getId());
            for (String student : course.getWaitingStudents()) lines.add("W|" + student + "|" + course.getId());
        }
        lines.addAll(system.getCompletedRecords());
        Files.write(REGISTRATION_FILE, lines, StandardCharsets.UTF_8);
    }

    private static void createStarterFilesIfMissing() throws IOException {
        if (!Files.exists(COURSE_FILE)) {
            Files.writeString(COURSE_FILE,
                    "# ID|title|capacity|prerequisite course IDs (comma separated)\n"
                    + "CS101|Introduction to Programming|2|\n"
                    + "CS102|Data Structures|2|CS101\n"
                    + "CS201|Algorithms|2|CS102\n"
                    + "MA101|Discrete Mathematics|2|\n",
                    StandardCharsets.UTF_8);
        }
        if (!Files.exists(REGISTRATION_FILE)) {
            Files.writeString(REGISTRATION_FILE,
                    "# E|student|course = enrolled, W|student|course = waiting, C|student|course = completed\n",
                    StandardCharsets.UTF_8);
        }
    }
}
