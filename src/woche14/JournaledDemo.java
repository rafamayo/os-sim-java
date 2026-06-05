package woche14;

/**
 * Demonstriert Journaling und Recovery (Woche 14, Aufgaben 3-6).
 */
class JournaledDemo {

    public static void main(String[] args) throws Exception {

        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║  Journaling & Recovery (Woche 14)                ║");
        System.out.println("╚══════════════════════════════════════════════════╝");

        // --- Demo 1: Normaler Betrieb mit Journal ---
        System.out.println("\n=== Demo 1: Normaler Betrieb ===\n");

        JournaledFileSystem jfs = new JournaledFileSystem();
        jfs.mkdir("/home");
        jfs.mkdir("/home/user");
        jfs.createFileJournaled("/home/user/readme.txt");
        jfs.createFileJournaled("/home/user/daten.csv");

        System.out.println();
        jfs.tree();
        System.out.println();
        jfs.journal.dump();

        // --- Demo 2: Absturz NACH TxE → Recovery stellt her ---
        System.out.println("\n=== Demo 2: Absturz nach TxE → Recovery ===\n");

        JournaledFileSystem jfs2 = new JournaledFileSystem();
        jfs2.mkdir("/docs");

        // Journal normal schreiben, aber Checkpoint simulieren als würde er scheitern
        // (Journal ist committed, Checkpoint fehlt)
        System.out.println("Simul.: Datei wird erstellt, Absturz NACH commit() aber VOR Checkpoint");

        // Manuell: Journal committen aber nicht auf FS anwenden
        int txId = jfs2.journal.beginTransaction();
        jfs2.journal.log(txId, "INIT_INODE",
            new Journal.InodeInit(2, Inode.MODE_FILE));
        jfs2.journal.log(txId, "ADD_DIR_ENTRY",
            new Journal.DirEntryData(1, "bericht.txt", 2));
        jfs2.journal.commit(txId);  // TxE geschrieben

        System.out.println("\nZustand VOR Recovery (FS noch unverändert):");
        jfs2.listDir("/docs");

        System.out.println("\nJournal-Replay:");
        jfs2.journal.replay(jfs2);

        System.out.println("\nZustand NACH Recovery:");
        jfs2.listDir("/docs");

        // --- Demo 3: Absturz VOR TxE → kein Recovery (sicher) ---
        System.out.println("\n=== Demo 3: Absturz vor TxE → kein Recovery ===\n");

        JournaledFileSystem jfs3 = new JournaledFileSystem();
        jfs3.mkdir("/tmp");

        // Journal mit Absturz vor TxE
        jfs3.journal.simulateCrashAfterEntry(3); // Absturz nach 3 Einträgen (vor TxE)

        System.out.println("Versuch: createFileJournaled mit simuliertem Absturz vor TxE");
        try {
            jfs3.createFileJournaled("/tmp/work.tmp");
        } catch (Journal.SimulatedCrashException e) {
            System.out.println("*** ABSTURZ *** " + e.getMessage());
        }

        System.out.println("\nJournal-Inhalt (kein TxE!):");
        jfs3.journal.dump();

        System.out.println("\nJournal-Replay:");
        jfs3.journal.simulateCrashAfterEntry(-1); // Crash-Simulation deaktivieren
        jfs3.journal.replay(jfs3);

        System.out.println("\nDateisystem nach Recovery (work.tmp wurde NICHT erstellt):");
        jfs3.listDir("/tmp");
        System.out.println("→ Korrekt: unvollständige Transaktion wird ignoriert ✓");
    }
}

