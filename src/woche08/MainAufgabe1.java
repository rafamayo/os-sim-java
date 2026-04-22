package woche08;

/**
 * Test für Aufgabe 1 — Platzierungsstrategien.
 *
 * Voraussetzung: firstFit(), bestFit(), worstFit() implementiert.
 * free(), coalesce() und Metriken werden hier NICHT benötigt.
 *
 * Ziel: zeigen dass alle drei Strategien denselben Block wählen,
 * solange der Speicher noch nicht fragmentiert ist — der Unterschied
 * wird erst nach gezielten Freigaben sichtbar (→ Aufgabe 2).
 */
public class MainAufgabe1 {

    static final int MEMORY_SIZE = 1024;

    public static void main(String[] args) {

        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║  Aufgabe 1 — Platzierungsstrategien          ║");
        System.out.println("╚══════════════════════════════════════════════╝\n");

        for (AllocationStrategy strategy : AllocationStrategy.values()) {
            testStrategy(strategy);
        }

        System.out.println("━━━ Beobachtung ━━━");
        System.out.println("Alle drei Strategien liefern die gleichen Adressen,");
        System.out.println("solange der Speicher noch nicht fragmentiert ist.");
        System.out.println("Der Unterschied wird erst nach Freigaben sichtbar");
        System.out.println("→ siehe Aufgabe 2.");
    }

    static void testStrategy(AllocationStrategy strategy) {
        System.out.println("━━━ " + strategy + " ━━━");
        ContiguousMemoryManager mm =
            new ContiguousMemoryManager(MEMORY_SIZE, strategy);

        int a1 = mm.allocate(200, 1);
        int a2 = mm.allocate(150, 2);
        int a3 = mm.allocate(300, 3);
        int a4 = mm.allocate(100, 4);

        System.out.printf("  P1@%-4d  P2@%-4d  P3@%-4d  P4@%-4d%n",
                          a1, a2, a3, a4);

        // Ausgabe der Block-Liste ohne Metriken
        System.out.println("  Blöcke:");
        for (MemoryBlock b : mm.getBlocks()) {
            System.out.println("    " + b);
        }

        // Fehlschlagendes Allozieren (zu groß)
        int a5 = mm.allocate(500, 5);
        System.out.println("  allocate(500) → " + a5
            + (a5 == -1 ? "  [kein zusammenhängender Block groß genug]" : ""));
        System.out.println();
    }
}
