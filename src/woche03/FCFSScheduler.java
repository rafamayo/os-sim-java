package woche03;

import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;

public class FCFSScheduler implements Scheduler {
    @Override
    public ProcessControlBlock schedule(List<ProcessControlBlock> readyQueue, int currentTime) {
        // 1. Filtere nur nicht-terminierte Prozesse mit remainingTime > 0
        List<ProcessControlBlock> filteredQueue = readyQueue.stream()
            .filter(pcb -> pcb.getState() != ProcessState.TERMINATED)
            .filter(pcb -> pcb.getRemainingTime() > 0)
            .collect(Collectors.toList());

        if (filteredQueue.isEmpty()) {
            return null;  // Keine bereiten Prozesse verfügbar
        }

        // 2. Sortiere nach Ankunftszeit (FCFS)
        filteredQueue.sort(Comparator.comparingInt(ProcessControlBlock::getArrivalTime));
        ProcessControlBlock selected = filteredQueue.get(0);

        // 3. Setze Zustand auf RUNNING
        selected.setState(ProcessState.RUNNING);

        // 4. Setze startTime, falls noch nicht gesetzt
        if (selected.getStartTime() == -1) {
            selected.setStartTime(currentTime);
        }

        // 5. FCFS: Prozess läuft bis zum Ende (usedTime = burstTime)
        int usedTime = selected.getBurstTime();
        selected.setUsedTime(usedTime);
        selected.setRemainingTime(0);  // Prozess ist fertig
        selected.setState(ProcessState.TERMINATED);
        selected.setFinishTime(currentTime + usedTime);

        return selected;
    }
}
