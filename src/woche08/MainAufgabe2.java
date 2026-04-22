package woche08;

/**
 * Test für Aufgabe 2 — Freigabe und Coalescing.
 *
 * Voraussetzung: Aufgabe 1 vollständig + free() und coalesce() implementiert.
 * Metriken werden hier noch NICHT benötigt.
 *
 * Ziel: Coalescing sichtbar machen — angrenzende freie Blöcke werden
 * nach der Freigabe automatisch zusammengeführt.
 */
public class MainAufgabe2 {

    static final int MEMORY_SIZE = 1024;

    public static void main(String[] args) {

        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║  Aufgabe 2 — Freigabe und Coalescing         ║");
        System.out.println("╚══════════════════════════════════════════════╝\n");

        testCoalescing();
        testStrategyDifference();
    }

    // -------------------------------------------------------------------------

    /**
     * Zeigt Coalescing Schritt für Schritt:
     * erst einzelne Freigabe, dann Freigabe mit Zusammenführung.
     */
    static void testCoalescing() {
        System.out.println("━━━ Coalescing Schritt für Schritt (FIRST_FIT) ━━━\n");
        ContiguousMemoryManager mm =
            new ContiguousMemoryManager(MEMORY_SIZE, AllocationStrategy.FIRST_FIT);

        int a1 = mm.allocate(200, 1);
        int a2 = mm.allocate(150, 2);
        int a3 = mm.allocate(300, 3);
        int a4 = mm.allocate(100, 4);

        printBlocks(mm, "Nach Allokation P1–P4:");

        // Freigabe P2 — isoliertes Loch, kein Coalescing möglich
        mm.free(a2);
        printBlocks(mm, "Nach free(P2) — isoliertes Loch:");

        // Freigabe P4 — angrenzend an freien Rest → Coalescing
        mm.free(a4);
        printBlocks(mm, "Nach free(P4) — Coalescing mit freiem Rest:");

        // Freigabe P3 — jetzt grenzt P3 links an P2-Loch und rechts an P4+Rest
        // → beide Seiten werden zusammengeführt
        mm.free(a3);
        printBlocks(mm, "Nach free(P3) — Coalescing beider Seiten:");

        // Freigabe P1 — jetzt ist der gesamte Speicher wieder ein Block
        mm.free(a1);
        printBlocks(mm, "Nach free(P1) — vollständige Rekombination:");
    }

    // -------------------------------------------------------------------------

    /**
     * Zeigt den ersten Unterschied zwischen den Strategien:
     * Nach Freigabe von P2 (150 Bytes) und P4 (100 Bytes) entstehen Löcher.
     * Eine neue Anfrage von 130 Bytes landet je nach Strategie an
     * unterschiedlicher Stelle.
     */
    static void testStrategyDifference() {
        System.out.println("━━━ Strategievergleich nach Fragmentierung ━━━\n");
        System.out.println("Workload: allocate(200,300,150,100), free(P2,P4),"
                         + " dann allocate(130)\n");

        for (AllocationStrategy strategy : AllocationStrategy.values()) {
            ContiguousMemoryManager mm =
                new ContiguousMemoryManager(MEMORY_SIZE, strategy);

            int a1 = mm.allocate(200, 1);
            int a2 = mm.allocate(150, 2);
            int a3 = mm.allocate(300, 3);
            int a4 = mm.allocate(100, 4);

            mm.free(a2);   // Loch: 150 Bytes bei Adresse 200
            mm.free(a4);   // Loch: 100 Bytes bei Adresse 650 → coalesct mit Rest

            int a5 = mm.allocate(130, 5);
            System.out.printf("  %-12s → P5@%d%n", strategy, a5);
            printBlocks(mm, null);
        }

        System.out.println("Beobachtung:");
        System.out.println("  FIRST_FIT: nimmt das erste passende Loch (Adresse 200, 150B)");
        System.out.println("  → passt nicht (150 < 130+Rest? nein, 150 >= 130) → P5@200");
        System.out.println("  BEST_FIT:  sucht kleinstes passendes Loch");
        System.out.println("  WORST_FIT: sucht größtes Loch");
    }

    // -------------------------------------------------------------------------

    static void printBlocks(ContiguousMemoryManager mm, String label) {
        if (label != null) System.out.println("  " + label);
        for (MemoryBlock b : mm.getBlocks()) {
            System.out.println("    " + b);
        }
        System.out.println();
    }
}
