package woche10;

/**
 * Test für Aufgabe 1 -- TLB lookup(), insert(), flush().
 *
 * Voraussetzung: lookup(), insert(), flush() implementiert.
 * MMUWithTLB wird hier NICHT benötigt -- TLB wird direkt getestet.
 */
public class MainAufgabe1 {

    public static void main(String[] args) {
        System.out.println("=== Aufgabe 1: TLB Grundfunktionen ===\n");

        testLookupAndInsert();
        testLRUEviction();
        testFlush();
        testEAT();
    }

    static void testLookupAndInsert() {
        System.out.println("-- lookup() und insert() --\n");
        TLB tlb = new TLB(4);

        // Leerer TLB: alles Miss
        System.out.println("Leerer TLB:");
        System.out.println("  lookup(pid=1, vpn=0) = " + tlb.lookup(1, 0)
            + "  [erwartet: -1]");
        System.out.println("  lookup(pid=1, vpn=1) = " + tlb.lookup(1, 1)
            + "  [erwartet: -1]");

        // Einträge einfügen
        tlb.insert(1, 0, 5);   // VPN 0 -> Frame 5
        tlb.insert(1, 1, 2);   // VPN 1 -> Frame 2
        tlb.insert(1, 2, 7);   // VPN 2 -> Frame 7

        System.out.println("\nNach insert(1,0,5), insert(1,1,2), insert(1,2,7):");
        tlb.printState();

        System.out.println("  lookup(pid=1, vpn=0) = " + tlb.lookup(1, 0)
            + "  [erwartet: 5]");
        System.out.println("  lookup(pid=1, vpn=1) = " + tlb.lookup(1, 1)
            + "  [erwartet: 2]");
        System.out.println("  lookup(pid=1, vpn=9) = " + tlb.lookup(1, 9)
            + "  [erwartet: -1 (nicht vorhanden)]");

        // ASID: PID=2 sieht PID=1-Einträge nicht
        System.out.println("  lookup(pid=2, vpn=0) = " + tlb.lookup(2, 0)
            + "  [erwartet: -1 (anderer Prozess)]");
        System.out.println();
        tlb.printStats();
        System.out.println();
    }

    static void testLRUEviction() {
        System.out.println("-- LRU-Verdrängung (capacity=2) --\n");
        TLB tlb = new TLB(2);

        tlb.insert(1, 0, 10);  // t=1
        tlb.insert(1, 1, 20);  // t=2
        System.out.println("Nach insert(0->10) und insert(1->20):");
        tlb.printState();

        // Seite 0 wird erneut zugegriffen -> LRU-Zeitstempel aktualisiert
        tlb.lookup(1, 0);      // t=3 für VPN=0
        System.out.println("Nach lookup(vpn=0) [LRU-Update auf VPN=0]:");
        tlb.printState();

        // Dritter Insert: VPN=1 ist jetzt LRU -> wird verdrängt
        tlb.insert(1, 2, 30);  // t=4
        System.out.println("Nach insert(2->30) [VPN=1 sollte verdrängt werden]:");
        tlb.printState();

        System.out.println("  lookup(vpn=0) = " + tlb.lookup(1, 0)
            + "  [erwartet: 10 (nicht verdrängt)]");
        System.out.println("  lookup(vpn=1) = " + tlb.lookup(1, 1)
            + "  [erwartet: -1 (verdrängt)]");
        System.out.println("  lookup(vpn=2) = " + tlb.lookup(1, 2)
            + "  [erwartet: 30]");
        System.out.println();
        tlb.printStats();
        System.out.println();
    }

    static void testFlush() {
        System.out.println("-- flush() --\n");
        TLB tlb = new TLB(4);

        tlb.insert(1, 0, 5);
        tlb.insert(1, 1, 6);
        tlb.insert(2, 0, 9);   // anderer Prozess
        System.out.println("Vor flush: P1 und P2 im TLB:");
        tlb.printState();

        tlb.flush(1);  // nur P1 flushen
        System.out.println("Nach flush(pid=1): P1-Einträge weg, P2 bleibt:");
        tlb.printState();

        System.out.println("  lookup(pid=1, vpn=0) = " + tlb.lookup(1, 0)
            + "  [erwartet: -1]");
        System.out.println("  lookup(pid=2, vpn=0) = " + tlb.lookup(2, 0)
            + "  [erwartet: 9 (nicht geflusht)]");
        System.out.println();
    }

    static void testEAT() {
        System.out.println("-- Effective Access Time (EAT) --\n");
        // Folienwerte: t_hit=1ns, t_miss=301ns (2-level walk + data)
        double tHit  = 1.0;
        double tMiss = 301.0;

        // Simuliere verschiedene Hit-Raten
        int[][] scenarios = {{99, 1}, {95, 5}, {80, 20}, {50, 50}};
        System.out.printf("  %-10s  %-10s  %s%n", "Hits", "Misses", "EAT (ns)");
        System.out.println("  " + "-".repeat(35));

        for (int[] s : scenarios) {
            TLB tlb = new TLB(64);
            // Hits und Misses manuell simulieren
            for (int i = 0; i < s[0]; i++) tlb.lookup(1, i);     // alle Miss (leer)
            // Fülle TLB für Hits
            TLB tlb2 = new TLB(64);
            for (int i = 0; i < 64; i++) tlb2.insert(1, i, i);
            for (int i = 0; i < s[0]; i++) tlb2.lookup(1, i);    // Hits
            for (int i = 100; i < 100 + s[1]; i++) tlb2.lookup(1, i); // Misses

            double eat = tlb2.effectiveAccessTime(tHit, tMiss);
            System.out.printf("  %-10d  %-10d  %.2f%n", s[0], s[1], eat);
        }
        System.out.println();
        System.out.println("  Folienwert (missrate=1%): EAT = 0.99*1 + 0.01*301 = 4.01 ns");
        System.out.println("  -> TLB ist entscheidend fuer die Performance!");
    }
}
