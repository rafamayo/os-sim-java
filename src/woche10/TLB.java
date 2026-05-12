package woche10;

/**
 * Translation Lookaside Buffer (TLB) -- Woche 10
 *
 * Fully-associative TLB mit konfigurierbarer Kapazität und LRU-Verdrängung.
 * Einträge sind pro PID getrennt (ASID-Simulation).
 */
public class TLB {

    public static final int DEFAULT_CAPACITY = 4;

    private final int        capacity;
    private final TLBEntry[] entries;
    private long             clock;

    // Statistiken
    private int hits;
    private int misses;
    private int evictions;
    private int flushes;

    public TLB(int capacity) {
        this.capacity = capacity;
        this.entries  = new TLBEntry[capacity];
        for (int i = 0; i < capacity; i++) entries[i] = new TLBEntry();
        this.clock = 0;
    }

    public TLB() { this(DEFAULT_CAPACITY); }

    // =========================================================================
    // AUFGABE 1a -- TLB Lookup
    // =========================================================================
    // Suche den Eintrag der zu (pid, vpn) passt.
    // Ein Eintrag passt wenn: entry.isValid() && entry.getPid()==pid
    //                                         && entry.getVpn()==vpn
    // Bei Treffer: entry.setLast(++clock), hits++, frame zurückgeben.
    // Kein Treffer: misses++, -1 zurückgeben.
    // =========================================================================

    /**
     * AUFGABE 1a -- Lookup
     *
     * @return Framenummer bei Hit, -1 bei Miss
     */
    public int lookup(int pid, int vpn) {
        // TODO: Durchsuchen Sie entries[] nach einem passenden Eintrag.
        throw new UnsupportedOperationException("lookup() nicht implementiert");
    }

    // =========================================================================
    // AUFGABE 1b -- TLB Insert mit LRU-Verdrängung
    // =========================================================================
    // 1. Suche einen freien (ungültigen) Eintrag in entries[].
    // 2. Falls keiner frei: wählen Sie den Eintrag mit kleinstem getLast()
    //    als LRU-Opfer, evictions++.
    // 3. Befüllen Sie den gewählten Eintrag:
    //      setValid(true), setPid(pid), setVpn(vpn),
    //      setFrame(frame), setLast(++clock)
    // =========================================================================

    /**
     * AUFGABE 1b -- Insert
     */
    public void insert(int pid, int vpn, int frame) {
        // TODO: Implementieren Sie Insert mit LRU-Verdrängung.
        // Tipp: fill(e, pid, vpn, frame) befüllt einen Eintrag (bereits impl.)
        throw new UnsupportedOperationException("insert() nicht implementiert");
    }

    /** Hilfsmethode: befüllt einen Eintrag (bereits implementiert). */
    private void fill(TLBEntry e, int pid, int vpn, int frame) {
        e.setValid(true);
        e.setPid(pid);
        e.setVpn(vpn);
        e.setFrame(frame);
        e.setLast(++clock);
    }

    // =========================================================================
    // AUFGABE 1c -- TLB Flush
    // =========================================================================
    // Invalidieren Sie alle Einträge, für die gilt:
    //   pid == -1  (vollständiger Flush)  ODER  entry.getPid() == pid
    // Rufen Sie entry.invalidate() auf.
    // flushes++ nach der Schleife.
    // =========================================================================

    /**
     * AUFGABE 1c -- Flush
     *
     * @param pid  PID des zu flushenden Prozesses; -1 = vollständiger Flush
     */
    public void flush(int pid) {
        // TODO: Invalidieren Sie alle Einträge des angegebenen Prozesses.
        throw new UnsupportedOperationException("flush() nicht implementiert");
    }

    public void flushAll() { flush(-1); }

    // =========================================================================
    // Hilfsmethoden (bereits implementiert)
    // =========================================================================

    public void printState() {
        System.out.println("  TLB (capacity=" + capacity + "):");
        for (int i = 0; i < capacity; i++)
            System.out.println("    [" + i + "] " + entries[i]);
    }

    public void printStats() {
        int total = hits + misses;
        double hitRate = total == 0 ? 0.0 : (double) hits / total * 100;
        System.out.println("=== TLB Statistiken ===");
        System.out.printf("  Hits:      %d%n", hits);
        System.out.printf("  Misses:    %d%n", misses);
        System.out.printf("  Hit Rate:  %.1f%%%n", hitRate);
        System.out.printf("  Evictions: %d%n", evictions);
        System.out.printf("  Flushes:   %d%n", flushes);
    }

    /**
     * Berechnet die Effective Access Time (EAT):
     *   EAT = (1 - p_miss) * t_hit + p_miss * t_miss
     * Diese Methode ist bereits implementiert.
     */
    public double effectiveAccessTime(double tHit, double tMiss) {
        int total = hits + misses;
        if (total == 0) return tHit;
        double pMiss = (double) misses / total;
        return (1 - pMiss) * tHit + pMiss * tMiss;
    }

    public int getHits()      { return hits; }
    public int getMisses()    { return misses; }
    public int getCapacity()  { return capacity; }
    public int getEvictions() { return evictions; }
    public int getFlushes()   { return flushes; }
}
