package woche11;

/**
 * Interface für Seitenersetzungsalgorithmen -- Woche 11
 *
 * Jede Implementierung verwaltet eine feste Anzahl physischer Frames
 * und entscheidet bei einem Page Fault, welcher Frame verdrängt wird.
 */
public interface PageReplacer {

    /**
     * Verarbeitet einen Seitenzugriff.
     * Gibt die Anzahl der Page Faults zurück, die dieser Zugriff ausgelöst hat
     * (0 = Hit, 1 = Fault).
     *
     * @param page  Seitennummer des Zugriffs
     * @return 1 bei Page Fault, 0 bei Hit
     */
    int access(int page);

    /** Gibt die Gesamtzahl der Page Faults zurück. */
    int getPageFaults();

    /** Gibt den aktuellen Frame-Inhalt zurück (für Visualisierung). */
    int[] getFrames();

    /** Name des Algorithmus (für Ausgaben). */
    String name();
}
