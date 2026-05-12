package woche11;

/**
 * LRU-Seitenersetzungsalgorithmus -- Woche 11
 *
 * Verdrängt die Seite, die am längsten nicht mehr referenziert wurde.
 * Verwendet einen logischen Zeitstempel (clock) pro Frame.
 */
public class LRUReplacer implements PageReplacer {

    private final int    numFrames;
    private final int[]  frames;
    private final long[] lastUsed;
    private long         clock;
    private int          pageFaults;

    public LRUReplacer(int numFrames) {
        this.numFrames  = numFrames;
        this.frames     = new int[numFrames];
        this.lastUsed   = new long[numFrames];
        this.clock      = 0;
        this.pageFaults = 0;
        java.util.Arrays.fill(frames, -1);
    }

    // =========================================================================
    // AUFGABE 2 -- LRU access()
    // =========================================================================
    // Schritt 1: Hit-Check -- ist page bereits in frames[i]?
    //              Falls ja: lastUsed[i] = ++clock, return 0.
    //
    // Schritt 2: Page Fault: pageFaults++
    //
    // Schritt 3: Freier Frame vorhanden (frames[i] == -1)?
    //              frames[i] = page, lastUsed[i] = ++clock, return 1.
    //
    // Schritt 4: LRU-Opfer: Frame mit kleinstem lastUsed-Wert wählen.
    //              frames[lru] = page, lastUsed[lru] = ++clock, return 1.
    //
    // Tipp: "kleinster lastUsed" bedeutet "am längsten nicht zugegriffen".
    // =========================================================================

    @Override
    public int access(int page) {
        // TODO: Implementieren Sie LRU gemäß dem Algorithmus oben.
        throw new UnsupportedOperationException("LRUReplacer.access() nicht implementiert");
    }

    @Override public int    getPageFaults() { return pageFaults; }
    @Override public int[]  getFrames()     { return frames.clone(); }
    @Override public String name()          { return "LRU"; }
}
