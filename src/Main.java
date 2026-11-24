import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/** Starts the College Course Registration System. */
public class Main {
    private static CourseRegistrationSystem system;

    public static void main(String[] args) {
        try {
            system = DataStore.load();
        } catch (IOException error) {
            System.out.println("Could not load the data files: " + error.getMessage());
            return;
        }

        System.out.println("College Course Registration System");
        System.out.println("Loaded " + system.getCoursesSorted().size() + " courses.");

        Scanner input = new Scanner(System.in);
        boolean running = true;

        while (running) {
            showMenu();
            System.out.print("Choose an option: ");
            String choice = input.nextLine().trim();

            try {
                switch (choice) {
                    case "1":
                        registerStudent(input);
                        break;
                    case "2":
                        completeCourse(input);
                        break;
                    case "3":
                        dropCourse(input);
                        break;
                    case "4":
                        showCourses();
                        break;
                    case "5":
                        showStudent(input);
                        break;
                    case "6":
                        running = false;
                        break;
                    default:
                        System.out.println("Choose a number from 1 to 6.");
                }
            } catch (IllegalArgumentException error) {
                System.out.println(error.getMessage());
            }
            System.out.println();
        }

        try {
            DataStore.save(system);
            System.out.println("Data saved. Goodbye.");
        } catch (IOException error) {
            System.out.println("Could not save the data: " + error.getMessage());
        }
        input.close();
    }

    private static void showMenu() {
        System.out.println("1. Register for a course");
        System.out.println("2. Mark a course completed");
        System.out.println("3. Drop a course");
        System.out.println("4. Show all courses");
        System.out.println("5. Show one student's courses");
        System.out.println("6. Save and exit");
    }

    private static void registerStudent(Scanner input) {
        System.out.print("Student name: ");
        String name = input.nextLine();
        System.out.print("Course ID: ");
        String courseId = input.nextLine();
        printMessage(system.register(name, courseId));
    }

    private static void completeCourse(Scanner input) {
        System.out.print("Student name: ");
        String name = input.nextLine();
        System.out.print("Course ID: ");
        String courseId = input.nextLine();
        printMessage(system.markCourseCompleted(name, courseId));
    }

    private static void dropCourse(Scanner input) {
        System.out.print("Student name: ");
        String name = input.nextLine();
        System.out.print("Course ID: ");
        String courseId = input.nextLine();
        printMessage(system.drop(name, courseId));
    }

    private static void showCourses() {
        List<Course> courses = system.getCoursesSorted();
        for (Course course : courses) {
            System.out.println(course.getId() + " - " + course.getTitle());
            System.out.println("Available seats: " + course.getAvailableSeats() + " of " + course.getCapacity());
            System.out.println("Enrolled: " + showNames(course.getEnrolledStudents()));
            System.out.println("Waiting:  " + showNames(course.getWaitingStudents()));
        }
    }

    private static void showStudent(Scanner input) {
        System.out.print("Student name: ");
        String name = CourseRegistrationSystem.cleanStudentName(input.nextLine());
        boolean found = false;
        List<Course> courses = system.getCoursesSorted();

        for (Course course : courses) {
            if (course.hasEnrolledStudent(name)) {
                System.out.println(course.getId() + " - enrolled");
                found = true;
            } else if (course.hasWaitingStudent(name)) {
                System.out.println(course.getId() + " - waiting");
                found = true;
            }
        }

        if (!found) System.out.println("No registrations found for " + name + ".");
    }

    private static String showNames(List<String> names) {
        if (names.size() == 0) return "-";

        String result = "";
        for (int i = 0; i < names.size(); i++) {
            if (i > 0) result += ", ";
            result += names.get(i);
        }
        return result;
    }

    private static void printMessage(String message) {
        System.out.println(message);
    }
}
