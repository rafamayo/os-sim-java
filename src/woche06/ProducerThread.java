package woche06;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Producer-Thread für die Producer-Consumer-Demo.
 *
 * Aufgabe eines Producers:
 *
 *   - neue Daten erzeugen
 *   - diese Daten in den gemeinsamen Buffer einfügen
 *
 * In dieser Demo erzeugt der Producer einfach Integer-Werte:
 *
 *   startValue, startValue+1, startValue+2, ...
 *
 * Beispiel:
 *
 *   P1 erzeugt: 100, 101, 102, ...
 *   P2 erzeugt: 200, 201, 202, ...
 *
 * Dadurch kann man in der Ausgabe leicht erkennen,
 * welcher Producer welches Item erzeugt hat.
 *
 * Wichtig:
 *
 * Der Producer kümmert sich NICHT selbst um Synchronisation.
 * Er ruft dafür Methoden des BoundedBufferMonitor auf.
 *
 * Das ist didaktisch wichtig:
 *
 *   ProducerThread      -> beschreibt das Verhalten des Producers
 *   BoundedBufferMonitor -> sorgt für sichere Synchronisation
 */
public class ProducerThread implements Runnable {

    /**
     * Anzeigename des Producers.
     *
     * Wird nur für Logging-Ausgaben verwendet.
     */
    private final String name;

    /**
     * Referenz auf den gemeinsamen Monitor / Buffer.
     *
     * Alle Producer und Consumer greifen auf denselben Buffer zu.
     */
    private final BoundedBufferMonitor buffer;

    /**
     * Startwert der erzeugten Zahlenfolge.
     *
     * Beispiel:
     * - startValue = 100 → 100, 101, 102, ...
     * - startValue = 200 → 200, 201, 202, ...
     */
    private final int startValue;

    /**
     * Wie viele Items dieser Producer insgesamt erzeugen soll.
     */
    private final int nItems;

    public ProducerThread(String name, BoundedBufferMonitor buffer, int startValue, int nItems) {
        this.name = name;
        this.buffer = buffer;
        this.startValue = startValue;
        this.nItems = nItems;
    }

    /**
     * run() ist die Methode, die ausgeführt wird,
     * wenn ein Thread mit diesem Runnable gestartet wird.
     *
     * Beispiel:
     *
     *     new Thread(new ProducerThread(...)).start();
     *
     * Dann ruft die JVM intern diese run()-Methode auf.
     */
    @Override
    public void run() {
        try {

            /**
             * Schleife über alle zu erzeugenden Items.
             */
            for (int i = 0; i < nItems; i++) {

                /**
                 * Nächstes zu produzierendes Item berechnen.
                 */
                int v = startValue + i;

                /**
                 * Item in den gemeinsamen Buffer einfügen.
                 *
                 * WICHTIG:
                 * putWithSnapshot(...) ist eine Monitor-Methode.
                 *
                 * Das bedeutet:
                 * - Falls der Buffer voll ist, blockiert der Producer dort mit wait()
                 * - Falls Platz vorhanden ist, wird das Item eingefügt
                 * - Danach wird ein Snapshot des Bufferzustands zurückgegeben
                 *
                 * Aus Sicht des ProducerThreads ist das sehr komfortabel:
                 * Die ganze Synchronisationslogik steckt im Monitor.
                 */
                String snap = buffer.putWithSnapshot(v);

                /**
                 * Logging-Ausgabe:
                 * Welches Item wurde erzeugt und wie sieht der Buffer danach aus?
                 */
                System.out.println("[" + name + "] put " + v + " | " + snap);

                /**
                 * Kleine zufällige Pause.
                 *
                 * Warum?
                 *
                 * Diese Pause ist didaktisch nützlich, weil sie Interleavings sichtbarer macht.
                 * Dadurch sieht man in der Ausgabe besser,
                 * wie Producer und Consumer sich abwechseln.
                 *
                 * Ohne diese Pause wäre die Ausgabe oft weniger interessant,
                 * weil ein Thread eventuell sehr viele Schritte "am Stück" machen würde.
                 */
                Thread.sleep(ThreadLocalRandom.current().nextInt(10, 60));
            }

        } catch (InterruptedException e) {

            /**
             * Falls der Thread unterbrochen wird,
             * setzen wir das Interrupt-Flag erneut.
             *
             * Das ist Best Practice in Java.
             */
            Thread.currentThread().interrupt();
        }
    }
}
