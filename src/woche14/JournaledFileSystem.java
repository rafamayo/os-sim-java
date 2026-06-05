package woche14;

import java.io.FileNotFoundException;

/**
 * Dateisystem mit Write-Ahead Journaling (Woche 14).
 *
 * Erweitert DirectoryFileSystem um:
 *   - Journal für alle Metadaten-Änderungen
 *   - Journaled createFile() und delete()
 *   - Recovery-Methode (applyOperation)
 */
public class JournaledFileSystem extends DirectoryFileSystem {

    public final Journal journal;

    public JournaledFileSystem() {
        super();
        this.journal = new Journal();
    }

    // =========================================================
    // Journaled Operationen
    // =========================================================

    /**
     * Erstellt eine Datei mit Journal-Schutz.
     *
     * Ablauf:
     *   1. beginTransaction()
     *   2. Alle Änderungen ins Journal schreiben
     *   3. commit() — TxE schreiben
     *   4. Checkpoint: eigentliche Strukturen aktualisieren
     *
     * @param path Absoluter Pfad der neuen Datei
     * @return Inode-Nummer
     */
    public int createFileJournaled(String path)
            throws FileNotFoundException, BlockBitmap.OutOfSpaceException {

        String parentPath = getParentPath(path);
        String baseName   = getBaseName(path);
        int parentIno     = resolve(parentPath);

        // Ressourcen vorab bestimmen (ohne FS zu ändern)
        int newIno = findFreeInode();

        System.out.println("[JFS] createFileJournaled(\"" + path + "\")");

        // --- Phase 1: Journal ---
        int txId = journal.beginTransaction();
        journal.log(txId, "SET_INODE_BITMAP",
            new Journal.InodeBitmapData(newIno, true));
        journal.log(txId, "INIT_INODE",
            new Journal.InodeInit(newIno, Inode.MODE_FILE));
        journal.log(txId, "ADD_DIR_ENTRY",
            new Journal.DirEntryData(parentIno, baseName, newIno));
        journal.commit(txId);

        // --- Phase 2: Checkpoint (eigentliche Strukturen) ---
        System.out.println("[JFS] Checkpoint: Strukturen aktualisieren");
        applyOperation("SET_INODE_BITMAP", new Journal.InodeBitmapData(newIno, true));
        applyOperation("INIT_INODE",       new Journal.InodeInit(newIno, Inode.MODE_FILE));
        applyOperation("ADD_DIR_ENTRY",
            new Journal.DirEntryData(parentIno, baseName, newIno));

        return newIno;
    }

    /**
     * Löscht eine Datei mit Journal-Schutz.
     */
    public void deleteJournaled(String path)
            throws FileNotFoundException {

        String parentPath = getParentPath(path);
        String name       = getBaseName(path);
        int parentIno     = resolve(parentPath);
        int inoNum        = getDirectory(parentIno).lookup(name);
        if (inoNum == -1) {
            throw new FileNotFoundException("Nicht gefunden: " + path);
        }

        System.out.println("[JFS] deleteJournaled(\"" + path + "\")");

        int txId = journal.beginTransaction();
        journal.log(txId, "REMOVE_DIR_ENTRY",
            new Journal.DirEntryData(parentIno, name, inoNum));
        journal.log(txId, "DECREMENT_LINK_COUNT",
            new Journal.InodeBitmapData(inoNum, false));

        if (inodeTable[inoNum].linkCount - 1 == 0) {
            for (int i = 0; i < Inode.DIRECT_BLOCKS; i++) {
                if (inodeTable[inoNum].direct[i] != -1) {
                    journal.log(txId, "FREE_BLOCK",
                        new Journal.BlockBitmapData(inodeTable[inoNum].direct[i], false));
                }
            }
            journal.log(txId, "FREE_INODE",
                new Journal.InodeBitmapData(inoNum, false));
        }

        journal.commit(txId);

        // Checkpoint
        applyOperation("REMOVE_DIR_ENTRY",
            new Journal.DirEntryData(parentIno, name, inoNum));
        applyOperation("DECREMENT_LINK_COUNT",
            new Journal.InodeBitmapData(inoNum, false));
        if (inodeTable[inoNum].linkCount == 0) {
            for (int i = 0; i < Inode.DIRECT_BLOCKS; i++) {
                if (inodeTable[inoNum].direct[i] != -1) {
                    applyOperation("FREE_BLOCK",
                        new Journal.BlockBitmapData(inodeTable[inoNum].direct[i], false));
                }
            }
            applyOperation("FREE_INODE",
                new Journal.InodeBitmapData(inoNum, false));
        }
    }

    // =========================================================
    // Recovery: Journal-Einträge auf FS anwenden
    // =========================================================

    /**
     * Wendet einen Journal-Eintrag auf das Dateisystem an.
     * Wird von Journal.replay() aufgerufen.
     *
     * MUSS idempotent sein: mehrfaches Anwenden liefert dasselbe Ergebnis.
     */
    public void applyOperation(String operation, Object data) {
        try {
            switch (operation) {
                case "SET_INODE_BITMAP" -> {
                    Journal.InodeBitmapData d = (Journal.InodeBitmapData) data;
                    if (d.value() && inodeBitmap.isFree(d.inodeNum())) {
                        inodeBitmap.allocate(); // belegt nächsten freien — vereinfacht
                    }
                    // idempotent: nochmaliges Setzen ändert nichts
                }
                case "INIT_INODE" -> {
                    Journal.InodeInit d = (Journal.InodeInit) data;
                    inodeTable[d.inodeNum()].init(d.mode());
                }
                case "ADD_DIR_ENTRY" -> {
                    Journal.DirEntryData d = (Journal.DirEntryData) data;
                    Directory dir = getDirectory(d.parentIno());
                    if (dir.lookup(d.name()) == -1) { // idempotent: nur hinzufügen wenn fehlt
                        dir.addEntry(d.name(), d.inodeNum());
                    }
                }
                case "REMOVE_DIR_ENTRY" -> {
                    Journal.DirEntryData d = (Journal.DirEntryData) data;
                    Directory dir = getDirectory(d.parentIno());
                    if (dir.lookup(d.name()) != -1) { // idempotent
                        dir.removeEntry(d.name());
                    }
                }
                case "DECREMENT_LINK_COUNT" -> {
                    Journal.InodeBitmapData d = (Journal.InodeBitmapData) data;
                    if (inodeTable[d.inodeNum()].linkCount > 0) {
                        inodeTable[d.inodeNum()].linkCount--;
                    }
                }
                case "FREE_BLOCK" -> {
                    Journal.BlockBitmapData d = (Journal.BlockBitmapData) data;
                    if (!blockBitmap.isFree(d.blockNum())) {
                        blockBitmap.free(d.blockNum());
                    }
                }
                case "FREE_INODE" -> {
                    Journal.InodeBitmapData d = (Journal.InodeBitmapData) data;
                    if (!inodeBitmap.isFree(d.inodeNum())) {
                        inodeBitmap.free(d.inodeNum());
                    }
                }
                default -> System.out.println("[JFS] Unbekannte Operation: " + operation);
            }
        } catch (Exception e) {
            System.out.println("[JFS] applyOperation Fehler: " + e.getMessage());
        }
    }

    // =========================================================
    // Hilfsmethoden
    // =========================================================

    /** Findet die nächste freie Inode-Nummer ohne sie zu allozieren */
    private int findFreeInode() {
        for (int i = 0; i < MAX_INODES; i++) {
            if (inodeBitmap.isFree(i)) return i;
        }
        throw new RuntimeException("Keine freien Inodes");
    }
}
