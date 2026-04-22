package woche09;

/**
 * Memory Management Unit (MMU) — Woche 09
 *
 * Übernimmt die Adressübersetzung (VA → PA) anhand der einstufigen
 * Seitentabelle im PCB des aktiven Prozesses.
 *
 * Wirft PageFaultException wenn die angefragte Seite nicht im Speicher ist.
 * Der Page Fault Handler (handlePageFault) lädt die Seite in einen freien
 * physischen Frame und aktualisiert den PTE.
 */
public class MemoryManagementUnit {

    private final PhysicalMemory physMem;

    // Statistiken
    private int totalTranslations;
    private int totalPageFaults;
    private int totalProtectionFaults;

    public MemoryManagementUnit(PhysicalMemory physMem) {
        this.physMem = physMem;
    }

    // =========================================================================
    // AUFGABE 1 – Adressübersetzung VA → PA
    // =========================================================================
    // Implementieren Sie translate(). Die Methode soll:
    //
    //  Schritt 1: Adresse validieren (va < 0 oder >= virtualAddressSpaceSize → Exception)
    //
    //  Schritt 2: VPN und Offset berechnen:
    //               int pageSize = physMem.getPageSize()
    //               int vpn      = va / pageSize
    //               int offset   = va % pageSize
    //
    //  Schritt 3: totalTranslations++
    //
    //  Schritt 4: PTE holen: PageTableEntry pte = pcb.getPTE(vpn)
    //             Falls pte == null oder !pte.isValid():
    //               → throw new PageFaultException(pcb.getPid(), vpn, va)
    //
    //  Schritt 5: Protection-Check (nur bei Schreibzugriff):
    //             Falls write && !pte.canWrite():
    //               totalProtectionFaults++
    //               → throw new IllegalArgumentException(...)
    //
    //  Schritt 6: Bits aktualisieren:
    //               pte.setReferenced(true)
    //               Falls write: pte.setDirty(true)
    //
    //  Schritt 7: Physische Adresse berechnen und zurückgeben:
    //               return physMem.physicalAddress(pte.getFrameNumber(), offset)
    // =========================================================================

    /**
     * AUFGABE 1 – Adressübersetzung
     *
     * @param pcb    Prozess, dessen Seitentabelle verwendet wird
     * @param va     Virtuelle Adresse
     * @param write  true = Schreibzugriff, false = Lesezugriff
     * @return Physische Adresse
     * @throws PageFaultException       wenn Seite nicht im Speicher
     * @throws IllegalArgumentException bei Schutzverletzung oder ungültiger VA
     */
    public int translate(ProcessControlBlock pcb, int va, boolean write) {
        // TODO: Implementieren Sie die Adressübersetzung gemäß dem Algorithmus oben.
        // Tipp: physMem.getPageSize() liefert die Seitengröße.
        //       pcb.virtualAddressSpaceSize() liefert die Größe des virtuellen Adressraums.
        throw new UnsupportedOperationException("translate() nicht implementiert");
    }

    // =========================================================================
    // AUFGABE 2 – Page Fault Handler
    // =========================================================================
    // Implementieren Sie handlePageFault(). Die Methode soll:
    //
    //  Schritt 1: Freien Frame anfordern:
    //               int frame = physMem.allocateFrame(pcb.getPid())
    //
    //  Schritt 2: Falls frame == -1:
    //               → throw new IllegalStateException("Out of Memory: ...")
    //
    //  Schritt 3: Neuen PTE anlegen und im PCB setzen:
    //               PageTableEntry newPte = new PageTableEntry(frame,
    //                   PageTableEntry.READ | PageTableEntry.WRITE)
    //               pcb.setPTE(fault.getVpn(), newPte)
    //
    //  Schritt 4: totalPageFaults++
    //
    //  Schritt 5: Meldung ausgeben (optional, aber für die Übung hilfreich):
    //               System.out.printf("  [Page Fault] PID=%d VPN=%d → Frame=%d ...%n", ...)
    // =========================================================================

    /**
     * AUFGABE 2 – Page Fault Handler
     *
     * @param pcb    Betroffener Prozess
     * @param fault  Die ausgelöste PageFaultException
     * @throws IllegalStateException wenn kein freier Frame verfügbar
     */
    public void handlePageFault(ProcessControlBlock pcb, PageFaultException fault) {
        // TODO: Implementieren Sie den Page Fault Handler gemäß dem Algorithmus oben.
        // Nützliche Methoden:
        //   physMem.allocateFrame(pid)       → reserviert einen Frame, gibt Nummer zurück
        //   fault.getVpn()                   → VPN der fehlenden Seite
        //   new PageTableEntry(frame, prot)  → neuen gültigen PTE anlegen
        //   pcb.setPTE(vpn, pte)             → PTE in Seitentabelle eintragen
        throw new UnsupportedOperationException("handlePageFault() nicht implementiert");
    }

    // =========================================================================
    // Hilfsmethode (bereits implementiert)
    // =========================================================================

    /**
     * Übersetzt eine VA mit automatischer Page-Fault-Behandlung.
     * Diese Methode ist bereits vollständig — sie nutzt Ihre Implementierungen
     * von translate() und handlePageFault().
     */
    public int translateWithFaultHandling(ProcessControlBlock pcb, int va, boolean write) {
        try {
            return translate(pcb, va, write);
        } catch (PageFaultException fault) {
            handlePageFault(pcb, fault);
            return translate(pcb, va, write);
        }
    }

    /** Gibt Statistiken auf der Konsole aus. */
    public void printStats() {
        System.out.println("=== MMU Statistiken ===");
        System.out.println("  Adressübersetzungen: " + totalTranslations);
        System.out.println("  Page Faults:         " + totalPageFaults);
        System.out.println("  Schutzverletzungen:  " + totalProtectionFaults);
        double pfRate = totalTranslations == 0 ? 0.0
            : (double) totalPageFaults / totalTranslations * 100;
        System.out.printf( "  Page-Fault-Rate:     %.1f%%%n", pfRate);
    }

    // Getter
    public int getTotalTranslations()     { return totalTranslations; }
    public int getTotalPageFaults()       { return totalPageFaults; }
    public int getTotalProtectionFaults() { return totalProtectionFaults; }
    public PhysicalMemory getPhysicalMemory() { return physMem; }
}
