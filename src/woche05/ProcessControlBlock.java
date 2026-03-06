package woche05;

/**
 * Stark vereinfachtes Process Control Block (PCB).
 *
 * Ziel:
 * Die Klasse dient nicht als vollständige OS-Modellierung,
 * sondern als kleine Hilfe, um gemeinsame Zustände sichtbar zu machen.
 *
 * In Woche 05 ist besonders wichtig:
 * Mehrere Threads können denselben Zustand gleichzeitig verändern.
 * Dadurch können unerwartete Zustandsfolgen entstehen.
 */
public class ProcessControlBlock {

    /** Prozess-ID (hier nur für die Demo). */
    private final int pid;

    /** Anzeigename für Logging-Ausgaben. */
    private final String name;

    /** Aktueller Zustand. */
    private ProcessState state;

    public ProcessControlBlock(int pid, String name) {
        this.pid = pid;
        this.name = name;
        this.state = ProcessState.NEW;
    }

    public int getPid() {
        return pid;
    }

    public String getName() {
        return name;
    }

    public ProcessState getState() {
        return state;
    }

    /**
     * Setzt den Zustand des PCB.
     *
     * WICHTIG:
     * Diese Methode ist absichtlich nicht synchronisiert.
     * In Woche 05 wollen wir genau beobachten,
     * dass konkurrierende Zugriffe auf gemeinsame Zustände problematisch sein können.
     */
    public void setState(ProcessState state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "PCB{pid=" + pid + ", name='" + name + "', state=" + state + "}";
    }
}