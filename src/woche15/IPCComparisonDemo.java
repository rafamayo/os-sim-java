package woche15;

// ============================================================
// IPCComparisonDemo.java
// ============================================================

/**
 * Vergleicht alle drei IPC-Mechanismen am gleichen Szenario:
 * 10 Nachrichten von einem Producer zu einem Consumer.
 */
class IPCComparisonDemo {

    static final int N = 10;

    public static void main(String[] args) throws InterruptedException {

        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║  IPC Vergleich — alle 3 Mechanismen      ║");
        System.out.println("╚══════════════════════════════════════════╝");

        System.out.println("\n=== 1. Pipe ===");
        runWithPipe();

        System.out.println("\n=== 2. Message Queue ===");
        runWithMessageQueue();

        System.out.println("\n=== 3. Shared Memory ===");
        runWithSharedMemory();

        System.out.println("\n=== Zusammenfassung ===");
        System.out.println("Pipe:          Einfach, Bytestrom, Synchronisation eingebaut");
        System.out.println("Message Queue: Nachrichtengrenzen, Kapazitätslimit, Synchronisation eingebaut");
        System.out.println("Shared Memory: Schnellstes, kein Kopieren, Synchronisation MANUELL");
    }

    private static void runWithPipe() throws InterruptedException {
        Pipe pipe = new Pipe(256, "VglPipe");
        Thread writer = new Thread(() -> {
            try {
                for (int i = 1; i <= N; i++) {
                    pipe.write(("MSG" + i + "\n").getBytes());
                }
                pipe.close();
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        Thread reader = new Thread(() -> {
            try {
                int count = 0;
                while (true) {
                    byte[] data = pipe.read(64);
                    if (data.length == 0) break;
                    count++;
                }
                System.out.printf("  Pipe: %d Lesevorgänge für %d Nachrichten%n",
                    count, N);
                System.out.println("  (Bytestrom: mehrere Nachrichten können zusammen gelesen werden!)");
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        writer.start(); reader.start();
        writer.join(); reader.join();
    }

    private static void runWithMessageQueue() throws InterruptedException {
        MessageQueue mq = new MessageQueue(N, "VglQueue");
        Thread sender = new Thread(() -> {
            try {
                for (int i = 1; i <= N; i++) {
                    mq.send(new Message("DATA", "Nachricht " + i));
                }
                mq.send(new Message("EOF", ""));
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        Thread receiver = new Thread(() -> {
            try {
                int count = 0;
                while (true) {
                    Message m = mq.receive();
                    if ("EOF".equals(m.getType())) break;
                    count++;
                }
                System.out.printf("  MessageQueue: %d receive() für %d Nachrichten%n",
                    count, N);
                System.out.println("  (Nachrichtengrenzen erhalten: immer genau 1 Nachricht pro receive())");
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        sender.start(); receiver.start();
        sender.join(); receiver.join();
    }

    private static void runWithSharedMemory() throws InterruptedException {
        SharedMemory shm = new SharedMemory(256, "VglSHM");
        Thread writer = new Thread(() -> {
            try {
                for (int i = 1; i <= N; i++) {
                    shm.writeAndNotify(0, ("Nachricht " + i).getBytes());
                    Thread.sleep(50); // Zeit für Consumer
                }
                shm.writeAndNotify(0, "EOF".getBytes());
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        Thread reader = new Thread(() -> {
            try {
                int count = 0;
                while (true) {
                    shm.waitForData();
                    String data = shm.readString(32);
                    if ("EOF".equals(data)) break;
                    count++;
                }
                System.out.printf("  SharedMemory: %d Lese-Zyklen für %d Nachrichten%n",
                    count, N);
                System.out.println("  (Kein Kernel-Eingriff für Daten — aber manuelles Signaling nötig)");
                shm.printStats();
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        writer.start(); reader.start();
        writer.join(); reader.join();
    }
}
