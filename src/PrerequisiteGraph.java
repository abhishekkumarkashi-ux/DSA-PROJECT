import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Graph where each course points to the courses it requires. */
public class PrerequisiteGraph {
    private HashMap<String, ArrayList<String>> graph;

    public PrerequisiteGraph() {
        graph = new HashMap<String, ArrayList<String>>();
    }

    public void addCourse(String courseId) {
        String id = courseId.toUpperCase();
        if (!graph.containsKey(id)) graph.put(id, new ArrayList<String>());
    }

    public void addPrerequisite(String courseId, String prerequisiteId) {
        addCourse(courseId);
        addCourse(prerequisiteId);

        ArrayList<String> prerequisites = graph.get(courseId.toUpperCase());
        String prerequisite = prerequisiteId.toUpperCase();
        if (!prerequisites.contains(prerequisite)) prerequisites.add(prerequisite);
    }

    /** Breadth-first search checks the requested course and its prerequisite chain. */
    public List<String> findUnmetPrerequisites(String courseId, Set<String> completedCourses) {
        ArrayList<String> unmet = new ArrayList<String>();
        ArrayList<String> queue = new ArrayList<String>();
        HashSet<String> visited = new HashSet<String>();

        String start = courseId.toUpperCase();
        queue.add(start);
        visited.add(start);
        int next = 0;

        while (next < queue.size()) {
            String current = queue.get(next);
            next++;

            ArrayList<String> prerequisites = graph.get(current);
            if (prerequisites == null) continue;

            for (String prerequisite : prerequisites) {
                if (!visited.contains(prerequisite)) {
                    visited.add(prerequisite);
                    queue.add(prerequisite);
                    if (!completedCourses.contains(prerequisite)) unmet.add(prerequisite);
                }
            }
        }
        return unmet;
    }
}
