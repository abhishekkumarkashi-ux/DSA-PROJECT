import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/** Directed adjacency-list graph. Each course points to its prerequisites. */
public final class PrerequisiteGraph {
    private final Map<String, List<String>> adjacencyList = new HashMap<>();

    public void addCourse(String courseId) {
        adjacencyList.computeIfAbsent(courseId.toUpperCase(), ignored -> new ArrayList<>());
    }

    public void addPrerequisite(String courseId, String prerequisiteId) {
        addCourse(courseId);
        addCourse(prerequisiteId);
        List<String> prerequisites = adjacencyList.get(courseId.toUpperCase());
        String prerequisite = prerequisiteId.toUpperCase();
        if (!prerequisites.contains(prerequisite)) prerequisites.add(prerequisite);
    }

    /** BFS visits the complete prerequisite chain and returns every unmet course. */
    public List<String> findUnmetPrerequisites(String courseId, Set<String> completedCourses) {
        List<String> unmet = new ArrayList<>();
        Queue<String> pending = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        pending.add(courseId.toUpperCase());
        visited.add(courseId.toUpperCase());

        while (!pending.isEmpty()) {
            String current = pending.remove();
            for (String prerequisite : adjacencyList.getOrDefault(current, List.of())) {
                if (!visited.add(prerequisite)) continue;
                if (!completedCourses.contains(prerequisite)) unmet.add(prerequisite);
                pending.add(prerequisite);
            }
        }
        return unmet;
    }
}
