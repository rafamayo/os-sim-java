package woche08;

/**
 * Test für Aufgabe 3 — Fragmentierungsmetriken.
 *
 * Voraussetzung: Aufgaben 1 und 2 vollständig implementiert
 *               + externalFragmentation(), numberOfHoles(), utilization().
 *
 * Ziel: Metriken interpretieren und den Zusammenhang zwischen
 * Fragmentierungszustand und messbarer EF verstehen.
 */
public class MainAufgabe3 {

    static final int MEMORY_SIZE = 1024;

    public static void main(String[] args) {

        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║  Aufgabe 3 — Fragmentierungsmetriken         ║");
        System.out.println("╚══════════════════════════════════════════════╝\n");

        testMetricsStepByStep();
        testMetricsComparison();
    }

    // -------------------------------------------------------------------------

    /**
     * Zeigt wie sich die Metriken Schritt für Schritt verändern:
     * von komplett belegt über fragmentiert bis vollständig frei.
     */
    static void testMetricsStepByStep() {
        System.out.println("━━━ Metriken: Schritt für Schritt (FIRST_FIT) ━━━\n");
        ContiguousMemoryManager mm =
            new ContiguousMemoryManager(MEMORY_SIZE, AllocationStrategy.FIRST_FIT);

        printMetrics(mm, "Ausgangszustand (leer):");

        int a1 = mm.allocate(200, 1);
        int a2 = mm.allocate(150, 2);
        int a3 = mm.allocate(300, 3);
        int a4 = mm.allocate(100, 4);
        printMetrics(mm, "Nach Allokation P1–P4:");

        mm.free(a2);
        printMetrics(mm, "Nach free(P2) — 1 Loch:");

        mm.free(a4);
        printMetrics(mm, "Nach free(P4) — Loch + coalescter Rest:");

        // Allokation scheitert wegen Fragmentierung
        int a5 = mm.allocate(600, 5);
        System.out.println("  allocate(600) → " + a5
            + (a5 == -1 ? "  [Fragmentierung verhindert Allokation!]" : ""));
        System.out.println();

        mm.free(a3);
        printMetrics(mm, "Nach free(P3) — Coalescing:");

        // Jetzt passt 600 Bytes
        a5 = mm.allocate(600, 5);
        System.out.println("  allocate(600) nach Coalescing → " + a5
            + (a5 != -1 ? "  [jetzt möglich ✓]" : ""));
        System.out.println();
    }

    // -------------------------------------------------------------------------

    /**
     * Vergleicht EF, Holes und Utilization aller drei Strategien
     * unter identischem Workload — als Vorbereitung auf den großen
     * Vergleich in Main.java.
     */
    static void testMetricsComparison() {
        System.out.println("━━━ Metriken: Strategievergleich ━━━\n");
        System.out.printf("  %-12s  %6s  %5s  %8s%n",
                          "Strategie", "EF", "Holes", "Util");
        System.out.println("  " + "─".repeat(38));

        int[] sizes = {100, 250, 80, 300, 120};
        int[] toFree = {1, 3};   // 0-basierte Indizes

        for (AllocationStrategy s : AllocationStrategy.values()) {
            ContiguousMemoryManager mm =
                new ContiguousMemoryManager(MEMORY_SIZE, s);
            int[] addrs = new int[sizes.length];
            for (int i = 0; i < sizes.length; i++) {
                addrs[i] = mm.allocate(sizes[i], i + 1);
            }
            for (int idx : toFree) {
                mm.free(addrs[idx]);
            }
            System.out.printf("  %-12s  %6.3f  %5d  %7.1f%%%n",
                s,
                mm.externalFragmentation(),
                mm.numberOfHoles(),
                mm.utilization() * 100);
        }
        System.out.println();
        System.out.println("Denkanstöße:");
        System.out.println("  - Welche Strategie liefert die geringste externe Fragmentierung?");
        System.out.println("  - Warum ist die Utilization bei allen Strategien gleich?");
        System.out.println("  - Was sagt EF=0.0 aus?");
    }

    // -------------------------------------------------------------------------

    static void printMetrics(ContiguousMemoryManager mm, String label) {
        System.out.println("  " + label);
        System.out.printf("    EF=%.3f  Holes=%d  Util=%.1f%%%n",
            mm.externalFragmentation(),
            mm.numberOfHoles(),
            mm.utilization() * 100);
        System.out.println();
    }
}
