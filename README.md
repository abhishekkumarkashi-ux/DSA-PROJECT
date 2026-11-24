# College Course Registration System

A Java console project for managing course enrollment, prerequisites, course capacity, waiting lists, and saved registrations. It uses the data structures named in Project Statement 57 and demonstrates topics from the Third Semester Data Structures and Algorithms syllabus.

## Requirements

- Java Development Kit (JDK) 17 or later
- No external libraries

## Run

Open a terminal in this project folder and run:

```text
run.bat
```

On macOS or Linux, run `./run.sh`. You can also compile and launch manually:

```text
javac -d out src/*.java
java -cp out Main
```

On first launch, the program creates `courses.txt` and `registrations.txt` in the current project folder if they do not exist. On exit, it saves enrollment, wait-list order, and completed-course records to `registrations.txt`.

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
| Enrolled student roster | Custom singly linked list | Unit 2: singly linked list |
| FIFO waiting queue | Custom linked queue | Unit 2: queue and linked-list implementation |
| Course prerequisites | Directed adjacency-list graph, searched with BFS | Unit 3 and lab P13-P15: graph representation and BFS |
| Capacity and duplicate checks | Roster traversal and available-seat calculation | Unit 1: traversal and algorithm analysis |
| Sorted course display | Java comparison sort by course ID | Unit 4: sorting algorithm concepts |
| Persistence | Pipe-delimited text file input/output | File handling required by Project Statement 57 |

The chosen project exercises the syllabus topics needed for the assignment. Searching/sorting implementations, trees, and greedy algorithms are separate syllabus units and are not required by this registration-system problem statement.

## Complexity notes

Let `s` be the number of enrolled or waiting students in a course and `v + e` be the number of courses and prerequisite edges reachable from a requested course.

- Course lookup: expected O(1) with the HashMap.
- Enrolled-list or waiting-queue duplicate check: O(s).
- Enqueue: O(s) here because duplicate names are checked first; dequeue: O(1).
- Prerequisite validation: O(v + e) for the reachable prerequisite graph.
- Course listing: O(c log c) to sort `c` courses for predictable display order.

## Assignment test scenarios

Start with an empty `registrations.txt` (the supplied starter file is empty apart from its comment).

1. **Normal registration:** choose `1`, enter `Aman`, then `CS101`. Expected: `Registration successful: Aman -> CS101` and one available seat.
2. **Full course:** register two students for `CS101`, then try `Riya` for `CS101`. Expected: `Course full. Riya added to waiting queue.`
3. **Unknown course:** try `C999`. Expected: `Course C999 not found.`
4. **One seat left:** with one seat open in `CS101`, register `Aman`. Expected: success and one available seat after registration.

For a prerequisite check, register a student in `CS101`, mark it completed using menu option 2, then register that student in `CS102`. Trying `CS102` first is rejected with the unmet prerequisite listed.
