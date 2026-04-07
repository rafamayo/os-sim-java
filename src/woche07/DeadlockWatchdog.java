package woche07;

import java.util.List;

/**
 * Timeout-basierter Watchdog zur Deadlock-Heuristik (Teil A).
 *
 * Idee: Wenn ALLE Philosophen gleichzeitig länger als WATCHDOG_TIMEOUT_MS
 * im Zustand BLOCKED sind, ist wahrscheinlich ein Deadlock eingetreten.
 *
 * Dies ist eine einfache, praxisnahe Heuristik (kein exakter Beweis).
 * Die exakte Zyklusdetektion folgt in Teil B mit dem Wait-for Graph.
 *
 * Vor- und Nachteile dieser Heuristik:
 *   + Einfach zu implementieren, keine Graphstruktur nötig
 *   - False Positives möglich (alle könnten zufällig gleichzeitig schlafen)
 *   - Schwellenwert (Timeout) schwer zu wählen
 */
public class DeadlockWatchdog extends Thread {

    private final List<DeadlockPhilosopher> philosophers;
    private volatile boolean running = true;

    public DeadlockWatchdog(List<DeadlockPhilosopher> philosophers) {
        super("Watchdog");
        this.setDaemon(true);
        this.philosophers = philosophers;
    }

    @Override
    public void run() {
        System.out.println("\n[Watchdog] Gestartet – überwache " + philosophers.size() + " Philosophen.");
        long allBlockedSince = -1;

        while (running) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                break;
            }

            // TODO (Aufgabe 3a): Zähle, wie viele Philosophen noch leben UND
            //   im Zustand BLOCKED sind. Nutze einen Stream mit zwei filter()-Aufrufen:
            //     Filter 1: p.isAlive()
            //     Filter 2: p.getPhilosopherState() == ProcessState.BLOCKED
            //   Ergebnis: long blockedCount
            // --->
            long blockedCount = 0; // TODO: ersetzen

            // TODO (Aufgabe 3b): Zähle, wie viele Philosophen noch leben (aliveCount).
            // --->
            long aliveCount = 0; // TODO: ersetzen

            if (aliveCount > 0 && blockedCount == aliveCount) {
                // Alle lebenden Philosophen sind BLOCKED
                if (allBlockedSince < 0) {
                    allBlockedSince = System.currentTimeMillis();
                    System.out.println("\n[Watchdog] ⚠  Alle Philosophen BLOCKED – Timeout-Zähler läuft...");
                } else if (System.currentTimeMillis() - allBlockedSince >= SimConfig.WATCHDOG_TIMEOUT_MS) {
                    System.out.println();
                    System.out.println("╔══════════════════════════════════════════════════╗");
                    System.out.println("║  [Watchdog] 🔴  DEADLOCK ERKANNT (Heuristik)    ║");
                    System.out.printf( "║  Alle %d Philosophen warten seit %4d ms          ║%n",
                            aliveCount, System.currentTimeMillis() - allBlockedSince);
                    System.out.println("║  Recovery: Unterbreche alle Threads (Abort)     ║");
                    System.out.println("╚══════════════════════════════════════════════════╝");

                    // TODO (Aufgabe 3c): Unterbreche alle Philosophen-Threads.
                    //   Dies ist die einfachste Recovery-Maßnahme: Process Abort.
                    //   Nutze: philosophers.forEach(Thread::interrupt)
                    // --->

                    running = false;
                }
            } else {
                if (allBlockedSince >= 0) {
                    System.out.println("[Watchdog] Entspannung erkannt – reset Timeout.");
                    allBlockedSince = -1;
                }
            }
        }
        System.out.println("[Watchdog] Beendet.");
    }

    public void stopWatchdog() {
        running = false;
        this.interrupt();
    }
}
