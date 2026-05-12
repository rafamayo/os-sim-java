package woche10;

/**
 * Woche 10 -- Gesamttest: TLB & EAT (setzt Aufgaben 1 und 2 voraus).
 *
 * Szenarien:
 *  1. Sequenzielles Zugriffsmuster (hohe Lokalität)
 *  2. Zufälliges Zugriffsmuster (niedrige Lokalität)
 *  3. Kontextwechsel-Kosten (TLB-Flush)
 *  4. EAT-Vergleich: verschiedene TLB-Größen und Zugriffsmuster
 */
public class Main {

    static final int NUM_FRAMES = 16;
    static final int PAGE_SIZE  = 64;
    static final int NUM_PAGES  = 8;

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║  Woche 10 -- TLB & Effective Access Time     ║");
        System.out.println("╚══════════════════════════════════════════════╝\n");

        scenario1_sequential();
        scenario2_random();
        scenario3_contextSwitch();
        scenario4_eatComparison();
    }

    // Szenario 1: Sequenzieller Zugriff -- hohe Hit-Rate erwartet
    static void scenario1_sequential() {
        System.out.println("=== Szenario 1: Sequenzieller Zugriff ===\n");
        MMUWithTLB mmu = buildMmu(4);
        ProcessControlBlock p = buildProcess(1);

        // 50 Zugriffe, immer die gleichen 4 Seiten
        for (int i = 0; i < 50; i++) {
            int va = (i % 4) * PAGE_SIZE + (i % PAGE_SIZE);
            mmu.translateWithFaultHandling(p, va, false);
        }
        mmu.printStats();
        mmu.getTLB().printStats();
        System.out.println();
    }

    // Szenario 2: Zufälliger Zugriff -- niedrige Hit-Rate
    static void scenario2_random() {
        System.out.println("=== Szenario 2: Zufälliger Zugriff (8 Seiten, TLB=4) ===\n");
        MMUWithTLB mmu = buildMmu(4);
        ProcessControlBlock p = buildProcess(2);

        // Rotierendes Muster durch alle 8 Seiten -> TLB-Kapazität zu klein
        for (int i = 0; i < 40; i++) {
            int va = (i % NUM_PAGES) * PAGE_SIZE;
            mmu.translateWithFaultHandling(p, va, false);
        }
        mmu.printStats();
        mmu.getTLB().printStats();
        System.out.println();
    }

    // Szenario 3: Kontextwechsel-Kosten
    static void scenario3_contextSwitch() {
        System.out.println("=== Szenario 3: Kontextwechsel ===\n");

        PhysicalMemory mem = new PhysicalMemory(NUM_FRAMES, PAGE_SIZE);
        TLB tlb = new TLB(4);
        MMUWithTLB mmu = new MMUWithTLB(mem, tlb);

        ProcessControlBlock p1 = buildProcessWith(1, mem);
        ProcessControlBlock p2 = buildProcessWith(2, mem);

        // P1 läuft: 10 Zugriffe, TLB befüllt
        for (int i = 0; i < 10; i++)
            mmu.translateWithFaultHandling(p1, (i % 4) * PAGE_SIZE, false);

        System.out.println("TLB nach P1 (10 Zugriffe):");
        tlb.printState();

        // Kontextwechsel
        mmu.contextSwitch(1);
        System.out.println("TLB nach Kontextwechsel (P1 -> P2):");
        tlb.printState();

        // P2 läuft: erste Zugriffe alle Miss
        for (int i = 0; i < 4; i++)
            mmu.translateWithFaultHandling(p2, i * PAGE_SIZE, false);

        System.out.println("TLB nach P2 (4 Zugriffe):");
        tlb.printState();
        mmu.printStats();
        System.out.println();
    }

    // Szenario 4: EAT-Vergleich verschiedener TLB-Größen
    static void scenario4_eatComparison() {
        System.out.println("=== Szenario 4: EAT-Vergleich ===\n");
        double tHit  = 1.0;
        double tMiss = 301.0;   // 2-level walk + data

        System.out.printf("  %-8s  %-10s  %-10s  %-10s  %s%n",
            "TLB-Cap", "Hits", "Misses", "HitRate", "EAT (ns)");
        System.out.println("  " + "-".repeat(52));

        // Zugriffsmuster mit starker Lokalität (4 heiße Seiten)
        int[] hotPattern = new int[80];
        for (int i = 0; i < 80; i++) hotPattern[i] = (i % 4) * PAGE_SIZE;

        for (int cap : new int[]{1, 2, 4, 8, 16}) {
            PhysicalMemory mem = new PhysicalMemory(NUM_FRAMES, PAGE_SIZE);
            TLB tlb = new TLB(cap);
            MMUWithTLB mmu = new MMUWithTLB(mem, tlb);
            ProcessControlBlock p = buildProcessWith(1, mem);

            for (int va : hotPattern)
                mmu.translateWithFaultHandling(p, va, false);

            int h = tlb.getHits(), m = tlb.getMisses();
            double eat = tlb.effectiveAccessTime(tHit, tMiss);
            System.out.printf("  %-8d  %-10d  %-10d  %6.1f%%  %.2f%n",
                cap, h, m, (double) h / (h + m) * 100, eat);
        }
        System.out.println();
        System.out.println("  Beobachtung: Ab TLB-Groesse 4 (= Anzahl heisser Seiten)");
        System.out.println("  werden alle Wiederholungszugriffe zu Hits.");
    }

    // --- Hilfsmethoden ---

    static MMUWithTLB buildMmu(int tlbCapacity) {
        PhysicalMemory mem = new PhysicalMemory(NUM_FRAMES, PAGE_SIZE);
        TLB tlb = new TLB(tlbCapacity);
        return new MMUWithTLB(mem, tlb);
    }

    static ProcessControlBlock buildProcess(int pid) {
        ProcessControlBlock p = new ProcessControlBlock(pid, "P" + pid, 0, 10);
        p.initPageTable(NUM_PAGES, PAGE_SIZE);
        return p;
    }

    static ProcessControlBlock buildProcessWith(int pid, PhysicalMemory mem) {
        ProcessControlBlock p = new ProcessControlBlock(pid, "P" + pid, 0, 10);
        p.initPageTable(NUM_PAGES, PAGE_SIZE);
        return p;
    }
}
