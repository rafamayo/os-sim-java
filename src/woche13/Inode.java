package woche13;

/**
 * Repräsentiert den Inode einer Datei im simulierten Dateisystem.
 *
 * Ein Inode speichert alle Metadaten einer Datei — aber NICHT ihren Namen.
 * Der Name lebt im Verzeichnis (Woche 13). Ein Inode kann mehrere Namen
 * haben (Hard Links): linkCount gibt an wie viele Verzeichniseinträge
 * auf diesen Inode zeigen.
 *
 * Disk-Layout der Blockzeiger:
 *   direct[0..11]  → direkte Zeiger auf Datenblöcke (12 × BLOCK_SIZE Bytes)
 *   singleIndirect → zeigt auf einen Block der BLOCK_SIZE/4 weitere Zeiger enthält
 *   doubleIndirect → zwei Ebenen indirekt (in diesem Simulator nicht implementiert)
 */
public class Inode {

    // --- Konstanten ---
    public static final int DIRECT_BLOCKS = 12;
    public static final int MODE_FILE      = 0;
    public static final int MODE_DIRECTORY = 1;

    // --- Metadaten ---
    public int  mode;       // Dateityp: MODE_FILE oder MODE_DIRECTORY
    public int  uid;        // Eigentümer-ID (vereinfacht: immer 0)
    public int  gid;        // Gruppen-ID   (vereinfacht: immer 0)
    public long size;       // Dateigröße in Bytes
    public long atime;      // Zeitstempel letzter Zugriff  (ms seit Epoch)
    public long mtime;      // Zeitstempel letzte Änderung  (ms seit Epoch)
    public long ctime;      // Zeitstempel letzte Metadatenänderung
    public int  linkCount;  // Anzahl Verzeichniseinträge → dieser Inode

    // --- Blockzeiger ---
    public int[] direct = new int[DIRECT_BLOCKS]; // direkte Zeiger
    public int   singleIndirect;                   // einfach indirekter Zeiger
    public int   doubleIndirect;                   // doppelt indirekter (nicht implementiert)

    /**
     * Erstellt einen leeren, nicht-allokierten Inode.
     * Alle Zeiger werden auf -1 gesetzt (= kein Block belegt).
     */
    public Inode() {
        this.size      = 0;
        this.linkCount = 0;
        this.mode      = MODE_FILE;
        java.util.Arrays.fill(direct, -1);
        this.singleIndirect = -1;
        this.doubleIndirect = -1;
    }

    /**
     * Gibt zurück ob dieser Inode eine gültige Datei repräsentiert.
     * Ein Inode ist allokiert wenn linkCount > 0.
     */
    public boolean isAllocated() {
        return linkCount > 0;
    }

    /**
     * Initialisiert den Inode als neue Datei mit aktuellem Zeitstempel.
     *
     * @param mode MODE_FILE oder MODE_DIRECTORY
     */
    public void init(int mode) {
        this.mode      = mode;
        this.size      = 0;
        this.linkCount = 1;
        long now = System.currentTimeMillis();
        this.atime = now;
        this.mtime = now;
        this.ctime = now;
        java.util.Arrays.fill(direct, -1);
        this.singleIndirect = -1;
    }

    /**
     * Gibt eine lesbare Darstellung des Inodes zurück.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Inode[mode=%s, size=%d B, linkCount=%d, mtime=%d]%n",
            mode == MODE_DIRECTORY ? "DIR" : "FILE", size, linkCount, mtime));
        sb.append("  Direkte Zeiger: ");
        for (int i = 0; i < DIRECT_BLOCKS; i++) {
            if (direct[i] != -1) sb.append(direct[i]).append(" ");
        }
        if (singleIndirect != -1) {
            sb.append(" | singleIndirect=").append(singleIndirect);
        }
        return sb.toString();
    }
}
