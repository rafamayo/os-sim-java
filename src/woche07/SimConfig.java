package woche07;

/**
 * Zentrale Simulationsparameter für Woche 07.
 * Alle Zeitwerte in Millisekunden.
 */
public class SimConfig {

    /** Anzahl Philosophen (= Anzahl Gabeln) */
    public static final int NUM_PHILOSOPHERS = 5;

    /** Denkzeit pro Runde */
    public static final long THINK_TIME_MS = 100;

    /** Essenszeit pro Runde */
    public static final long EAT_TIME_MS = 150;

    /**
     * Verzögerung zwischen dem Aufnehmen der ersten und zweiten Gabel.
     * Vergrößert das Zeitfenster, in dem alle Philosophen gleichzeitig
     * ihre erste Gabel halten → Deadlock wahrscheinlicher.
     */
    public static final long FORK_PICKUP_DELAY_MS = 80;

    /**
     * Watchdog-Timeout: Wenn alle Threads länger als diese Zeit im
     * BLOCKED-Zustand sind, wird ein Deadlock vermutet.
     */
    public static final long WATCHDOG_TIMEOUT_MS = 3000;

    /** Anzahl Ess-Runden pro Philosoph */
    public static final int ROUNDS = 3;

    private SimConfig() {}
}
