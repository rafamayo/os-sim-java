package woche10;

/**
 * MMU mit TLB -- Woche 10
 *
 * Erweitert die Adressübersetzung aus Woche 09 um einen TLB-Cache.
 */
public class MMUWithTLB {

    private final PhysicalMemory physMem;
    private final TLB            tlb;

    private int tlbHits;
    private int tlbMisses;
    private int pageFaults;
    private int protectionFaults;

    public MMUWithTLB(PhysicalMemory physMem, TLB tlb) {
        this.physMem = physMem;
        this.tlb     = tlb;
    }

    // =========================================================================
    // AUFGABE 2 -- Adressübersetzung mit TLB
    // =========================================================================
    // Erweitern Sie translate() aus Woche 09 um den TLB:
    //
    //  Schritt 1: VA validieren, VPN und Offset berechnen (wie Woche 09).
    //
    //  Schritt 2: TLB-Lookup: int frame = tlb.lookup(pid, vpn)
    //    Hit  (frame != -1):
    //      tlbHits++
    //      Protection-Check (wie Woche 09)
    //      pte.setReferenced(true), bei write pte.setDirty(true)
    //      return physMem.physicalAddress(frame, offset)
    //
    //    Miss (frame == -1):
    //      tlbMisses++
    //      Seitentabelle konsultieren: pte = pcb.getPTE(vpn)
    //      Falls !pte.valid: throw new PageFaultException(pid, vpn, va)
    //      Protection-Check, Bits setzen (wie Hit-Pfad)
    //      TLB befüllen: tlb.insert(pid, vpn, pte.getFrameNumber())
    //      return physMem.physicalAddress(pte.getFrameNumber(), offset)
    // =========================================================================

    /**
     * AUFGABE 2 -- translate() mit TLB
     *
     * @param pcb    Prozess
     * @param va     Virtuelle Adresse
     * @param write  true = Schreibzugriff
     * @return Physische Adresse
     * @throws PageFaultException       bei ungültigem PTE
     * @throws IllegalArgumentException bei Schutzverletzung oder ungültiger VA
     */
    public int translate(ProcessControlBlock pcb, int va, boolean write) {
        // TODO: Implementieren Sie translate() mit TLB-Integration.
        // Nützliche Aufrufe:
        //   tlb.lookup(pid, vpn)            -> frame oder -1
        //   tlb.insert(pid, vpn, frame)     -> TLB befüllen
        //   physMem.physicalAddress(f, off) -> physische Adresse
        //   pcb.getPTE(vpn)                 -> PageTableEntry
        //   throw new PageFaultException(pid, vpn, va)
        throw new UnsupportedOperationException("translate() nicht implementiert");
    }

    // =========================================================================
    // Hilfsmethoden (bereits implementiert)
    // =========================================================================

    /**
     * Übersetzt mit automatischer Page-Fault-Behandlung.
     */
    public int translateWithFaultHandling(ProcessControlBlock pcb,
                                          int va, boolean write) {
        try {
            return translate(pcb, va, write);
        } catch (PageFaultException fault) {
            handlePageFault(pcb, fault);
            return translate(pcb, va, write);
        }
    }

    /** Page Fault Handler (bereits implementiert). */
    public void handlePageFault(ProcessControlBlock pcb, PageFaultException fault) {
        int frame = physMem.allocateFrame(pcb.getPid());
        if (frame == -1) throw new IllegalStateException(
            "Out of Memory: kein freier Frame für PID=" + pcb.getPid());
        PageTableEntry newPte = new PageTableEntry(
            frame, PageTableEntry.READ | PageTableEntry.WRITE);
        pcb.setPTE(fault.getVpn(), newPte);
        pageFaults++;
        System.out.printf("  [Page Fault] PID=%d VPN=%d -> Frame=%d%n",
            pcb.getPid(), fault.getVpn(), frame);
    }

    /** Kontextwechsel: TLB des alten Prozesses flushen (bereits implementiert). */
    public void contextSwitch(int oldPid) {
        tlb.flush(oldPid);
        System.out.printf("  [Context Switch] TLB flush für PID=%d%n", oldPid);
    }

    public void printStats() {
        int total = tlbHits + tlbMisses;
        double hitRate = total == 0 ? 0.0 : (double) tlbHits / total * 100;
        System.out.println("=== MMU+TLB Statistiken ===");
        System.out.printf("  TLB Hits:     %d%n", tlbHits);
        System.out.printf("  TLB Misses:   %d%n", tlbMisses);
        System.out.printf("  Hit Rate:     %.1f%%%n", hitRate);
        System.out.printf("  Page Faults:  %d%n", pageFaults);
    }

    public TLB getTLB()        { return tlb; }
    public int getTlbHits()    { return tlbHits; }
    public int getTlbMisses()  { return tlbMisses; }
    public int getPageFaults() { return pageFaults; }
    public PhysicalMemory getPhysicalMemory() { return physMem; }
}
