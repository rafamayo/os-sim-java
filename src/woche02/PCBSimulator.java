package woche02;

import java.util.HashMap;
import java.util.Map;

/**
 * Ein Simulator, der ProcessControlBlocks (PCBs) statt einfacher Prozesse verwendet.
 */
public class PCBSimulator {
    
    // Map mit allen Prozessen im System
    private Map<Integer, ProcessControlBlock> pcbs = new HashMap<>();
    private int nextId = 1;

    /**
     * Erstellt einen neuen Prozess (als PCB) und setzt seinen Zustand auf RUNNING.
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
        ProcessControlBlock pcb = pcbs.get(pid);
        if (pcb == null) {
            System.err.println("Error: Process " + pid + " not found!");
            return;
        }
        pcb.setState(ProcessState.BLOCKED);
        System.out.println("Blocked PCB: " + pcb);
    }

    /**
     * Setzt die Priorität eines Prozesses.
     * @param pid Die PID des Prozesses.
     * @param priority Die neue Priorität (1 = niedrig, 10 = hoch).
     */
    public void setPriority(int pid, int priority) {
        ProcessControlBlock pcb = pcbs.get(pid);
        if (pcb == null) {
            System.err.println("Error: Process " + pid + " not found!");
            return;
        }
        pcb.setPriority(priority);
        System.out.println("Updated priority of PCB " + pid + " to " + priority);
    }

    /**
     * Gibt den PCB eines Prozesses zurück.
     * @param pid Die PID des Prozesses.
     * @return Der PCB oder null, wenn der Prozess nicht existiert.
     */
    public ProcessControlBlock getPCB(int pid) {
        return pcbs.get(pid);
    }

    /**
     * Gibt alle PCBs aus (für Debugging).
     */
    public void listPCBs() {
        System.out.println("--- PCB List ---");
        for (ProcessControlBlock pcb : pcbs.values()) {
            System.out.println(pcb);
        }
        System.out.println("----------------");
    }
}
