package woche06;

import java.util.Arrays;

/**
 * RingBuffer (nicht thread-safe).
 *
 * Diese Klasse implementiert einen sogenannten Ringpuffer
 * (englisch: circular buffer).
 *
 * Ein Ringbuffer speichert Elemente in einem festen Array
 * und verwendet zwei Zeiger:
 *
 *   head -> Position des nächsten Elements, das gelesen wird
 *   tail -> Position der nächsten freien Stelle zum Schreiben
 *
 * Zusätzlich speichern wir:
 *
 *   size -> Anzahl der aktuell im Buffer befindlichen Elemente
 *
 * Wichtiger Hinweis:
 *
 * Diese Klasse ist absichtlich NICHT thread-safe.
 *
 * Das bedeutet:
 * Mehrere Threads dürfen NICHT gleichzeitig direkt auf put() oder get()
 * zugreifen.
 *
 * Die notwendige Synchronisation übernimmt in dieser Aufgabe
 * die Klasse BoundedBufferMonitor.
 *
 * Didaktisch ist diese Trennung wichtig:
 *
 *   RingBuffer      -> Datenstruktur
 *   Monitor         -> Synchronisation
 *
 * So sehen die Studierenden klarer, welche Klasse für welche Aufgabe
 * zuständig ist.
 */
public class RingBuffer {

    /**
     * Das feste Array, in dem die Daten gespeichert werden.
     *
     * Die Kapazität wird beim Erzeugen des Buffers festgelegt
     * und ändert sich danach nicht mehr.
     */
    private final int[] data;

    /**
     * Index des nächsten Elements, das gelesen (entnommen) wird.
     *
     * Man kann sich head als "Leseposition" vorstellen.
     */
    private int head = 0;

    /**
     * Index der nächsten freien Position, an die geschrieben wird.
     *
     * Man kann sich tail als "Schreibposition" vorstellen.
     */
    private int tail = 0;

    /**
     * Anzahl der aktuell im Buffer gespeicherten Elemente.
     *
     * Warum brauchen wir size zusätzlich?
     *
     * Nur mit head und tail könnte man "leer" und "voll"
     * nicht immer eindeutig unterscheiden.
     */
    private int size = 0;

    /**
     * Konstruktor.
     *
     * Erzeugt einen RingBuffer mit fester Kapazität.
     */
    public RingBuffer(int capacity) {

        /**
         * Ein Buffer mit Kapazität 0 wäre sinnlos.
         */
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be > 0");
        }

        this.data = new int[capacity];
    }

    /**
     * Maximale Kapazität des Buffers.
     */
    public int capacity() {
        return data.length;
    }

    /**
     * Anzahl der aktuell enthaltenen Elemente.
     */
    public int size() {
        return size;
    }

    /**
     * Fügt ein neues Element in den Buffer ein.
     *
     * Ablauf:
     *
     * 1. Schreibe an Position tail
     * 2. Bewege tail um 1 weiter
     * 3. Erhöhe size
     *
     * Falls das Ende des Arrays erreicht ist,
     * springt tail wieder an den Anfang.
     *
     * Das geschieht durch:
     *
     *     (tail + 1) % data.length
     *
     * Genau dadurch wird das Array "ringförmig" verwendet.
     */
    public void put(int v) {

        /**
         * Falls der Buffer bereits voll ist,
         * darf nichts mehr eingefügt werden.
         *
         * In einer korrekt synchronisierten Producer-Consumer-Lösung
         * sollte dieser Fehler eigentlich nicht auftreten,
         * weil der Monitor vorher wartet.
         *
         * Trotzdem ist diese Prüfung sinnvoll,
         * um Fehler früh sichtbar zu machen.
         */
        if (size == data.length) {
            throw new IllegalStateException("Buffer overflow");
        }

        /**
         * Neues Element an der aktuellen Schreibposition einfügen.
         */
        data[tail] = v;

        /**
         * Schreibposition zyklisch weiterschieben.
         */
        tail = (tail + 1) % data.length;

        /**
         * Anzahl der gespeicherten Elemente erhöhen.
         */
        size++;
    }

    /**
     * Entfernt das nächste Element aus dem Buffer.
     *
     * Ablauf:
     *
     * 1. Lies an Position head
     * 2. Bewege head um 1 weiter
     * 3. Verringere size
     */
    public int get() {

        /**
         * Wenn der Buffer leer ist, kann nichts gelesen werden.
         *
         * Auch hier gilt:
         * In einer korrekt synchronisierten Lösung sollte dieser Fall
         * normalerweise nicht auftreten, weil der Monitor den Consumer
         * vorher warten lässt.
         */
        if (size == 0) {
            throw new IllegalStateException("Buffer underflow");
        }

        /**
         * Wert an der aktuellen Leseposition holen.
         */
        int v = data[head];

        /**
         * Leseposition zyklisch weiterschieben.
         */
        head = (head + 1) % data.length;

        /**
         * Anzahl der gespeicherten Elemente verringern.
         */
        size--;

        return v;
    }

    /**
     * Liefert die logische Sicht des Buffers.
     *
     * Das ist didaktisch sehr hilfreich:
     *
     * Intern liegen die Werte im Array eventuell "um den Ring herum"
     * verteilt, aber nach außen möchten wir den Buffer in der logischen
     * FIFO-Reihenfolge sehen.
     *
     * Beispiel:
     *
     *   head = 2, tail = 1, size = 2
     *
     * Dann könnte intern im Array etwas wie
     *
     *   [300, 0, 101]
     *
     * stehen, aber logisch enthält der Buffer:
     *
     *   [101, 300]
     *
     * Genau diese logische Reihenfolge berechnet die Methode.
     */
    public int[] logicalView() {

        /**
         * Neues Array mit genau size Elementen erzeugen.
         */
        int[] out = new int[size];

        /**
         * Die aktuell gespeicherten Elemente in FIFO-Reihenfolge kopieren.
         *
         * Wir starten bei head und laufen size Schritte weiter,
         * jeweils zyklisch modulo data.length.
         */
        for (int i = 0; i < size; i++) {
            out[i] = data[(head + i) % data.length];
        }

        return out;
    }

    /**
     * Liefert eine gut lesbare Darstellung des aktuellen Bufferzustands.
     *
     * Diese Methode ist vor allem für Debugging und Lehrzwecke gedacht.
     *
     * Die Ausgabe zeigt:
     *
     * - size      -> wie viele Elemente aktuell im Buffer sind
     * - head      -> wo das nächste Element gelesen wird
     * - tail      -> wo das nächste Element geschrieben wird
     * - logical   -> welche Elemente logisch im Buffer enthalten sind
     *
     * Die logische Sicht ist für Studierende meist viel verständlicher
     * als die rohe interne Arraydarstellung.
     */
    public String snapshot() {
        return "size=" + size + "/" + data.length +
               " head=" + head +
               " tail=" + tail +
               " logical=" + Arrays.toString(logicalView());
    }
}