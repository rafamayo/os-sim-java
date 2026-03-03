package woche03;

import java.util.List;
import java.util.Optional;

/**
 * SRT-Scheduler (Shortest Remaining Time First):
 * Ein präemptiver Scheduler, der immer den Prozess mit der kürzesten verbleibenden Ausführungszeit (remainingTime) auswählt.
 * Falls ein neuer Prozess mit kürzerer remainingTime ankommt, wird der aktuelle Prozess unterbrochen.
 */
public class SRTScheduler implements Scheduler {

    @Override
    public ProcessControlBlock schedule(List<ProcessControlBlock> readyQueue, int currentTime) {
        // --- 1. Filtere alle bereiten Prozesse ---
        // Berücksichtige nur Prozesse, die:
        // - bereits angekommen sind (arrivalTime <= currentTime),
        // - nicht terminiert sind (state != TERMINATED),
        // - noch Ausführungszeit benötigen (remainingTime > 0).
        Optional<ProcessControlBlock> shortestOpt = readyQueue.stream()
            .filter(pcb -> pcb.getArrivalTime() <= currentTime)      // Nur angekommene Prozesse
            .filter(pcb -> pcb.getState() != ProcessState.TERMINATED) // Ignoriere terminierte Prozesse
            .filter(pcb -> pcb.getRemainingTime() > 0)               // Nur Prozesse mit verbleibender Zeit
            .min((a, b) -> Integer.compare(a.getRemainingTime(), b.getRemainingTime()));  // Wähle den mit kürzester remainingTime

        // --- 2. Falls keine bereiten Prozesse vorhanden sind ---
        if (shortestOpt.isEmpty()) {
            return null;  // Kein Prozess verfügbar (Leerlauf)
        }

        // --- 3. Wähle den Prozess mit der kürzesten remainingTime ---
        ProcessControlBlock selected = shortestOpt.get();

        // Setze den Zustand auf RUNNING (falls nicht bereits gesetzt).
        // Dies ist besonders wichtig bei Präemption: Ein unterbrochener Prozess wird wieder auf RUNNING gesetzt.
        selected.setState(ProcessState.RUNNING);

        // --- 4. Setze die Startzeit, falls der Prozess zum ersten Mal läuft ---
        if (selected.getStartTime() == -1) {
            selected.setStartTime(currentTime);  // Erste Ausführung: Merke die Startzeit
        }

        // --- 5. Simuliere die Ausführung für 1 Zeiteinheit ---
        // SRT ist präemptiv: Jeder Prozess läuft maximal 1 Zeiteinheit, bevor neu entschieden wird.
        selected.setUsedTime(1);                          // Verbrauchte Zeit in diesem Quantum
        selected.setRemainingTime(selected.getRemainingTime() - 1);  // Reduziere die verbleibende Zeit

        // --- 6. Falls der Prozess fertig ist, markiere ihn als TERMINATED ---
        if (selected.getRemainingTime() == 0) {
            selected.setState(ProcessState.TERMINATED);
            selected.setFinishTime(currentTime + 1);  // Fertigstellungszeit = currentTime + 1 (da 1 Zeiteinheit ausgeführt wird)
        }

        // --- 7. Gib den ausgewählten Prozess zurück ---
        // Der Rückgabewert wird in testSRTScheduler verwendet, um den aktuellen Prozess zu aktualisieren.
        return selected;
    }
}

