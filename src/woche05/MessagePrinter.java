package woche05;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Aufgabe 1:
 * Ein MessagePrinter ist eine kleine Aufgabe, die mehrfach eine Nachricht ausgibt.
 *
 * Die zufälligen Sleep-Zeiten sind nur didaktisch motiviert:
 * Sie erhöhen die Chance, dass sich die Ausgaben der Threads sichtbar mischen.
 */
public class MessagePrinter implements Runnable {

    private final String workerName;
    private final int repetitions;

    public MessagePrinter(String workerName, int repetitions) {
        this.workerName = workerName;
        this.repetitions = repetitions;
    }

    @Override
    public void run() {
        // TODO:
        // 1. Durchlaufen Sie eine Schleife von 1 bis repetitions.
        // 2. Geben Sie pro Durchlauf eine Zeile der Form aus:
        //    [Name] message i
        // 3. Schlafen Sie pro Iteration kurz:
        //    Thread.sleep(ThreadLocalRandom.current().nextInt(10, 60));
        // 4. Falls InterruptedException auftritt:
        //    - setzen Sie das Interrupt-Flag erneut
        //    - beenden Sie die Methode
    }
}
