package woche09;

/**
 * Woche 09 — Demo: Adressübersetzung mit einstufiger Seitentabelle.
 *
 * Szenarien:
 *  1. Einfache Adressübersetzung (Seiten bereits im Speicher)
 *  2. Demand Paging: Seiten werden erst bei Zugriff geladen (Page Fault)
 *  3. Mehrere Prozesse mit getrennten Adressräumen
 *  4. Schutzverletzung (kein Schreibrecht)
 *  5. Out-of-Memory (kein freier Frame mehr)
 */
public class Main {

    // Konfiguration — kann frei angepasst werden
    static final int NUM_FRAMES = 8;
    static final int PAGE_SIZE  = 64;   // Bytes

    public static void main(String[] args) {

        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║  Woche 09 — Adressübersetzung & Page Fault Handling  ║");
        System.out.println("╚══════════════════════════════════════════════════════╝\n");

        scenario1_simpleTranslation();
        scenario2_demandPaging();
        scenario3_multipleProcesses();
        scenario4_protectionFault();
        scenario5_outOfMemory();
    }

    // -------------------------------------------------------------------------

    /**
     * Szenario 1: Seiten bereits im Speicher — reine Adressübersetzung.
     * Alle PTEs sind von Anfang an gültig (als ob der Prozess bereits
     * vollständig geladen wäre).
     */
    static void scenario1_simpleTranslation() {
        System.out.println("━━━ Szenario 1: Einfache Adressübersetzung ━━━\n");

        PhysicalMemory mem = new PhysicalMemory(NUM_FRAMES, PAGE_SIZE);
        MemoryManagementUnit mmu = new MemoryManagementUnit(mem);

        // Prozess mit 4 virtuellen Seiten anlegen
        ProcessControlBlock p1 = new ProcessControlBlock(1, "P1", 0, 10);
        p1.initPageTable(4, PAGE_SIZE);

        // Frames manuell zuweisen (Simulation: Prozess vollständig geladen)
        // VPN 0 → Frame 2, VPN 1 → Frame 5, VPN 2 → Frame 1, VPN 3 → Frame 7
        int[] mapping = {2, 5, 1, 7};
        for (int vpn = 0; vpn < 4; vpn++) {
            mem.allocateFrame(1);   // Frame reservieren (vereinfacht: sequenziell)
        }
        // Direkte PTE-Setzung für die Demonstration
        p1.setPTE(0, new PageTableEntry(2, PageTableEntry.READ | PageTableEntry.WRITE));
        p1.setPTE(1, new PageTableEntry(5, PageTableEntry.READ | PageTableEntry.WRITE));
        p1.setPTE(2, new PageTableEntry(1, PageTableEntry.READ | PageTableEntry.WRITE));
        p1.setPTE(3, new PageTableEntry(7, PageTableEntry.READ | PageTableEntry.EXEC));

        p1.printPageTable();
        mem.printFrameMap();

        // Verschiedene Adressen übersetzen
        int[] testAddresses = {0, 63, 64, 100, 128, 200, 255};
        System.out.println("  Adressübersetzungen:");
        for (int va : testAddresses) {
            try {
                int pa = mmu.translate(p1, va, false);
                System.out.printf("    VA %3d  →  PA %3d   (VPN=%d, Offset=%d)%n",
                    va, pa, va / PAGE_SIZE, va % PAGE_SIZE);
            } catch (Exception e) {
                System.out.printf("    VA %3d  →  FEHLER: %s%n", va, e.getMessage());
            }
        }

        System.out.println();
        mmu.printStats();
        System.out.println();
    }

    // -------------------------------------------------------------------------

    /**
     * Szenario 2: Demand Paging — Seiten werden erst bei Zugriff geladen.
     * Alle PTEs starten als invalid. Page Faults werden automatisch behandelt.
     */
    static void scenario2_demandPaging() {
        System.out.println("━━━ Szenario 2: Demand Paging ━━━\n");

        PhysicalMemory mem = new PhysicalMemory(NUM_FRAMES, PAGE_SIZE);
        MemoryManagementUnit mmu = new MemoryManagementUnit(mem);

        // Prozess mit 4 Seiten, alle noch nicht im Speicher
        ProcessControlBlock p2 = new ProcessControlBlock(2, "P2", 0, 20);
        p2.initPageTable(4, PAGE_SIZE);

        System.out.println("  Zustand vor Zugriffen (alle Seiten invalid):");
        p2.printPageTable();

        // Zugriffe — lösen Page Faults aus, werden automatisch behandelt
        int[] accesses = {10, 80, 150, 200, 10, 80};
        System.out.println("  Speicherzugriffe:");
        for (int va : accesses) {
            int pa = mmu.translateWithFaultHandling(p2, va, false);
            System.out.printf("    VA %3d  →  PA %3d%n", va, pa);
        }

        System.out.println("\n  Zustand nach Zugriffen (geladene Seiten):");
        p2.printPageTable();
        mem.printFrameMap();
        mmu.printStats();
        System.out.println();
    }

    // -------------------------------------------------------------------------

    /**
     * Szenario 3: Zwei Prozesse mit getrennten Adressräumen.
     * VA 0 in P3 zeigt auf einen anderen physischen Frame als VA 0 in P4.
     */
    static void scenario3_multipleProcesses() {
        System.out.println("━━━ Szenario 3: Mehrere Prozesse ━━━\n");

        PhysicalMemory mem = new PhysicalMemory(NUM_FRAMES, PAGE_SIZE);
        MemoryManagementUnit mmu = new MemoryManagementUnit(mem);

        ProcessControlBlock p3 = new ProcessControlBlock(3, "P3", 0, 10);
        p3.initPageTable(2, PAGE_SIZE);

        ProcessControlBlock p4 = new ProcessControlBlock(4, "P4", 0, 10);
        p4.initPageTable(2, PAGE_SIZE);

        // P3: VPN 0 → Frame 0, VPN 1 → Frame 1
        // P4: VPN 0 → Frame 2, VPN 1 → Frame 3
        p3.setPTE(0, new PageTableEntry(0, PageTableEntry.READ | PageTableEntry.WRITE));
        p3.setPTE(1, new PageTableEntry(1, PageTableEntry.READ | PageTableEntry.WRITE));
        p4.setPTE(0, new PageTableEntry(2, PageTableEntry.READ | PageTableEntry.WRITE));
        p4.setPTE(1, new PageTableEntry(3, PageTableEntry.READ | PageTableEntry.WRITE));
        mem.allocateFrame(3); mem.allocateFrame(3);
        mem.allocateFrame(4); mem.allocateFrame(4);

        // Gleiche virtuelle Adresse → verschiedene physische Adressen
        int va = 10;
        int pa3 = mmu.translate(p3, va, false);
        int pa4 = mmu.translate(p4, va, false);
        System.out.printf("  P3: VA %d → PA %d%n", va, pa3);
        System.out.printf("  P4: VA %d → PA %d%n", va, pa4);
        System.out.println("  → Isolation: gleiche VA, verschiedene PA ✓\n");
    }

    // -------------------------------------------------------------------------

    /**
     * Szenario 4: Schutzverletzung — Seite hat kein Schreibrecht.
     */
    static void scenario4_protectionFault() {
        System.out.println("━━━ Szenario 4: Schutzverletzung ━━━\n");

        PhysicalMemory mem = new PhysicalMemory(NUM_FRAMES, PAGE_SIZE);
        MemoryManagementUnit mmu = new MemoryManagementUnit(mem);

        ProcessControlBlock p5 = new ProcessControlBlock(5, "P5", 0, 5);
        p5.initPageTable(2, PAGE_SIZE);
        // VPN 0: read-only (z.B. Code-Segment)
        p5.setPTE(0, new PageTableEntry(0, PageTableEntry.READ | PageTableEntry.EXEC));
        p5.setPTE(1, new PageTableEntry(1, PageTableEntry.READ | PageTableEntry.WRITE));

        // Leseversuch auf read-only Seite → OK
        System.out.println("  Leseversuch auf read-only Seite (VA=10):");
        try {
            int pa = mmu.translate(p5, 10, false);
            System.out.println("    → PA=" + pa + " (Lesen erlaubt ✓)");
        } catch (Exception e) {
            System.out.println("    → " + e.getMessage());
        }

        // Schreibversuch auf read-only Seite → Schutzverletzung
        System.out.println("  Schreibversuch auf read-only Seite (VA=10):");
        try {
            mmu.translate(p5, 10, true);
            System.out.println("    → [FEHLER: hätte eine Exception werfen sollen!]");
        } catch (IllegalArgumentException e) {
            System.out.println("    → " + e.getMessage() + " ✓");
        }
        System.out.println();
    }

    // -------------------------------------------------------------------------

    /**
     * Szenario 5: Out-of-Memory — kein freier Frame mehr verfügbar.
     * In Woche 11 wird dieses Problem durch Seitenersetzung gelöst.
     */
    static void scenario5_outOfMemory() {
        System.out.println("━━━ Szenario 5: Out of Memory ━━━\n");

        // Sehr kleiner Speicher: nur 3 Frames
        PhysicalMemory mem = new PhysicalMemory(3, PAGE_SIZE);
        MemoryManagementUnit mmu = new MemoryManagementUnit(mem);

        ProcessControlBlock p6 = new ProcessControlBlock(6, "P6", 0, 10);
        p6.initPageTable(5, PAGE_SIZE);  // 5 Seiten, aber nur 3 Frames

        System.out.println("  Lade Seiten 0, 1, 2 (3 Frames verfügbar):");
        for (int vpn = 0; vpn < 3; vpn++) {
            int va = vpn * PAGE_SIZE;
            int pa = mmu.translateWithFaultHandling(p6, va, false);
            System.out.printf("    VPN %d → PA %d ✓%n", vpn, pa);
        }

        System.out.println("  Versuche Seite 3 zu laden (kein Frame mehr frei):");
        try {
            mmu.translateWithFaultHandling(p6, 3 * PAGE_SIZE, false);
            System.out.println("    → [FEHLER: hätte Out-of-Memory werfen sollen!]");
        } catch (IllegalStateException e) {
            System.out.println("    → " + e.getMessage());
            System.out.println("    → In Woche 11: Seitenersetzungsalgorithmus nötig!");
        }
        System.out.println();
        mmu.printStats();
    }
}
