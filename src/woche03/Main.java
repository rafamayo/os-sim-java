package woche03;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        // Workload 1: Convoy-Effekt (FCFS leidet)
        List<ProcessControlBlock> workload1 = List.of(
            new ProcessControlBlock(1, "P1", 0, 30),  // PID, Name, Ankunftszeit, Burst-Time
            new ProcessControlBlock(2, "P2", 1, 3),
            new ProcessControlBlock(3, "P3", 2, 1)
        );
        System.out.println("Workload 1: Prozesse vor der Simulation");
        workload1.forEach(pcb -> System.out.println(pcb));

        // Workload 2: I/O-bound (viele kurze Jobs)
        List<ProcessControlBlock> workload2 = List.of(
            new ProcessControlBlock(1, "P1", 0, 2),
            new ProcessControlBlock(2, "P2", 0, 2),
            new ProcessControlBlock(3, "P3", 3, 2),
            new ProcessControlBlock(4, "P4", 4, 2),
            new ProcessControlBlock(5, "P5", 5, 2)
        );
        System.out.println("Workload 2: Prozesse vor der Simulation");
        workload2.forEach(pcb -> System.out.println(pcb));


        // Workload 3: Gemischt
        List<ProcessControlBlock> workload3 = List.of(
            new ProcessControlBlock(1, "P1", 0, 5),
            new ProcessControlBlock(2, "P2", 1, 10),
            new ProcessControlBlock(3, "P3", 2, 1),
            new ProcessControlBlock(4, "P4", 3, 4)
        );
        System.out.println("Workload 3: Prozesse vor der Simulation");
        workload3.forEach(pcb -> System.out.println(pcb));



        // Teste die Scheduler
        testScheduler(new FCFSScheduler(), deepCopyWorkload(workload1), "FCFS mit Workload 1");
    
        testScheduler(new SJFScheduler(), deepCopyWorkload(workload1), "SJF mit Workload 1");
        workload1.forEach(pcb -> System.out.println(pcb));

        testScheduler(new RoundRobinScheduler(2), deepCopyWorkload(workload1), "Round-Robin (q=2) mit Workload 1");
        workload1.forEach(pcb -> System.out.println(pcb));

        // Teste präemptiven SRT-Scheduler (separate Methode)
        testSRTScheduler(deepCopyWorkload(workload1), "SRT mit Workload 1");

        // Setzen Sie die anderen Workloads ein!
        // Erstellen Sie weitere Workloads!
    }

    

    // Teste die Scheduler FCFS, SJF, RR
    // Die Zeit wird immer um usedTime() erhöht. usedTime ist entweder die vollständige burstTime (FCFS, SJF) oder en Quantum bzw. remainingTime
    private static void testScheduler(Scheduler scheduler, List<ProcessControlBlock> workload, String description) {
        System.out.println("\n=== " + description + " ===");
        int currentTime = 0;
        List<ProcessControlBlock> workloadCopy = new ArrayList<>(workload);

        while (workloadCopy.stream().anyMatch(pcb -> pcb.getState() != ProcessState.TERMINATED)) {
            ProcessControlBlock selected = scheduler.schedule(workloadCopy, currentTime);

            if (selected != null) {
                System.out.println("Time " + currentTime + ": " + selected.getName() +
                                " (State: " + selected.getState() + ") " +
                                "RemainingTime: " + selected.getRemainingTime() +
                                " UsedTime: " + selected.getUsedTime());  // Debug-Ausgabe

                // Verwende usedTime aus dem PCB
                currentTime += selected.getUsedTime();
            } else {
                currentTime += 1;  // Leerlauf
            }
        }

        // Ausgabe der Prozesse
        System.out.println("Workload: Prozesse nach der Simulation");
        workloadCopy.forEach(pcb -> System.out.println(pcb));

        // Berechne Metriken
        Map<String, Double> metrics = SchedulerUtils.calculateMetrics(workloadCopy);
        System.out.println("Metriken:\n" + metrics);
    }


    // Teste den SRTScheduler
    // SRT ist präemptiv: die Zeit muss immer in Scheiben von Dauer 1 aktualisiert werden
    private static void testSRTScheduler(List<ProcessControlBlock> workload, String description) {
        System.out.println("\n=== " + description + " ===");
        int currentTime = 0;
        List<ProcessControlBlock> workloadCopy = deepCopyWorkload(workload);
        ProcessControlBlock currentProcess = null;
        SRTScheduler scheduler = new SRTScheduler();

        while (workloadCopy.stream().anyMatch(pcb -> pcb.getState() != ProcessState.TERMINATED)) {
            final int timeSnapshot = currentTime;

            // 1. Wähle den nächsten Prozess (oder behalte den aktuellen, falls er kürzere remainingTime hat)
            ProcessControlBlock nextProcess = scheduler.schedule(workloadCopy, timeSnapshot);

            // 2. Falls ein neuer Prozess ausgewählt wurde (oder der aktuelle weiterläuft)
            if (nextProcess != null) {
                currentProcess = nextProcess;
                System.out.println("Current process:");
                System.out.println("Time " + currentTime + ": " + currentProcess.getName() +
                                " (State: RUNNING) RemainingTime: " + currentProcess.getRemainingTime());
            } else {
                System.out.println("Time " + currentTime + ": Leerlauf");
            }

            currentTime += 1;  // Erhöhe die Zeit in jedem Schritt
        }

        // Ausgabe der Prozesse
        System.out.println("Workload: Prozesse nach der Simulation");
        workloadCopy.forEach(pcb -> System.out.println(pcb));

        // Berechne und gebe Metriken aus
        Map<String, Double> metrics = SchedulerUtils.calculateMetrics(workloadCopy);
        System.out.println("Metriken:\n" + metrics);
    }

    // DeepCopy: kopiere alle Elemente der Liste (keine Referenzen!)
    private static List<ProcessControlBlock> deepCopyWorkload(List<ProcessControlBlock> original) {
    return original.stream()
        .map(ProcessControlBlock::clone)  // Klone jedes PCB-Objekt
        .collect(Collectors.toList());
    }
}
