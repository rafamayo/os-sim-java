package woche11;

import java.util.List;

/**
 * Optimaler Seitenersetzungsalgorithmus (OPT / Belady) -- Woche 11
 *
 * Verdrängt immer die Seite, deren nächster Zugriff am weitesten in der
 * Zukunft liegt. Benötigt die vollständige Referenzfolge im Voraus
 * (nicht online einsetzbar -- dient als theoretisches Optimum / Baseline).
 */
public class OPTReplacer implements PageReplacer {

    private final int        numFrames;
    private final int[]      frames;
    private final List<Integer> refString; // gesamte Referenzfolge
    private int              position;     // aktueller Index in refString
    private int              pageFaults;

    /**
     * @param numFrames  Anzahl physischer Frames
     * @param refString  Vollständige Referenzfolge (wird für Vorausschau benötigt)
     */
    public OPTReplacer(int numFrames, List<Integer> refString) {
        this.numFrames  = numFrames;
        this.frames     = new int[numFrames];
        this.refString  = refString;
        this.position   = 0;
        this.pageFaults = 0;
        java.util.Arrays.fill(frames, -1);
    }

    @Override
    public int access(int page) {
        // Hit-Check
        for (int f : frames) if (f == page) { position++; return 0; }

        pageFaults++;

        // Freien Frame suchen
        for (int i = 0; i < numFrames; i++) {
            if (frames[i] == -1) {
                frames[i] = page;
                position++;
                return 1;
            }
        }

        // OPT: Seite verdrängen, die am spätesten wieder gebraucht wird
        int victim    = 0;
        int farthest  = -1;
        for (int i = 0; i < numFrames; i++) {
            int next = nextUse(frames[i]);
            if (next > farthest) { farthest = next; victim = i; }
        }
        frames[victim] = page;
        position++;
        return 1;
    }

    /**
     * Gibt den Index des nächsten Zugriffs auf 'page' ab der aktuellen
     * Position zurück. Integer.MAX_VALUE wenn die Seite nicht mehr referenziert wird
     * (-> perfekter Kandidat für Verdrängung).
     */
    private int nextUse(int page) {
        for (int i = position; i < refString.size(); i++) {
            if (refString.get(i) == page) return i;
        }
        return Integer.MAX_VALUE;
    }

    @Override public int    getPageFaults() { return pageFaults; }
    @Override public int[]  getFrames()     { return frames.clone(); }
    @Override public String name()          { return "OPT"; }
}
