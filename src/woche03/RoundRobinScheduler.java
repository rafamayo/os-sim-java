package woche03;

import java.util.*;

public class RoundRobinScheduler implements Scheduler {
    private final int quantum;
    private Queue<ProcessControlBlock> readyQueue = new LinkedList<>();
    private ProcessControlBlock lastExecutedProcess;  // Speichere den zuletzt ausgeführten Prozess

    public RoundRobinScheduler(int quantum) {
        this.quantum = quantum;
    }

    public int getQuantum() {
        return quantum;
    }

    @Override
    public ProcessControlBlock schedule(List<ProcessControlBlock> readyList, int currentTime) {
        // 1. Sammle alle neu angekommenen Prozesse (seit letztem Aufruf)
        List<ProcessControlBlock> newProcesses = new ArrayList<>();
        readyList.stream()
            .filter(pcb -> pcb.getArrivalTime() <= currentTime)
            .filter(pcb -> pcb.getState() != ProcessState.TERMINATED)
            .filter(pcb -> pcb.getRemainingTime() > 0)
            .filter(pcb -> !readyQueue.contains(pcb))
            .forEach(newProcesses::add);

        // 2. Füge neu angekommene Prozesse VOR den zuletzt ausgeführten Prozess ein
        if (lastExecutedProcess != null) {
            // Entferne den zuletzt ausgeführten Prozess temporär
            readyQueue.remove(lastExecutedProcess);
            // Füge alle neu angekommenen Prozesse hinzu
            readyQueue.addAll(newProcesses);
            // Füge den zuletzt ausgeführten Prozess hinten wieder ein
            readyQueue.add(lastExecutedProcess);
        } else {
            // Falls kein letzter Prozess, einfach alle neu angekommenen Prozesse anfügen
            readyQueue.addAll(newProcesses);
        }

        if (readyQueue.isEmpty()) {
            return null;
        }

        // 3. Wähle den nächsten Prozess
        ProcessControlBlock selected = readyQueue.poll();
        lastExecutedProcess = selected;  // Merke den ausgewählten Prozess
        selected.setState(ProcessState.RUNNING);

        // 4. Setze startTime, falls noch nicht gesetzt
        if (selected.getStartTime() == -1) {
            selected.setStartTime(currentTime);
        }

        // 5. Berechne die verbrauchte Zeit und speichere sie in usedTime
        int timeUsed = Math.min(quantum, selected.getRemainingTime());
        selected.setUsedTime(timeUsed);
        selected.setRemainingTime(selected.getRemainingTime() - timeUsed);

        // 6. Falls nicht fertig, wieder in die Queue
        if (selected.getRemainingTime() > 0) {
            selected.setState(ProcessState.READY);
            readyQueue.add(selected);
        } else {
            selected.setState(ProcessState.TERMINATED);
            selected.setFinishTime(currentTime + timeUsed);
            lastExecutedProcess = null;  // Zurücksetzen, falls Prozess fertig ist
        }

        return selected;
    }
}



/*
package woche03;

import java.util.*;

public class RoundRobinScheduler implements Scheduler {
    private final int quantum;
    private Queue<ProcessControlBlock> readyQueue = new LinkedList<>();
    private ProcessControlBlock lastExecutedProcess;  // Speichere den zuletzt ausgeführten Prozess

    public RoundRobinScheduler(int quantum) {
        this.quantum = quantum;
    }

    public int getQuantum() {
        return quantum;
    }

    @Override
    public ProcessControlBlock schedule(List<ProcessControlBlock> readyList, int currentTime) {
        // 1. Füge alle bereiten Prozesse zur Queue hinzu
        readyList.stream()
            .filter(pcb -> pcb.getArrivalTime() <= currentTime)
            .filter(pcb -> pcb.getState() != ProcessState.TERMINATED)
            .filter(pcb -> pcb.getRemainingTime() > 0)
            .forEach(pcb -> {
                if (!readyQueue.contains(pcb)) {
                    readyQueue.add(pcb);
                }
            });

        if (readyQueue.isEmpty()) {
            return null;
        }

        // 2. Wähle den nächsten Prozess
        ProcessControlBlock selected = readyQueue.poll();
        selected.setState(ProcessState.RUNNING);

        // 3. Setze startTime, falls noch nicht gesetzt
        if (selected.getStartTime() == -1) {
            selected.setStartTime(currentTime);
        }

        // 4. Berechne die verbrauchte Zeit und speichere sie in usedTime
        int timeUsed = Math.min(quantum, selected.getRemainingTime());
        selected.setUsedTime(timeUsed);  // Speichere die verbrauchte Zeit
        selected.setRemainingTime(selected.getRemainingTime() - timeUsed);

        // 5. Falls nicht fertig, wieder in die Queue
        if (selected.getRemainingTime() > 0) {
            selected.setState(ProcessState.READY);
            readyQueue.add(selected);
        } else {
            selected.setState(ProcessState.TERMINATED);
            selected.setFinishTime(currentTime + timeUsed);
        }

        return selected;
    }
}
*/

