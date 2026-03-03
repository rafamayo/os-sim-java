package woche03;

import java.util.List;

/**
 * Interface für CPU-Scheduling-Algorithmen.
 * Jeder Scheduler muss diese Methode implementieren, um den nächsten Prozess auszuwählen.
 */
public interface Scheduler {

    /**
     * Wählt den nächsten Prozess aus der Ready-Queue aus und aktualisiert seinen Zustand.
     *
     * @param readyQueue Liste der bereitstehenden Prozesse (ProcessControlBlocks).
     * @param currentTime Aktuelle Simulationszeit (für Timing-Berechnungen).
     * @return Der ausgewählte ProcessControlBlock (oder null, wenn keine Prozesse bereit sind).
     */
    ProcessControlBlock schedule(List<ProcessControlBlock> readyQueue, int currentTime);
}