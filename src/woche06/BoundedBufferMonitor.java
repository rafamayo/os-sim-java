package woche06;

/**
 * Aufgabe 4: Producer–Consumer mit einem Monitor.
 *
 * Diese Klasse implementiert einen sogenannten "Monitor".
 *
 * Ein Monitor ist ein Synchronisationsmuster, das zwei Dinge kombiniert:
 *
 *   1. Mutual Exclusion
 *      → Nur ein Thread darf gleichzeitig den kritischen Abschnitt betreten.
 *
 *   2. Condition Synchronization
 *      → Threads können warten, bis eine bestimmte Bedingung erfüllt ist.
 *
 * In Java wird ein Monitor typischerweise mit folgenden Mechanismen umgesetzt:
 *
 *     synchronized
 *     wait()
 *     notifyAll()
 *
 * In dieser Klasse verwalten wir einen begrenzten Puffer (bounded buffer),
 * der von mehreren Producer- und Consumer-Threads gemeinsam benutzt wird.
 *
 * Die Regeln:
 *
 *   Producer dürfen nur dann ein Item einfügen, wenn der Buffer NICHT voll ist.
 *
 *   Consumer dürfen nur dann ein Item entnehmen, wenn der Buffer NICHT leer ist.
 *
 * Diese beiden Bedingungen werden hier mit wait() und notifyAll() koordiniert.
 */
public class BoundedBufferMonitor {

    /**
     * Der eigentliche Datenpuffer.
     *
     * RingBuffer ist eine einfache FIFO-Datenstruktur
     * (First-In-First-Out).
     *
     * Producer legen neue Items hinein.
     * Consumer holen Items wieder heraus.
     *
     * WICHTIG:
     * RingBuffer selbst ist NICHT thread-safe.
     *
     * Deshalb dürfen alle Zugriffe nur innerhalb der
     * synchronized-Methoden dieses Monitors stattfinden.
     */
    private final RingBuffer rb;

    /**
     * Konstruktor.
     *
     * Erzeugt einen neuen RingBuffer mit einer festen Kapazität.
     */
    public BoundedBufferMonitor(int capacity) {
        this.rb = new RingBuffer(capacity);
    }

    /**
     * Producer-Methode: fügt ein neues Item in den Buffer ein.
     *
     * Der Name "putWithSnapshot" bedeutet:
     *
     * - put → Item einfügen
     * - Snapshot → aktuellen Bufferzustand als String zurückgeben
     *
     * Der Snapshot wird hauptsächlich für Logging und Debugging benutzt.
     */
    public synchronized String putWithSnapshot(int item) throws InterruptedException {

        // TODO:
        // 1. Warten, solange der Buffer voll ist
        //    while (rb.size() == rb.capacity()) { wait(); }
        // 2. Item einfügen
        //    rb.put(item);
        // 3. Snapshot erzeugen
        //    String snap = rb.snapshot();
        // 4. notifyAll() aufrufen
        // 5. snap zurückgeben

        /**
         * Solange der Buffer voll ist, muss der Producer warten.
         *
         * Bedingung:
         *
         *     buffer not full
         *
         * Solange diese Bedingung NICHT erfüllt ist,
         * darf kein neues Item eingefügt werden.
         */
        // ---> CODE

        /**
         * Jetzt ist garantiert:
         *
         *     buffer not full
         *
         * Also kann der Producer ein neues Item einfügen.
         */
        // ---> CODE

        /**
         * Snapshot des aktuellen Bufferzustands erzeugen.
         *
         * Dies dient nur zur Visualisierung der Systemzustände.
         */
        String snap = rb.snapshot();

        /**
         * Alle wartenden Threads aufwecken!
         *
         * Warum?
         *
         * Möglicherweise warten gerade Consumer darauf,
         * dass ein neues Item verfügbar wird.
         *
         * Diese Consumer sollen jetzt wieder prüfen dürfen,
         * ob ihre Bedingung erfüllt ist.
         */
        // ---> CODE

        notifyAll();

        // Zurückgeben (Logging)
        return snap;
    }

    /**
     * Consumer-Methode: entnimmt ein Item aus dem Buffer.
     *
     * Auch hier geben wir zusätzlich einen Snapshot zurück,
     * damit man den Bufferzustand nach der Operation sehen kann.
     */
    public synchronized GetResult getWithSnapshot() throws InterruptedException {

        // TODO:
        // 1. Warten, solange der Buffer leer ist
        //    while (rb.size() == 0) { wait(); }
        // 2. Wert entnehmen
        //    int v = rb.get();
        // 3. Snapshot erzeugen
        //    String snap = rb.snapshot();
        // 4. notifyAll() aufrufen
        // 5. return new GetResult(v, snap);

        /**
         * Solange der Buffer leer ist,
         * müssen Consumer warten.
         *
         * Bedingung:
         *
         *     buffer not empty
         */
        // ---> CODE

        /**
         * Jetzt ist garantiert:
         *
         *     buffer not empty
         *
         * Also kann ein Item entnommen werden.
         */
        // ---> CODE

        /**
         * Snapshot nach der Operation erzeugen.
         */
        String snap = rb.snapshot();

        /**
         * Alle wartenden Producer aufwecken!
         *
         * Warum?
         *
         * Ein Producer könnte darauf warten,
         * dass wieder Platz im Buffer frei wird.
         */
        // ---> CODE
        

        /**
         * Rückgabe als kleines Ergebnisobjekt.
         *
         * Java record ist eine kompakte Klasse,
         * die nur Daten enthält.
         */
        return new GetResult(v, snap);
    }

    /**
     * Kleine Hilfsklasse (Record) für das Ergebnis eines get().
     *
     * Sie enthält:
     *
     *   - den entnommenen Wert
     *   - einen Snapshot des Buffers
     */
    public static record GetResult(int value, String snapshot) {}

    /**
     * Liefert einen Snapshot des aktuellen Bufferzustands.
     *
     * Auch diese Methode ist synchronized,
     * damit der Bufferzustand konsistent gelesen wird.
     */
    public synchronized String snapshot() {
        return rb.snapshot();
    }
}