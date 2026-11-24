# College Course Registration System

A Java desktop application for managing course enrollment, prerequisites, course capacity, waiting lists, and saved registrations. Its Swing frontend is integrated into the single-file Java application.

## Requirements

- Java Development Kit (JDK) 11 or later
- No external libraries

## Run

Open a terminal in this project folder and run:

```text
run.bat
```

In VS Code, open this folder and choose **Run Course Registration System** from Run and Debug (F5). Install the **Extension Pack for Java** if VS Code asks for Java support.

The app opens its graphical interface when a desktop is available. If started in a headless environment, it uses the console menu. On macOS or Linux, run `./run.sh`. You can also compile and launch manually:

```text
javac -encoding UTF-8 -d . src/Main.java
java -cp . Main
```

On first launch, the program creates `courses.txt` and `registrations.txt` in the current project folder if they do not exist. Changes are saved to `registrations.txt` after each graphical action and when the app exits.

## Graphical interface

The dashboard displays course capacity, enrolled students, and waiting lists. Use the left-side actions to register a student, mark a course completed, drop a course or waiting-list entry, find a student's registrations, or run the built-in test cases. The tests run against temporary data and do not change saved registrations.

## Menu

1. Register for a course
2. Mark an enrolled course completed (used to satisfy prerequisites)
3. Drop a course or leave its waiting queue; the first waiting student is promoted when an enrolled student drops
4. List courses, enrolled students, waiting students, and available seats
5. Show a student's current registrations
6. Save and exit

Course IDs and student names are matched without case sensitivity. Duplicate registrations are rejected. A student must have completed every direct and transitive prerequisite before joining a course or its waiting list.

## Text file formats

`courses.txt` uses one course per line:

```text
courseId|title|capacity|prerequisiteCourseIds
```

Separate multiple prerequisites with commas. Lines beginning with `#` are comments. The starter file contains:

```text
CS101|Introduction to Programming|2|
CS102|Data Structures|2|CS101
CS201|Algorithms|2|CS102
MA101|Discrete Mathematics|2|
```

`registrations.txt` is written when the program exits. Its record types are:

```text
E|student name|course ID
W|student name|course ID
C|student name|course ID
```

`E` means enrolled, `W` means waiting (the file preserves FIFO order), and `C` means completed. Keep the `|` separator out of names and course titles.

## Syllabus and project mapping

| Project feature | Implementation | Syllabus topic |
| --- | --- | --- |
| Course lookup by ID | `HashMap<String, Course>` | Unit 3: hashing and collision resolution |
| Enrolled student roster | `StudentLinkedList.java`, a custom singly linked list | Unit 2: singly linked list |
| FIFO waiting queue | `StudentQueue.java`, a custom linked queue | Unit 2: queue and linked-list implementation |
| Course prerequisites | Directed adjacency-list graph, searched with BFS | Unit 3 and lab P13-P15: graph representation and BFS |
| Capacity and duplicate checks | Roster traversal and available-seat calculation | Unit 1: traversal and algorithm analysis |
| Sorted course display | Simple bubble sort by course ID | Unit 4: bubble sort |
| Persistence | Pipe-delimited text file input/output | File handling required by Project Statement 57 |

The chosen project exercises the syllabus topics needed for the assignment. Searching/sorting implementations, trees, and greedy algorithms are separate syllabus units and are not required by this registration-system problem statement.

## Complexity notes

Let `s` be the number of enrolled or waiting students in a course and `v + e` be the number of courses and prerequisite edges reachable from a requested course.

- Course lookup: expected O(1) with the HashMap.
- Duplicate checks in a course's list and queue: O(s).
- Add a student to the linked list: O(s); queue enqueue and dequeue: O(1).
- Prerequisite validation: O(v + e) for the reachable prerequisite graph.
- Course listing: O(c^2) to bubble sort `c` courses for predictable display order.

## Assignment test scenarios

Start with an empty `registrations.txt` (the supplied starter file is empty apart from its comment).

1. **Normal registration:** choose `1`, enter `Aman`, then `CS101`. Expected: `Registration successful: Aman -> CS101` and one available seat.
2. **Full course:** register two students for `CS101`, then try `Riya` for `CS101`. Expected: `Course full. Riya added to waiting queue.`
3. **Unknown course:** try `C999`. Expected: `Course C999 not found.`
4. **One seat left:** with one seat open in `CS101`, register `Aman`. Expected: success and one available seat after registration.

For a prerequisite check, register a student in `CS101`, mark it completed using menu option 2, then register that student in `CS102`. Trying `CS102` first is rejected with the unmet prerequisite listed.
