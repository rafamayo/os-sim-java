package woche11;

/**
 * Page Table Entry (PTE) — Eintrag in der Seitentabelle eines Prozesses.
 *
 * Felder:
 *   valid      – true: Seite ist im physischen Speicher vorhanden
 *   frameNumber – physischer Frame (nur gültig wenn valid == true)
 *   dirty      – true: Seite wurde seit dem Laden geschrieben (für Woche 11)
 *   referenced – true: Seite wurde seit dem letzten Clock-Tick zugegriffen
 *                      (accessed bit, für Woche 10/11)
 *   protection – Zugriffsrechte als Bitfeld: READ=1, WRITE=2, EXEC=4
 */
public class PageTableEntry {

    public static final int READ  = 1;
    public static final int WRITE = 2;
    public static final int EXEC  = 4;

    private boolean valid;
    private int     frameNumber;
    private boolean dirty;
    private boolean referenced;
    private int     protection;

    /** Erzeugt einen ungültigen PTE (Seite nicht im Speicher). */
    public PageTableEntry() {
        this.valid       = false;
        this.frameNumber = -1;
        this.dirty       = false;
        this.referenced  = false;
        this.protection  = READ | WRITE;  // Standard: lesen + schreiben
    }

    /** Erzeugt einen gültigen PTE mit dem angegebenen Frame. */
    public PageTableEntry(int frameNumber, int protection) {
        this.valid       = true;
        this.frameNumber = frameNumber;
        this.dirty       = false;
        this.referenced  = false;
        this.protection  = protection;
    }

    // --- Getter / Setter ---
    public boolean isValid()           { return valid; }
    public void    setValid(boolean v) { this.valid = v; }

    public int  getFrameNumber()       { return frameNumber; }
    public void setFrameNumber(int f)  { this.frameNumber = f; }

    public boolean isDirty()           { return dirty; }
    public void    setDirty(boolean d) { this.dirty = d; }

    public boolean isReferenced()           { return referenced; }
    public void    setReferenced(boolean r) { this.referenced = r; }

    public int  getProtection()        { return protection; }
    public void setProtection(int p)   { this.protection = p; }

    public boolean canRead()  { return (protection & READ)  != 0; }
    public boolean canWrite() { return (protection & WRITE) != 0; }
    public boolean canExec()  { return (protection & EXEC)  != 0; }

    @Override
    public String toString() {
        if (!valid) return "PTE[invalid]";
        return String.format("PTE[frame=%d, %s%s%s, dirty=%b, ref=%b]",
            frameNumber,
            canRead()  ? "R" : "-",
            canWrite() ? "W" : "-",
            canExec()  ? "X" : "-",
            dirty, referenced);
    }
}
