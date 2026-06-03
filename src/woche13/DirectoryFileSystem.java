package woche13;


import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

/**
 * Erweitertes Dateisystem mit Verzeichnisunterstützung (Woche 13).
 */
public class DirectoryFileSystem extends SimpleFileSystem {

    private final Map<Integer, Directory> directories = new HashMap<>();

    public DirectoryFileSystem() {
        super();
        directories.put(ROOT_INODE, new Directory(ROOT_INODE, ROOT_INODE));
    }

    // =========================================================
    // Bereits implementiert — zum Verstehen lesen
    // =========================================================

    /**
     * Erstellt eine neue Datei im angegebenen Pfad. (Bereits implementiert)
     */
    public int createFile(String path) throws FileNotFoundException,
            BlockBitmap.OutOfSpaceException {
        String parentPath = getParentPath(path);
        String fileName   = getBaseName(path);
        int parentIno = resolve(parentPath);
        Directory parentDir = getDirectory(parentIno);
        int newIno = inodeBitmap.allocate();
        inodeTable[newIno].init(Inode.MODE_FILE);
        parentDir.addEntry(fileName, newIno);
        System.out.printf("[FS] createFile(\"%s\") -> Inode #%d%n", path, newIno);
        return newIno;
    }

    // =========================================================
    // TODO-Aufgaben für Studierende
    // =========================================================

    /**
     * Löst einen absoluten Pfad auf und gibt die Inode-Nummer zurück.
     *
     * TODO (Aufgabe 3):
     *   Sonderfall: path.equals("/") → return ROOT_INODE
     *
     *   Allgemein:
     *   1. String[] parts = path.split("/")
     *   2. int currentIno = ROOT_INODE
     *   3. for (String part : parts) {
     *        if (part.isEmpty()) continue;
     *        Directory dir = getDirectory(currentIno);
     *        int nextIno = dir.lookup(part);
     *        if (nextIno == -1) throw new FileNotFoundException(...)
     *        currentIno = nextIno;
     *      }
     *   4. return currentIno
     */
    public int resolve(String path) throws FileNotFoundException {
        // TODO: Implementieren
        throw new FileNotFoundException("resolve() noch nicht implementiert: " + path);
    }
    
    /**
     * Erstellt ein neues Verzeichnis.
     *
     * TODO (Aufgabe 2):
     *   1. parentPath = getParentPath(path),  dirName = getBaseName(path)
     *   2. parentIno = resolve(parentPath)
     *   3. parentDir = getDirectory(parentIno)
     *   4. newIno = inodeBitmap.allocate()
     *   5. inodeTable[newIno].init(Inode.MODE_DIRECTORY)
     *   6. Directory newDir = new Directory(newIno, parentIno)
     *      directories.put(newIno, newDir)
     *   7. parentDir.addEntry(dirName, newIno)
     *   8. inodeTable[parentIno].linkCount++  (wegen "..")
     *   9. Meldung ausgeben, newIno zurückgeben
     */
    public int mkdir(String path) throws FileNotFoundException,
            BlockBitmap.OutOfSpaceException {
        // TODO: Implementieren
        throw new UnsupportedOperationException("mkdir() noch nicht implementiert");
    }

    /**
     * Erstellt einen Hard Link.
     *
     * TODO (Aufgabe 4):
     *   1. inoNum = resolve(existingPath)
     *   2. Prüfe: kein Hard Link auf Verzeichnisse (MODE_DIRECTORY → Exception)
     *   3. inodeTable[inoNum].linkCount++
     *   4. parentPath = getParentPath(newPath), linkName = getBaseName(newPath)
     *   5. parentIno = resolve(parentPath)
     *   6. getDirectory(parentIno).addEntry(linkName, inoNum)
     *   7. Meldung ausgeben
     */
    public void hardLink(String existingPath, String newPath)
            throws FileNotFoundException, BlockBitmap.OutOfSpaceException {
        // TODO: Implementieren
        throw new UnsupportedOperationException("hardLink() noch nicht implementiert");
    }

    /**
     * Benennt eine Datei um / verschiebt sie.
     *
     * TODO (Aufgabe 5):
     *   WICHTIG: Erst neuen Eintrag anlegen, dann alten entfernen!
     *
     *   1. inoNum = resolve(oldPath)
     *   2. Neuen Eintrag anlegen:
     *        newParentIno = resolve(getParentPath(newPath))
     *        getDirectory(newParentIno).addEntry(getBaseName(newPath), inoNum)
     *   3. Alten Eintrag entfernen:
     *        oldParentIno = resolve(getParentPath(oldPath))
     *        getDirectory(oldParentIno).removeEntry(getBaseName(oldPath))
     *   4. inodeTable[inoNum].ctime aktualisieren
     *   5. Meldung ausgeben
     */
    public void rename(String oldPath, String newPath)
            throws FileNotFoundException, BlockBitmap.OutOfSpaceException {
        // TODO: Implementieren
        throw new UnsupportedOperationException("rename() noch nicht implementiert");
    }

    // =========================================================
    // Bereits implementiert — Ausgabe-Hilfsmethoden
    // =========================================================

    public void deletePath(String path) throws FileNotFoundException {
        String parentPath = getParentPath(path);
        String name = getBaseName(path);
        int parentIno = resolve(parentPath);
        Directory parentDir = getDirectory(parentIno);
        int inoNum = parentDir.lookup(name);
        if (inoNum == -1) throw new FileNotFoundException("Nicht gefunden: " + path);
        parentDir.removeEntry(name);
        inodeTable[inoNum].linkCount--;
        System.out.printf("[FS] delete(\"%s\"): Inode #%d, linkCount jetzt %d%n",
            path, inoNum, inodeTable[inoNum].linkCount);
        if (inodeTable[inoNum].linkCount == 0) {
            for (int i = 0; i < Inode.DIRECT_BLOCKS; i++) {
                if (inodeTable[inoNum].direct[i] != -1) {
                    blockBitmap.free(inodeTable[inoNum].direct[i]);
                    inodeTable[inoNum].direct[i] = -1;
                }
            }
            inodeTable[inoNum].size = 0;
            inodeBitmap.free(inoNum);
        }
    }

    public void listDir(String path) throws FileNotFoundException {
        int ino = resolve(path);
        Directory dir = getDirectory(ino);
        System.out.printf("Inhalt von %s (Inode #%d):%n", path, ino);
        for (DirEntry e : dir.entries()) {
            Inode inode = inodeTable[e.inodeNumber];
            System.out.printf("  %-20s -> Inode #%d  %s  %4d Bytes  links=%d%n",
                e.name, e.inodeNumber,
                inode.mode == Inode.MODE_DIRECTORY ? "DIR " : "FILE",
                inode.size, inode.linkCount);
        }
    }

    public void tree() throws FileNotFoundException {
        System.out.println("Dateisystem-Baum:");
        printTree(ROOT_INODE, "/", "", true);
    }

    // =========================================================
    // Hilfsmethoden — nicht ändern
    // =========================================================

    protected String getParentPath(String path) {
        if (path.equals("/")) return "/";
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash == 0) return "/";
        return path.substring(0, lastSlash);
    }

    protected String getBaseName(String path) {
        return path.substring(path.lastIndexOf('/') + 1);
    }

    protected Directory getDirectory(int inoNum) throws FileNotFoundException {
        Directory dir = directories.get(inoNum);
        if (dir == null)
            throw new FileNotFoundException("Kein Verzeichnis für Inode #" + inoNum);
        return dir;
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
                printTree(e.inodeNumber, e.name,
                    indent + (last ? "    " : "│   "), i == children.size() - 1);
            }
        }
    }

    // Sichtbar für Unterklassen (Woche 14)
    // Felder inodeTable, inodeBitmap, blockBitmap werden von SimpleFileSystem geerbt
}
