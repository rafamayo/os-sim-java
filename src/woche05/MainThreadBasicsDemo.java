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

        t1.join();
        t2.join();

        System.out.println("[Main] done.");
    }
}
