package woche08;

/**
 * Repräsentiert einen zusammenhängenden Speicherblock.
 * Wird sowohl für belegte als auch für freie Blöcke verwendet.
 */
public class MemoryBlock {

    private int     start;      // Startadresse (inklusiv)
    private int     size;       // Größe in Bytes
    private boolean free;       // true = frei, false = belegt
    private int     pid;        // PID des belegenden Prozesses (-1 wenn frei)

    public MemoryBlock(int start, int size, boolean free, int pid) {
        this.start = start;
        this.size  = size;
        this.free  = free;
        this.pid   = pid;
    }

    // Bequemer Konstruktor für freie Blöcke
    public MemoryBlock(int start, int size) {
        this(start, size, true, -1);
    }

    public int     getStart()          { return start; }
    public void    setStart(int s)     { this.start = s; }
    public int     getSize()           { return size; }
    public void    setSize(int s)      { this.size = s; }
    public boolean isFree()            { return free; }
    public void    setFree(boolean f)  { this.free = f; }
    public int     getPid()            { return pid; }
    public void    setPid(int p)       { this.pid = p; }

    /** Endadresse (exklusiv) */
    public int end() { return start + size; }

    @Override
    public String toString() {
        if (free) {
            return String.format("[%4d – %4d | size=%4d | FREE  ]", start, end() - 1, size);
        } else {
            return String.format("[%4d – %4d | size=%4d | PID=%-3d]", start, end() - 1, size, pid);
        }
    }
}
