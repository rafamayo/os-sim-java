package woche10;

/**
 * Test für Aufgabe 2 -- MMU mit TLB (translate mit TLB-Integration).
 *
 * Voraussetzung: Aufgabe 1 vollständig + MMUWithTLB.translate() implementiert.
 */
public class MainAufgabe2 {

    static final int NUM_FRAMES = 8;
    static final int PAGE_SIZE  = 64;

    public static void main(String[] args) {
        System.out.println("=== Aufgabe 2: MMU mit TLB ===\n");

        testTLBHitVsMiss();
        testContextSwitch();
        testEATWithRealAccesses();
    }

    static void testTLBHitVsMiss() {
        System.out.println("-- TLB Hit vs. Miss bei wiederholten Zugriffen --\n");

        PhysicalMemory mem = new PhysicalMemory(NUM_FRAMES, PAGE_SIZE);
        TLB tlb = new TLB(4);
        MMUWithTLB mmu = new MMUWithTLB(mem, tlb);

        ProcessControlBlock p1 = new ProcessControlBlock(1, "P1", 0, 10);
        p1.initPageTable(4, PAGE_SIZE);

        // Erster Zugriff auf jede Seite: Page Fault + TLB-Miss + TLB-Insert
        System.out.println("  Erster Zugriff (alle Seiten invalid -> Page Fault + TLB-Miss):");
        for (int vpn = 0; vpn < 4; vpn++) {
            int va = vpn * PAGE_SIZE;
            int pa = mmu.translateWithFaultHandling(p1, va, false);
            System.out.printf("    VA=%3d -> PA=%3d%n", va, pa);
        }

        System.out.println();
        tlb.printState();

        // Zweiter Zugriff auf gleiche Seiten: TLB-Hit, kein Page Walk
        System.out.println("\n  Zweiter Zugriff (TLB bereits befüllt -> Hits):");
        for (int vpn = 0; vpn < 4; vpn++) {
            int va = vpn * PAGE_SIZE + 10;
            int pa = mmu.translateWithFaultHandling(p1, va, false);
            System.out.printf("    VA=%3d -> PA=%3d%n", va, pa);
        }

        System.out.println();
        mmu.printStats();
        System.out.println();
    }

    static void testContextSwitch() {
        System.out.println("-- Kontextwechsel und TLB-Flush --\n");

        PhysicalMemory mem = new PhysicalMemory(NUM_FRAMES, PAGE_SIZE);
        TLB tlb = new TLB(4);
        MMUWithTLB mmu = new MMUWithTLB(mem, tlb);

        ProcessControlBlock p1 = new ProcessControlBlock(1, "P1", 0, 5);
        p1.initPageTable(2, PAGE_SIZE);

        ProcessControlBlock p2 = new ProcessControlBlock(2, "P2", 0, 5);
        p2.initPageTable(2, PAGE_SIZE);

        // P1 greift zu -> TLB befüllt
        mmu.translateWithFaultHandling(p1, 0, false);
        mmu.translateWithFaultHandling(p1, PAGE_SIZE, false);
        System.out.println("TLB nach P1-Zugriffen:");
        tlb.printState();

        // Kontextwechsel zu P2: P1-Einträge werden geflusht
        mmu.contextSwitch(p1.getPid());
        System.out.println("TLB nach contextSwitch(P1):");
        tlb.printState();

        // P2 greift zu -> TLB-Miss (P1-Einträge sind weg)
        System.out.println("P2-Zugriff nach Kontextwechsel:");
        int pa = mmu.translateWithFaultHandling(p2, 0, false);
        System.out.printf("  P2: VA=0 -> PA=%d  (TLB-Miss -> Page Fault -> TLB-Insert)%n%n", pa);
        tlb.printState();
        System.out.println();
    }

    static void testEATWithRealAccesses() {
        System.out.println("-- EAT Vergleich: kleiner vs. grosser TLB --\n");
        // Zugriffsmuster: 100 Zugriffe auf 8 Seiten (lokales Muster)
        int[] pattern = new int[100];
        for (int i = 0; i < 100; i++) pattern[i] = (i % 8) * PAGE_SIZE;

        double tHit  = 1.0;
        double tMiss = 201.0;   // 2-level walk: 2*100ns + 1ns hit

        for (int cap : new int[]{1, 2, 4, 8}) {
            PhysicalMemory mem = new PhysicalMemory(16, PAGE_SIZE);
            TLB tlb = new TLB(cap);
            MMUWithTLB mmu = new MMUWithTLB(mem, tlb);

            ProcessControlBlock p = new ProcessControlBlock(1, "P", 0, 10);
            p.initPageTable(8, PAGE_SIZE);

            for (int va : pattern) {
                mmu.translateWithFaultHandling(p, va, false);
            }

            double eat = tlb.effectiveAccessTime(tHit, tMiss);
            System.out.printf("  TLB capacity=%2d: Hits=%3d, Misses=%3d, "
                + "HitRate=%5.1f%%, EAT=%.1f ns%n",
                cap, tlb.getHits(), tlb.getMisses(),
                (double) tlb.getHits() / (tlb.getHits() + tlb.getMisses()) * 100,
                eat);
        }
        System.out.println();
        System.out.println("  Beobachtung: Ab welcher TLB-Groesse wird die EAT");
        System.out.println("  annähernd optimal für dieses Zugriffsmuster?");
    }
}
