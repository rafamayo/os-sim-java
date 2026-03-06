package woche05;

/**
 * Vereinfachte Prozesszustände für die Lehrdemo.
 *
 * In echten Betriebssystemen gibt es je nach Modell noch weitere Zustände
 * (z. B. WAITING, BLOCKED, SUSPENDED ...).
 * Für Woche 05 reichen uns diese vier Zustände.
 */
public enum ProcessState {
    NEW,
    READY,
    RUNNING,
    TERMINATED
}
