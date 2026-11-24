import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/** Console entry point for the College Course Registration System. */
public final class Main {
    private static CourseRegistrationSystem system;

    private Main() { }

    public static void main(String[] args) {
        try {
            system = DataStore.load();
        } catch (IOException error) {
            System.err.println("Could not load project data: " + error.getMessage());
            return;
        }

        System.out.println("College Course Registration System");
        System.out.println("Loaded " + system.getCoursesSorted().size() + " courses.");
        try (Scanner input = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                printMenu();
                System.out.print("Choose an option: ");
                String choice = input.nextLine().trim();
                try {
                    switch (choice) {
                        case "1" -> register(input);
                        case "2" -> completeCourse(input);
                        case "3" -> drop(input);
                        case "4" -> showCourses();
                        case "5" -> showStudent(input);
                        case "6" -> running = false;
                        default -> System.out.println("Choose a number from 1 to 6.");
                    }
                } catch (IllegalArgumentException error) {
                    System.out.println(error.getMessage());
                }
                System.out.println();
            }
        }

        try {
            DataStore.save(system);
            System.out.println("Data saved to registrations.txt. Goodbye.");
        } catch (IOException error) {
            System.err.println("Could not save registrations.txt: " + error.getMessage());
        }
    }

    private static void printMenu() {
        System.out.println("1. Register for a course");
        System.out.println("2. Mark an enrolled course completed");
        System.out.println("3. Drop a course or leave its waiting queue");
        System.out.println("4. List courses and seats");
        System.out.println("5. Show a student's registrations");
        System.out.println("6. Save and exit");
    }

    private static void register(Scanner input) {
        System.out.print("Student name: ");
        String student = input.nextLine();
        System.out.print("Course ID: ");
        String course = input.nextLine();
        System.out.println(system.register(student, course));
    }

    private static void completeCourse(Scanner input) {
        System.out.print("Student name: ");
        String student = input.nextLine();
        System.out.print("Course ID: ");
        String course = input.nextLine();
        System.out.println(system.markCourseCompleted(student, course));
    }

    private static void drop(Scanner input) {
        System.out.print("Student name: ");
        String student = input.nextLine();
        System.out.print("Course ID: ");
        String course = input.nextLine();
        System.out.println(system.drop(student, course));
    }

    private static void showCourses() {
        System.out.printf("%-8s %-32s %s%n", "ID", "COURSE", "SEATS");
        for (Course course : system.getCoursesSorted()) {
            System.out.printf("%-8s %-32s %d/%d available%n", course.getId(), course.getTitle(),
                    course.getAvailableSeats(), course.getCapacity());
            System.out.println("         Enrolled: " + display(course.getEnrolledStudents()));
            System.out.println("         Waiting:  " + display(course.getWaitingStudents()));
        }
    }

    private static void showStudent(Scanner input) {
        System.out.print("Student name: ");
        String name = CourseRegistrationSystem.cleanStudentName(input.nextLine());
        boolean found = false;
        for (Course course : system.getCoursesSorted()) {
            if (course.hasEnrolledStudent(name)) {
                System.out.println(course.getId() + " - enrolled");
                found = true;
            } else if (course.hasWaitingStudent(name)) {
                System.out.println(course.getId() + " - waiting");
                found = true;
            }
        }
        if (!found) System.out.println("No current registrations for " + name + ".");
    }

    private static String display(List<String> names) { return names.isEmpty() ? "-" : String.join(", ", names); }

}
