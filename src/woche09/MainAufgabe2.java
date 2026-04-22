package woche09;

/**
 * Test für Aufgabe 2 — Page Fault Handler.
 *
 * Voraussetzung: Aufgabe 1 vollständig + handlePageFault() implementiert.
 *
 * Ziel: Demand Paging Schritt für Schritt erleben — Seiten werden erst
 * bei Zugriff in physische Frames geladen. Danach: Out-of-Memory als
 * Motivation für Woche 11 (Seitenersetzung).
 */
public class MainAufgabe2 {

    static final int NUM_FRAMES = 6;
    static final int PAGE_SIZE  = 64;

    public static void main(String[] args) {

        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║  Aufgabe 2 — Page Fault Handler              ║");
        System.out.println("╚══════════════════════════════════════════════╝\n");

        testDemandPaging();
        testDirtyAndReferencedBits();
        testMultipleProcesses();
        testOutOfMemory();
    }

    // -------------------------------------------------------------------------

    /**
     * Demand Paging: alle PTEs starten invalid, Seiten werden on-demand geladen.
     * Zweiter Zugriff auf dieselbe Seite → kein Page Fault mehr.
     */
    static void testDemandPaging() {
        System.out.println("━━━ Demand Paging ━━━\n");

        PhysicalMemory mem = new PhysicalMemory(NUM_FRAMES, PAGE_SIZE);
        MemoryManagementUnit mmu = new MemoryManagementUnit(mem);

        ProcessControlBlock p1 = new ProcessControlBlock(1, "P1", 0, 20);
        p1.initPageTable(4, PAGE_SIZE);

        System.out.println("  Seitentabelle vor Zugriffen (alle invalid):");
        p1.printPageTable();

        // Zugriffe — erste Zugriffe lösen Page Faults aus
        int[] accesses = {10, 80, 150, 10, 80, 200};
        System.out.println("  Zugriffe:");
        for (int va : accesses) {
            boolean wasValid = p1.getPTE(va / PAGE_SIZE).isValid();
            int pa = mmu.translateWithFaultHandling(p1, va, false);
            System.out.printf("    VA %3d → PA %3d%s%n",
                va, pa,
                wasValid ? "" : "  [Page Fault → Frame geladen]");
        }

        System.out.println("\n  Seitentabelle nach Zugriffen:");
        p1.printPageTable();
        System.out.println();
        mmu.printStats();
        System.out.println();
    }

    // -------------------------------------------------------------------------

    /**
     * Prüft ob dirty- und referenced-Bits korrekt gesetzt werden.
     */
    static void testDirtyAndReferencedBits() {
        System.out.println("━━━ dirty- und referenced-Bits ━━━\n");

        PhysicalMemory mem = new PhysicalMemory(NUM_FRAMES, PAGE_SIZE);
        MemoryManagementUnit mmu = new MemoryManagementUnit(mem);

        ProcessControlBlock p2 = new ProcessControlBlock(2, "P2", 0, 10);
        p2.initPageTable(2, PAGE_SIZE);

        // Seite 0 laden (Page Fault)
        mmu.translateWithFaultHandling(p2, 0, false);   // Lesen
        PageTableEntry pte0 = p2.getPTE(0);
        System.out.printf("  Nach Lesen  VPN=0: referenced=%b, dirty=%b%n",
                          pte0.isReferenced(), pte0.isDirty());

        // Seite 1 laden und schreiben
        mmu.translateWithFaultHandling(p2, 64, true);   // Schreiben
        PageTableEntry pte1 = p2.getPTE(1);
        System.out.printf("  Nach Schreiben VPN=1: referenced=%b, dirty=%b%n",
                          pte1.isReferenced(), pte1.isDirty());

        System.out.println();
        System.out.println("  Diese Bits werden in Woche 11 für den");
        System.out.println("  Clock-Algorithmus (Seitenersetzung) benötigt.");
        System.out.println();
    }

    // -------------------------------------------------------------------------

    /**
     * Zwei Prozesse mit getrennten Adressräumen: gleiche VA → verschiedene PA.
     */
    static void testMultipleProcesses() {
        System.out.println("━━━ Prozess-Isolation ━━━\n");

        PhysicalMemory mem = new PhysicalMemory(NUM_FRAMES, PAGE_SIZE);
        MemoryManagementUnit mmu = new MemoryManagementUnit(mem);

        ProcessControlBlock p3 = new ProcessControlBlock(3, "P3", 0, 5);
        p3.initPageTable(2, PAGE_SIZE);

        ProcessControlBlock p4 = new ProcessControlBlock(4, "P4", 0, 5);
        p4.initPageTable(2, PAGE_SIZE);

        // Beide Prozesse greifen auf VA=0 zu
        int pa3 = mmu.translateWithFaultHandling(p3, 0, false);
        int pa4 = mmu.translateWithFaultHandling(p4, 0, false);

        System.out.printf("  P3: VA=0 → PA=%d (Frame=%d)%n", pa3, p3.getPTE(0).getFrameNumber());
        System.out.printf("  P4: VA=0 → PA=%d (Frame=%d)%n", pa4, p4.getPTE(0).getFrameNumber());
        System.out.println("  → Gleiche VA, verschiedene PA: Isolation ✓");
        System.out.println();
    }

    // -------------------------------------------------------------------------

    /**
     * Out-of-Memory: kein freier Frame mehr — Motivation für Woche 11.
     */
    static void testOutOfMemory() {
        System.out.println("━━━ Out of Memory ━━━\n");

        // Nur 3 Frames, Prozess hat 5 Seiten
        PhysicalMemory mem = new PhysicalMemory(3, PAGE_SIZE);
        MemoryManagementUnit mmu = new MemoryManagementUnit(mem);

        ProcessControlBlock p5 = new ProcessControlBlock(5, "P5", 0, 10);
        p5.initPageTable(5, PAGE_SIZE);

        System.out.println("  Lade Seiten 0–2 (3 Frames verfügbar):");
        for (int vpn = 0; vpn < 3; vpn++) {
            int pa = mmu.translateWithFaultHandling(p5, vpn * PAGE_SIZE, false);
            System.out.printf("    VPN %d → PA %d ✓%n", vpn, pa);
        }

        System.out.println("\n  Versuche Seite 3 zu laden (kein Frame frei):");
        try {
            mmu.translateWithFaultHandling(p5, 3 * PAGE_SIZE, false);
            System.out.println("  [FEHLER: hätte IllegalStateException werfen sollen!]");
        } catch (IllegalStateException e) {
            System.out.println("  → " + e.getMessage());
            System.out.println("  → Lösung: Seitenersetzung (Woche 11)");
        }
        System.out.println();
        mmu.printStats();
    }
}
