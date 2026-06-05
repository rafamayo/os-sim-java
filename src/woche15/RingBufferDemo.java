package woche15;

// ============================================================
// RingBufferDemo.java
// ============================================================

/**
 * Demonstriert den Blocking Ringpuffer mit mehreren Producern und Consumern.
 */
class RingBufferDemo {

    public static void main(String[] args) throws InterruptedException {

        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║  Blocking Ringbuffer Demo (Woche 15)     ║");
        System.out.println("╚══════════════════════════════════════════╝");

        BlockingRingBuffer<String> buffer = new BlockingRingBuffer<>(4, "SharedBuffer");

        // 2 Producer, 2 Consumer
        Thread p1 = new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    buffer.put("P1-Item-" + i);
                    System.out.printf("[P1] put: P1-Item-%d  %s%n", i, buffer);
                    Thread.sleep(80);
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }, "Producer-1");

        Thread p2 = new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    buffer.put("P2-Item-" + i);
                    System.out.printf("[P2] put: P2-Item-%d  %s%n", i, buffer);
                    Thread.sleep(120);
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }, "Producer-2");

        Thread c1 = new Thread(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    String item = buffer.take();
                    System.out.printf("[C1] take: %s  %s%n", item, buffer);
                    Thread.sleep(150);
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }, "Consumer-1");

        Thread c2 = new Thread(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    String item = buffer.take();
                    System.out.printf("[C2] take: %s  %s%n", item, buffer);
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }, "Consumer-2");

        c1.start(); c2.start();
        p1.start(); p2.start();

        p1.join(); p2.join(); c1.join(); c2.join();

        buffer.printStats();
    }
}