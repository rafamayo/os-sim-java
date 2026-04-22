package woche09;

/**
 * Test für Aufgabe 1 — Adressübersetzung (translate).
 *
 * Voraussetzung: translate() implementiert.
 * handlePageFault() wird hier NICHT benötigt — alle PTEs sind bereits
 * von Anfang an gültig gesetzt (kein Page Fault möglich).
 *
 * Ziel: die Dekomposition VA → VPN + Offset → PA Schritt für Schritt
 * nachvollziehen und die Schutzprüfung testen.
 */
public class MainAufgabe1 {

    static final int NUM_FRAMES = 8;
    static final int PAGE_SIZE  = 64;  // Bytes — kleine Werte für übersichtliche Ausgabe

    public static void main(String[] args) {

        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║  Aufgabe 1 — Adressübersetzung VA → PA       ║");
        System.out.println("╚══════════════════════════════════════════════╝\n");

        testBasicTranslation();
        testBoundaries();
        testProtection();
    }

    // -------------------------------------------------------------------------

    /**
     * Grundlegende Übersetzungen mit einer bekannten Seitentabelle.
     * Studierenden können Ergebnisse manuell nachrechnen.
     */
    static void testBasicTranslation() {
        System.out.println("━━━ Grundlegende Adressübersetzung ━━━\n");

        PhysicalMemory mem = new PhysicalMemory(NUM_FRAMES, PAGE_SIZE);
        MemoryManagementUnit mmu = new MemoryManagementUnit(mem);

        // Prozess mit 4 Seiten, alle direkt in Frames geladen
        // Sie Seitentabelle ist bereits vorgegeben und voll!
        // VPN 0 → Frame 2, VPN 1 → Frame 5, VPN 2 → Frame 1, VPN 3 → Frame 7
        ProcessControlBlock p1 = new ProcessControlBlock(1, "P1", 0, 10);
        p1.initPageTable(4, PAGE_SIZE);
        p1.setPTE(0, new PageTableEntry(2, PageTableEntry.READ | PageTableEntry.WRITE));
        p1.setPTE(1, new PageTableEntry(5, PageTableEntry.READ | PageTableEntry.WRITE));
        p1.setPTE(2, new PageTableEntry(1, PageTableEntry.READ | PageTableEntry.WRITE));
        p1.setPTE(3, new PageTableEntry(7, PageTableEntry.READ | PageTableEntry.EXEC));

        System.out.println("  Seitentabelle:");
        p1.printPageTable();

        System.out.println("  Erwartete Übersetzungen (pageSize=" + PAGE_SIZE + "):");
        System.out.printf("  %-6s  %-5s  %-6s  %-6s  %s%n",
                          "VA", "VPN", "Offset", "Frame", "PA");
        System.out.println("  " + "─".repeat(38));

        // Testfälle: VA → erwartetes Ergebnis manuell vorausberechnet
        // PA = frame * pageSize + offset
        int[][] cases = {
            {0,   2},    // VPN=0, Offset=0,  Frame=2 → PA=128
            {63,  2},    // VPN=0, Offset=63, Frame=2 → PA=191
            {64,  5},    // VPN=1, Offset=0,  Frame=5 → PA=320
            {100, 5},    // VPN=1, Offset=36, Frame=5 → PA=356
            {128, 1},    // VPN=2, Offset=0,  Frame=1 → PA=64
            {200, 1},    // VPN=3, Offset=8,  Frame=7 → PA=456  (200/64=3, 200%64=8)
            {255, 7},    // VPN=3, Offset=63, Frame=7 → PA=511
        };

        for (int[] tc : cases) {
            int va = tc[0];
            try {
                int pa     = mmu.translate(p1, va, false);
                int vpn    = va / PAGE_SIZE;
                int offset = va % PAGE_SIZE;
                int frame  = p1.getPTE(vpn).getFrameNumber();
                System.out.printf("  %-6d  %-5d  %-6d  %-6d  %d%n",
                                  va, vpn, offset, frame, pa);
            } catch (Exception e) {
                System.out.printf("  %-6d  FEHLER: %s%n", va, e.getMessage());
            }
        }
        System.out.println();
        mmu.printStats();
        System.out.println();
    }

    // -------------------------------------------------------------------------

    /**
     * Randbedingungen: Adresse 0, letzte gültige Adresse, erste ungültige.
     */
    static void testBoundaries() {
        System.out.println("━━━ Randbedingungen ━━━\n");

        PhysicalMemory mem = new PhysicalMemory(NUM_FRAMES, PAGE_SIZE);
        MemoryManagementUnit mmu = new MemoryManagementUnit(mem);

        ProcessControlBlock p2 = new ProcessControlBlock(2, "P2", 0, 5);
        p2.initPageTable(2, PAGE_SIZE);  // Adressraum: 0–127
        p2.setPTE(0, new PageTableEntry(0, PageTableEntry.READ | PageTableEntry.WRITE));
        p2.setPTE(1, new PageTableEntry(3, PageTableEntry.READ | PageTableEntry.WRITE));

        int lastValid   = p2.virtualAddressSpaceSize() - 1;  // 127
        int firstInvalid = p2.virtualAddressSpaceSize();      // 128

        System.out.println("  Adressraum: 0–" + lastValid);

        tryTranslate(mmu, p2, 0,            false, "VA=0 (erste Adresse)");
        tryTranslate(mmu, p2, lastValid,    false, "VA=" + lastValid + " (letzte gültige)");
        tryTranslate(mmu, p2, firstInvalid, false, "VA=" + firstInvalid + " (erste ungültige)");
        tryTranslate(mmu, p2, -1,           false, "VA=-1 (negativ)");
        System.out.println();
    }

    // -------------------------------------------------------------------------

    /**
     * Schutzmechanismus: Lesen auf read-only → OK, Schreiben → Exception.
     */
    static void testProtection() {
        System.out.println("━━━ Schutzverletzung ━━━\n");

        PhysicalMemory mem = new PhysicalMemory(NUM_FRAMES, PAGE_SIZE);
        MemoryManagementUnit mmu = new MemoryManagementUnit(mem);

        ProcessControlBlock p3 = new ProcessControlBlock(3, "P3", 0, 5);
        p3.initPageTable(2, PAGE_SIZE);
        // VPN 0: read-only (Code-Segment), VPN 1: read-write (Data-Segment)
        p3.setPTE(0, new PageTableEntry(0, PageTableEntry.READ | PageTableEntry.EXEC));
        p3.setPTE(1, new PageTableEntry(1, PageTableEntry.READ | PageTableEntry.WRITE));

        tryTranslate(mmu, p3, 10,  false, "Lesen  VPN=0 (read-only)  → erwartet: OK");
        tryTranslate(mmu, p3, 10,  true,  "Schreiben VPN=0 (read-only) → erwartet: Exception");
        tryTranslate(mmu, p3, 70,  true,  "Schreiben VPN=1 (read-write) → erwartet: OK");
        System.out.println();
    }

    // -------------------------------------------------------------------------

    static void tryTranslate(MemoryManagementUnit mmu, ProcessControlBlock pcb,
                              int va, boolean write, String label) {
        try {
            int pa = mmu.translate(pcb, va, write);
            System.out.printf("  %-45s PA=%d ✓%n", label + ":", pa);
        } catch (PageFaultException e) {
            System.out.printf("  %-45s PageFault (VPN=%d)%n", label + ":", e.getVpn());
        } catch (IllegalArgumentException e) {
            System.out.printf("  %-45s %s ✓%n", label + ":", e.getMessage());
        }
    }
}
