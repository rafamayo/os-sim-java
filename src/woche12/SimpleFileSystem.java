package woche12;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Einfacher Dateisystem-Simulator (Woche 12).
 *
 * Disk-Layout:
 *   Block 0:     Superblock (hier nur als Konstanten)
 *   Block 1:     Inode Bitmap
 *   Block 2:     Block Bitmap
 *   Blöcke 3-18: Inode Tabelle (16 Inodes, je 1 Block)
 *   Blöcke 19+:  Datenblöcke
 *
 * Vereinfachungen für Woche 12:
 *   - Flache Verzeichnisstruktur (Map statt Directory-Objekt)
 *   - Nur direkte Blockzeiger
 *   - Kein Journaling
 *   - Kein echter Disk-Puffer
 */
public class SimpleFileSystem {

    // --- Konstanten ---
    public static final int BLOCK_SIZE = 512;
    public static final int MAX_INODES = 16;
    public static final int MAX_BLOCKS = 64;
    public static final int ROOT_INODE = 0;

    // --- Interne Strukturen ---
    private final Inode[]     inodeTable;
    private final byte[][]    dataBlocks;
    private final BlockBitmap inodeBitmap;
    private final BlockBitmap blockBitmap;

    private final Map<String, Integer> rootDir = new HashMap<>();

    /**
     * Zählt wie viele Prozesse einen Inode aktuell geöffnet haben.
     *
     * Ein Inode darf erst freigegeben werden wenn:
     *   linkCount == 0  (kein Verzeichniseintrag mehr)
     *   UND openFileCount[i] == 0  (kein Prozess hat ihn offen)
     */
    private final int[] openFileCount;

    // Statistik
    private int totalCreates = 0;
    private int totalDeletes = 0;
    private int totalWrites  = 0;

    public SimpleFileSystem() {
        this.inodeTable    = new Inode[MAX_INODES];
        this.dataBlocks    = new byte[MAX_BLOCKS][BLOCK_SIZE];
        this.inodeBitmap   = new BlockBitmap(MAX_INODES, "Inode Bitmap");
        this.blockBitmap   = new BlockBitmap(MAX_BLOCKS, "Block Bitmap");
        this.openFileCount = new int[MAX_INODES];

        for (int i = 0; i < MAX_INODES; i++) {
            inodeTable[i] = new Inode();
        }
        try {
            int rootIno = inodeBitmap.allocate();
            inodeTable[rootIno].init(Inode.MODE_DIRECTORY);
        } catch (BlockBitmap.OutOfSpaceException e) {
            throw new RuntimeException("Fehler beim Initialisieren", e);
        }
    }

    // =========================================================
    // Aufgabe 3 — create()
    // =========================================================

    /**
     * Erstellt eine neue leere Datei.
     *
     * TODO (Aufgabe 3):
     *   Schritt 1: Prüfen ob der Name bereits existiert.
     *              rootDir.containsKey(name) → IllegalArgumentException
     *
     *   Schritt 2: Freien Inode allozieren:
     *              int inoNum = inodeBitmap.allocate();
     *
     *   Schritt 3: Inode initialisieren:
     *              inodeTable[inoNum].init(Inode.MODE_FILE);
     *
     *   Schritt 4: Verzeichniseintrag anlegen:
     *              rootDir.put(name, inoNum);
     *
     *   Schritt 5: totalCreates++ und Meldung ausgeben:
     *              System.out.printf("[FS] create(\"%s\") -> Inode #%d%n", name, inoNum);
     *
     *   Schritt 6: inoNum zurückgeben
     */
    public int create(String name) throws BlockBitmap.OutOfSpaceException {
        // TODO: Implementieren
        throw new UnsupportedOperationException("create() noch nicht implementiert");
    }

    // =========================================================
    // Aufgabe 4 — writeBlock()
    // =========================================================

    /**
     * Schreibt Daten in einen Block einer Datei.
     *
     * TODO (Aufgabe 4): Nur den markierten Abschnitt implementieren.
     *   Falls inode.direct[blockIndex] == -1 (Block noch nicht alloziert):
     *     a) int newBlock = blockBitmap.allocate();
     *     b) inode.direct[blockIndex] = newBlock;
     *     c) Meldung: "[FS] writeBlock: Neuer Block #X für Inode #Y"
     *
     *   Alles andere ist bereits implementiert.
     */
    public void writeBlock(int inodeNum, int blockIndex, byte[] data)
            throws BlockBitmap.OutOfSpaceException {

        Inode inode = getInode(inodeNum);

        if (blockIndex >= Inode.DIRECT_BLOCKS) {
            throw new IllegalArgumentException("Nur direkte Zeiger unterstützt");
        }

        // TODO (Aufgabe 4): Block allozieren wenn noch nicht belegt
        // if (inode.direct[blockIndex] == -1) { ... }


        // --- Ab hier bereits implementiert: nicht ändern ---
        int len = Math.min(data.length, BLOCK_SIZE);
        if (inode.direct[blockIndex] != -1) {
            System.arraycopy(data, 0,
                dataBlocks[inode.direct[blockIndex]], 0, len);
            inode.size  = Math.max(inode.size,
                (long) blockIndex * BLOCK_SIZE + len);
            inode.mtime = System.currentTimeMillis();
            totalWrites++;
        }
    }

    // =========================================================
    // Aufgabe 5 — delete()
    // =========================================================

    /**
     * Löscht eine Datei.
     *
     * TODO (Aufgabe 5):
     *   Schritt 1: Inode-Nummer nachschlagen:
     *              Integer inoNum = rootDir.get(name);
     *              Falls null: IllegalArgumentException("Datei nicht gefunden: " + name)
     *
     *   Schritt 2: Inode laden:
     *              Inode inode = inodeTable[inoNum];
     *
     *   Schritt 3: Verzeichniseintrag entfernen:
     *              rootDir.remove(name);
     *
     *   Schritt 4: linkCount dekrementieren:
     *              inode.linkCount--;
     *              Meldung: "[FS] delete(...): Inode #X, linkCount=Y, openCount=Z"
     *
     *   Schritt 5: Ressourcen freigeben — NUR wenn BEIDE zutreffen:
     *              inode.linkCount == 0  UND  openFileCount[inoNum] == 0
     *              → freeInodeResources(inoNum, inode)
     *
     *              Falls linkCount==0 aber openCount>0:
     *              Meldung: "Freigabe verzögert bis close()"
     *              (Der Prozess hält die Datei noch offen!)
     *
     *   Schritt 6: totalDeletes++
     */
    public void delete(String name) {
        // TODO: Implementieren
        throw new UnsupportedOperationException("delete() noch nicht implementiert");
    }

    // =========================================================
    // Bereits implementiert — nicht ändern
    // =========================================================

    /** Liest Daten aus einem Block einer Datei. */
    public byte[] readBlock(int inodeNum, int blockIndex) {
        Inode inode = getInode(inodeNum);
        if (blockIndex >= Inode.DIRECT_BLOCKS) {
            throw new IllegalArgumentException("Nur direkte Zeiger unterstützt");
        }
        if (inode.direct[blockIndex] == -1) {
            return new byte[0];
        }
        inode.atime = System.currentTimeMillis();
        return Arrays.copyOf(dataBlocks[inode.direct[blockIndex]], BLOCK_SIZE);
    }

    /**
     * Wird vom PCB aufgerufen wenn ein File Descriptor geöffnet wird.
     * Erhöht den Zähler offener fds für diesen Inode.
     * (Bereits implementiert — Konzept in der Demo beobachten)
     */
    public void notifyOpen(int inodeNum) {
        checkRange(inodeNum);
        openFileCount[inodeNum]++;
        System.out.printf("[FS] notifyOpen:  Inode #%d, openCount=%d%n",
            inodeNum, openFileCount[inodeNum]);
    }

    /**
     * Wird vom PCB aufgerufen wenn ein File Descriptor geschlossen wird.
     * Gibt Ressourcen frei wenn linkCount==0 und openCount==0.
     * (Bereits implementiert — Konzept in der Demo beobachten)
     */
    public void notifyClose(int inodeNum) {
        checkRange(inodeNum);
        if (openFileCount[inodeNum] > 0) {
            openFileCount[inodeNum]--;
        }
        System.out.printf("[FS] notifyClose: Inode #%d, openCount=%d%n",
            inodeNum, openFileCount[inodeNum]);

        Inode inode = inodeTable[inodeNum];
        if (inode.linkCount == 0 && openFileCount[inodeNum] == 0) {
            System.out.printf(
                "[FS] Inode #%d: letzter fd geschlossen + linkCount=0" +
                " -> jetzt freigeben%n", inodeNum);
            freeInodeResources(inodeNum, inode);
        }
    }

    public int lookup(String name) {
        return rootDir.getOrDefault(name, -1);
    }

    public void fsInfo() {
        System.out.println("=== Superblock / Dateisystem-Info ===");
        System.out.printf("  Blockgröße:       %d Bytes%n", BLOCK_SIZE);
        System.out.printf("  Max. Inodes:      %d%n", MAX_INODES);
        System.out.printf("  Max. Datenblöcke: %d%n", MAX_BLOCKS);
        System.out.printf("  Freie Inodes:     %d%n", inodeBitmap.freeCount());
        System.out.printf("  Freie Blöcke:     %d%n", blockBitmap.freeCount());
        System.out.printf("  Dateien:          %d%n", rootDir.size());
        System.out.println();
        System.out.println("  " + inodeBitmap);
        System.out.println("  " + blockBitmap);
    }

    public void dump() {
        System.out.println("=== Dateisystem-Dump ===");
        fsInfo();
        System.out.println("  Verzeichnis (Root):");
        if (rootDir.isEmpty()) {
            System.out.println("    (leer)");
        } else {
            for (Map.Entry<String, Integer> e : rootDir.entrySet()) {
                Inode ino = inodeTable[e.getValue()];
                System.out.printf("    %-20s -> Inode #%d  (%s, %d Bytes)%n",
                    e.getKey(), e.getValue(),
                    ino.mode == Inode.MODE_DIRECTORY ? "DIR" : "FILE",
                    ino.size);
            }
        }
        System.out.printf("%n  Operationen: %d creates, %d deletes, %d writes%n",
            totalCreates, totalDeletes, totalWrites);
    }

    // =========================================================
    // Interne Hilfsmethoden — nicht ändern
    // =========================================================

    private Inode getInode(int inoNum) {
        checkRange(inoNum);
        Inode inode = inodeTable[inoNum];
        if (!inode.isAllocated()) {
            throw new IllegalArgumentException(
                "Inode #" + inoNum + " ist nicht allokiert");
        }
        return inode;
    }

    private void checkRange(int inoNum) {
        if (inoNum < 0 || inoNum >= MAX_INODES) {
            throw new IllegalArgumentException(
                "Ungültige Inode-Nummer: " + inoNum);
        }
    }

    private void freeInodeResources(int inoNum, Inode inode) {
        System.out.printf("[FS] Inode #%d: gebe Ressourcen frei%n", inoNum);
        for (int i = 0; i < Inode.DIRECT_BLOCKS; i++) {
            if (inode.direct[i] != -1) {
                blockBitmap.free(inode.direct[i]);
                System.out.printf("[FS]   Block #%d freigegeben%n",
                    inode.direct[i]);
                inode.direct[i] = -1;
            }
        }
        inode.linkCount = 0;
        inode.size      = 0;
        inodeBitmap.free(inoNum);
        System.out.printf("[FS]   Inode #%d freigegeben%n", inoNum);
    }
}
