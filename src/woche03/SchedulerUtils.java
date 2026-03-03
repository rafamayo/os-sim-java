package woche03;

import java.util.List;
import java.util.Map;

public class SchedulerUtils {
    public static Map<String, Double> calculateMetrics(List<ProcessControlBlock> pcbs) {
        // Filtere nur terminierte Prozesse
        List<ProcessControlBlock> terminated = pcbs.stream()
            .filter(pcb -> pcb.getState() == ProcessState.TERMINATED)
            .toList();

        if (terminated.isEmpty()) {
            return Map.of(
                "avgWaitingTime", 0.0,
                "avgTurnaroundTime", 0.0,
                "avgResponseTime", 0.0,
                "throughput", 0.0
            );
        }

        double avgWaitingTime = terminated.stream()
            .mapToDouble(pcb -> pcb.getFinishTime() - pcb.getArrivalTime() - pcb.getBurstTime())
            .average()
            .orElse(0.0);

        double avgTurnaroundTime = terminated.stream()
            .mapToDouble(pcb -> pcb.getFinishTime() - pcb.getArrivalTime())
            .average()
            .orElse(0.0);

        double avgResponseTime = terminated.stream()
            .mapToDouble(pcb -> pcb.getStartTime() - pcb.getArrivalTime())
            .average()
            .orElse(0.0);

        // Durchsatz: Anzahl Prozesse / (max finishTime - min arrivalTime)
        int totalTime = terminated.stream()
            .mapToInt(pcb -> pcb.getFinishTime())
            .max()
            .orElse(0) - terminated.stream()
            .mapToInt(pcb -> pcb.getArrivalTime())
            .min()
            .orElse(0);
        double throughput = totalTime > 0 ? (double) terminated.size() / totalTime : 0.0;

        return Map.of(
            "avgWaitingTime", avgWaitingTime,
            "avgTurnaroundTime", avgTurnaroundTime,
            "avgResponseTime", avgResponseTime,
            "throughput", throughput
        );
    }
}