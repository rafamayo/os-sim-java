package woche11;

import java.util.Arrays;
import java.util.List;

/**
 * Test fuer Aufgabe 1 -- FIFO-Ersetzungsalgorithmus.
 *
 * Voraussetzung: FIFOReplacer.access() implementiert.
 * LRU, Clock, OPT werden hier NICHT benoetigt.
 *
 * Ziel: FIFO Schritt fuer Schritt nachvollziehen, Beladys Anomalie beobachten.
 */
public class MainAufgabe1 {

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║  Aufgabe 1 -- FIFO                           ║");
        System.out.println("╚══════════════════════════════════════════════╝\n");

        testFIFOBasic();
        testBeladysAnomaly();
    }

    static void testFIFOBasic() {
        System.out.println("-- FIFO Schritt fuer Schritt (3 Frames) --\n");

        // Klassisches Beispiel aus den Folien
        List<Integer> ref = Arrays.asList(1, 2, 3, 4, 1, 2, 5, 1, 2, 3, 4, 5);
        FIFOReplacer fifo = new FIFOReplacer(3);

        System.out.println("Referenzfolge: " + ref);
        System.out.println("Frames: 3\n");
        ReplacementSimulator.simulate(fifo, ref, true);
        System.out.println("Erwartete Page Faults: 9\n");
    }

    static void testBeladysAnomaly() {
        System.out.println("-- Beladys Anomalie --\n");
        System.out.println("Referenzfolge: [1,2,3,4,1,2,5,1,2,3,4,5]");
        System.out.println("Erwartung: FIFO mit 4 Frames hat MEHR Faults als mit 3!\n");

        List<Integer> ref = Arrays.asList(1, 2, 3, 4, 1, 2, 5, 1, 2, 3, 4, 5);

        for (int f : new int[]{3, 4}) {
            FIFOReplacer fifo = new FIFOReplacer(f);
            int faults = ReplacementSimulator.simulate(fifo, ref, false);
            System.out.printf("  FIFO %d Frames: %d Page Faults%n", f, faults);
        }
        System.out.println();
        System.out.println("  --> Mit 4 Frames gibt es MORE Faults als mit 3: Beladys Anomalie!\n");
        System.out.println("  Denkanstoesse:");
        System.out.println("  - Warum tritt diese Anomalie bei FIFO auf?");
        System.out.println("  - Welche Algorithmen sind vor Beladys Anomalie geschuetzt?");
    }
}
