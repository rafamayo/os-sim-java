package woche06;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Aufgabe 1:
 * Ein einfacher Mutex für die Simulator-Demo.
 *
 * WICHTIG:
 * - Diese Klasse blockiert KEINE Java-Threads.
 * - Stattdessen werden wartende PCBs in eine Queue gelegt.
 */
public class SimulatorMutex {

    private ProcessControlBlock owner = null;
    private final Deque<ProcessControlBlock> waiters = new ArrayDeque<>();

    public ProcessControlBlock getOwner() { return owner; }

    public synchronized void acquire(ProcessControlBlock p) {
        // TODO:
        // Falls owner == null:
        //   owner = p
        // Sonst:
        //   waiters.addLast(p)
        //   p.blockOn(this)
    }

    public synchronized void release(ProcessControlBlock p) {
        // TODO:
        // 1. Prüfen: Ist p wirklich der owner?
        //    Falls nein: IllegalStateException werfen
        // 2. Falls waiters nicht leer:
        //    - nächsten Prozess holen
        //    - owner auf diesen Prozess setzen
        //    - Prozess mit unblock() wecken
        // 3. Falls waiters leer:
        //    - owner = null
    }

    @Override
    public String toString() {
        return "SimulatorMutex{owner=" + (owner == null ? "null" : owner.getName()) +
                ", waiters=" + waiters.size() + "}";
    }
}
