package woche13;

import java.util.ArrayList;
import java.util.List;

/**
 * Ein einzelner Verzeichniseintrag: Dateiname → Inode-Nummer.
 *
 * In echten Dateisystemen (ext4) enthält ein DirEntry zusätzlich
 * die Länge des Namens und den Inode-Typ — für unseren Simulator
 * reichen Name und Inode-Nummer.
 */
class DirEntry {
    public final String name;
    public final int    inodeNumber;

    public DirEntry(String name, int inodeNumber) {
        this.name        = name;
        this.inodeNumber = inodeNumber;
    }

    @Override
    public String toString() {
        return String.format("%-20s -> Inode #%d", name, inodeNumber);
    }
}

/**
 * Verzeichnis — eine spezielle Datei die Name → Inode-Nummer Paare speichert.
 *
 * Jedes Verzeichnis enthält immer zwei Sondereinträge:
 *   "."  → Inode des Verzeichnisses selbst
 *   ".." → Inode des Elternverzeichnisses
 *
 * Das Wurzelverzeichnis ist der einzige Fall wo beide auf denselben Inode zeigen.
 */
public class Directory {

    private final List<DirEntry> entries = new ArrayList<>();
    private final int selfInode;

    /**
     * Erstellt ein neues Verzeichnis mit "." und ".." Einträgen.
     *
     * @param selfIno   Inode-Nummer dieses Verzeichnisses
     * @param parentIno Inode-Nummer des Elternverzeichnisses
     *                  (= selfIno für das Wurzelverzeichnis)
     */
    public Directory(int selfIno, int parentIno) {
        this.selfInode = selfIno;
        entries.add(new DirEntry(".",  selfIno));
        entries.add(new DirEntry("..", parentIno));
    }

    /**
     * Sucht einen Eintrag nach Name und gibt seine Inode-Nummer zurück.
     *
     * @param name Datei- oder Verzeichnisname
     * @return Inode-Nummer oder -1 wenn nicht gefunden (ENOENT)
     */
    public int lookup(String name) {
        for (DirEntry e : entries) {
            if (e.name.equals(name)) return e.inodeNumber;
        }
        return -1;
    }

    /**
     * Fügt einen neuen Eintrag hinzu.
     *
     * @param name        Name (muss eindeutig sein)
     * @param inodeNumber Ziel-Inode
     * @throws IllegalArgumentException wenn der Name bereits existiert
     */
    public void addEntry(String name, int inodeNumber) {
        if (lookup(name) != -1) {
            throw new IllegalArgumentException("Eintrag existiert bereits: " + name);
        }
        entries.add(new DirEntry(name, inodeNumber));
    }

    /**
     * Entfernt einen Eintrag.
     *
     * @param name Name des zu entfernenden Eintrags
     * @throws IllegalArgumentException wenn der Name nicht gefunden (ENOENT)
     */
    public void removeEntry(String name) {
        boolean removed = entries.removeIf(e -> e.name.equals(name));
        if (!removed) {
            throw new IllegalArgumentException("ENOENT: " + name + " nicht gefunden");
        }
    }

    /**
     * Gibt alle Einträge zurück (inkl. "." und "..").
     */
    public List<DirEntry> entries() {
        return new ArrayList<>(entries);
    }

    /**
     * Anzahl der Einträge (inkl. "." und "..").
     */
    public int size() {
        return entries.size();
    }

    /**
     * Gibt den Inode-Nummer dieses Verzeichnisses zurück.
     */
    public int getSelfInode() {
        return selfInode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Directory [Inode #%d, %d Einträge]:%n",
            selfInode, entries.size()));
        for (DirEntry e : entries) {
            sb.append("  ").append(e).append("\n");
        }
        return sb.toString();
    }
}
