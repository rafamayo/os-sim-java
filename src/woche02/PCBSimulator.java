package woche02;

import java.util.HashMap;
import java.util.Map;

/**
 * Ein Simulator, der ProcessControlBlocks (PCBs) verwendet.
 * Vervollständige die Methoden gemäß der Aufgabenstellung!
 */
public class PCBSimulator {
    private Map<Integer, ProcessControlBlock> pcbs = new HashMap<>();
    private int nextId = 1;

    /**
     * Erstellt einen neuen Prozess (als PCB) und fügt ihn der Map hinzu.
     * @param name Name des Prozesses.
     * @return Die PID des neuen Prozesses.
     */
    public int createProcess(String name) {
        int pid = nextId++;
        ProcessControlBlock pcb = new ProcessControlBlock(pid, name);
        pcb.setState(ProcessState.RUNNING);
        pcbs.put(pid, pcb);
        System.out.println("Created PCB: " + pcb);
        return pid;
    }

    /**
     * Blockiert einen Prozess (setzt Zustand auf BLOCKED).
     * @param pid Die PID des Prozesses.
     */
    public void blockProcess(int pid) {
        // TODO: Implementiere diese Methode!
        // 1. Hole den PCB aus der Map (mit pcbs.get(pid)).
        // 2. Prüfe, ob der Prozess existiert (sonst: IllegalArgumentException).
        // 3. Setze den Zustand auf BLOCKED (nur wenn aktuell RUNNING!).
        // 4. Gib eine Bestätigungsmeldung aus (z. B. "Blocked PCB: ...").
    }

    /**
     * Beendet einen Prozess (Zustand: RUNNING/BLOCKED → TERMINATED).
     * @param pid Die PID des Prozesses.
     * @throws IllegalArgumentException Falls der Prozess nicht existiert.
     */
    public void terminateProcess(int pid) {
        // TODO: Implementiere diese Methode!
        // 1. Hole den PCB aus der Map.
        // 2. Prüfe, ob der Prozess existiert (sonst: Exception).
        // 3. Setze den Zustand auf TERMINATED (unabhängig vom aktuellen Zustand!).
        // 4. Gib eine Bestätigungsmeldung aus.
    }

    /**
     * Setzt die Priorität eines Prozesses.
     * @param pid Die PID des Prozesses.
     * @param priority Die neue Priorität (1 = niedrig, 10 = hoch).
     */
    public void setPriority(int pid, int priority) {
        // TODO: Implementiere diese Methode
    }

    /**
     * Gibt den PCB eines Prozesses zurück.
     * @param pid Die PID des Prozesses.
     * @return Der PCB oder null, wenn der Prozess nicht existiert.
     */
    public ProcessControlBlock getPCB(int pid) {
        // TODO: Implementiere diese Methode
        return null; // Platzhalter
    }

    /**
     * Gibt alle PCBs aus (für Debugging).
     */
    public void listPCBs() {
        // TODO: Implementiere diese Methode
    }
}
