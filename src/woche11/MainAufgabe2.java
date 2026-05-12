package woche11;

import java.util.Arrays;
import java.util.List;

/**
 * Test fuer Aufgabe 2 -- LRU-Ersetzungsalgorithmus.
 *
 * Voraussetzung: Aufgabe 1 vollstaendig + LRUReplacer.access() implementiert.
 */
public class MainAufgabe2 {

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║  Aufgabe 2 -- LRU                            ║");
        System.out.println("╚══════════════════════════════════════════════╝\n");

        testLRUBasic();
        testLRUvsFlFO();
        testNoBeladysAnomaly();
    }

    static void testLRUBasic() {
        System.out.println("-- LRU Schritt fuer Schritt (3 Frames) --\n");

        List<Integer> ref = Arrays.asList(1, 2, 3, 4, 1, 2, 5, 1, 2, 3, 4, 5);
        LRUReplacer lru = new LRUReplacer(3);

        System.out.println("Referenzfolge: " + ref);
        System.out.println("Frames: 3\n");
        ReplacementSimulator.simulate(lru, ref, true);
    }

    static void testLRUvsFlFO() {
        System.out.println("-- LRU vs. FIFO (gleiche Referenzfolge) --\n");

        List<Integer> ref = Arrays.asList(1, 2, 3, 1, 4, 2, 5, 2, 1, 3, 4, 5);
        System.out.println("Referenzfolge: " + ref + "  (Frames: 3)\n");

        for (PageReplacer r : new PageReplacer[]{
                new FIFOReplacer(3), new LRUReplacer(3)}) {
            int faults = ReplacementSimulator.simulate(r, ref, false);
            System.out.printf("  %-6s: %d Page Faults%n", r.name(), faults);
        }
        System.out.println();
        System.out.println("  Denkanstoesse:");
        System.out.println("  - Bei welchen Zugriffsmustern ist LRU besonders gut?");
        System.out.println("  - Wann verhält sich LRU annaehernd wie FIFO?");
        System.out.println();
    }

    static void testNoBeladysAnomaly() {
        System.out.println("-- LRU: keine Beladys Anomalie --\n");

        List<Integer> ref = Arrays.asList(1, 2, 3, 4, 1, 2, 5, 1, 2, 3, 4, 5);
        System.out.println("Gleiche Folge wie bei FIFO-Anomalie:");

        for (int f : new int[]{3, 4, 5}) {
            LRUReplacer lru = new LRUReplacer(f);
            int faults = ReplacementSimulator.simulate(lru, ref, false);
            System.out.printf("  LRU %d Frames: %d Page Faults%n", f, faults);
        }
        System.out.println();
        System.out.println("  --> Faults nehmen mit mehr Frames ab oder bleiben gleich.");
        System.out.println("  LRU ist ein Stack-Algorithmus: keine Beladys Anomalie.\n");
    }
}
