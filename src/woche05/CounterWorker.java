package woche05;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Ein Worker, der denselben SharedCounter mehrfach erhöht.
 */
public class CounterWorker implements Runnable {

    private final String name;
    private final SharedCounter counter;
    private final int increments;

    public CounterWorker(String name, SharedCounter counter, int increments) {
        this.name = name;
        this.counter = counter;
        this.increments = increments;
    }

    @Override
    public void run() {
        for (int i = 0; i < increments; i++) {
            counter.increment();

            /**
             * Kleine Zufallspausen machen Interleavings sichtbarer
             * und erhöhen die Chance, dass Race Conditions beobachtet werden.
             */
            if (i % 100 == 0) {
                try {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(0, 2));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        System.out.println("[" + name + "] finished.");
    }
}
