package woche12;

/**
 * Testprogramm für BlockBitmap (Woche 12, Aufgabe 2).
 *
 * Testet die drei Methoden allocate(), free() und isFree()
 * mit erwarteten Ausgaben zum direkten Vergleich.
 *
 * Führen Sie dieses Programm nach jeder implementierten Methode aus
 * und prüfen Sie ob die Ausgabe mit der erwarteten Ausgabe übereinstimmt.
 */
public class BlockBitmapTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {

        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║  BlockBitmap Test (Woche 12, Aufgabe 2)  ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println();

        testAllocate();
        testFree();
        testIsFree();
        testOutOfSpace();
        testFreeNotAllocated();
        testOutOfRange();

        System.out.println();
        System.out.println("══════════════════════════════════════════");
        System.out.printf("  Ergebnis: %d bestanden, %d fehlgeschlagen%n",
            passed, failed);
        System.out.println("══════════════════════════════════════════");
        if (failed == 0) {
            System.out.println("  ✓ Alle Tests bestanden — BlockBitmap korrekt!");
        } else {
            System.out.println("  ✗ Es gibt noch Fehler — Implementierung prüfen.");
        }
    }

    // =========================================================
    // Einzelne Testmethoden
    // =========================================================

    private static void testAllocate() {
        System.out.println("--- Test: allocate() ---");

        BlockBitmap bm = new BlockBitmap(8, "TestBitmap");

        // Erster Aufruf: Block 0
        test("Erster freier Block ist 0",
            () -> bm.allocate() == 0);

        // Zweiter Aufruf: Block 1
        test("Zweiter freier Block ist 1",
            () -> bm.allocate() == 1);

        // Dritter Aufruf: Block 2
        test("Dritter freier Block ist 2",
            () -> bm.allocate() == 2);

        // Nach 3 Allokationen: 5 Blöcke noch frei
        test("Nach 3 allocate(): freeCount() == 5",
            () -> bm.freeCount() == 5);

        System.out.println("  " + bm);
        System.out.println();
    }

    private static void testFree() {
        System.out.println("--- Test: free() ---");

        BlockBitmap bm = new BlockBitmap(8, "TestBitmap");

        try {
            // Block 0 allozieren, dann freigeben
            int b0 = bm.allocate();
            int b1 = bm.allocate();
            int b2 = bm.allocate();

            // Block 0 freigeben
            bm.free(b0);
            test("Nach free(0): Block 0 ist wieder frei",
                () -> bm.isFree(0));

            // freeCount nach free()
            test("Nach free(0): freeCount() == 6",
                () -> bm.freeCount() == 6);

            // Nächste Allokation sollte Block 0 zurückgeben (First-Fit)
            test("allocate() nach free(0) gibt Block 0 zurück",
                () -> bm.allocate() == 0);

            System.out.println("  " + bm);

        } catch (BlockBitmap.OutOfSpaceException e) {
            System.out.println("  FEHLER: OutOfSpaceException unerwartet: " + e.getMessage());
            failed++;
        }
        System.out.println();
    }

    private static void testIsFree() {
        System.out.println("--- Test: isFree() ---");

        BlockBitmap bm = new BlockBitmap(4, "TestBitmap");

        try {
            // Anfangs alle frei
            test("Block 0 anfangs frei", () -> bm.isFree(0));
            test("Block 3 anfangs frei", () -> bm.isFree(3));

            // Block 2 allozieren
            bm.allocate(); // Block 0
            bm.allocate(); // Block 1
            int b2 = bm.allocate(); // Block 2

            test("Block 2 nach allocate() belegt", () -> !bm.isFree(2));
            test("Block 3 noch frei",              () -> bm.isFree(3));
            test("Block 0 belegt",                 () -> !bm.isFree(0));

        } catch (BlockBitmap.OutOfSpaceException e) {
            System.out.println("  FEHLER: " + e.getMessage());
            failed++;
        }
        System.out.println();
    }

    private static void testOutOfSpace() {
        System.out.println("--- Test: OutOfSpaceException ---");

        BlockBitmap bm = new BlockBitmap(3, "KleineBitmap");

        try {
            bm.allocate(); // Block 0
            bm.allocate(); // Block 1
            bm.allocate(); // Block 2 — letzter Block

            test("freeCount() == 0 wenn voll", () -> bm.freeCount() == 0);

            // Jetzt sollte OutOfSpaceException kommen
            try {
                bm.allocate(); // muss Exception werfen!
                System.out.println("  FEHLER: allocate() hätte OutOfSpaceException werfen sollen");
                failed++;
            } catch (BlockBitmap.OutOfSpaceException e) {
                System.out.println("  ✓ OutOfSpaceException korrekt geworfen: " + e.getMessage());
                passed++;
            }

        } catch (BlockBitmap.OutOfSpaceException e) {
            System.out.println("  FEHLER: Zu früh OutOfSpaceException: " + e.getMessage());
            failed++;
        }
        System.out.println();
    }

    private static void testFreeNotAllocated() {
        System.out.println("--- Test: free() auf nicht-allokierten Block ---");

        BlockBitmap bm = new BlockBitmap(4, "TestBitmap");

        // Block 0 ist noch frei — free(0) sollte Exception werfen
        try {
            bm.free(0);
            System.out.println("  FEHLER: IllegalArgumentException hätte geworfen werden sollen");
            failed++;
        } catch (IllegalArgumentException e) {
            System.out.println("  ✓ IllegalArgumentException korrekt geworfen: " + e.getMessage());
            passed++;
        }
        System.out.println();
    }

    private static void testOutOfRange() {
        System.out.println("--- Test: Bereichsprüfung ---");

        BlockBitmap bm = new BlockBitmap(4, "TestBitmap");

        // Negative Nummer
        try {
            bm.isFree(-1);
            System.out.println("  FEHLER: IllegalArgumentException für -1 erwartet");
            failed++;
        } catch (IllegalArgumentException e) {
            System.out.println("  ✓ Negative Nummer korrekt abgefangen");
            passed++;
        }

        // Zu große Nummer
        try {
            bm.isFree(4); // Kapazität ist 4, gültig: 0-3
            System.out.println("  FEHLER: IllegalArgumentException für Index 4 erwartet");
            failed++;
        } catch (IllegalArgumentException e) {
            System.out.println("  ✓ Zu große Nummer korrekt abgefangen");
            passed++;
        }
        System.out.println();
    }

    // =========================================================
    // Hilfsmethode
    // =========================================================

    /**
     * Führt einen einzelnen Test aus und gibt das Ergebnis aus.
     *
     * @param description Beschreibung des Tests
     * @param condition   Lambda das true zurückgibt wenn der Test besteht
     */
    private static void test(String description, TestCondition condition) {
        try {
            if (condition.check()) {
                System.out.println("  ✓ " + description);
                passed++;
            } else {
                System.out.println("  ✗ FEHLER: " + description);
                failed++;
            }
        } catch (Exception e) {
            System.out.println("  ✗ FEHLER: " + description
                + " — Exception: " + e.getMessage());
            failed++;
        }
    }

    @FunctionalInterface
    interface TestCondition {
        boolean check() throws Exception;
    }
}
