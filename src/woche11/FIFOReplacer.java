package woche11;

import java.util.LinkedList;
import java.util.Queue;

/**
 * FIFO-Seitenersetzungsalgorithmus -- Woche 11
 *
 * Verdrängt immer die Seite, die am längsten im Speicher ist.
 */
public class FIFOReplacer implements PageReplacer {

    private final int     numFrames;
    private final int[]   frames;
    private final Queue<Integer> order;
    private int           pageFaults;

    public FIFOReplacer(int numFrames) {
        this.numFrames  = numFrames;
        this.frames     = new int[numFrames];
        this.order      = new LinkedList<>();
        this.pageFaults = 0;
        java.util.Arrays.fill(frames, -1);
    }

    // =========================================================================
    // AUFGABE 1 -- FIFO access()
    // =========================================================================
    // Schritt 1: Hit-Check -- ist page bereits in frames[]?
    //            Falls ja: return 0 (kein Page Fault).
    //
    // Schritt 2: Page Fault: pageFaults++
    //
    // Schritt 3: Freier Frame vorhanden (frames[i] == -1)?
    //              Seite eintragen: frames[i] = page
    //              In Queue merken: order.add(page)
    //              return 1
    //
    // Schritt 4: Kein freier Frame -- FIFO-Verdrängung:
    //              Älteste Seite: int victim = order.poll()
    //              Zugehörigen Frame finden und überschreiben: frames[i] = page
    //              Neue Seite hinten eintragen: order.add(page)
    //              return 1
    // =========================================================================

    @Override
    public int access(int page) {
        // TODO: Implementieren Sie FIFO gemäß dem Algorithmus oben.
        throw new UnsupportedOperationException("FIFOReplacer.access() nicht implementiert");
    }

    @Override public int    getPageFaults() { return pageFaults; }
    @Override public int[]  getFrames()     { return frames.clone(); }
    @Override public String name()          { return "FIFO"; }
}
