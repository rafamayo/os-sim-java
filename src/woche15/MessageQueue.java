package woche15;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Simulierte Message Queue — Nachrichtenwarteschlange (Woche 15).
 *
 * Unterschied zur Pipe:
 *   - Nachrichtengrenzen bleiben erhalten: jedes send/receive ist eine Einheit
 *   - Kapazitätslimit in Anzahl Nachrichten (nicht Bytes)
 *   - send() blockiert wenn Queue voll
 *   - receive() blockiert wenn Queue leer
 *
 * Das Synchronisationsmuster ist dasselbe wie beim BlockingRingBuffer
 * (Aufgabe 2) — nur die Datenstruktur ist eine Queue statt ein Array.
 */
public class MessageQueue {

    private final Queue<Message> queue;
    private final int            capacity;
    private final String         name;
    private boolean              closed = false;

    private int totalSent = 0, totalReceived = 0, totalWaits = 0;

    public MessageQueue(int capacity, String name) {
        this.capacity = capacity;
        this.name     = name;
        this.queue    = new ArrayDeque<>(capacity);
    }

    public MessageQueue(int capacity) { this(capacity, "MessageQueue"); }

    // =========================================================
    // Aufgabe 3a — send()
    // =========================================================

    /**
     * Sendet eine Nachricht. Blockiert wenn Queue voll.
     *
     * TODO (Aufgabe 3a) — Methode muss synchronized sein:
     *
     *   Schritt 1: Falls closed → IllegalStateException
     *
     *   Schritt 2: WHILE queue.size() >= capacity:
     *                  System.out.printf("[%s] Queue voll — Sender wartet%n", name)
     *                  totalWaits++
     *                  wait()
     *
     *   Schritt 3: queue.add(msg)
     *
     *   Schritt 4: totalSent++
     *
     *   Schritt 5: System.out.printf("[%s] send: %s  (Queue: %d/%d)%n",
     *                  name, msg, queue.size(), capacity)
     *
     *   Schritt 6: notifyAll()
     */
    public synchronized void send(Message msg) throws InterruptedException {
        // TODO: Implementieren
        throw new UnsupportedOperationException("send() nicht implementiert");
    }

    // =========================================================
    // Aufgabe 3b — receive()
    // =========================================================

    /**
     * Empfängt eine Nachricht. Blockiert wenn Queue leer.
     * Gibt null zurück wenn Queue geschlossen und leer (EOF).
     *
     * TODO (Aufgabe 3b) — Methode muss synchronized sein:
     *
     *   Schritt 1: WHILE queue.isEmpty() UND !closed:
     *                  System.out.printf("[%s] Queue leer — Empfänger wartet%n", name)
     *                  totalWaits++
     *                  wait()
     *
     *   Schritt 2: Falls queue.isEmpty() → return null  (EOF)
     *
     *   Schritt 3: Message msg = queue.poll()
     *
     *   Schritt 4: totalReceived++
     *
     *   Schritt 5: System.out.printf("[%s] receive: %s  (Queue: %d/%d)%n",
     *                  name, msg, queue.size(), capacity)
     *
     *   Schritt 6: notifyAll()
     *
     *   Schritt 7: return msg
     */
    public synchronized Message receive() throws InterruptedException {
        // TODO: Implementieren
        throw new UnsupportedOperationException("receive() nicht implementiert");
    }

    // =========================================================
    // Bereits implementiert — nicht ändern
    // =========================================================

    /**
     * Non-blocking Empfang — bereits implementiert.
     * Gibt null zurück wenn Queue leer, ohne zu warten.
     */
    public synchronized Message tryReceive() {
        if (queue.isEmpty()) return null;
        Message msg = queue.poll();
        totalReceived++;
        notifyAll();
        return msg;
    }

    public synchronized void close() {
        closed = true;
        notifyAll();
        System.out.printf("[%s] Queue geschlossen%n", name);
    }

    public synchronized int     size()     { return queue.size(); }
    public int                  capacity() { return capacity; }
    public synchronized boolean isEmpty()  { return queue.isEmpty(); }
    public synchronized boolean isClosed() { return closed; }
    public String               getName()  { return name; }

    public void printStats() {
        System.out.printf("[%s] Statistik: sent=%d, received=%d, waits=%d%n",
            name, totalSent, totalReceived, totalWaits);
    }
}
