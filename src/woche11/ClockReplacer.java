package woche11;

/**
 * Clock-Algorithmus (Second Chance) -- Woche 11
 *
 * LRU-Approximation mit Reference-Bits und zirkulärem Uhrzeiger.
 */
public class ClockReplacer implements PageReplacer {

    private final int[]     frames;
    private final boolean[] referenced;
    private int             hand;        // Zeiger (zirkulär)
    private int             pageFaults;

    public ClockReplacer(int numFrames) {
        this.frames     = new int[numFrames];
        this.referenced = new boolean[numFrames];
        this.hand       = 0;
        this.pageFaults = 0;
        java.util.Arrays.fill(frames, -1);
    }

    // =========================================================================
    // AUFGABE 3 -- Clock access()
    // =========================================================================
    // Schritt 1: Hit-Check -- ist page in frames[i]?
    //              Falls ja: referenced[i] = true, return 0.
    //
    // Schritt 2: Page Fault: pageFaults++
    //
    // Schritt 3: Freier Frame (frames[hand] == -1)?
    //              frames[hand] = page, referenced[hand] = true,
    //              hand = (hand + 1) % frames.length, return 1.
    //
    // Schritt 4: Clock-Schleife -- solange referenced[hand] == true:
    //              referenced[hand] = false  (zweite Chance vergeben)
    //              hand = (hand + 1) % frames.length
    //            Wenn referenced[hand] == false:
    //              frames[hand] = page, referenced[hand] = true,
    //              hand = (hand + 1) % frames.length, return 1.
    //
    // Visualisierung: Der "Uhrzeiger" (hand) läuft im Kreis.
    //   Seiten mit gesetztem Reference-Bit bekommen eine zweite Chance.
    //   Das Bit wird beim Vorbeilaufen des Zeigers gelöscht.
    // =========================================================================

    @Override
    public int access(int page) {
        // TODO: Implementieren Sie den Clock-Algorithmus gemäß dem Algorithmus oben.
        throw new UnsupportedOperationException("ClockReplacer.access() nicht implementiert");
    }

    @Override public int      getPageFaults() { return pageFaults; }
    @Override public int[]    getFrames()     { return frames.clone(); }
    @Override public String   name()          { return "Clock"; }
    public    boolean[]       getReferenced() { return referenced.clone(); }
    public    int             getHand()       { return hand; }
}
