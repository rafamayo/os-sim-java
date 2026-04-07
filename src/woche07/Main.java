package woche07;

import java.util.ArrayList;
import java.util.List;

/**
 * Hauptklasse Woche 07 – Deadlocks.
 *
 * Pflichtaufgaben (Aufgaben 1–3):
 *   java woche07.Main partA_deadlock   → Teil A, naive Variante
 *   java woche07.Main partA_safe       → Teil A, Lock-Ordering
 *
 * Bonusaufgaben (Aufgaben 4–6):
 *   java woche07.Main partB            → Teil B, Wait-for Graph
 *   java woche07.Main all              → alles nacheinander
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {
        String mode = args.length > 0 ? args[0] : "partA_deadlock";

        switch (mode) {
            case "partA_deadlock" -> runPartA_Deadlock();
            case "partA_safe"     -> runPartA_Safe();
            case "partB"          -> runPartB();
            case "all" -> {
                runPartA_Deadlock();
                separator();
                runPartA_Safe();
                separator();
                runPartB();
            }
            default -> System.err.println("Unbekannter Modus: " + mode);
        }
    }

    // =========================================================================
    //  PFLICHT – Teil A, Szenario 1: Naive Strategie → Deadlock möglich
    // =========================================================================
    static void runPartA_Deadlock() throws InterruptedException {
        header("TEIL A – Szenario 1: Naive Strategie (Deadlock möglich)");
        System.out.println("Jeder Philosoph nimmt zuerst die LINKE, dann die RECHTE Gabel.");
        System.out.println("Coffman-Bedingung 'Circular Wait' ist erfüllbar!\n");

        printTable();

        int n = SimConfig.NUM_PHILOSOPHERS;
        Fork[] forks = createForks(n);

        List<DeadlockPhilosopher> philosophers = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            philosophers.add(new DeadlockPhilosopher(
                    i, forks[i], forks[(i + 1) % n], SimConfig.ROUNDS));
        }

        DeadlockWatchdog watchdog = new DeadlockWatchdog(philosophers);
        watchdog.start();

        long t0 = System.currentTimeMillis();
        philosophers.forEach(Thread::start);
        for (DeadlockPhilosopher p : philosophers) {
            p.join(SimConfig.WATCHDOG_TIMEOUT_MS * 2);
        }
        watchdog.stopWatchdog();

        printSummary(philosophers.stream().mapToInt(DeadlockPhilosopher::getMealsEaten).sum(),
                System.currentTimeMillis() - t0);
    }

    // =========================================================================
    //  PFLICHT – Teil A, Szenario 2: Lock Ordering → kein Deadlock
    // =========================================================================
    static void runPartA_Safe() throws InterruptedException {
        header("TEIL A – Szenario 2: Lock Ordering (kein Deadlock)");
        System.out.println("Gabeln immer in aufsteigender ID-Reihenfolge aufnehmen.");
        System.out.println("'Circular Wait' ist strukturell ausgeschlossen.\n");

        printTable();

        int n = SimConfig.NUM_PHILOSOPHERS;
        Fork[] forks = createForks(n);

        List<SafePhilosopher> philosophers = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            philosophers.add(new SafePhilosopher(
                    i, forks[i], forks[(i + 1) % n], SimConfig.ROUNDS));
        }

        long t0 = System.currentTimeMillis();
        philosophers.forEach(Thread::start);
        for (SafePhilosopher p : philosophers) p.join();

        printSummary(philosophers.stream().mapToInt(SafePhilosopher::getMealsEaten).sum(),
                System.currentTimeMillis() - t0);
    }

    // =========================================================================
    //  BONUS – Teil B: Wait-for Graph und Zyklusdetektion (Aufgaben 4–6)
    // =========================================================================
    static void runPartB() {
        header("TEIL B – Wait-for Graph & Deadlock-Detection  [BONUS]");

        // --- Szenario B1: Kein Deadlock ---
        System.out.println("── B1: Keine Wartekanten (Grundzustand) ──────────────");
        WaitForGraph wfg1 = new WaitForGraph();
        for (int i = 0; i < 4; i++) wfg1.addProcess(i);
        wfg1.printGraph();
        printDetection(wfg1.detectDeadlock());

        // --- Szenario B2: Lineare Kette, kein Zyklus ---
        System.out.println("── B2: Lineare Wartekette P0→P1→P2 (kein Deadlock) ──");
        WaitForGraph wfg2 = new WaitForGraph();
        for (int i = 0; i < 3; i++) wfg2.addProcess(i);
        wfg2.addWaitEdge(0, 1);
        wfg2.addWaitEdge(1, 2);
        wfg2.printGraph();
        printDetection(wfg2.detectDeadlock());

        // --- Szenario B3: 2-Prozess-Zyklus ---
        System.out.println("── B3: 2-Prozess-Deadlock P0↔P1 ─────────────────────");
        WaitForGraph wfg3 = new WaitForGraph();
        wfg3.addProcess(0); wfg3.addProcess(1);
        wfg3.addWaitEdge(0, 1);
        wfg3.addWaitEdge(1, 0);
        wfg3.printGraph();
        printDetection(wfg3.detectDeadlock());

        // --- Szenario B4: 3-Prozess-Zyklus + Recovery ---
        System.out.println("── B4: 3-Prozess-Zyklus P0→P1→P2→P0 + Recovery ──────");
        WaitForGraph wfg4 = new WaitForGraph();
        for (int i = 0; i < 3; i++) wfg4.addProcess(i);
        wfg4.addWaitEdge(0, 1);
        wfg4.addWaitEdge(1, 2);
        wfg4.addWaitEdge(2, 0);
        wfg4.printGraph();
        List<Integer> cycle = wfg4.detectDeadlock();
        printDetection(cycle);
        if (!cycle.isEmpty()) {
            int victim = cycle.get(0);
            System.out.println("→ Victim Selection: P" + victim + " wird abgebrochen.");
            wfg4.removeProcess(victim);
            wfg4.printGraph();
            System.out.print("→ Nach Recovery: ");
            printDetection(wfg4.detectDeadlock());
        }

        // --- Szenario B5: ResourceManager ---
        System.out.println("── B5: ResourceManager – dynamische Erkennung ────────");
        ResourceManager rm = new ResourceManager();
        rm.addResource("R1"); rm.addResource("R2"); rm.addResource("R3");
        rm.addProcess(0);     rm.addProcess(1);     rm.addProcess(2);
        System.out.println();
        rm.requestResource(0, "R1");
        rm.requestResource(1, "R2");
        rm.requestResource(2, "R3");
        rm.printState();
        try {
            rm.requestResource(0, "R2");  // P0 → P1
            rm.requestResource(1, "R3");  // P1 → P2
            rm.requestResource(2, "R1");  // P2 → P0 → ZYKLUS!
        } catch (IllegalStateException e) {
            System.out.println("\n  Exception: " + e.getMessage());
            System.out.println("  → Im echten OS: Victim auswählen und Recovery einleiten.\n");
        }

        // --- Aufgabe 6 (Bonus): eigenes Experiment ---
        // TODO (Aufgabe 6 – Bonus): Füge hier ein eigenes Szenario ein.
        //   Erstelle einen WFG mit 4 Prozessen und einer Konstellation,
        //   bei der P0, P1, P2 in einem Zyklus sind, P3 aber NICHT.
        //   Führe detectDeadlock() aus: Wird P3 fälschlicherweise gemeldet?
        // --->

    }

    // =========================================================================
    //  Hilfsmethoden (fertig – nicht ändern)
    // =========================================================================

    private static Fork[] createForks(int n) {
        Fork[] forks = new Fork[n];
        for (int i = 0; i < n; i++) forks[i] = new Fork(i);
        return forks;
    }

    private static void printTable() {
        System.out.println("Tischanordnung:");
        System.out.println("            P0");
        System.out.println("           /  \\");
        System.out.println("         G4    G0");
        System.out.println("         /      \\");
        System.out.println("       P4        P1");
        System.out.println("       |          |");
        System.out.println("       G3        G1");
        System.out.println("         \\      /");
        System.out.println("          P3--G2--P2");
        System.out.println("  Pi hält G(i) links, G((i+1)%5) rechts\n");
    }

    static void printDetection(List<Integer> cycle) {
        if (cycle.isEmpty()) {
            System.out.println("✅ Kein Deadlock.\n");
        } else {
            String s = cycle.stream().map(i -> "P" + i).reduce((a, b) -> a + "→" + b).orElse("");
            System.out.println("🔴 DEADLOCK! Zyklus: " + s + "\n");
        }
    }

    private static void printSummary(int totalMeals, long elapsed) {
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.printf( "║  Laufzeit:        %6d ms           ║%n", elapsed);
        System.out.printf( "║  Mahlzeiten ges.: %6d              ║%n", totalMeals);
        System.out.println("╚══════════════════════════════════════╝\n");
    }

    private static void header(String title) {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.printf( "║  %-52s  ║%n", title);
        System.out.println("╚══════════════════════════════════════════════════════╝\n");
    }

    private static void separator() {
        System.out.println("\n──────────────────────────────────────────────────────\n");
    }
}
