package woche14;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

/**
 * Erweitertes Dateisystem mit Verzeichnisunterstützung (Woche 13).
 *
 * Erweitert SimpleFileSystem um:
 *   - Echte Verzeichnisstruktur (Directory-Objekte statt flacher Map)
 *   - Pfadauflösung (resolve)
 *   - Hard Links
 *   - rename() als atomares Primitiv
 */
public class DirectoryFileSystem extends SimpleFileSystem {

    // Verzeichnis-Cache: inodeNumber → Directory-Objekt
    private final Map<Integer, Directory> directories = new HashMap<>();

    /**
     * Erstellt ein neues Dateisystem mit Wurzelverzeichnis.
     */
    public DirectoryFileSystem() {
        super();
        // Root-Verzeichnis (Inode 0) als Directory anlegen
        // "." und ".." zeigen beide auf Inode 0 (Wurzel)
        directories.put(ROOT_INODE, new Directory(ROOT_INODE, ROOT_INODE));
    }

    /**
     * Erstellt eine neue Datei im angegebenen Pfad.
     *
     * @param path Absoluter Pfad der neuen Datei
     *             Beispiel: "/home/user/readme.txt"
     * @return Inode-Nummer der neuen Datei
     */
    public int createFile(String path) throws FileNotFoundException,
            BlockBitmap.OutOfSpaceException {

        String parentPath = getParentPath(path);
        String fileName   = getBaseName(path);

        int parentIno   = resolve(parentPath);
        Directory parentDir = getDirectory(parentIno);

        // Inode allozieren
        int newIno = inodeBitmap.allocate();
        inodeTable[newIno].init(Inode.MODE_FILE);

        // Im Verzeichnis eintragen
        parentDir.addEntry(fileName, newIno);

        System.out.printf("[FS] createFile(\"%s\") -> Inode #%d%n", path, newIno);
        return newIno;
    }

    // =========================================================
    // Verzeichnis-Operationen
    // =========================================================

    /**
     * Löst einen Pfad auf und gibt die Inode-Nummer zurück.
     *
     * Algorithmus:
     *   1. Starte bei ROOT_INODE
     *   2. Teile den Pfad bei "/"
     *   3. Für jede nicht-leere Komponente:
     *      a. Lade das aktuelle Verzeichnis
     *      b. lookup(komponente) → nächste Inode
     *      c. Falls -1: FileNotFoundException
     *   4. Gib den letzten Inode zurück
     *
     * @param path Absoluter Pfad (beginnt mit "/")
     * @return Inode-Nummer
     * @throws FileNotFoundException wenn eine Komponente nicht gefunden
     */
    public int resolve(String path) throws FileNotFoundException {
        if (path.equals("/")) return ROOT_INODE;

        String[] parts = path.split("/");
        int currentIno = ROOT_INODE;

        for (String part : parts) {
            if (part.isEmpty()) continue; // führendes "/"

            Directory dir = getDirectory(currentIno);
            int nextIno = dir.lookup(part);

            if (nextIno == -1) {
                throw new FileNotFoundException(
                    "Komponente nicht gefunden: '" + part + "' in Pfad: " + path);
            }
            currentIno = nextIno;
        }

        return currentIno;
    }

    /**
     * Erstellt ein neues Verzeichnis.
     *
     * @param path Absoluter Pfad des neuen Verzeichnisses
     *             Beispiel: "/home" oder "/home/user"
     * @return Inode-Nummer des neuen Verzeichnisses
     * @throws FileNotFoundException wenn Elternverzeichnis nicht existiert
     * @throws BlockBitmap.OutOfSpaceException wenn keine Ressourcen frei
     */
    public int mkdir(String path) throws FileNotFoundException,
            BlockBitmap.OutOfSpaceException {

        // Pfad parsen
        String parentPath = getParentPath(path);
        String dirName    = getBaseName(path);

        // Eltern-Verzeichnis finden
        int parentIno = resolve(parentPath);
        Directory parentDir = getDirectory(parentIno);

        // Neuen Inode allozieren
        int newIno = inodeBitmap.allocate();
        inodeTable[newIno].init(Inode.MODE_DIRECTORY);

        // Directory-Objekt erstellen
        Directory newDir = new Directory(newIno, parentIno);
        directories.put(newIno, newDir);

        // Im Eltern-Verzeichnis eintragen
        parentDir.addEntry(dirName, newIno);

        // linkCount des Elternverzeichnisses erhöhen (wegen "..")
        inodeTable[parentIno].linkCount++;

        System.out.printf("[FS] mkdir(\"%s\") -> Inode #%d%n", path, newIno);
        return newIno;
    }

    /**
     * Erstellt einen Hard Link.
     *
     * Beide Pfade zeigen nach dem Aufruf auf denselben Inode.
     * linkCount des Inodes wird erhöht.
     *
     * @param existingPath Quellpfad (muss existieren, kein Verzeichnis)
     * @param newPath      Zielpfad (darf noch nicht existieren)
     */
    public void hardLink(String existingPath, String newPath)
            throws FileNotFoundException, BlockBitmap.OutOfSpaceException {

        int inoNum = resolve(existingPath);

        // Hard Links auf Verzeichnisse sind verboten
        if (inodeTable[inoNum].mode == Inode.MODE_DIRECTORY) {
            throw new IllegalArgumentException(
                "Hard Links auf Verzeichnisse sind nicht erlaubt: " + existingPath);
        }

        // linkCount erhöhen
        inodeTable[inoNum].linkCount++;

        // Neuen Verzeichniseintrag anlegen
        String parentPath = getParentPath(newPath);
        String linkName   = getBaseName(newPath);
        int parentIno = resolve(parentPath);
        getDirectory(parentIno).addEntry(linkName, inoNum);

        System.out.printf("[FS] hardLink(\"%s\" -> \"%s\"): Inode #%d, linkCount=%d%n",
            existingPath, newPath, inoNum, inodeTable[inoNum].linkCount);
    }

    /**
     * Benennt eine Datei um oder verschiebt sie.
     *
     * POSIX-Garantie: rename() ist atomar — nach einem Absturz gibt es
     * entweder den alten oder den neuen Eintrag, niemals keinen von beiden.
     * (In diesem Simulator ohne Journaling — Atomizität wird in Woche 14 ergänzt)
     *
     * Strategie: erst neuen Eintrag anlegen, dann alten entfernen.
     * So existiert zu keinem Zeitpunkt ein Zustand ohne gültigen Eintrag.
     *
     * @param oldPath Quellpfad
     * @param newPath Zielpfad
     */
    public void rename(String oldPath, String newPath)
            throws FileNotFoundException, BlockBitmap.OutOfSpaceException {

        int inoNum = resolve(oldPath);

        String newParentPath = getParentPath(newPath);
        String newName       = getBaseName(newPath);
        int newParentIno = resolve(newParentPath);

        // Schritt 1: Neuen Eintrag anlegen (erst dann ist die Datei unter beiden Namen erreichbar)
        getDirectory(newParentIno).addEntry(newName, inoNum);

        // Schritt 2: Alten Eintrag entfernen
        String oldParentPath = getParentPath(oldPath);
        String oldName       = getBaseName(oldPath);
        int oldParentIno = resolve(oldParentPath);
        getDirectory(oldParentIno).removeEntry(oldName);

        // linkCount bleibt gleich (ein Eintrag entfernt, einer hinzugefügt)
        inodeTable[inoNum].ctime = System.currentTimeMillis();

        System.out.printf("[FS] rename(\"%s\" -> \"%s\"): Inode #%d%n",
            oldPath, newPath, inoNum);
    }

    /**
     * Löscht eine Datei (über Pfad).
     */
    public void deletePath(String path) throws FileNotFoundException {
        String parentPath = getParentPath(path);
        String name       = getBaseName(path);
        int parentIno = resolve(parentPath);
        Directory parentDir = getDirectory(parentIno);

        int inoNum = parentDir.lookup(name);
        if (inoNum == -1) {
            throw new FileNotFoundException("Nicht gefunden: " + path);
        }

        parentDir.removeEntry(name);
        inodeTable[inoNum].linkCount--;

        System.out.printf("[FS] delete(\"%s\"): Inode #%d, linkCount jetzt %d%n",
            path, inoNum, inodeTable[inoNum].linkCount);

        if (inodeTable[inoNum].linkCount == 0) {
            // Ressourcen freigeben (Datenblöcke + Inode)
            for (int i = 0; i < Inode.DIRECT_BLOCKS; i++) {
                if (inodeTable[inoNum].direct[i] != -1) {
                    blockBitmap.free(inodeTable[inoNum].direct[i]);
                    inodeTable[inoNum].direct[i] = -1;
                }
            }
            inodeTable[inoNum].size = 0;
            inodeBitmap.free(inoNum);
            System.out.printf("[FS]   Inode #%d vollständig freigegeben%n", inoNum);
        }
    }

    /**
     * Gibt Verzeichnisinhalt aus (wie `ls -la`).
     */
    public void listDir(String path) throws FileNotFoundException {
        int ino = resolve(path);
        Directory dir = getDirectory(ino);
        System.out.printf("Inhalt von %s (Inode #%d):%n", path, ino);
        for (DirEntry e : dir.entries()) {
            if (e.name.equals(".") || e.name.equals("..")) {
                System.out.printf("  %-20s -> Inode #%d%n", e.name, e.inodeNumber);
            } else {
                Inode inode = inodeTable[e.inodeNumber];
                System.out.printf("  %-20s -> Inode #%d  %s  %4d Bytes  links=%d%n",
                    e.name, e.inodeNumber,
                    inode.mode == Inode.MODE_DIRECTORY ? "DIR " : "FILE",
                    inode.size, inode.linkCount);
            }
        }
    }

    /**
     * Gibt das gesamte Dateisystem als Baumstruktur aus.
     */
    public void tree() throws FileNotFoundException {
        System.out.println("Dateisystem-Baum:");
        printTree(ROOT_INODE, "/", "", true);
    }

    // =========================================================
    // Interne Hilfsmethoden
    // =========================================================

    protected Directory getDirectory(int inoNum) throws FileNotFoundException {
        Directory dir = directories.get(inoNum);
        if (dir == null) {
            throw new FileNotFoundException("Kein Verzeichnis für Inode #" + inoNum);
        }
        return dir;
    }

    protected String getParentPath(String path) {
        if (path.equals("/")) return "/";
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash == 0) return "/";
        return path.substring(0, lastSlash);
    }

    protected String getBaseName(String path) {
        return path.substring(path.lastIndexOf('/') + 1);
    }

    private void printTree(int ino, String name, String indent, boolean last)
            throws FileNotFoundException {
        String prefix = indent + (last ? "└── " : "├── ");
        Inode inode = inodeTable[ino];
        System.out.printf("%s%s  [Inode #%d, links=%d]%n",
            prefix, name, ino, inode.linkCount);

        if (inode.mode == Inode.MODE_DIRECTORY && directories.containsKey(ino)) {
            Directory dir = directories.get(ino);
            var children = dir.entries().stream()
                .filter(e -> !e.name.equals(".") && !e.name.equals(".."))
                .toList();
            for (int i = 0; i < children.size(); i++) {
                DirEntry e = children.get(i);
                boolean isLast = (i == children.size() - 1);
                printTree(e.inodeNumber, e.name,
                    indent + (last ? "    " : "│   "), isLast);
            }
        }
    }

    // Sichtbar für Unterklassen (Woche 14)
    // Felder inodeTable, inodeBitmap, blockBitmap werden von SimpleFileSystem geerbt
}
