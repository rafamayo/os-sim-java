package woche07;

/**
 * Prozesszustände – erweitert aus Woche 02/03.
 * In Woche 07 ist BLOCKED besonders relevant:
 * ein Prozess im BLOCKED-Zustand wartet auf eine Ressource.
 */
public enum ProcessState {
    NEW, READY, RUNNING, BLOCKED, TERMINATED
}
