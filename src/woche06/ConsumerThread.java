package woche06;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Consumer-Thread für die Producer-Consumer-Demo.
 *
 * Aufgabe eines Consumers:
 *
 *   - Daten aus dem gemeinsamen Buffer entnehmen
 *   - diese "verbrauchen"
 *
 * In dieser Demo bedeutet "verbrauchen" vor allem:
 *
 *   - das Item aus dem Buffer holen
 *   - den Zustand des Buffers ausgeben
 *
 * Wie beim Producer gilt:
 *
 * Der Consumer kümmert sich NICHT selbst um Synchronisation.
 * Stattdessen benutzt er die Methoden des BoundedBufferMonitor.
 *
 * Dadurch bleibt die Rollenverteilung klar:
 *
 *   ConsumerThread       -> beschreibt das Verhalten des Consumers
 *   BoundedBufferMonitor -> sorgt für sichere Synchronisation
 */
public class ConsumerThread implements Runnable {

    /**
     * Anzeigename des Consumers.
     *
     * Wird nur für Logging-Ausgaben verwendet.
     */
    private final String name;

    /**
     * Referenz auf den gemeinsamen Monitor / Buffer.
     *
     * Alle Consumer und Producer benutzen dieselbe Instanz.
     */
    private final BoundedBufferMonitor buffer;

    /**
     * Wie viele Items dieser Consumer insgesamt entnehmen soll.
     */
    private final int nItems;

    public ConsumerThread(String name, BoundedBufferMonitor buffer, int nItems) {
        this.name = name;
        this.buffer = buffer;
        this.nItems = nItems;
    }

    /**
     * run() ist die Einstiegsmethode des Threads.
     *
     * Sie wird von der JVM ausgeführt, sobald der Thread gestartet wird.
     */
    @Override
    public void run() {
        try {

            /**
             * Schleife über alle zu konsumierenden Items.
             */
            for (int i = 0; i < nItems; i++) {

                /**
                 * Nächstes Item aus dem Buffer entnehmen.
                 *
                 * WICHTIG:
                 * getWithSnapshot() ist eine Monitor-Methode.
                 *
                 * Das bedeutet:
                 * - Falls der Buffer leer ist, blockiert der Consumer dort mit wait()
                 * - Falls ein Item vorhanden ist, wird es entnommen
                 * - Danach wird ein Snapshot des Bufferzustands zurückgegeben
                 *
                 * Die Methode liefert ein kleines Ergebnisobjekt zurück:
                 *
                 *   - value     -> der entnommene Wert
                 *   - snapshot  -> Bufferzustand nach der Operation
                 */
                BoundedBufferMonitor.GetResult r = buffer.getWithSnapshot();

                /**
                 * Logging-Ausgabe:
                 * Welches Item wurde konsumiert und wie sieht der Buffer danach aus?
                 */
                System.out.println("[" + name + "] got " + r.value() + " | " + r.snapshot());

                /**
                 * Kleine zufällige Pause.
                 *
                 * Genau wie beim Producer dient diese Pause vor allem dazu,
                 * Interleavings sichtbar zu machen.
                 *
                 * Dadurch beobachtet man besser,
                 * wie sich Producer und Consumer in der Ausgabe abwechseln.
                 */
                Thread.sleep(ThreadLocalRandom.current().nextInt(10, 60));
            }

        } catch (InterruptedException e) {

            /**
             * Falls der Thread unterbrochen wird,
             * setzen wir das Interrupt-Flag erneut.
             */
            Thread.currentThread().interrupt();
        }
    }
}