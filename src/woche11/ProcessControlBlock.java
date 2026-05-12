package woche11;

/**
 * Process Control Block (PCB) — Woche 09
 *
 * Erweiterung gegenüber Woche 08:
 *   - pageTable:    Array von PTEs (einstufige Seitentabelle)
 *   - numPages:     Anzahl virtueller Seiten des Prozesses
 *   - pageSize:     Seitengröße in Bytes (wird vom MemoryManagementUnit vorgegeben)
 *
 * Die Seitentabelle wird beim Erzeugen des PCB mit ungültigen PTEs initialisiert.
 * Der Page Fault Handler setzt einzelne Einträge auf valid, sobald eine Seite
 * in einen physischen Frame geladen wird.
 */
public class ProcessControlBlock {

    private int    pid;
    private String name;
    private ProcessState state;
    private int    priority;
    private int    arrivalTime;
    private int    burstTime;
    private int    remainingTime;
    private int    startTime;
    private int    finishTime;
    private int    usedTime;

    // --- Speicherverwaltung (neu in Woche 09) ---
    private PageTableEntry[] pageTable;   // einstufige Seitentabelle
    private int numPages;                 // Anzahl virtueller Seiten
    private int pageSize;                 // Seitengröße in Bytes

    /**
     * Erzeugt einen neuen PCB ohne Seitentabelle.
     * Seitentabelle wird später über initPageTable() angelegt.
     */
    public ProcessControlBlock(int pid, String name, int arrivalTime, int burstTime) {
        this.pid           = pid;
        this.name          = name;
        this.arrivalTime   = arrivalTime;
        this.burstTime     = burstTime;
        this.remainingTime = burstTime;
        this.state         = ProcessState.NEW;
        this.priority      = 0;
        this.startTime     = -1;
        this.finishTime    = -1;
        this.usedTime      = 0;
        this.pageTable     = null;
        this.numPages      = 0;
        this.pageSize      = 0;
    }

    /**
     * Initialisiert die Seitentabelle für diesen Prozess.
     * Alle PTEs werden als ungültig (invalid) angelegt.
     *
     * @param numPages  Anzahl der virtuellen Seiten
     * @param pageSize  Seitengröße in Bytes
     */
    public void initPageTable(int numPages, int pageSize) {
        this.numPages  = numPages;
        this.pageSize  = pageSize;
        this.pageTable = new PageTableEntry[numPages];
        for (int i = 0; i < numPages; i++) {
            pageTable[i] = new PageTableEntry();  // invalid
        }
    }

    /**
     * Gibt die virtuelle Adressgröße des Prozesses in Bytes zurück.
     */
    public int virtualAddressSpaceSize() {
        return numPages * pageSize;
    }

    /** Gibt den PTE für die angegebene virtuelle Seitennummer zurück. */
    public PageTableEntry getPTE(int vpn) {
        if (pageTable == null || vpn < 0 || vpn >= numPages) return null;
        return pageTable[vpn];
    }

    /** Setzt den PTE für die angegebene virtuelle Seitennummer. */
    public void setPTE(int vpn, PageTableEntry pte) {
        if (pageTable != null && vpn >= 0 && vpn < numPages) {
            pageTable[vpn] = pte;
        }
    }

    /** Gibt die gesamte Seitentabelle aus (für Debugging). */
    public void printPageTable() {
        if (pageTable == null) {
            System.out.println("  [keine Seitentabelle]");
            return;
        }
        System.out.println("  Seitentabelle PID=" + pid
                         + " (" + numPages + " Seiten, pageSize=" + pageSize + "):");
        for (int i = 0; i < numPages; i++) {
            System.out.printf("    VPN %3d → %s%n", i, pageTable[i]);
        }
    }

    // --- Getter / Setter (Scheduling-Felder, unverändert) ---
    public int    getPid()            { return pid; }
    public String getName()           { return name; }
    public ProcessState getState()    { return state; }
    public void   setState(ProcessState s) { this.state = s; }
    public int    getPriority()       { return priority; }
    public void   setPriority(int p)  { this.priority = p; }
    public int    getArrivalTime()    { return arrivalTime; }
    public int    getBurstTime()      { return burstTime; }
    public int    getRemainingTime()  { return remainingTime; }
    public void   setRemainingTime(int r) { this.remainingTime = r; }
    public int    getStartTime()      { return startTime; }
    public void   setStartTime(int t) { this.startTime = t; }
    public int    getFinishTime()     { return finishTime; }
    public void   setFinishTime(int t){ this.finishTime = t; }
    public int    getUsedTime()       { return usedTime; }
    public void   setUsedTime(int u)  { this.usedTime = u; }

    // --- Getter für Speicherverwaltung ---
    public PageTableEntry[] getPageTable() { return pageTable; }
    public int getNumPages()               { return numPages; }
    public int getPageSize()               { return pageSize; }

    @Override
    public String toString() {
        return String.format(
            "PCB{pid=%d, name='%s', state=%s, pages=%d, pageSize=%d}",
            pid, name, state, numPages, pageSize);
    }
}
