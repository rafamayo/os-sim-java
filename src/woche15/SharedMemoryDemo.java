package ipc;

/**
 * Demo für Aufgabe 4 — Shared Memory (Woche 15).
 *
 * Drei Szenarien zeigen den Unterschied zwischen:
 *   1. Kein Signaling  → Consumer liest leeren Puffer
 *   2. Mit Signaling   → writeAndNotify() + waitForData() korrekt
 *   3. Producer-Consumer mit mehreren Nachrichten
 */
public class SharedMemoryDemo {

    public static void main(String[] args) throws InterruptedException {

        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║  Shared Memory Demo — Woche 15, Aufgabe 4        ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();

        demoOhneSignaling();
        demoMitSignaling();
        demoMehrereMachrichten();
    }

    // =========================================================
    // Szenario 1: Kein Signaling — das Problem
    // =========================================================

    static void demoOhneSignaling() throws InterruptedException {

        System.out.println("=== Szenario 1: Kein Signaling (das Problem) ===");
        System.out.println();
        System.out.println("  Producer schreibt nach 200ms.");
        System.out.println("  Consumer liest sofort — ohne zu warten.");
        System.out.println();

        SharedMemory shm = new SharedMemory(64, "SHM-ohneSignal");

        Thread producer = new Thread(() -> {
            try {
                Thread.sleep(200);  // Producer ist langsam
                shm.write(0, "Hallo Welt".getBytes());
                System.out.println("  [Producer] geschrieben: 'Hallo Welt'");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Producer");

        Thread consumer = new Thread(() -> {
            // Kein Warten — liest sofort
            String data = shm.readString(10);
            System.out.printf("  [Consumer] gelesen:     '%s'%n", data);
            System.out.println("  [Consumer] → leer! Producer hat noch nicht geschrieben.");
        }, "Consumer");

        consumer.start();  // Consumer startet zuerst
        producer.start();

        consumer.join();
        producer.join();

        System.out.println();
        System.out.println("  Problem: Consumer und Producer sind nicht synchronisiert.");
        System.out.println("  Der Consumer liest bevor der Producer schreibt.");
        System.out.println();
        System.out.println("  ────────────────────────────────────────────────");
        System.out.println();
    }

    // =========================================================
    // Szenario 2: Mit Signaling — die Lösung
    // =========================================================

    static void demoMitSignaling() throws InterruptedException {

        System.out.println("=== Szenario 2: Mit Signaling (die Lösung) ===");
        System.out.println();
        System.out.println("  Producer schreibt nach 200ms mit writeAndNotify().");
        System.out.println("  Consumer wartet mit waitForData() — wird aufgeweckt.");
        System.out.println();

        SharedMemory shm = new SharedMemory(64, "SHM-mitSignal");

        Thread producer = new Thread(() -> {
            try {
                Thread.sleep(200);  // Producer ist langsam
                System.out.println("  [Producer] schreibe mit writeAndNotify()...");
                shm.writeAndNotify(0, "Hallo Welt".getBytes());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Producer");

        Thread consumer = new Thread(() -> {
            try {
                System.out.println("  [Consumer] warte auf Daten mit waitForData()...");
                shm.waitForData();          // blockiert bis Producer schreibt
                String data = shm.readString(10);
                System.out.printf("  [Consumer] gelesen: '%s'  ✓%n", data);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Consumer");

        consumer.start();
        producer.start();

        consumer.join();
        producer.join();

        System.out.println();
        System.out.println("  Lösung: writeAndNotify() setzt dataAvailable=true");
        System.out.println("  und weckt den wartenden Consumer auf.");
        System.out.println();
        System.out.println("  ────────────────────────────────────────────────");
        System.out.println();
    }

    // =========================================================
    // Szenario 3: Mehrere Nachrichten
    // =========================================================

    static void demoMehrereMachrichten() throws InterruptedException {

        System.out.println("=== Szenario 3: Mehrere Nachrichten ===");
        System.out.println();
        System.out.println("  Producer sendet 5 Nachrichten.");
        System.out.println("  Consumer wartet auf jede einzeln.");
        System.out.println();

        SharedMemory shm = new SharedMemory(64, "SHM-mehrere");

        String[] nachrichten = {
            "Bestellung #1",
            "Bestellung #2",
            "Bestellung #3",
            "Bestellung #4",
            "Bestellung #5"
        };

        Thread producer = new Thread(() -> {
            try {
                for (String msg : nachrichten) {
                    Thread.sleep(150);
                    shm.writeAndNotify(0, msg.getBytes());
                    System.out.printf("  [Producer] gesendet:  '%s'%n", msg);
                }
                // EOF-Signal
                Thread.sleep(150);
                shm.writeAndNotify(0, "EOF".getBytes());
                System.out.println("  [Producer] EOF gesendet");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Producer");

        Thread consumer = new Thread(() -> {
            try {
                while (true) {
                    shm.waitForData();
                    String data = shm.readString(20);
                    if ("EOF".equals(data)) {
                        System.out.println("  [Consumer] EOF — fertig");
                        break;
                    }
                    System.out.printf("  [Consumer] empfangen: '%s'%n", data);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Consumer");

        producer.start();
        consumer.start();

        producer.join();
        consumer.join();

        System.out.println();
        shm.printStats();
        System.out.println();
        System.out.println("Beobachtung:");
        System.out.println("  writeAndNotify() und waitForData() synchronisieren");
        System.out.println("  Producer und Consumer — ohne Kernel-Eingriff für die Daten.");
        System.out.println("  Nur das Signaling läuft über den Kernel (wait/notify).");
        System.out.println("  Die Daten selbst liegen direkt im gemeinsamen Buffer.");
    }
}
