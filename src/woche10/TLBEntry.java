package woche10;

/**
 * Ein Eintrag im Translation Lookaside Buffer (TLB).
 *
 * Felder:
 *   valid     - true: dieser Eintrag enthält eine gültige Übersetzung
 *   vpn       - Virtual Page Number (Schlüssel für die Suche)
 *   frame     - zugehöriger physischer Frame (Wert)
 *   lastUsed  - logischer Zähler des letzten Zugriffs (für LRU-Verdrängung)
 *   pid       - PID des Prozesses (für ASID-Simulation)
 */
public class TLBEntry {

    private boolean valid;
    private int     vpn;
    private int     frame;
    private long    lastUsed;
    private int     pid;

    public TLBEntry() {
        this.valid    = false;
        this.vpn      = -1;
        this.frame    = -1;
        this.lastUsed = 0;
        this.pid      = -1;
    }

    public boolean isValid()           { return valid; }
    public void    setValid(boolean v) { this.valid = v; }
    public int     getVpn()            { return vpn; }
    public void    setVpn(int v)       { this.vpn = v; }
    public int     getFrame()          { return frame; }
    public void    setFrame(int f)     { this.frame = f; }
    public long    getLast()           { return lastUsed; }
    public void    setLast(long t)     { this.lastUsed = t; }
    public int     getPid()            { return pid; }
    public void    setPid(int p)       { this.pid = p; }

    public void invalidate() {
        this.valid = false;
        this.vpn   = -1;
        this.frame = -1;
        this.pid   = -1;
    }

    @Override
    public String toString() {
        if (!valid) return "TLBEntry[invalid]";
        return String.format("TLBEntry[pid=%d, VPN=%d -> Frame=%d, last=%d]",
                             pid, vpn, frame, lastUsed);
    }
}
