package woche13;


/**
 * Demonstration des DirectoryFileSystem-Simulators (Woche 13).
 *
 * Führt die Übungsaufgaben schrittweise durch — jede Methode
 * kann direkt nach ihrer Implementierung getestet werden.
 *
 * Erwartete Verzeichnisstruktur am Ende von Aufgabe 3:
 *
 *   /                        [Inode #0]
 *   ├── home/                [Inode #1]
 *   │   └── user/            [Inode #2]
 *   │       ├── readme.txt   [Inode #4]
 *   │       └── daten.csv    [Inode #5]
 *   └── tmp/                 [Inode #3]
 *       └── work.tmp         [Inode #6]
 */
public class DirectoryDemo {

    public static void main(String[] args) throws Exception {

        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║  Dateisystem-Simulator — Woche 13               ║");
        System.out.println("║  Verzeichnisse, Links & Pfadauflösung            ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();

        // ─────────────────────────────────────────────────
        // Aufgabe 2: resolve() testen (erst resolve, dann alles andere)
        // ─────────────────────────────────────────────────
        System.out.println("══════════════════════════════════════════");
        System.out.println("  Aufgabe 2 — resolve()");
        System.out.println("══════════════════════════════════════════\n");
        testResolve();

        // ─────────────────────────────────────────────────
        // Aufgabe 3: mkdir() testen
        // ─────────────────────────────────────────────────
        System.out.println("══════════════════════════════════════════");
        System.out.println("  Aufgabe 3 — mkdir()");
        System.out.println("══════════════════════════════════════════\n");
        DirectoryFileSystem fs = testMkdir();

        // ─────────────────────────────────────────────────
        // Aufgabe 4: hardLink() testen
        // ─────────────────────────────────────────────────
        System.out.println("══════════════════════════════════════════");
        System.out.println("  Aufgabe 4 — hardLink()");
        System.out.println("══════════════════════════════════════════\n");
        testHardLink(fs);

        // ─────────────────────────────────────────────────
        // Aufgabe 5: rename() testen
        // ─────────────────────────────────────────────────
        System.out.println("══════════════════════════════════════════");
        System.out.println("  Aufgabe 5 — rename()");
        System.out.println("══════════════════════════════════════════\n");
        testRename(fs);

        // ─────────────────────────────────────────────────
        // Aufgabe 6: Integration
        // ─────────────────────────────────────────────────
        System.out.println("══════════════════════════════════════════");
        System.out.println("  Aufgabe 6 — Integrations-Test");
        System.out.println("══════════════════════════════════════════\n");
        testIntegration(fs);
    }

    // =========================================================
    // Aufgabe 2 — resolve()
    // =========================================================

    static void testResolve() throws Exception {
        System.out.println("Einfaches Dateisystem für resolve()-Test:\n");

        DirectoryFileSystem fs = new DirectoryFileSystem();

        // Nur Wurzelverzeichnis -- resolve("/") muss funktionieren
        System.out.println("Test 1: resolve(\"/\")");
        int rootIno = fs.resolve("/");
        System.out.printf("  → Inode #%d  (erwartet: %d) %s%n",
            rootIno, SimpleFileSystem.ROOT_INODE,
            rootIno == SimpleFileSystem.ROOT_INODE ? "✓" : "✗ FEHLER");
        System.out.println();

        // Ein Unterverzeichnis anlegen (intern, ohne mkdir())
        // damit resolve() getestet werden kann bevor mkdir() implementiert ist
        System.out.println("Test 2: resolve(\"/home\") nach manuellem mkdir");
        fs.mkdir("/home");   // resolve() muss bereits funktionieren!
        int homeIno = fs.resolve("/home");
        System.out.printf("  → Inode #%d  %s%n", homeIno,
            homeIno != -1 ? "✓" : "✗ FEHLER");
        System.out.println();

        // Verschachtelter Pfad
        fs.mkdir("/home/user");
        System.out.println("Test 3: resolve(\"/home/user\")");
        int userIno = fs.resolve("/home/user");
        System.out.printf("  → Inode #%d  %s%n", userIno,
            userIno != -1 ? "✓" : "✗ FEHLER");
        System.out.println();

        // Nicht existierender Pfad
        System.out.println("Test 4: resolve(\"/nicht/vorhanden\") → FileNotFoundException");
        try {
            fs.resolve("/nicht/vorhanden");
            System.out.println("  ✗ FEHLER: Exception hätte geworfen werden sollen");
        } catch (java.io.FileNotFoundException e) {
            System.out.println("  ✓ FileNotFoundException korrekt: " + e.getMessage());
        }
        System.out.println();
    }

    // =========================================================
    // Aufgabe 3 — mkdir()
    // =========================================================

    static DirectoryFileSystem testMkdir() throws Exception {
        DirectoryFileSystem fs = new DirectoryFileSystem();

        System.out.println("Verzeichnisstruktur aufbauen:\n");

        // Erste Ebene
        int homeIno = fs.mkdir("/home");
        int tmpIno  = fs.mkdir("/tmp");
        System.out.printf("  mkdir(\"/home\") → Inode #%d%n", homeIno);
        System.out.printf("  mkdir(\"/tmp\")  → Inode #%d%n", tmpIno);

        // Zweite Ebene
        int userIno = fs.mkdir("/home/user");
        System.out.printf("  mkdir(\"/home/user\") → Inode #%d%n", userIno);
        System.out.println();

        // Dateien anlegen
        int readme = fs.createFile("/home/user/readme.txt");
        int daten  = fs.createFile("/home/user/daten.csv");
        int work   = fs.createFile("/tmp/work.tmp");
        System.out.printf("  createFile(\"/home/user/readme.txt\") → Inode #%d%n", readme);
        System.out.printf("  createFile(\"/home/user/daten.csv\")  → Inode #%d%n", daten);
        System.out.printf("  createFile(\"/tmp/work.tmp\")         → Inode #%d%n", work);
        System.out.println();

        // Baum ausgeben
        fs.tree();
        System.out.println();

        // Verzeichnisinhalt prüfen
        System.out.println("Inhalt von /home/user:");
        fs.listDir("/home/user");
        System.out.println();

        // linkCount prüfen: /home sollte linkCount=3 haben
        // (/: "home"-Eintrag) + (".": home selbst) + (".." in user)
        System.out.println("linkCount von /home:");
        int ln = fs.resolve("/home");
        System.out.printf("  Inode #%d, linkCount=%d  " +
            "(erwartet ≥ 3: '.' + '..' in /home/user + Eintrag in '/')%n",
            ln, fs.inodeTable[ln].linkCount);
        System.out.println();

        return fs;
    }

    // =========================================================
    // Aufgabe 4 — hardLink()
    // =========================================================

    static void testHardLink(DirectoryFileSystem fs) throws Exception {

        System.out.println("Hard Link anlegen:\n");

        int inoBefore = fs.resolve("/home/user/daten.csv");
        System.out.printf("  daten.csv vor hardLink: Inode #%d, linkCount=%d%n",
            inoBefore, fs.inodeTable[inoBefore].linkCount);

        fs.hardLink("/home/user/daten.csv", "/home/user/backup.csv");

        int inoAfter = fs.resolve("/home/user/backup.csv");
        System.out.printf("  backup.csv nach hardLink: Inode #%d, linkCount=%d%n",
            inoAfter, fs.inodeTable[inoAfter].linkCount);

        System.out.printf("  Gleicher Inode? %s%n",
            inoBefore == inoAfter ? "✓ Ja" : "✗ Nein (FEHLER)");
        System.out.println();

        // Originaldatei löschen — backup.csv muss noch existieren
        System.out.println("  Original löschen — Hard Link bleibt erhalten:");
        fs.deletePath("/home/user/daten.csv");
        try {
            int backupIno = fs.resolve("/home/user/backup.csv");
            System.out.printf("  backup.csv noch erreichbar: Inode #%d, linkCount=%d  ✓%n",
                backupIno, fs.inodeTable[backupIno].linkCount);
        } catch (java.io.FileNotFoundException e) {
            System.out.println("  ✗ FEHLER: backup.csv nicht mehr erreichbar");
        }
        System.out.println();

        // Hard Link auf Verzeichnis — muss verboten sein
        System.out.println("  Hard Link auf Verzeichnis (muss Exception werfen):");
        try {
            fs.hardLink("/home/user", "/home/user_link");
            System.out.println("  ✗ FEHLER: Exception hätte geworfen werden sollen");
        } catch (IllegalArgumentException e) {
            System.out.println("  ✓ Korrekt verhindert: " + e.getMessage());
        }
        System.out.println();
    }

    // =========================================================
    // Aufgabe 5 — rename()
    // =========================================================

    static void testRename(DirectoryFileSystem fs) throws Exception {

        System.out.println("rename() — Datei umbenennen und verschieben:\n");

        // Umbenennen innerhalb desselben Verzeichnisses
        int inoBefore = fs.resolve("/tmp/work.tmp");
        System.out.printf("  Vor rename: /tmp/work.tmp → Inode #%d%n", inoBefore);

        fs.rename("/tmp/work.tmp", "/home/user/work.txt");

        System.out.println("  Nach rename(\"/tmp/work.tmp\", \"/home/user/work.txt\"):");

        // Alter Pfad muss weg sein
        try {
            fs.resolve("/tmp/work.tmp");
            System.out.println("  ✗ FEHLER: /tmp/work.tmp sollte nicht mehr existieren");
        } catch (java.io.FileNotFoundException e) {
            System.out.println("  ✓ /tmp/work.tmp nicht mehr erreichbar");
        }

        // Neuer Pfad muss denselben Inode haben
        try {
            int inoAfter = fs.resolve("/home/user/work.txt");
            System.out.printf("  ✓ /home/user/work.txt → Inode #%d  %s%n",
                inoAfter, inoBefore == inoAfter ? "(gleicher Inode ✓)" : "(ANDERER Inode ✗)");
        } catch (java.io.FileNotFoundException e) {
            System.out.println("  ✗ FEHLER: /home/user/work.txt nicht gefunden");
        }
        System.out.println();

        // Wichtige Eigenschaft: linkCount bleibt gleich
        int inoAfter = fs.resolve("/home/user/work.txt");
        System.out.printf("  linkCount vor und nach rename: %d → %d  %s%n",
            fs.inodeTable[inoBefore].linkCount,
            fs.inodeTable[inoAfter].linkCount,
            fs.inodeTable[inoBefore].linkCount == fs.inodeTable[inoAfter].linkCount
                ? "✓ (unverändert)" : "✗ FEHLER");
        System.out.println();

        // Baum nach rename()
        System.out.println("Dateisystem nach rename():");
        fs.tree();
        System.out.println();
    }

    // =========================================================
    // Aufgabe 6 — Integration
    // =========================================================

    static void testIntegration(DirectoryFileSystem fs) throws Exception {

        System.out.println("Gesamtübersicht und Abschlusstest:\n");

        // Finaler Baum
        fs.tree();
        System.out.println();

        // Dateisystem-Info
        fs.fsInfo();
        System.out.println();

        // Fehlerfall: Nicht existierender Pfad
        System.out.println("Fehlerfall: resolve() auf nicht existierten Pfad:");
        try {
            fs.resolve("/home/user/unbekannt.txt");
        } catch (java.io.FileNotFoundException e) {
            System.out.println("  ✓ " + e.getMessage());
        }

        // Fehlerfall: mkdir() für existierendes Verzeichnis
        System.out.println("\nFehlerfall: mkdir() für existierendes Verzeichnis:");
        try {
            fs.mkdir("/home/user");
        } catch (Exception e) {
            System.out.println("  ✓ " + e.getMessage());
        }

        System.out.println();
        System.out.println("Denkanstöße nach dem Testen:");
        System.out.println("  1. Was zeigt listDir(\"/home/user\") für die Einträge '.' und '..'?");
        System.out.println("     Welche Inode-Nummern haben sie?");
        System.out.println("  2. Warum ändert rename() den linkCount nicht?");
        System.out.println("  3. Was passiert mit dem Inode wenn Sie backup.csv auch löschen?");
        System.out.println("     Führen Sie deletePath(\"/home/user/backup.csv\") aus");
        System.out.println("     und prüfen Sie die Bitmap.");
    }
}
