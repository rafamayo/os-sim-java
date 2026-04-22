package woche08;

/**
 * Woche 08 — Demo und Tests für ContiguousMemoryManager und BuddyAllocator.
 *
 * Führt mehrere Szenarien durch und gibt jeweils die Speicherkarte und
 * Fragmentierungsmetriken aus.
 */
public class Main {

    public static void main(String[] args) {

        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║  Woche 08 — Speicherverwaltung: Contiguous + Buddy   ║");
        System.out.println("╚══════════════════════════════════════════════════════╝\n");

        // --- Szenario 1: First-Fit ---
        runContiguousScenario(AllocationStrategy.FIRST_FIT);

        // --- Szenario 2: Best-Fit ---
        runContiguousScenario(AllocationStrategy.BEST_FIT);

        // --- Szenario 3: Worst-Fit ---
        runContiguousScenario(AllocationStrategy.WORST_FIT);

        // --- Szenario 4: Buddy-Allocator ---
        runBuddyScenario();

        // --- Szenario 5: Vergleich Fragmentierung ---
        compareFragmentation();
    }

    // -------------------------------------------------------------------------

    /**
     * Führt ein typisches Allokations-/Freigabe-Szenario durch.
     * Zeigt Fragmentierung nach gezielten Freigaben.
     */
    static void runContiguousScenario(AllocationStrategy strategy) {
        System.out.println("━━━ Szenario: " + strategy + " (1024 Bytes) ━━━\n");
        ContiguousMemoryManager mm = new ContiguousMemoryManager(1024, strategy);

        // Alloziere mehrere Prozesse
        int a1 = mm.allocate(200, 1);
        int a2 = mm.allocate(150, 2);
        int a3 = mm.allocate(300, 3);
        int a4 = mm.allocate(100, 4);
        System.out.printf("Allokiert: P1@%d, P2@%d, P3@%d, P4@%d%n", a1, a2, a3, a4);
        mm.printMemoryMap();

        // Freigabe von P2 und P4 → externe Fragmentierung
        mm.free(a2);
        mm.free(a4);
        System.out.println("Nach Freigabe P2 und P4:");
        mm.printMemoryMap();

        // Neue Anfrage: passt P5=130 in den Speicher?
        int a5 = mm.allocate(130, 5);
        System.out.println("Allokation P5 (130 Bytes) → Adresse: " + a5);
        mm.printMemoryMap();

        // Anfrage die fehlschlägt (zu groß)
        int a6 = mm.allocate(700, 6);
        System.out.println("Allokation P6 (700 Bytes) → Adresse: " + a6
                         + (a6 == -1 ? " [fehlgeschlagen – externe Fragmentierung!]" : ""));
        mm.printMemoryMap();

        mm.printStats();
        System.out.println();
    }

    // -------------------------------------------------------------------------

    /**
     * Demonstriert den Buddy-Allocator: Splitten und Zusammenführen.
     */
    static void runBuddyScenario() {
        System.out.println("━━━ Szenario: Buddy-Allocator (2^10 = 1024 Bytes) ━━━\n");
        BuddyAllocator buddy = new BuddyAllocator(10);  // 1024 Bytes

        buddy.printFreeLists();

        int b1 = buddy.allocate(100);   // → Ordnung 7 (128 Bytes)
        int b2 = buddy.allocate(200);   // → Ordnung 8 (256 Bytes)
        int b3 = buddy.allocate(50);    // → Ordnung 6 (64 Bytes)
        System.out.printf("%nAllokiert: b1@%d (100→128), b2@%d (200→256), b3@%d (50→64)%n",
                          b1, b2, b3);
        buddy.printFreeLists();

        // Freigabe b1 → Merge mit Buddy?
        buddy.free(b1);
        System.out.println("Nach free(b1):");
        buddy.printFreeLists();

        // Freigabe b2 → weiterer Merge
        buddy.free(b2);
        System.out.println("Nach free(b2):");
        buddy.printFreeLists();

        // Freigabe b3 → vollständige Rekombination?
        buddy.free(b3);
        System.out.println("Nach free(b3) — Heap vollständig zusammengeführt?");
        buddy.printFreeLists();

        buddy.printStats();
        System.out.println();
    }

    // -------------------------------------------------------------------------

    /**
     * Vergleicht die externe Fragmentierung aller drei Strategien
     * unter demselben Workload.
     */
    static void compareFragmentation() {
        System.out.println("━━━ Fragmentierungsvergleich (gleicher Workload) ━━━\n");

        int[] sizes  = {100, 250, 80, 300, 120};
        int[] frees  = {1, 3};   // PID-Indizes (0-basiert) die freigegeben werden

        for (AllocationStrategy s : AllocationStrategy.values()) {
            ContiguousMemoryManager mm = new ContiguousMemoryManager(1024, s);
            int[] addrs = new int[sizes.length];

            for (int i = 0; i < sizes.length; i++) {
                addrs[i] = mm.allocate(sizes[i], i + 1);
            }
            for (int idx : frees) {
                mm.free(addrs[idx]);
            }

            System.out.printf("%-12s → EF=%.3f, Holes=%d, Util=%.1f%%%n",
                s, mm.externalFragmentation(),
                mm.numberOfHoles(),
                mm.utilization() * 100);
        }
        System.out.println();
    }
}
