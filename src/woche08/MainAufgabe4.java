package woche08;

/**
 * Test für Aufgabe 4 — Buddy-Allocator.
 *
 * Voraussetzung: allocate() und free() im BuddyAllocator implementiert.
 * ContiguousMemoryManager wird hier NICHT benötigt.
 *
 * Ziel: Splitting und Coalescing im Buddy-System Schritt für Schritt
 * nachvollziehen und interne Fragmentierung sichtbar machen.
 */
public class MainAufgabe4 {

    public static void main(String[] args) {

        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║  Aufgabe 4 — Buddy-Allocator                 ║");
        System.out.println("╚══════════════════════════════════════════════╝\n");

        testSplitting();
        testCoalescing();
        testInternalFragmentation();
    }

    // -------------------------------------------------------------------------

    /**
     * Zeigt das Splitting: ein Anfang-Block der Ordnung 10 (1024 Bytes)
     * wird schrittweise aufgespalten bis zur gewünschten Ordnung.
     */
    static void testSplitting() {
        System.out.println("━━━ Splitting ━━━\n");
        BuddyAllocator buddy = new BuddyAllocator(10);  // 1024 Bytes

        System.out.println("Ausgangszustand:");
        buddy.printFreeLists();

        // 100 Bytes → Ordnung 7 (128 Bytes): Splits 10→9→8→7
        int b1 = buddy.allocate(100);
        System.out.printf("%nNach allocate(100) → b1@%d (Ordnung 7 = 128 Bytes):%n", b1);
        buddy.printFreeLists();

        // 200 Bytes → Ordnung 8 (256 Bytes)
        int b2 = buddy.allocate(200);
        System.out.printf("%nNach allocate(200) → b2@%d (Ordnung 8 = 256 Bytes):%n", b2);
        buddy.printFreeLists();

        // 50 Bytes → Ordnung 6 (64 Bytes)
        int b3 = buddy.allocate(50);
        System.out.printf("%nNach allocate(50) → b3@%d (Ordnung 6 = 64 Bytes):%n", b3);
        buddy.printFreeLists();

        System.out.println();
        buddy.printStats();
        System.out.println();

        // Aufräumen für nächsten Test
    }

    // -------------------------------------------------------------------------

    /**
     * Zeigt das Coalescing: Freigaben in verschiedener Reihenfolge,
     * am Ende ist der Heap vollständig rekombiniert.
     */
    static void testCoalescing() {
        System.out.println("━━━ Coalescing ━━━\n");
        BuddyAllocator buddy = new BuddyAllocator(10);

        int b1 = buddy.allocate(100);   // Ord. 7 = 128B
        int b2 = buddy.allocate(200);   // Ord. 8 = 256B
        int b3 = buddy.allocate(50);    // Ord. 6 =  64B
        System.out.printf("Alloziert: b1@%d, b2@%d, b3@%d%n%n", b1, b2, b3);

        buddy.free(b1);
        System.out.printf("Nach free(b1@%d):%n", b1);
        buddy.printFreeLists();

        buddy.free(b2);
        System.out.printf("Nach free(b2@%d):%n", b2);
        buddy.printFreeLists();

        buddy.free(b3);
        System.out.printf("Nach free(b3@%d) — vollständige Rekombination?%n", b3);
        buddy.printFreeLists();

        buddy.printStats();
        System.out.println();
    }

    // -------------------------------------------------------------------------

    /**
     * Zeigt interne Fragmentierung: der Buddy-Allocator rundet auf die
     * nächste Zweierpotenz auf. Wie viel Speicher geht verloren?
     */
    static void testInternalFragmentation() {
        System.out.println("━━━ Interne Fragmentierung ━━━\n");
        System.out.printf("  %-15s  %-10s  %-10s  %s%n",
                          "Anfrage", "Ordnung", "Reserviert", "Verschwendet");
        System.out.println("  " + "─".repeat(52));

        int[] requests = {1, 3, 4, 5, 64, 65, 100, 200, 500, 1024};
        BuddyAllocator buddy = new BuddyAllocator(10);

        for (int size : requests) {
            int order     = BuddyAllocator.ceilLog2(size);
            int reserved  = 1 << order;
            int wasted    = reserved - size;
            System.out.printf("  %-15d  %-10d  %-10d  %d Bytes (%.0f%%)%n",
                size, order, reserved, wasted,
                (double) wasted / reserved * 100);
        }
        System.out.println();
        System.out.println("Denkanstöße:");
        System.out.println("  - Bei welchen Anfragegrößen ist die interne");
        System.out.println("    Fragmentierung am größten?");
        System.out.println("  - Wann ist sie exakt 0?");
        System.out.println("  - Wie verhält sich das im Vergleich zu FIRST_FIT?");
    }
}
