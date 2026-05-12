package woche11;

import java.util.Arrays;
import java.util.List;

/**
 * Woche 11 -- Gesamtvergleich aller vier Algorithmen.
 * Setzt alle Aufgaben (FIFO, LRU, Clock, OPT) voraus.
 *
 * Szenarien:
 *  1. Vergleich auf klassischer Referenzfolge
 *  2. Fault-vs-Frames Kurven (inkl. Beladys Anomalie bei FIFO)
 *  3. Verschiedene Zugriffsmuster: lokal, random, schleifenfoermig
 *  4. OPT als Baseline: wie nah kommen FIFO / LRU / Clock ans Optimum?
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║  Woche 11 -- Seitenersetzungsalgorithmen     ║");
        System.out.println("╚══════════════════════════════════════════════╝\n");

        scenario1_classicComparison();
        scenario2_faultVsFrames();
        scenario3_accessPatterns();
        scenario4_optBaseline();
    }

    // Szenario 1: Klassische Referenzfolge aus den Folien
    static void scenario1_classicComparison() {
        System.out.println("=== Szenario 1: Klassischer Vergleich ===\n");

        List<Integer> ref = Arrays.asList(1, 2, 3, 4, 1, 2, 5, 1, 2, 3, 4, 5);
        int frames = 3;

        ReplacementSimulator.compare(Arrays.asList(
            new FIFOReplacer(frames),
            new LRUReplacer(frames),
            new ClockReplacer(frames),
            new OPTReplacer(frames, ref)
        ), ref, frames);

        System.out.println("Trace FIFO:");
        ReplacementSimulator.simulate(new FIFOReplacer(frames), ref, true);
        System.out.println("Trace LRU:");
        ReplacementSimulator.simulate(new LRUReplacer(frames), ref, true);
        System.out.println("Trace OPT:");
        ReplacementSimulator.simulate(new OPTReplacer(frames, ref), ref, true);
    }

    // Szenario 2: Page Faults vs. Frames Kurven
    static void scenario2_faultVsFrames() {
        System.out.println("=== Szenario 2: Faults vs. Frames ===\n");
        List<Integer> ref = Arrays.asList(1, 2, 3, 4, 1, 2, 5, 1, 2, 3, 4, 5);
        ReplacementSimulator.faultVsFrames(ref, 7);
        System.out.println("  FIFO-Zeile 3->4: mehr Frames -> mehr Faults? --> Beladys Anomalie!\n");
    }

    // Szenario 3: Verschiedene Zugriffsmuster
    static void scenario3_accessPatterns() {
        System.out.println("=== Szenario 3: Zugriffsmuster ===\n");

        // Lokal: immer dieselben 3 Seiten (fuer alle Algorithmen optimal)
        List<Integer> local = Arrays.asList(1,2,3,1,2,3,1,2,3,1,2,3);

        // Schleifenfoermig: rotiert durch 5 Seiten (Working-Set > Frames)
        List<Integer> loop  = Arrays.asList(1,2,3,4,5,1,2,3,4,5,1,2,3,4,5);

        // Abwechselnd: breite Streuung, kaum Wiederholungen
        List<Integer> rnd   = Arrays.asList(1,5,2,7,3,6,4,8,1,3,5,7,2,4,6);

        String[] labels = {"Lokal     ", "Schleife  ", "Breit     "};
        List<?>[] patterns = {local, loop, rnd};

        for (int p = 0; p < patterns.length; p++) {
            @SuppressWarnings("unchecked")
            List<Integer> ref = (List<Integer>) patterns[p];
            System.out.printf("%-12s (Frames=3): ", labels[p]);
            String[] names = new String[4];
            int[] faults   = new int[4];
            PageReplacer[] algs = {
                new FIFOReplacer(3), new LRUReplacer(3),
                new ClockReplacer(3), new OPTReplacer(3, ref)
            };
            for (int i = 0; i < 4; i++) {
                faults[i] = ReplacementSimulator.simulate(algs[i], ref, false);
                names[i]  = algs[i].name();
            }
            for (int i = 0; i < 4; i++)
                System.out.printf("%s=%d  ", names[i], faults[i]);
            System.out.println();
        }
        System.out.println();
    }

    // Szenario 4: OPT als Baseline -- wie nah ist jeder Algorithmus am Optimum?
    static void scenario4_optBaseline() {
        System.out.println("=== Szenario 4: Abstand zum Optimum (OPT) ===\n");

        List<Integer> ref = Arrays.asList(
            1,2,3,4,2,1,5,3,2,4,1,5,3,2,4,1,3,5,2,4);
        int frames = 3;

        int optFaults  = ReplacementSimulator.simulate(
            new OPTReplacer(frames, ref), ref, false);

        System.out.printf("%-8s  %-12s  %s%n", "Algo", "Faults", "Overhead vs OPT");
        System.out.println("-".repeat(36));

        for (PageReplacer r : new PageReplacer[]{
                new FIFOReplacer(frames),
                new LRUReplacer(frames),
                new ClockReplacer(frames),
                new OPTReplacer(frames, ref)}) {
            int f = ReplacementSimulator.simulate(r, ref, false);
            System.out.printf("%-8s  %-12d  +%d Faults%n",
                r.name(), f, f - optFaults);
        }
        System.out.println();
        System.out.println("OPT ist das theoretische Minimum.");
        System.out.println("LRU und Clock kommen ihm oft sehr nah.");
    }
}
