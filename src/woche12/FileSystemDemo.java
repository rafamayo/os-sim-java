package woche12;

/**
 * Demonstration des Dateisystem-Simulators (Woche 12).
 *
 * Führt eine typische Sequenz von Dateisystem-Operationen durch
 * und zeigt den Zustand der Bitmaps nach jedem Schritt.
 *
 * Besonders lehrreich: Schritt 5 und 6 zeigen das Unix-Verhalten
 * beim Löschen einer geöffneten Datei:
 *   - delete() entfernt nur den Namen (Verzeichniseintrag)
 *   - Der Inode lebt weiter solange ein fd offen ist
 *   - Erst close() gibt den Inode wirklich frei
 */
public class FileSystemDemo {

    public static void main(String[] args) throws Exception {

        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║  Dateisystem-Simulator — Woche 12        ║");
        System.out.println("║  Inodes & Block Bitmap                   ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println();

        SimpleFileSystem fs = new SimpleFileSystem();

        // --- Schritt 1: Dateisystem-Info ---
        System.out.println("=== Initialer Zustand ===");
        fs.fsInfo();
        System.out.println();

        // --- Schritt 2: Dateien erstellen ---
        System.out.println("=== Dateien erstellen ===");
        int ino1 = fs.create("readme.txt");
        int ino2 = fs.create("daten.csv");
        int ino3 = fs.create("config.ini");
        System.out.println();

        // --- Schritt 3: Daten schreiben ---
        System.out.println("=== Daten schreiben ===");
        byte[] inhalt1 = "Hallo Welt! Dies ist der Inhalt von readme.txt".getBytes();
        fs.writeBlock(ino1, 0, inhalt1);

        byte[] inhalt2 = "Name,Alter,Stadt\nAlice,30,München\nBob,25,Berlin".getBytes();
        fs.writeBlock(ino2, 0, inhalt2);
        fs.writeBlock(ino2, 1, "Zweiter Block von daten.csv".getBytes());

        System.out.println();
        System.out.println("  Nach Schreiben:");
        fs.fsInfo();
        System.out.println();

        // --- Schritt 4: PCB und File Descriptors ---
        System.out.println("=== Prozess öffnet Dateien ===");
        ProcessControlBlock proc = new ProcessControlBlock(42, "TextEditor");
        proc.state = ProcessControlBlock.State.RUNNING;
        proc.setFileSystem(fs);   // PCB mit FS verknüpfen

        int fd1 = proc.open(ino1);
        int fd2 = proc.open(ino2);
        System.out.println("  " + proc);
        System.out.println();

        // --- Schritt 5: Datei löschen während Prozess sie geöffnet hat ---
        System.out.println("=== Datei löschen (Prozess hat sie noch offen) ===");
        System.out.println();
        System.out.println("  ┌─────────────────────────────────────────────────────────────┐");
        System.out.println("  │ BEOBACHTUNG (Woche 12):                                     │");
        System.out.println("  │ delete() entfernt nur den NAMEN aus dem Verzeichnis.        │");
        System.out.println("  │ Der INODE bleibt erhalten solange ein Prozess die Datei     │");
        System.out.println("  │ noch geöffnet hält (openCount > 0).                         │");
        System.out.println("  │ Erst close() gibt Inode und Blöcke wirklich frei.           │");
        System.out.println("  └─────────────────────────────────────────────────────────────┘");
        System.out.println();

        System.out.println("  Vor delete: Inode #" + ino1
            + " von Prozess geöffnet? " + proc.hasFileOpen(ino1));

        fs.delete("readme.txt");
        // Erwartete Ausgabe:
        //   linkCount=0 ABER openCount=1 → Inode bleibt erhalten

        System.out.println();
        System.out.println("  Nach delete('readme.txt'):");
        System.out.println("  → Name ist weg: lookup = " + fs.lookup("readme.txt"));
        System.out.println("  → Aber Prozess kann noch lesen (fd zeigt auf Inode, nicht auf Name):");

        byte[] gelesen = fs.readBlock(proc.getInodeForFd(fd1), 0);
        System.out.println("  → Gelesener Inhalt: \"" + new String(gelesen).trim() + "\"");
        System.out.println();

        // --- Schritt 6: File Descriptor schließen ---
        System.out.println("=== File Descriptors schließen ===");
        System.out.println("  (Erst jetzt wird Inode #" + ino1
            + " freigegeben: linkCount=0 AND openCount wird 0)");
        System.out.println();
        proc.close(fd1);   // → löst freeInodeResources aus
        proc.close(fd2);
        System.out.println();

        // --- Schritt 7: Weitere Datei löschen ---
        System.out.println("=== Weitere Datei löschen ===");
        fs.delete("config.ini");
        System.out.println();

        // --- Schritt 8: Finaler Zustand ---
        System.out.println("=== Finaler Zustand ===");
        fs.dump();

        // --- Schritt 9: Fehlerfall testen ---
        System.out.println();
        System.out.println("=== Fehlerfall: doppeltes Erstellen ===");
        try {
            fs.create("daten.csv");
        } catch (IllegalArgumentException e) {
            System.out.println("  Erwarteter Fehler: " + e.getMessage());
        }

        System.out.println();
        System.out.println("=== Fehlerfall: Löschen nicht existierender Datei ===");
        try {
            fs.delete("existiert_nicht.txt");
        } catch (IllegalArgumentException e) {
            System.out.println("  Erwarteter Fehler: " + e.getMessage());
        }
    }
}
