package woche06;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Aufgabe 3: TaskQueue (Condition Variable Pattern mit wait/notifyAll).
 *
 * Diese Klasse implementiert eine sehr einfache Aufgaben-Warteschlange,
 * die von mehreren Threads gleichzeitig benutzt werden kann.
 *
 * Typisches Szenario:
 *
 *   - Producer-Threads legen Aufgaben in die Queue.
 *   - Worker-Threads holen Aufgaben aus der Queue und führen sie aus.
 *
 * Das Problem:
 *
 *   Wenn gerade keine Aufgaben vorhanden sind, sollen Worker-Threads
 *   NICHT ständig prüfen ("busy waiting"), sondern effizient schlafen.
 *
 * Die Lösung:
 *
 *   Wir verwenden das klassische Monitor-Muster mit
 *
 *        synchronized
 *        wait()
 *        notifyAll()
 *
 * Grundidee:
 *
 *   Worker warten solange, bis eine Aufgabe verfügbar ist.
 *
 *   Bedingung:
 *
 *        queue ist NICHT leer
 *
 *   Sobald ein Producer eine Aufgabe hinzufügt, werden wartende
 *   Worker geweckt.
 *
 * Zusätzlich gibt es ein Shutdown-Signal, damit Worker sauber
 * beendet werden können.
 */
public class TaskQueue {

    /**
     * Die eigentliche Aufgaben-Warteschlange.
     *
     * Wir verwenden eine Deque (Double Ended Queue), weil sie
     * sehr effizient für FIFO-Warteschlangen ist.
     *
     * Typischer Zugriff:
     *
     *   addLast()   -> neue Aufgabe hinten einfügen
     *   removeFirst() -> nächste Aufgabe vorne entnehmen
     *
     * Wichtig:
     * Diese Datenstruktur selbst ist NICHT thread-safe.
     * Deshalb schützen wir alle Zugriffe mit "synchronized".
     */
    private final Deque<Runnable> q = new ArrayDeque<>();

    /**
     * Gemeinsames Lock-Objekt.
     *
     * Dieses Objekt dient als Monitor:
     *
     *   synchronized(lock)
     *
     * Nur ein Thread gleichzeitig darf den geschützten Bereich betreten.
     *
     * Zusätzlich wird dieses Objekt für wait() / notifyAll() verwendet.
     */
    private final Object lock = new Object();

    /**
     * Shutdown-Flag.
     *
     * Wenn shutdown = true gesetzt wird,
     * sollen wartende Worker nicht weiter auf Aufgaben warten,
     * sondern die Arbeit beenden.
     */
    private boolean shutdown = false;

    /**
     * Liefert den aktuellen Shutdown-Status.
     *
     * Auch hier verwenden wir synchronized(lock),
     * damit das Lesen des Flags konsistent ist.
     */
    public boolean isShutdown() {
        synchronized (lock) {
            return shutdown;
        }
    }

    /**
     * Signalisiert, dass das System beendet werden soll.
     *
     * Schritte:
     *
     * 1. shutdown-Flag setzen
     * 2. alle wartenden Threads aufwecken
     *
     * Warum notifyAll()?
     *
     * Es könnten mehrere Worker in take() blockiert sein.
     * Diese müssen alle geweckt werden, damit sie bemerken:
     *
     *     "Das System fährt herunter."
     */
    public void shutdown() {
        synchronized (lock) {
            // TODO:
            // shutdown = true;
            // lock.notifyAll();
        }
    }

    /**
     * Fügt eine neue Aufgabe zur Queue hinzu.
     *
     * Dies entspricht dem Producer-Teil des Producer-Consumer-Musters.
     *
     * Schritte:
     *
     * 1. Aufgabe in die Queue einfügen
     * 2. wartende Worker aufwecken
     */
    public void submit(Runnable task) {
        synchronized (lock) {
            // TODO:
            // 1. Aufgabe in die Queue einfügen
            // 2. wartende Worker aufwecken
        }
    }

    /**
     * Holt die nächste Aufgabe aus der Queue.
     *
     * Diese Methode wird von Worker-Threads aufgerufen.
     *
     * Verhalten:
     *
     *   - Wenn eine Aufgabe vorhanden ist -> zurückgeben
     *   - Wenn keine Aufgabe vorhanden ist -> warten
     *   - Wenn shutdown aktiv ist -> null zurückgeben
     *
     * Das Muster hier ist sehr wichtig:
     *
     *     while(condition) wait()
     *
     * und NICHT
     *
     *     if(condition) wait()
     *
     * Warum?
     *
     * 1. Mehrere Threads könnten gleichzeitig geweckt werden
     * 2. "Spurious wakeups" sind möglich
     * 3. Die Bedingung kann nach dem Aufwachen wieder falsch sein
     */
    public Runnable take() throws InterruptedException {

        synchronized (lock) {
            // TODO:
            // Solange die Queue leer ist und kein Shutdown signalisiert wurde -> warten

            // TODO:
            // Dann, falls die Queue nicht leer ist, soll die erste Task entnommen und zurückgegebe werden

            // Sonst (shutdown && queue leer)
            return null;
        }
    }
}