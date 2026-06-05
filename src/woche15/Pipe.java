package woche15;

import java.util.Arrays;

/**
 * Simulierte Pipe — unidirektionaler Bytestrom zwischen zwei Prozessen.
 *
 * Eine Pipe ist ein Kernel-Puffer (hier: byte[]) mit zwei Endpunkten:
 *   - Schreiber: ruft write() auf
 *   - Leser:     ruft read() auf
 *
 * Eigenschaften:
 *   - FIFO: was zuerst geschrieben wird, wird zuerst gelesen
 *   - Bytestrom: KEINE Nachrichtengrenzen (anders als Message Queue)
 *   - Blocking: read() blockiert wenn leer, write() blockiert wenn voll
 *   - EOF: wenn Schreiber close() aufruft, liefert read() leeres Array
 *
 * Typische Nutzung in Unix-Shells:
 *   ls | grep .java
 *   stdout von "ls" ist über eine Pipe mit stdin von "grep" verbunden.
 */
public class Pipe {

    private final byte[] buffer;
    private int          writePos = 0;   // nächste Schreibposition
    private int          readPos  = 0;   // nächste Leseposition
    private int          available = 0; // Bytes im Puffer
    private boolean      closed   = false; // Schreibseite geschlossen?

    private final String name; // für Diagnoseausgabe

    /**
     * Erstellt eine neue Pipe mit dem angegebenen Pufferpuffer.
     *
     * @param capacity Puffergröße in Bytes
     * @param name     Bezeichnung für Ausgabe
     */
    public Pipe(int capacity, String name) {
        this.buffer = new byte[capacity];
        this.name   = name;
    }

    public Pipe(int capacity) {
        this(capacity, "Pipe");
    }

    // =========================================================
    // Schreiben
    // =========================================================

    /**
     * Schreibt Bytes in die Pipe.
     *
     * Blockiert wenn der Puffer voll ist, bis Platz frei wird.
     * Wirft IllegalStateException wenn die Pipe geschlossen ist.
     *
     * @param data Zu schreibende Bytes
     */
    public synchronized void write(byte[] data) throws InterruptedException {
        if (closed) {
            throw new IllegalStateException(name + ": Pipe ist geschlossen");
        }
        int written = 0;
        while (written < data.length) {
            // Warten bis Platz vorhanden
            while (available == buffer.length) {
                System.out.printf("[%s] Puffer voll (%d Bytes) — Schreiber wartet%n",
                    name, buffer.length);
                wait();
            }
            // So viele Bytes schreiben wie möglich
            int space = buffer.length - available;
            int toWrite = Math.min(space, data.length - written);
            for (int i = 0; i < toWrite; i++) {
                buffer[writePos] = data[written + i];
                writePos = (writePos + 1) % buffer.length;
            }
            available += toWrite;
            written   += toWrite;
            notifyAll(); // Leser aufwecken
        }
    }

    /**
     * Convenience: schreibt einen String als UTF-8.
     */
    public void write(String s) throws InterruptedException {
        write(s.getBytes());
    }

    // =========================================================
    // Lesen
    // =========================================================

    /**
     * Liest bis zu n Bytes aus der Pipe.
     *
     * Blockiert wenn die Pipe leer ist (und noch nicht geschlossen).
     * Gibt leeres Array zurück wenn die Pipe geschlossen und leer ist (EOF).
     *
     * @param n Maximale Anzahl zu lesender Bytes
     * @return Gelesene Bytes (kann weniger als n sein), oder leeres Array bei EOF
     */
    public synchronized byte[] read(int n) throws InterruptedException {
        // Warten bis Daten vorhanden oder EOF
        while (available == 0 && !closed) {
            System.out.printf("[%s] Puffer leer — Leser wartet (EOF=%b)%n",
                name, closed);
            wait();
        }
        // EOF: Pipe leer und geschlossen
        if (available == 0) {
            return new byte[0];
        }
        int toRead = Math.min(n, available);
        byte[] result = new byte[toRead];
        for (int i = 0; i < toRead; i++) {
            result[i] = buffer[readPos];
            readPos = (readPos + 1) % buffer.length;
        }
        available -= toRead;
        notifyAll(); // Schreiber aufwecken
        return result;
    }

    /**
     * Liest Daten und gibt sie als String zurück.
     */
    public String readString(int maxBytes) throws InterruptedException {
        return new String(read(maxBytes)).trim();
    }

    // =========================================================
    // Schließen / Status
    // =========================================================

    /**
     * Schließt die Schreibseite der Pipe.
     * Nach dem Lesen aller verbleibenden Bytes liefert read() EOF.
     */
    public synchronized void close() {
        closed = true;
        notifyAll(); // Wartende Leser aufwecken (damit sie EOF bemerken)
        System.out.printf("[%s] Schreibseite geschlossen (EOF)%n", name);
    }

    public synchronized boolean isClosed()   { return closed; }
    public synchronized int     available()  { return available; }
    public synchronized int     capacity()   { return buffer.length; }
    public String               getName()    { return name; }

    @Override
    public String toString() {
        return String.format("%s[cap=%d, available=%d, closed=%b]",
            name, buffer.length, available, closed);
    }
}
