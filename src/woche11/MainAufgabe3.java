package woche11;

import java.util.Arrays;
import java.util.List;

/**
 * Test fuer Aufgabe 3 -- Clock-Algorithmus.
 *
 * Voraussetzung: Aufgaben 1+2 vollstaendig + ClockReplacer.access() implementiert.
 */
public class MainAufgabe3 {

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║  Aufgabe 3 -- Clock (Second Chance)          ║");
        System.out.println("╚══════════════════════════════════════════════╝\n");

        testClockBasic();
        testClockVsLRU();
        testReferencesBits();
    }

    static void testClockBasic() {
        System.out.println("-- Clock Schritt fuer Schritt (3 Frames) --\n");

        List<Integer> ref = Arrays.asList(1, 2, 3, 4, 1, 2, 5, 1, 2, 3, 4, 5);
        ClockReplacer clock = new ClockReplacer(3);

        System.out.println("Referenzfolge: " + ref);
        System.out.println("Frames: 3\n");
        ReplacementSimulator.simulate(clock, ref, true);
    }

    static void testClockVsLRU() {
        System.out.println("-- Clock vs. LRU vs. FIFO --\n");

        List<Integer> ref = Arrays.asList(1, 2, 3, 1, 4, 2, 5, 2, 1, 3, 4, 5);
        System.out.println("Referenzfolge: " + ref + "  (Frames: 3)\n");

        for (PageReplacer r : new PageReplacer[]{
                new FIFOReplacer(3), new LRUReplacer(3), new ClockReplacer(3)}) {
            int faults = ReplacementSimulator.simulate(r, ref, false);
            System.out.printf("  %-6s: %d Page Faults%n", r.name(), faults);
        }
        System.out.println();
        System.out.println("  Clock liegt oft zwischen FIFO und LRU.");
        System.out.println("  Es ist eine gute Approximation von LRU mit O(1) Aufwand.\n");
    }

    static void testReferencesBits() {
        System.out.println("-- Reference-Bits: zweite Chance beobachten --\n");

        // Kleines Beispiel, das zeigt wie Reference-Bits Verdrängung verhindern
        List<Integer> ref = Arrays.asList(1, 2, 3, 1, 2, 3, 4);
        ClockReplacer clock = new ClockReplacer(3);

        System.out.println("Referenzfolge: " + ref + "  (Frames: 3)");
        System.out.println("Seiten 1,2,3 werden mehrfach referenziert ->");
        System.out.println("ihre Reference-Bits sind gesetzt, wenn Seite 4 kommt.\n");
        ReplacementSimulator.simulate(clock, ref, true);

        System.out.println("Denkanstoesse:");
        System.out.println("  - Welche Seite wird am Ende verdraengt und warum?");
        System.out.println("  - Wie viele Rotationen des Zeigers sind noetig?");
    }
}
