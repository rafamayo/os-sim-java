package woche07;

import java.util.List;

/**
 * Bonus – Aufgaben 4–6: Wait-for Graph und Zyklusdetektion.
 *
 * Aufgabe 4: DFS-Zyklusdetektion nachvollziehen (Szenarien B1–B4).
 * Aufgabe 5: Integration in den ResourceManager nachvollziehen (Szenario B5).
 * Aufgabe 6: eigenes Experiment ergänzen (siehe TODO unten).
 *
 * Start:
 *   java woche07.MainWaitForGraph
 */
public class MainWaitForGraph {

    public static void main(String[] args) {
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

    private static void printDetection(List<Integer> cycle) {
        if (cycle.isEmpty()) {
            System.out.println("✅ Kein Deadlock.\n");
        } else {
            String s = cycle.stream().map(i -> "P" + i).reduce((a, b) -> a + "→" + b).orElse("");
            System.out.println("🔴 DEADLOCK! Zyklus: " + s + "\n");
        }
    }

    private static void header(String title) {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.printf( "║  %-52s  ║%n", title);
        System.out.println("╚══════════════════════════════════════════════════════╝\n");
    }
}
