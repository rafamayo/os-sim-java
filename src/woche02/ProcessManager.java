package woche02;

import java.util.HashMap;
import java.util.Map;

/**
 * Verwaltet Prozesszustände mit einer Map und einem enum.
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

        public void setState(ProcessState state) {
            this.state = state;
        }

        public ProcessState getState() {
            return state;
        }

        @Override
        public String toString() {
            return String.format("Process{pid=%d, name='%s', state=%s}", pid, name, state);
        }
    }

    private int nextId = 1;
    // Map zur Verwaltung der vorhandenen Prozesse
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
        Process process = processes.get(pid);
        if (process == null) {
            System.err.println("Error: Process " + pid + " not found!");
            return;
        }
        process.setState(ProcessState.BLOCKED);
        System.out.println("Blocked " + process);
    }

    /**
     * Beendet einen Prozess (setzt Zustand auf TERMINATED).
     * @param pid Die PID des Prozesses.
     */
    public void terminateProcess(int pid) {
        Process process = processes.get(pid);
        if (process == null) {
            System.err.println("Error: Process " + pid + " not found!");
            return;
        }
        process.setState(ProcessState.TERMINATED);
        System.out.println("Terminated " + process);
    }

    /**
     * Gibt den Zustand eines Prozesses zurück.
     * @param pid Die PID des Prozesses.
     * @return Der Zustand des Prozesses oder null, wenn der Prozess nicht existiert.
     */
    public ProcessState getProcessState(int pid) {
        Process process = processes.get(pid);
        return process != null ? process.getState() : null;
    }

    /**
     * Gibt alle Prozesse aus (für Debugging).
     */
    public void listProcesses() {
        System.out.println("--- Process List ---");
        for (Process process : processes.values()) {
            System.out.println(process);
        }
        System.out.println("---------------------");
    }
} 