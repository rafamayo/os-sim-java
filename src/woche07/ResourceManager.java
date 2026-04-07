package woche07;

import java.util.*;

/**
 * Einfacher Ressourcenmanager mit automatischer WFG-Pflege.
 *
 * Jede Ressource hat genau ein Exemplar (Single-Instance).
 * Der Manager aktualisiert den Wait-for Graph bei jeder
 * Anforderung und Freigabe und prüft nach jeder neuen Wartekante
 * auf Zyklen.
 */
public class ResourceManager {

    private final Map<String, Integer>      holder;        // Ressource → Halter (null = frei)
    private final Map<Integer, Set<String>> heldBy;        // Prozess → gehaltene Ressourcen
    private final Map<Integer, String>      waitingFor;    // Prozess → abgewartete Ressource
    private final WaitForGraph              wfg;

    public ResourceManager() {
        this.holder     = new LinkedHashMap<>();
        this.heldBy     = new LinkedHashMap<>();
        this.waitingFor = new LinkedHashMap<>();
        this.wfg        = new WaitForGraph();
    }

    public void addResource(String resourceId) {
        holder.put(resourceId, null);
        System.out.printf("[RM] Ressource '%s' registriert%n", resourceId);
    }

    public void addProcess(int pid) {
        heldBy.put(pid, new LinkedHashSet<>());
        waitingFor.put(pid, null);
        wfg.addProcess(pid);
        System.out.printf("[RM] Prozess P%d registriert%n", pid);
    }

    /**
     * Prozess pid fordert Ressource resourceId an.
     *
     * Wenn frei → sofort zuweisen.
     * Wenn belegt → Wartekante in WFG, dann Deadlock-Check.
     *
     * @return true = sofort zugeteilt; false = muss warten
     * @throws IllegalStateException bei erkanntem Deadlock
     */
    public synchronized boolean requestResource(int pid, String resourceId) {
        System.out.printf("[RM] P%d fordert '%s' an%n", pid, resourceId);
        Integer currentHolder = holder.get(resourceId);

        if (currentHolder == null) {
            assign(pid, resourceId);
            return true;
        } else {
            System.out.printf("[RM] '%s' belegt von P%d → P%d wartet%n",
                    resourceId, currentHolder, pid);
            waitingFor.put(pid, resourceId);
            wfg.addWaitEdge(pid, currentHolder);
            checkDeadlock();
            return false;
        }
    }

    /**
     * Prozess pid gibt Ressource resourceId frei.
     * Falls ein anderer Prozess wartet, wird er sofort bedient.
     */
    public synchronized void releaseResource(int pid, String resourceId) {
        System.out.printf("[RM] P%d gibt '%s' frei%n", pid, resourceId);
        if (!Objects.equals(holder.get(resourceId), pid)) {
            System.out.printf("[RM] WARNUNG: P%d hält '%s' nicht!%n", pid, resourceId);
            return;
        }
        holder.put(resourceId, null);
        heldBy.getOrDefault(pid, Collections.emptySet()).remove(resourceId);

        int waiter = findWaiter(resourceId);
        if (waiter != -1) {
            wfg.removeWaitEdge(waiter, pid);
            waitingFor.put(waiter, null);
            assign(waiter, resourceId);
        }
    }

    private void checkDeadlock() {
        List<Integer> cycle = wfg.detectDeadlock();
        if (!cycle.isEmpty()) {
            wfg.printGraph();
            String desc = cycle.stream().map(i -> "P" + i)
                               .reduce((a, b) -> a + " → " + b).orElse("");
            throw new IllegalStateException("🔴 DEADLOCK erkannt! Zyklus: " + desc);
        }
    }

    private void assign(int pid, String resourceId) {
        holder.put(resourceId, pid);
        heldBy.get(pid).add(resourceId);
        System.out.printf("[RM] '%s' → P%d ZUGETEILT%n", resourceId, pid);
    }

    private int findWaiter(String resourceId) {
        return waitingFor.entrySet().stream()
                .filter(e -> resourceId.equals(e.getValue()))
                .mapToInt(Map.Entry::getKey)
                .findFirst().orElse(-1);
    }

    public void printState() {
        System.out.println("\n┌─── Ressourcenzustand ───────────────────────────┐");
        holder.forEach((res, pid) -> {
            if (pid == null) System.out.printf("│  %-6s  frei%n", res);
            else             System.out.printf("│  %-6s  gehalten von P%d%n", res, pid);
        });
        System.out.println("└─────────────────────────────────────────────────┘");
        wfg.printGraph();
    }

    public WaitForGraph getWaitForGraph() { return wfg; }
}
