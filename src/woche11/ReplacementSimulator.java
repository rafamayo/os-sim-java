package woche11;

import java.util.Arrays;
import java.util.List;

/**
 * Simulator für Seitenersetzungsalgorithmen -- Woche 11
 *
 * Führt eine Referenzfolge durch einen PageReplacer und gibt
 * den Frame-Inhalt nach jedem Zugriff als Tabelle aus.
 * Zählt Page Faults und berechnet die Page-Fault-Rate.
 */
public class ReplacementSimulator {

    /**
     * Führt alle Zugriffe der Referenzfolge durch den Replacer durch
     * und gibt eine detaillierte Trace-Tabelle aus.
     *
     * @param replacer   Algorithmus-Instanz (FIFO, LRU, Clock, OPT)
     * @param refString  Referenzfolge als Liste von Seitennummern
     * @param verbose    true = Trace-Tabelle ausgeben
     * @return Anzahl der Page Faults
     */
    public static int simulate(PageReplacer replacer,
                                List<Integer> refString,
                                boolean verbose) {
        if (verbose) {
            System.out.printf("%-6s", "Seite");
            for (int i = 0; i < refString.size(); i++) {
                System.out.printf("%4d", refString.get(i));
            }
            System.out.println();
            System.out.println("  " + "-".repeat(4 + refString.size() * 4));
        }

        // Simulation
        boolean[] faultMarkers = new boolean[refString.size()];
        int[][] snapshots = new int[refString.size()][];

        for (int i = 0; i < refString.size(); i++) {
            faultMarkers[i] = (replacer.access(refString.get(i)) == 1);
            snapshots[i]    = replacer.getFrames();
        }

        if (verbose) {
            // Frame-Inhalt zeilenweise pro Frame-Slot ausgeben
            int numFrames = snapshots[0].length;
            for (int slot = 0; slot < numFrames; slot++) {
                System.out.printf("F%-5d", slot);
                for (int i = 0; i < refString.size(); i++) {
                    int p = snapshots[i][slot];
                    System.out.printf("%4s", p == -1 ? "." : String.valueOf(p));
                }
                System.out.println();
            }

            // Page-Fault-Markierungen
            System.out.printf("%-6s", "Fault");
            for (boolean f : faultMarkers) System.out.printf("%4s", f ? "*" : "");
            System.out.println();

            System.out.printf("%nPage Faults: %d / %d  (Rate: %.1f%%)%n%n",
                replacer.getPageFaults(), refString.size(),
                (double) replacer.getPageFaults() / refString.size() * 100);
        }

        return replacer.getPageFaults();
    }

    /**
     * Vergleicht mehrere Algorithmen auf derselben Referenzfolge
     * und gibt eine Übersichtstabelle aus.
     *
     * @param replacers  Algorithmen (müssen für dieselbe Referenzfolge frisch erzeugt werden)
     * @param refString  Referenzfolge
     * @param numFrames  Anzahl Frames (nur für Ausgabe)
     */
    public static void compare(List<PageReplacer> replacers,
                                List<Integer> refString,
                                int numFrames) {
        System.out.printf("Referenzfolge: %s%n", refString);
        System.out.printf("Frames: %d, Zugriffe: %d%n%n", numFrames, refString.size());

        System.out.printf("%-8s  %-12s  %s%n", "Algorithmus", "Page Faults", "Rate");
        System.out.println("-".repeat(36));

        for (PageReplacer r : replacers) {
            int faults = simulate(r, refString, false);
            System.out.printf("%-8s  %-12d  %.1f%%%n",
                r.name(), faults,
                (double) faults / refString.size() * 100);
        }
        System.out.println();
    }

    /**
     * Führt alle vier Algorithmen über eine Reihe von Frame-Anzahlen durch
     * und gibt aus, wie sich die Page-Fault-Rate mit mehr Frames verändert.
     * Demonstriert auch Beladys Anomalie bei FIFO.
     *
     * @param refString  Referenzfolge
     * @param maxFrames  Maximale Frame-Anzahl
     */
    public static void faultVsFrames(List<Integer> refString, int maxFrames) {
        System.out.println("Page Faults vs. Anzahl Frames:");
        System.out.printf("%-8s", "Frames");
        String[] names = {"FIFO", "LRU", "Clock", "OPT"};
        for (String n : names) System.out.printf("%8s", n);
        System.out.println();
        System.out.println("-".repeat(8 + names.length * 8));

        for (int f = 1; f <= maxFrames; f++) {
            System.out.printf("%-8d", f);
            final int frames = f;
            PageReplacer[] algs = {
                new FIFOReplacer(frames),
                new LRUReplacer(frames),
                new ClockReplacer(frames),
                new OPTReplacer(frames, refString)
            };
            for (PageReplacer alg : algs) {
                int faults = simulate(alg, refString, false);
                System.out.printf("%8d", faults);
            }
            System.out.println();
        }
        System.out.println();
    }
}
