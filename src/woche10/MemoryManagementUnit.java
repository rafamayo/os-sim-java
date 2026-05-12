package woche10;

/**
 * Memory Management Unit (MMU) — Woche 10
 *
 * Erweiterung gegenüber Woche 09: TLB als erste Übersetzungsstufe.
 */
public class MemoryManagementUnit {

    private final PhysicalMemory physMem;
    private final TLB            tlb;

    private int totalTranslations;
    private int totalPageFaults;
    private int totalProtectionFaults;

    public MemoryManagementUnit(PhysicalMemory physMem, TLB tlb) {
        this.physMem = physMem;
        this.tlb     = tlb;
    }

    public MemoryManagementUnit(PhysicalMemory physMem) {
        this(physMem, new TLB());
    }

    // =========================================================================
    // AUFGABE 2 – Adressübersetzung mit TLB
    // =========================================================================
    // Implementieren Sie translateWithTLB().
    //
    // Algorithmus:
    //  1. VA validieren: va < 0 oder va >= virtualAddressSpaceSize → Exception
    //
    //  2. Dekomposition:
    //       int pageSize = physMem.getPageSize()
    //       int vpn      = va / pageSize
    //       int offset   = va % pageSize
    //       int pid      = pcb.getPid()
    //       totalTranslations++
    //
    //  3. TLB-Lookup:
    //       int frame = tlb.lookup(vpn, pid)
    //
    //     Bei HIT (frame != -1):
    //       - PTE-Bits aktualisieren: pte.setReferenced(true), ggf. setDirty(true)
    //       - return physMem.physicalAddress(frame, offset)
    //
    //     Bei MISS (frame == -1):
    //       a. PTE holen: PageTableEntry pte = pcb.getPTE(vpn)
    //       b. PTE null oder !valid → throw new PageFaultException(pid, vpn, va)
    //       c. Protection-Check: write && !pte.canWrite()
    //            → totalProtectionFaults++
    //            → throw new IllegalArgumentException(...)
    //       d. TLB befüllen: tlb.insert(vpn, pte.getFrameNumber(), pid)
    //       e. Bits setzen: pte.setReferenced(true), ggf. setDirty(true)
    //       f. return physMem.physicalAddress(pte.getFrameNumber(), offset)
    // =========================================================================

    /**
     * AUFGABE 2 — Adressübersetzung mit TLB
     *
     * @param pcb    Prozess
     * @param va     Virtuelle Adresse
     * @param write  true = Schreibzugriff
     * @return Physische Adresse
     * @throws PageFaultException       bei ungültigem PTE
     * @throws IllegalArgumentException bei Schutzverletzung oder ungültiger VA
     */
    public int translateWithTLB(ProcessControlBlock pcb, int va, boolean write) {
        // TODO: Implementieren Sie translateWithTLB() gemäß dem Algorithmus oben.
        // Schlüsselunterschied zu Woche 09: erst TLB prüfen,
        // nur bei Miss den Page Table Walk durchführen.
        throw new UnsupportedOperationException("translateWithTLB() nicht implementiert");
    }

    // =========================================================================
    // Hilfsmethoden (bereits implementiert)
    // =========================================================================

    /** Page Fault Handler (unverändert aus Woche 09). */
    public void handlePageFault(ProcessControlBlock pcb, PageFaultException fault) {
        int frame = physMem.allocateFrame(pcb.getPid());
        if (frame == -1) {
            throw new IllegalStateException(
                "Out of Memory: kein freier Frame für PID=" + pcb.getPid());
        }
        PageTableEntry newPte = new PageTableEntry(
            frame, PageTableEntry.READ | PageTableEntry.WRITE);
        pcb.setPTE(fault.getVpn(), newPte);
        totalPageFaults++;
        System.out.printf("  [Page Fault] PID=%d VPN=%d → Frame=%d%n",
            pcb.getPid(), fault.getVpn(), frame);
    }

    /**
     * Übersetzt mit automatischer Page-Fault-Behandlung und TLB.
     * Diese Methode ist bereits vollständig — sie nutzt Ihre Implementierung
     * von translateWithTLB() und handlePageFault().
     */
    public int translateWithFaultHandling(ProcessControlBlock pcb,
                                          int va, boolean write) {
        try {
            return translateWithTLB(pcb, va, write);
        } catch (PageFaultException fault) {
            handlePageFault(pcb, fault);
            return translateWithTLB(pcb, va, write);
        }
    }

    /** Kontextwechsel: TLB flush. */
    public void contextSwitch() { tlb.flush(); }

    /** Gibt Statistiken aus. */
    public void printStats() {
        System.out.println("=== MMU Statistiken ===");
        System.out.println("  Adressübersetzungen: " + totalTranslations);
        System.out.println("  Page Faults:         " + totalPageFaults);
        System.out.println("  Schutzverletzungen:  " + totalProtectionFaults);
        tlb.printStats();
    }

    /** Berechnet und gibt EAT aus. */
    public void printEAT(double tHit, double tMem, int levels) {
        double tMiss = (levels + 1) * tMem;
        double eat   = tlb.effectiveAccessTime(tHit, tMiss);
        System.out.printf("=== Effective Access Time ===%n");
        System.out.printf("  t_hit  = %.1f ns%n", tHit);
        System.out.printf("  t_miss = %.1f ns (Walk: %d×%.0f ns + Daten)%n",
                          tMiss, levels, tMem);
        System.out.printf("  EAT    = %.2f ns%n", eat);
        System.out.printf("  (ohne TLB wäre EAT = %.1f ns)%n",
                          (levels + 1) * tMem);
    }

    // Getter
    public TLB            getTLB()            { return tlb; }
    public PhysicalMemory getPhysicalMemory() { return physMem; }
    public int getTotalTranslations()         { return totalTranslations; }
    public int getTotalPageFaults()           { return totalPageFaults; }
}
