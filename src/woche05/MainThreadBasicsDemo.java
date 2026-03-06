package woche05;

/**
 * Startet zwei einfache Threads und demonstriert:
 * - start()
 * - nicht deterministische Interleavings
 * - join()
 */
public class MainThreadBasicsDemo {

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(new MessagePrinter("T1", 5));
        Thread t2 = new Thread(new MessagePrinter("T2", 5));

        System.out.println("[Main] starting both threads...");
        t1.start();
        t2.start();

        /**
         * join() bedeutet:
         * Der Main-Thread wartet, bis der jeweilige Thread beendet ist.
         *
         * Ohne join() könnte main vor den Worker-Threads fertig werden.
         */
        t1.join();
        t2.join();

        System.out.println("[Main] done.");
    }
}
