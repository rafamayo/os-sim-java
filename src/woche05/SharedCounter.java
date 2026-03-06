package woche05;

/**
 * Gemeinsamer Zähler OHNE Synchronisation.
 *
 * Genau das ist hier didaktisch gewollt:
 * Die Studierenden sollen beobachten, dass parallele Zugriffe
 * auf gemeinsame Daten problematisch sind.
 */
public class SharedCounter {

    private int value = 0;

    /**
     * Aufgabe:
     * Erhöht den Zähler um 1.
     *
     * WICHTIG:
     * Diese Methode ist absichtlich nicht synchronisiert.
     * Das soll die Race Condition sichtbar machen.
     */
    public void increment() {
        // TODO:
        // Inkrementieren Sie den Wert!
    }

    public int getValue() {
        return value;
    }
}
