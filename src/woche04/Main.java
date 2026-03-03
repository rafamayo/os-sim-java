package woche04;

import woche03.ProcessState;
import woche03.ProcessControlBlock;
import woche03.Scheduler;
import woche03.SchedulerUtils;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {

        // Workload 1: 1 Lang laufender Prozess und ein kurzer Prozess (MLFQ)
        List<ProcessControlBlock> workload1 = List.of(
            new ProcessControlBlock(1, "P1", 0, 100),
            new ProcessControlBlock(2, "P2", 25, 5)
        );
        System.out.println("DEBUG Workload 5: Prozesse vor der Simulation");
        workload1.forEach(pcb -> System.out.println(pcb));

        testPreemptiveScheduler(
            new MLFQScheduler(Arrays.asList(2, 4, 8), 5),  // 3 Queues (q=2,4,8), Aging-Schwelle=5
            deepCopyWorkload(workload1),
            "MLFQ, 3 Queues (q=2,4,8), Aging-Schwelle=5"
        );

    }

    // DeepCopy: kopiere alle Elemente der Liste (keine Referenzen!)
    private static List<ProcessControlBlock> deepCopyWorkload(List<ProcessControlBlock> original) {
        return original.stream()
            .map(ProcessControlBlock::clone)  // Klone jedes PCB-Objekt
            .collect(Collectors.toList());
    }

    private static void testPreemptiveScheduler(Scheduler scheduler, List<ProcessControlBlock> workload, String description) {
        System.out.println("\n=== " + description + " ===");
        int currentTime = 0;
        List<ProcessControlBlock> workloadCopy = deepCopyWorkload(workload);
        ProcessControlBlock currentProcess = null;

        while (workloadCopy.stream().anyMatch(pcb -> pcb.getState() != ProcessState.TERMINATED)) {
            final int timeSnapshot = currentTime;
            ProcessControlBlock nextProcess = scheduler.schedule(workloadCopy, timeSnapshot);

            currentTime += 1;  // Präemptive Scheduler arbeiten in 1-Zeit-Schritten

            if (nextProcess != null) {
                currentProcess = nextProcess;

                // Standardausgabe für alle Scheduler
                String output = "Time " + currentTime + ": " + currentProcess.getName() +
                            " (State: " + currentProcess.getState() + ")" +
                            " Queue: " + currentProcess.getPriority() +
                            " RemainingTime: " + currentProcess.getRemainingTime();

                // Falls MLFQScheduler: Zeige Quantum-Informationen an
                if (scheduler instanceof MLFQScheduler) {
                    MLFQScheduler mlfqScheduler = (MLFQScheduler) scheduler;
                    int quantumUsed = mlfqScheduler.getQuantumUsedMap().getOrDefault(currentProcess, 0);
                    int quantum = mlfqScheduler.getQuanta().get(currentProcess.getPriority());
                    output += " QuantumUsed: " + quantumUsed + "/" + quantum;
                }

                System.out.println(output);
            } else {
                System.out.println("Time " + currentTime + ": Leerlauf");
            }

        }

        // Ausgabe der Ergebnisse
        System.out.println("DEBUG: Prozesse nach der Simulation");
        workloadCopy.forEach(pcb -> System.out.println(pcb));

        // Berechne und gebe die Metriken aus
        Map<String, Double> metrics = SchedulerUtils.calculateMetrics(workloadCopy);
        System.out.println("Metriken: " + metrics);
    }

}
