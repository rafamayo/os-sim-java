package woche15;

import java.util.Arrays;

/**
 * Simulierter Shared Memory (Woche 15).
 *
 * Schnellster IPC-Mechanismus: kein Kopieren in Kernel-Puffer,
 * beide Prozesse lesen und schreiben direkt ins selbe byte[]-Array.
 *
 * KEIN eingebauter Schutz — Synchronisation ist Aufgabe des Programmierers!
 * Das ist der fundamentale Unterschied zu Pipe und Message Queue.
 *
 * write() und read() sind bereits implementiert (einfaches synchronized).
 * Ihre Aufgabe: writeAndNotify() und waitForData() — das Producer-Consumer-
 * Signaling das nötig ist damit der Leser weiß wann neue Daten da sind.
 */
public class SharedMemory {

    private final byte[]  buffer;
    private final Object  lock          = new Object();
    private boolean       dataAvailable = false;
    private final String  name;

    private int writes = 0, reads = 0, waits = 0;

    public SharedMemory(int size, String name) {
        this.buffer = new byte[size];
        this.name   = name;
    }

    public SharedMemory(int size) { this(size, "SharedMemory"); }

    // =========================================================
    // Bereits implementiert — lesen und verstehen
    // =========================================================

    /**
     * Schreibt Daten an einen Offset.
     * Thread-sicher durch synchronized — aber kein Signaling an Leser.
     * Der Leser weiß nicht dass neue Daten da sind!
     */
    public void write(int offset, byte[] data) {
        synchronized (lock) {
            checkBounds(offset, data.length);
            System.arraycopy(data, 0, buffer, offset, data.length);
            writes++;
        }
    }

    /**
     * Liest Daten von einem Offset.
     * Thread-sicher — aber liest was auch immer gerade im Buffer steht.
     */
    public byte[] read(int offset, int length) {
        synchronized (lock) {
            checkBounds(offset, length);
            reads++;
            return Arrays.copyOfRange(buffer, offset, offset + length);
        }
    }

    // =========================================================
    // Aufgabe 4a — writeAndNotify()
    // =========================================================

    /**
     * Schreibt Daten UND signalisiert einem wartenden Leser.
     *
     * Warum reicht write() allein nicht?
     * Ein Consumer der waitForData() aufruft, schläft solange
     * dataAvailable == false. Ohne writeAndNotify() würde er
     * ewig schlafen — niemand weckt ihn auf.
     *
     * TODO (Aufgabe 4a):
     *   synchronized(lock) {
     *     Schritt 1: checkBounds(offset, data.length)
     *     Schritt 2: System.arraycopy(data, 0, buffer, offset, data.length)
     *     Schritt 3: writes++
     *     Schritt 4: dataAvailable = true
     *     Schritt 5: System.out.printf("[%s] writeAndNotify: %d Bytes ab Offset %d%n",
     *                    name, data.length, offset)
     *     Schritt 6: lock.notifyAll()
     *   }
     */
    public void writeAndNotify(int offset, byte[] data) {
        // TODO: Implementieren
        throw new UnsupportedOperationException("writeAndNotify() nicht implementiert");
    }

    // =========================================================
    // Aufgabe 4b — waitForData()
    // =========================================================

    /**
     * Blockiert bis writeAndNotify() aufgerufen wurde.
     *
     * Das Muster ist dasselbe wie bei BlockingRingBuffer.take() —
     * WHILE statt IF, weil mehrere Consumer gleichzeitig aufwachen könnten.
     *
     * TODO (Aufgabe 4b):
     *   synchronized(lock) {
     *     Schritt 1: WHILE !dataAvailable:
     *                    System.out.printf("[%s] waitForData(): warte...%n", name)
     *                    waits++
     *                    lock.wait()
     *     Schritt 2: dataAvailable = false
     *                (zurücksetzen — nächster Aufruf wartet wieder)
     *     Schritt 3: System.out.printf("[%s] waitForData(): Daten verfügbar%n", name)
     *   }
     */
    public void waitForData() throws InterruptedException {
        // TODO: Implementieren
        throw new UnsupportedOperationException("waitForData() nicht implementiert");
    }

    // =========================================================
    // Convenience-Methoden — bereits implementiert
    // =========================================================

    /** Schreibt String als UTF-8 an Offset 0. */
    public void writeString(String s) { write(0, s.getBytes()); }

    /** Liest n Bytes ab Offset 0 als String. */
    public String readString(int n) { return new String(read(0, n)).trim(); }

    public int    size()    { return buffer.length; }
    public String getName() { return name; }

    public void printStats() {
        System.out.printf("[%s] Statistik: writes=%d, reads=%d, waits=%d%n",
            name, writes, reads, waits);
    }

    private void checkBounds(int offset, int length) {
        if (offset < 0 || offset + length > buffer.length) {
            throw new ArrayIndexOutOfBoundsException(
                String.format("%s: offset=%d + length=%d > size=%d",
                    name, offset, length, buffer.length));
        }
    }
}
