package woche07;

import java.util.*;

/**
 * Wait-for Graph (WFG) zur exakten Deadlock-Detection (Teil B).
 *
 * Modell:
 *   - Knoten: Prozesse (Integer-IDs)
 *   - Gerichtete Kante p → q: "Prozess p wartet auf eine Ressource, die q hält"
 *
 * Deadlock-Bedingung (Einzelexemplar-Ressourcen):
 *   Zyklus im WFG ⟺ Deadlock
 *
 * Algorithmus: DFS mit Rekursions-Stack (Rückkanten-Erkennung)
 */
public class WaitForGraph {

    // Adjazenzliste: Prozess-ID → Menge der Prozesse, auf die gewartet wird
    private final Map<Integer, Set<Integer>> adj;

    public WaitForGraph() {
        this.adj = new LinkedHashMap<>();
    }

    /** Fügt einen Prozessknoten hinzu (ohne Kanten). */
    public void addProcess(int pid) {
        adj.putIfAbsent(pid, new LinkedHashSet<>());
    }

    /**
     * Fügt eine Wartekante ein: from → to
     * ("Prozess from wartet auf Ressource, die Prozess to hält")
     */
    public void addWaitEdge(int from, int to) {
        adj.putIfAbsent(from, new LinkedHashSet<>());
        adj.putIfAbsent(to,   new LinkedHashSet<>());
        adj.get(from).add(to);
        System.out.printf("[WFG] Kante hinzugefügt: P%d → P%d%n", from, to);
    }

    /** Entfernt eine Wartekante (Prozess wartet nicht mehr). */
    public void removeWaitEdge(int from, int to) {
        if (adj.containsKey(from)) {
            adj.get(from).remove(to);
            System.out.printf("[WFG] Kante entfernt:     P%d → P%d%n", from, to);
        }
    }

    /** Entfernt einen Prozess vollständig (z. B. nach Terminierung/Recovery). */
    public void removeProcess(int pid) {
        adj.remove(pid);
        adj.values().forEach(neighbors -> neighbors.remove(pid));
        System.out.printf("[WFG] Prozess P%d entfernt (Recovery)%n", pid);
    }

    /**
     * Deadlock-Detection via DFS.
     *
     * Für jeden noch nicht besuchten Knoten wird eine DFS gestartet.
     * Ein Zyklus liegt vor, wenn ein Nachbar bereits im aktuellen
     * Rekursions-Stack liegt (Rückkante).
     *
     * @return Liste der Prozess-IDs im Zyklus, oder leere Liste (kein Deadlock)
     */
    public List<Integer> detectDeadlock() {
        Set<Integer> visited        = new HashSet<>();
        Set<Integer> recursionStack = new HashSet<>();
        List<Integer> cycle         = new ArrayList<>();

        for (Integer node : adj.keySet()) {
            if (!visited.contains(node)) {
                if (dfs(node, visited, recursionStack, cycle)) {
                    return cycle;
                }
            }
        }
        return Collections.emptyList();
    }

    /**
     * Rekursive DFS-Hilfsmethode.
     *
     * @param node           Aktueller Knoten
     * @param visited        Vollständig abgearbeitete Knoten
     * @param recursionStack Knoten im aktuellen DFS-Pfad
     * @param cycle          Wird bei Fund mit dem Zyklus befüllt
     * @return true, wenn Zyklus gefunden
     */
    private boolean dfs(int node, Set<Integer> visited,
                         Set<Integer> recursionStack, List<Integer> cycle) {
        visited.add(node);
        recursionStack.add(node);

        for (int neighbor : adj.getOrDefault(node, Collections.emptySet())) {
            if (!visited.contains(neighbor)) {
                if (dfs(neighbor, visited, recursionStack, cycle)) {
                    cycle.add(0, node);
                    return true;
                }
            } else if (recursionStack.contains(neighbor)) {
                // Rückkante → Zyklus gefunden!
                cycle.add(neighbor); // Einstiegspunkt
                cycle.add(0, node);
                return true;
            }
        }

        recursionStack.remove(node);
        return false;
    }

    /** Gibt den Graphen als ASCII-Tabelle aus. */
    public void printGraph() {
        System.out.println("\n┌─── Wait-for Graph ──────────────────────────────┐");
        if (adj.isEmpty()) {
            System.out.println("│  (leer)                                         │");
        } else {
            List<Integer> sorted = new ArrayList<>(adj.keySet());
            Collections.sort(sorted);
            for (int node : sorted) {
                Set<Integer> neighbors = adj.get(node);
                String targets = neighbors.isEmpty() ? "(wartet auf niemanden)"
                        : neighbors.stream().sorted()
                                   .map(n -> "P" + n)
                                   .reduce((a, b) -> a + ", " + b).orElse("");
                System.out.printf("│  P%-3d  →  %-36s│%n", node, targets);
            }
        }
        System.out.println("└─────────────────────────────────────────────────┘\n");
    }

    public Set<Integer> getProcesses() {
        return Collections.unmodifiableSet(adj.keySet());
    }
}
