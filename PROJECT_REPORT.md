# College Course Registration System

## 1. Abstract

The College Course Registration System is a Java console application for registering students in courses. It checks course prerequisites, enforces a fixed seat capacity, prevents duplicate registrations, and places students in a first-in, first-out waiting queue when a course is full. Course and registration information is loaded from and saved to plain-text files.

## 2. Problem statement

The system must look up courses, track each course's enrolled and waiting students, validate prerequisite completion, and preserve data between runs. A course must never exceed its capacity. For this project, the starter courses each have a capacity of two.

## 3. Objectives

- Apply hashing, linked lists, queues, and graph traversal to a practical problem.
- Enforce course capacities and reject duplicate registrations.
- Check direct and transitive prerequisites before accepting a registration.
- Preserve enrollment, waiting-list, and completed-course data in text files.
- Display available seats and waiting-list status to the user.

## 4. Data structures and algorithms

| Structure or algorithm | Use in this system |
| --- | --- |
| HashMap | Maps a course ID to its course record for expected constant-time lookup. |
| `StudentLinkedList` | Stores each course's enrolled students in enrollment order. |
| `StudentQueue` | Stores each course's waiting students; the first student waiting is promoted when a seat opens. |
| Directed adjacency-list graph | Stores edges from a course to the courses that must be completed first. |
| Breadth-first search | Traverses the complete prerequisite chain and reports any course the student has not completed. |
| Bubble sort | Sorts course records by ID before displaying them, so the menu output is predictable. |

Course prerequisites form a directed graph. The starter courses progress from `CS101` to `CS102` to `CS201`: `CS102` requires `CS101`, and `CS201` requires `CS102` (and therefore `CS101`). Each graph edge points from a course to one of its prerequisites. The graph traversal starts at the requested course and visits each prerequisite once.

## 5. Program flow

1. The application loads courses from `courses.txt` into a HashMap and builds the prerequisite graph.
2. It loads enrolled, waiting, and completed records from `registrations.txt`.
3. The user selects an operation from the console menu.
4. On registration, the system checks that the course exists, rejects an existing enrollment or wait-list entry, and checks every prerequisite using BFS.
5. If a seat is available, the student is appended to the enrolled linked list. If the course is full, the student is enqueued.
6. When an enrolled student drops a course, the next waiting student is dequeued and enrolled.
7. On exit, the application writes current state to `registrations.txt`.

## 6. File formats

Courses use `courseId|title|capacity|prerequisiteIds`, with comma-separated prerequisite IDs. Registrations use one of the following record formats:

```text
E|student name|course ID
W|student name|course ID
C|student name|course ID
```

The record prefixes represent enrolled, waiting, and completed courses, respectively. The order of `W` records preserves FIFO waiting order for that course.

## 7. Complexity

Let `s` be the number of enrolled or waiting students in a course, `v` and `e` be the reachable prerequisite vertices and edges, and `c` be the number of courses.

- Course lookup: expected O(1).
- Duplicate checks in the list and queue: O(s).
- Add to the linked list: O(s); queue enqueue and dequeue: O(1).
- Prerequisite check: O(v + e).
- Course display ordering with bubble sort: O(c^2).

## 8. Verification

The program was compiled and run through `run.bat` with JDK 25.0.2. The console flow produced these results:

| Scenario | Result |
| --- | --- |
| Register Aman for an available CS101 seat | Registration succeeded and the available-seat count changed from two to one. |
| Fill CS101 and register Riya | Riya was added to the waiting queue. |
| Register for C999 | The application reported that course C999 was not found. |
| Drop an enrolled student while Riya is waiting | Riya was promoted from the waiting queue. |
| Register Riya for CS102 before completing CS101 | Registration was rejected and CS101 was reported as unmet. |
| Mark CS101 complete, then request CS102 | Registration succeeded. |
| Exit and inspect registrations.txt | Enrolled, wait-list, and completion records were saved. |

## 9. How to run

Install JDK 8 or later and start `run.bat` on Windows, or `./run.sh` on macOS/Linux. The program needs write access to its project folder to save `registrations.txt` when it exits.

## 10. Conclusion

This project demonstrates the syllabus topics of hashing, linked lists, queues, adjacency-list graphs, BFS traversal, sorting, and time/space complexity analysis in a small application. It also implements the course capacity, prerequisite, duplicate-registration, waiting-list, and persistence requirements from the project brief.
