package woche14;


/**
 * Demonstriert das Crash-Konsistenzproblem OHNE Journal (Woche 14, Aufgabe 1).
 *
 * create("wichtig.txt") besteht intern aus drei Schritten:
 *   1. Inode Bitmap setzen       (inodeBitmap.allocate())
 *   2. Inode initialisieren      (inodeTable[i].init())
 *   3. Verzeichniseintrag anlegen (rootDir.put(name, inoNum))
 *
 * Das Betriebssystem kann diese drei Schritte nicht atomar ausführen.
 * Zwischen jedem Schritt kann ein Absturz eintreten.
 * Diese Demo führt jeden Schritt einzeln aus und zeigt den
 * inkonsistenten Zustand nach dem "Absturz".
 */
public class CrashSimulator {

    public static void main(String[] args) throws Exception {

        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║  Crash-Konsistenz ohne Journal (Woche 14)        ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("create(\"wichtig.txt\") besteht aus 3 Schritten:");
        System.out.println("  1. Inode Bitmap setzen");
        System.out.println("  2. Inode initialisieren");
        System.out.println("  3. Verzeichniseintrag anlegen");
        System.out.println();
        System.out.println("Zwischen jedem Schritt kann ein Absturz eintreten.");
        System.out.println("════════════════════════════════════════════════════\n");

        szenario1();
        szenario2();
        szenario3();

        System.out.println("════════════════════════════════════════════════════");
        System.out.println("=== Fazit ===");
        System.out.println("  Szenario 1: Speicherleck — Inode belegt, aber unerreichbar");
        System.out.println("  Szenario 2: Speicherleck — Inode+Daten da, aber kein Name");
        System.out.println("  Szenario 3: Konsistent ✓");
        System.out.println();
        System.out.println("Lösung: Write-Ahead Log (Journaling) — siehe JournaledDemo");
    }

    // =========================================================
    // Szenario 1: Absturz nach Schritt 1
    // =========================================================

    static void szenario1() throws Exception {

        System.out.println("=== Szenario 1: Absturz nach Schritt 1 ===");
        System.out.println("(Inode Bitmap gesetzt, Inode und Verzeichnis fehlen)\n");

        CrashableFileSystem fs = new CrashableFileSystem();
        System.out.println("Zustand VOR create():");
        fs.fsInfo();

        try {
            System.out.println("Starte create(\"wichtig.txt\"):");
            int inoNum = fs.step1_allocateInode();   // Schritt 1 ausgeführt
            // *** ABSTURZ *** Schritte 2 und 3 nie erreicht
            throw new SimulatedCrash("Stromausfall nach Schritt 1!");
            // fs.step2_initInode(inoNum);           // wird nie ausgeführt
            // fs.step3_addDirEntry("wichtig.txt", inoNum); // wird nie ausgeführt

        } catch (SimulatedCrash e) {
            System.out.println("\n*** " + e.getMessage() + " ***\n");
        }

        System.out.println("Zustand NACH Absturz:");
        fs.fsInfo();
        System.out.printf("  lookup(\"wichtig.txt\") = %d  (erwartet: -1)%n",
            fs.lookup("wichtig.txt"));
        System.out.println();
        System.out.println("  Problem: Inode Bitmap sagt 'belegt',");
        System.out.println("           aber kein Verzeichniseintrag → Datei nicht auffindbar.");
        System.out.println("  fsck würde diesen Inode als 'lost' einstufen");
        System.out.println("  und in lost+found/ verschieben.\n");
    }

    // =========================================================
    // Szenario 2: Absturz nach Schritt 2
    // =========================================================

    static void szenario2() throws Exception {

        System.out.println("=== Szenario 2: Absturz nach Schritt 2 ===");
        System.out.println("(Inode Bitmap + Inode initialisiert, Verzeichnis fehlt)\n");

        CrashableFileSystem fs = new CrashableFileSystem();
        System.out.println("Zustand VOR create():");
        fs.fsInfo();

        try {
            System.out.println("Starte create(\"wichtig.txt\"):");
            int inoNum = fs.step1_allocateInode();   // Schritt 1 ✓
            fs.step2_initInode(inoNum);               // Schritt 2 ✓
            // *** ABSTURZ *** Schritt 3 nie erreicht
            throw new SimulatedCrash("Stromausfall nach Schritt 2!");
            // fs.step3_addDirEntry("wichtig.txt", inoNum); // wird nie ausgeführt

        } catch (SimulatedCrash e) {
            System.out.println("\n*** " + e.getMessage() + " ***\n");
        }

        System.out.println("Zustand NACH Absturz:");
        fs.fsInfo();
        System.out.printf("  lookup(\"wichtig.txt\") = %d  (erwartet: -1)%n",
            fs.lookup("wichtig.txt"));
        System.out.println();
        System.out.println("  Problem: Inode existiert und ist initialisiert,");
        System.out.println("           aber kein Verzeichniseintrag → Datei trotzdem unerreichbar.");
        System.out.println("  Mehr Speicher verloren als in Szenario 1");
        System.out.println("  (Inode-Slot + potenziell bereits allozierte Datenblöcke).\n");
    }

    // =========================================================
    // Szenario 3: Kein Absturz
    // =========================================================

    static void szenario3() throws Exception {

        System.out.println("=== Szenario 3: Kein Absturz (alle 3 Schritte) ===\n");

        CrashableFileSystem fs = new CrashableFileSystem();
        System.out.println("Zustand VOR create():");
        fs.fsInfo();

        System.out.println("Starte create(\"wichtig.txt\"):");
        int inoNum = fs.step1_allocateInode();        // Schritt 1 ✓
        fs.step2_initInode(inoNum);                   // Schritt 2 ✓
        fs.step3_addDirEntry("wichtig.txt", inoNum);  // Schritt 3 ✓

        System.out.println();
        System.out.println("Zustand NACH create():");
        fs.fsInfo();
        System.out.printf("  lookup(\"wichtig.txt\") = %d  ✓%n",
            fs.lookup("wichtig.txt"));
        System.out.println("  Konsistenter Zustand: alle drei Strukturen übereinstimmend.\n");
    }

    // =========================================================
    // Hilfsklasse
    // =========================================================

    static class SimulatedCrash extends RuntimeException {
        public SimulatedCrash(String msg) { super(msg); }
    }
}
