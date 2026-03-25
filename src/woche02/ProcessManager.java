package woche02;

import java.util.HashMap;
import java.util.Map;

/**
 * Verwaltet Prozesszustände mit einer Map und einem enum.
 * Vervollständige die Methoden gemäß der Aufgabenstellung!
 */
public class ProcessManager {

    // Innere Klasse zur Darstellung eines Prozesses
    private static class Process {
        private int pid;
        private String name;
        private ProcessState state;

        public Process(int pid, String name) {
            this.pid = pid;
            this.name = name;
            this.state = ProcessState.NEW;
        }

        // TODO: Füge Getter/Setter für 'state' hinzu
        // public void setState(ProcessState state) { ... }
        // public ProcessState getState() { ... }

        @Override
        public String toString() {
            return String.format("Process{pid=%d, name='%s', state=%s}", pid, name, state);
        }
    }

    private int nextId = 1;
    private Map<Integer, Process> processes = new HashMap<>();

    /**
     * Erstellt einen neuen Prozess und setzt seinen Zustand auf RUNNING.
     * @param name Name des Prozesses.
     * @return Die PID des neuen Prozesses.
     */
    public int createProcess(String name) {
        int pid = nextId++;
        Process process = new Process(pid, name);
        process.setState(ProcessState.RUNNING);
        processes.put(pid, process);
        System.out.println("Created " + process);
        return pid;
    }

    /**
     * Blockiert einen Prozess (setzt Zustand auf BLOCKED).
     * @param pid Die PID des Prozesses.
     */
    public void blockProcess(int pid) {
        // TODO: Implementiere diese Methode
        // 1. Hole den Prozess aus der Map
        // 2. Setze den Zustand auf BLOCKED (falls Prozess existiert)
    }

    /**
     * Beendet einen Prozess (setzt Zustand auf TERMINATED).
     * @param pid Die PID des Prozesses.
     */
    public void terminateProcess(int pid) {
        // TODO: Implementiere diese Methode
    }

    /**
     * Gibt den Zustand eines Prozesses zurück.
     * @param pid Die PID des Prozesses.
     * @return Der Zustand des Prozesses oder null, wenn der Prozess nicht existiert.
     */
    public ProcessState getProcessState(int pid) {
        // TODO: Implementiere diese Methode
        return null; // Platzhalter
    }

    /**
     * Gibt alle Prozesse aus (für Debugging).
     */
    public void listProcesses() {
        // TODO: Implementiere diese Methode
    }
}