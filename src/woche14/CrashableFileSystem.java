package woche14;

/**
 * Erweiterung von SimpleFileSystem — nur für die Crash-Demo (Woche 14).
 *
 * Legt die drei internen Schritte von create() einzeln frei,
 * damit der CrashSimulator jeden Schritt isoliert ausführen kann
 * und mitten in der Sequenz "abstürzen" kann.
 *
 * In einem echten Betriebssystem sind diese Schritte atomar geplant
 * aber nicht-atomar ausgeführt — genau das ist das Crash-Konsistenzproblem.
 * Hier machen wir die Nicht-Atomarität für die Demonstration sichtbar.
 */
public class CrashableFileSystem extends SimpleFileSystem {

    /**
     * Schritt 1 von create(): Inode Bitmap setzen.
     *
     * Nach diesem Schritt: Inode-Nummer gilt als belegt,
     * aber der Inode selbst ist noch nicht initialisiert
     * und es gibt keinen Verzeichniseintrag.
     *
     * @return allozierte Inode-Nummer
     */
    public int step1_allocateInode() throws BlockBitmap.OutOfSpaceException {
        int inoNum = inodeBitmap.allocate();
        System.out.printf(
            "[FS] Schritt 1: inodeBitmap.allocate() → Inode #%d als belegt markiert%n",
            inoNum);
        return inoNum;
    }

    /**
     * Schritt 2 von create(): Inode initialisieren.
     *
     * Nach diesem Schritt: Inode enthält gültige Metadaten,
     * aber es gibt noch keinen Verzeichniseintrag.
     * Die Datei ist also noch nicht erreichbar.
     *
     * @param inoNum Inode-Nummer aus Schritt 1
     */
    public void step2_initInode(int inoNum) {
        inodeTable[inoNum].init(Inode.MODE_FILE);
        System.out.printf(
            "[FS] Schritt 2: inodeTable[%d].init() → linkCount=1, mode=FILE%n",
            inoNum);
    }

    /**
     * Schritt 3 von create(): Verzeichniseintrag anlegen.
     *
     * Erst nach diesem Schritt ist die Datei über ihren Namen erreichbar.
     * Das Dateisystem ist jetzt konsistent.
     *
     * @param name   Dateiname
     * @param inoNum Inode-Nummer aus Schritt 1
     */
    public void step3_addDirEntry(String name, int inoNum) {
        rootDir.put(name, inoNum);
        System.out.printf(
            "[FS] Schritt 3: rootDir.put(\"%s\", %d) → Datei erreichbar%n",
            name, inoNum);
    }
}
