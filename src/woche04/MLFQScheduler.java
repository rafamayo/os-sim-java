package woche04;

import java.util.*;

import woche03.Scheduler;

public class MLFQScheduler implements Scheduler {
    private List<LinkedList<ProcessControlBlock>> queues;
    private List<Integer> quanta;
    private int currentTime;
    private int agingThreshold;  // Nach wie vielen Zeiteinheiten Aging stattfindet
    private Map<ProcessControlBlock, Integer> waitingTimes;  // Wartezeit pro Prozess
    private Map<ProcessControlBlock, Integer> quantumUsedMap;  // Trackt verbrauchte Quantum-Zeit pro Prozess

    public MLFQScheduler(List<Integer> quanta, int agingThreshold) {
        this.queues = new ArrayList<>();
        this.quanta = quanta;                             // Benutzerdefinierte Quanta (z. B. [2, 4, 8])
        this.agingThreshold = agingThreshold;             // Benutzerdefinierte Aging-Schwelle (z. B. 5)
        this.waitingTimes = new HashMap<>();              // Initialisiere die Map
        this.quantumUsedMap = new HashMap<>();            // Initialisiere die Map
        for (int i = 0; i < quanta.size(); i++) {
            queues.add(new LinkedList<>());
        }
    }

    public Map<ProcessControlBlock, Integer> getQuantumUsedMap() {
        return quantumUsedMap;
    }

    public List<Integer> getQuanta() {
        return quanta;
    }


    @Override
    public ProcessControlBlock schedule(List<ProcessControlBlock> readyQueue, int currentTime) {
        this.currentTime = currentTime;

        // 1. Füge alle neu angekommenen Prozesse in die höchste Queue ein
        for (ProcessControlBlock pcb : readyQueue) {
            if (pcb.getArrivalTime() <= currentTime &&
                pcb.getState() != ProcessState.TERMINATED &&
                pcb.getRemainingTime() > 0 &&
                !isInAnyQueue(pcb)) {
                // Neu angekommene Prozesse kommen VORNE in die Queue (für Präemption)
                if (pcb.getArrivalTime() == currentTime) {
                    queues.get(0).addFirst(pcb);
                } else {
                    queues.get(0).add(pcb);
                }
                pcb.setPriority(0);
                quantumUsedMap.put(pcb, 0);
                waitingTimes.put(pcb, 0);
            }
        }

        // 2. Aging: Erhöhe die Wartezeit aller Prozesse in den Queues
        for (int i = 0; i < queues.size(); i++) {
            for (ProcessControlBlock pcb : queues.get(i)) {
                waitingTimes.merge(pcb, 1, Integer::sum);
            }
        }

        // 3. Aging: Befördere Prozesse, die zu lange warten
        for (int i = 1; i < queues.size(); i++) {
            Iterator<ProcessControlBlock> iterator = queues.get(i).iterator();
            while (iterator.hasNext()) {
                ProcessControlBlock pcb = iterator.next();
                if (waitingTimes.getOrDefault(pcb, 0) >= agingThreshold) {
                    iterator.remove();
                    if (i > 0) {
                        queues.get(i - 1).add(pcb);
                        pcb.setPriority(i - 1);
                        quantumUsedMap.put(pcb, 0);
                    }
                    waitingTimes.put(pcb, 0);
                }
            }
        }

        // 4. Durchsuche die Queues von hoch nach niedrig
        for (int i = 0; i < queues.size(); i++) {
            LinkedList<ProcessControlBlock> queue = queues.get(i);
            if (!queue.isEmpty()) {
                ProcessControlBlock selected = queue.poll();
                selected.setState(ProcessState.RUNNING);

                if (selected.getStartTime() == -1) {
                    selected.setStartTime(currentTime);
                }

                // 5. Führe den Prozess für 1 Zeiteinheit aus
                int quantum = quanta.get(i);
                int usedSoFar = quantumUsedMap.getOrDefault(selected, 0);
                quantumUsedMap.put(selected, usedSoFar + 1);

                // 6. Reduziere remainingTime und prüfe auf Terminierung
                selected.setUsedTime(1);
                selected.setRemainingTime(selected.getRemainingTime() - 1);

                // 7. Falls der Prozess nicht fertig ist, füge ihn wieder in die Queue ein
                if (selected.getRemainingTime() > 0) {
                    selected.setState(ProcessState.READY);

                    // Demotion: Falls Quantum aufgebraucht, verschiebe in die nächste Queue
                    if (quantumUsedMap.get(selected) >= quantum) {
                        quantumUsedMap.put(selected, 0);
                        if (i < queues.size() - 1) {
                            selected.setPriority(i + 1);  // Demotion: Priority erhöhen
                            queues.get(i + 1).add(selected);  // In die NÄCHSTE Queue einfügen
                        } else {
                            queues.get(i).add(selected);  // Bleibt in der niedrigsten Queue
                        }
                    } else {
                        // Bleibt in der aktuellen Queue
                        // Nicht fertig, nicht den gesamten Quantum verbraucht
                        // Vorne in der aktuellen Queue, soll im nächsten Durchlauf (nach 1 Zeiteinheit) wieder ausgeführt werden
                        queues.get(i).addFirst(selected);
                    }
                    waitingTimes.put(selected, 0);
                } else {
                    selected.setState(ProcessState.TERMINATED);
                    selected.setFinishTime(currentTime + 1);
                    waitingTimes.remove(selected);
                    quantumUsedMap.remove(selected);
                }

                return selected;
            }
        }

        return null;
    }

    // Hilfsmethode: Prüfe, ob ein Prozess bereits in einer Queue ist
    private boolean isInAnyQueue(ProcessControlBlock pcb) {
        for (Queue<ProcessControlBlock> queue : queues) {
            if (queue.contains(pcb)) {
                return true;
            }
        }
        return false;
    }
}