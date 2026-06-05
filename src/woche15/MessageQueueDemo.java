package woche15;

// ============================================================
// MessageQueueDemo.java
// ============================================================

/**
 * Demonstriert die Message Queue mit Producer-Consumer-Muster.
 * Zeigt: Nachrichtengrenzen, Blocking, Kapazitätslimit.
 */
class MessageQueueDemo {

    public static void main(String[] args) throws InterruptedException {

        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║  Message Queue Demo (Woche 15)           ║");
        System.out.println("╚══════════════════════════════════════════╝");

        // Kleine Queue (Kapazität 3) um Blocking sichtbar zu machen
        MessageQueue queue = new MessageQueue(3, "Auftragsqueue");

        ProcessControlBlock producer = new ProcessControlBlock(10, "OrderService");
        ProcessControlBlock consumer = new ProcessControlBlock(11, "WorkerProcess");
        producer.attachQueue("orders", queue);
        consumer.attachQueue("orders", queue);

        // Producer: schickt 7 Aufträge schnell hintereinander
        Thread producerThread = new Thread(() -> {
            try {
                for (int i = 1; i <= 7; i++) {
                    producer.sendMessage("orders",
                        new Message("ORDER", "Bestellung #" + i));
                    Thread.sleep(50); // kurze Pause
                }
                // Shutdown-Signal senden
                producer.sendMessage("orders", new Message("SHUTDOWN", ""));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Producer");

        // Consumer: verarbeitet jeden Auftrag (langsamer als Producer)
        Thread consumerThread = new Thread(() -> {
            try {
                while (true) {
                    Message msg = consumer.receiveMessage("orders");
                    if (msg == null || "SHUTDOWN".equals(msg.getType())) {
                        System.out.println("[Consumer] SHUTDOWN empfangen — Ende");
                        break;
                    }
                    System.out.printf("[Consumer] Verarbeite: %s%n", msg.getData());
                    Thread.sleep(200); // Verarbeitung dauert länger
                }
                queue.printStats();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Consumer");

        consumerThread.start();
        producerThread.start();
        producerThread.join();
        consumerThread.join();

        System.out.println("\n[Demo] Beachte: Queue war voll = Producer musste warten!");
    }
}