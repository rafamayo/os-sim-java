package woche14;

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
 *
 * KORREKTUR gegenüber erster Version:
 *   delete() gibt Ressourcen nur frei wenn BEIDE Bedingungen erfüllt sind:
 *   (1) linkCount == 0  UND  (2) openFileCount[inode] == 0
 *   Entspricht dem Unix-Verhalten: unlink() entfernt nur den Namen —
 *   der Inode lebt weiter solange ein Prozess die Datei geöffnet hält.
 */
public class SimpleFileSystem {

    // --- Konstanten ---
    public static final int BLOCK_SIZE = 512;
    public static final int MAX_INODES = 16;
    public static final int MAX_BLOCKS = 64;
    public static final int ROOT_INODE = 0;

    // --- Interne Strukturen ---
    protected final Inode[]     inodeTable;
    protected final byte[][]    dataBlocks;
    protected final BlockBitmap inodeBitmap;
    protected final BlockBitmap blockBitmap;

    protected final Map<String, Integer> rootDir = new HashMap<>();

    /**
     * Zählt wie viele Prozesse einen Inode aktuell geöffnet haben.
     *
     * Ein Inode darf erst freigegeben werden wenn:
     *   linkCount == 0  (kein Verzeichniseintrag mehr)
     *   UND openFileCount[i] == 0  (kein Prozess hat ihn offen)
     *
     * Dies entspricht dem Kernel-internen "i_count" in Linux.
     */
    private final int[] openFileCount;

    // Statistik
    private int totalCreates = 0;
    private int totalDeletes = 0;
    private int totalWrites  = 0;

    public SimpleFileSystem() {
        this.inodeTable     = new Inode[MAX_INODES];
        this.dataBlocks     = new byte[MAX_BLOCKS][BLOCK_SIZE];
        this.inodeBitmap    = new BlockBitmap(MAX_INODES, "Inode Bitmap");
        this.blockBitmap    = new BlockBitmap(MAX_BLOCKS, "Block Bitmap");
        this.openFileCount  = new int[MAX_INODES]; // alle 0

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
    // Öffentliche Dateisystem-Operationen
    // =========================================================

    public int create(String name) throws BlockBitmap.OutOfSpaceException {
        if (rootDir.containsKey(name)) {
            throw new IllegalArgumentException("Datei existiert bereits: " + name);
        }
        int inoNum = inodeBitmap.allocate();
        inodeTable[inoNum].init(Inode.MODE_FILE);
        rootDir.put(name, inoNum);
        totalCreates++;
        System.out.printf("[FS] create(\"%s\") -> Inode #%d%n", name, inoNum);
        return inoNum;
    }

    public void writeBlock(int inodeNum, int blockIndex, byte[] data)
            throws BlockBitmap.OutOfSpaceException {
        Inode inode = getInode(inodeNum);
        if (blockIndex >= Inode.DIRECT_BLOCKS) {
            throw new IllegalArgumentException("Nur direkte Zeiger unterstützt");
        }
        if (inode.direct[blockIndex] == -1) {
            int newBlock = blockBitmap.allocate();
            inode.direct[blockIndex] = newBlock;
            System.out.printf("[FS] writeBlock: Neuer Block #%d für Inode #%d%n",
                newBlock, inodeNum);
        }
        int len = Math.min(data.length, BLOCK_SIZE);
        System.arraycopy(data, 0, dataBlocks[inode.direct[blockIndex]], 0, len);
        inode.size  = Math.max(inode.size, (long) blockIndex * BLOCK_SIZE + len);
        inode.mtime = System.currentTimeMillis();
        totalWrites++;
    }

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
     * Löscht eine Datei (entfernt Verzeichniseintrag, dekrementiert linkCount).
     *
     * Ressourcen werden NUR freigegeben wenn gilt:
     *   linkCount == 0  UND  openFileCount == 0
     *
     * Falls der Prozess die Datei noch geöffnet hält, bleibt der Inode
     * erhalten bis notifyClose() aufgerufen wird.
     */
    public void delete(String name) {
        Integer inoNum = rootDir.get(name);
        if (inoNum == null) {
            throw new IllegalArgumentException("Datei nicht gefunden: " + name);
        }
        Inode inode = inodeTable[inoNum];
        rootDir.remove(name);
        inode.linkCount--;
        System.out.printf("[FS] delete(\"%s\"): Inode #%d, linkCount=%d, openCount=%d%n",
            name, inoNum, inode.linkCount, openFileCount[inoNum]);

        // Freigeben nur wenn kein Name UND kein offener fd mehr
        if (inode.linkCount == 0 && openFileCount[inoNum] == 0) {
            freeInodeResources(inoNum, inode);
        } else if (inode.linkCount == 0) {
            System.out.printf(
                "[FS] Inode #%d: linkCount=0, aber noch %d fd(s) offen — " +
                "Freigabe verzögert bis close()%n",
                inoNum, openFileCount[inoNum]);
        }
        totalDeletes++;
    }

    // =========================================================
    // Benachrichtigungen vom PCB (open/close)
    // =========================================================

    /**
     * Wird vom PCB aufgerufen wenn ein File Descriptor geöffnet wird.
     * Erhöht den internen Zähler offener fds für diesen Inode.
     */
    public void notifyOpen(int inodeNum) {
        checkRange(inodeNum);
        openFileCount[inodeNum]++;
        System.out.printf("[FS] notifyOpen:  Inode #%d, openCount=%d%n",
            inodeNum, openFileCount[inodeNum]);
    }

    /**
     * Wird vom PCB aufgerufen wenn ein File Descriptor geschlossen wird.
     *
     * Wenn danach linkCount==0 UND openCount==0:
     * werden Inode und Blöcke jetzt freigegeben.
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
                "[FS] Inode #%d: letzter fd geschlossen + linkCount=0 " +
                "-> jetzt freigeben%n", inodeNum);
            freeInodeResources(inodeNum, inode);
        }
    }

    // =========================================================
    // Diagnose
    // =========================================================

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
    // Interne Hilfsmethoden
    // =========================================================

    private Inode getInode(int inoNum) {
        checkRange(inoNum);
        Inode inode = inodeTable[inoNum];
        if (!inode.isAllocated()) {
            throw new IllegalArgumentException("Inode #" + inoNum + " ist nicht allokiert");
        }
        return inode;
    }

    private void checkRange(int inoNum) {
        if (inoNum < 0 || inoNum >= MAX_INODES) {
            throw new IllegalArgumentException("Ungültige Inode-Nummer: " + inoNum);
        }
    }

    private void freeInodeResources(int inoNum, Inode inode) {
        System.out.printf("[FS] Inode #%d: gebe Ressourcen frei%n", inoNum);
        for (int i = 0; i < Inode.DIRECT_BLOCKS; i++) {
            if (inode.direct[i] != -1) {
                blockBitmap.free(inode.direct[i]);
                System.out.printf("[FS]   Block #%d freigegeben%n", inode.direct[i]);
                inode.direct[i] = -1;
            }
        }
        inode.linkCount = 0;
        inode.size      = 0;
        inodeBitmap.free(inoNum);
        System.out.printf("[FS]   Inode #%d freigegeben%n", inoNum);
    }
}
