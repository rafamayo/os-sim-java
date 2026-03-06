package woche05;

/**
 * Demonstriert eine Race Condition:
 * Zwei Threads erhöhen denselben gemeinsamen Zähler.
 */
public class MainRaceConditionDemo {

    public static void main(String[] args) throws InterruptedException {
        final int incrementsPerThread = 100_000;

        SharedCounter counter = new SharedCounter();

        Thread t1 = new Thread(new CounterWorker("W1", counter, incrementsPerThread));
        Thread t2 = new Thread(new CounterWorker("W2", counter, incrementsPerThread));

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        int expected = 2 * incrementsPerThread;

        System.out.println("[Main] expected = " + expected);
        System.out.println("[Main] actual   = " + counter.getValue());
        System.out.println("[Main] difference = " + (expected - counter.getValue()));

        /**
         * In vielen Läufen gilt:
         * actual < expected
         *
         * Grund:
         * value++ ist keine atomare Operation.
         */
    }
}
