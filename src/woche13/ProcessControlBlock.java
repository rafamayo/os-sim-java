package woche13;

import java.util.HashMap;
import java.util.Map;

/**
 * Process Control Block — repräsentiert einen simulierten Prozess.
 *
 * Erweitert den PCB aus den Wochen 2-4 um Dateisystem-Kontext:
 * - Offene File Descriptors (fd → inodeNumber)
 * - Aktuelles Arbeitsverzeichnis
 * - Schreib-/Lesecursor pro File Descriptor
 *
 * WICHTIG: open() und close() benachrichtigen das Dateisystem
 * über notifyOpen() / notifyClose(). Das ermöglicht dem Dateisystem
 * zu verfolgen ob ein Inode noch von einem Prozess gehalten wird —
 * notwendig für die korrekte verzögerte Freigabe nach delete().
 */
public class ProcessControlBlock {

    // --- Prozess-Identifikation ---
    public final int    pid;
    public final String name;

    // --- Prozess-Zustand ---
    public enum State { NEW, READY, RUNNING, WAITING, TERMINATED }
    public State state;

    // --- Dateisystem-Kontext ---
    public int currentDirectory;

    private final Map<Integer, Integer> openFiles   = new HashMap<>();
    private final Map<Integer, Long>    fileOffsets = new HashMap<>();
    private int nextFd = 3;

    /**
     * Referenz auf das Dateisystem — nötig für notifyOpen/notifyClose.
     * Wird in open() / close() genutzt.
     * null = kein Dateisystem verknüpft (für einfache Tests).
     */
    private SimpleFileSystem fs;

    public ProcessControlBlock(int pid, String name) {
        this.pid              = pid;
        this.name             = name;
        this.state            = State.NEW;
        this.currentDirectory = SimpleFileSystem.ROOT_INODE;
    }

    /**
     * Verknüpft diesen PCB mit einem Dateisystem.
     * Muss vor dem ersten open()-Aufruf gesetzt werden.
     */
    public void setFileSystem(SimpleFileSystem fs) {
        this.fs = fs;
    }

    // =========================================================
    // File Descriptor Verwaltung
    // =========================================================

    /**
     * Öffnet eine Datei und legt einen File Descriptor an.
     *
     * Benachrichtigt das Dateisystem über notifyOpen() —
     * der Inode-Zähler offener fds wird erhöht.
     *
     * @param inodeNum Inode-Nummer
     * @return File Descriptor (>= 3)
     */
    public int open(int inodeNum) {
        int fd = nextFd++;
        openFiles.put(fd, inodeNum);
        fileOffsets.put(fd, 0L);
        if (fs != null) {
            fs.notifyOpen(inodeNum);
        }
        System.out.printf("[PCB %d] open: Inode #%d -> fd %d%n", pid, inodeNum, fd);
        return fd;
    }

    /**
     * Schließt einen File Descriptor.
     *
     * Benachrichtigt das Dateisystem über notifyClose() —
     * der Zähler wird dekrementiert. Falls linkCount=0 und
     * openCount danach 0 ist, gibt das Dateisystem den Inode frei.
     *
     * @param fd File Descriptor
     */
    public void close(int fd) {
        if (!openFiles.containsKey(fd)) {
            throw new IllegalArgumentException("fd " + fd + " ist nicht offen");
        }
        int inodeNum = openFiles.get(fd);
        openFiles.remove(fd);
        fileOffsets.remove(fd);
        System.out.printf("[PCB %d] close: fd %d (Inode #%d)%n", pid, fd, inodeNum);
        if (fs != null) {
            fs.notifyClose(inodeNum);
        }
    }

    // =========================================================
    // Hilfsmethoden
    // =========================================================

    public int getInodeForFd(int fd) {
        Integer ino = openFiles.get(fd);
        if (ino == null) {
            throw new IllegalArgumentException("fd " + fd + " ist nicht offen");
        }
        return ino;
    }

    public long getOffset(int fd) {
        Long offset = fileOffsets.get(fd);
        if (offset == null) {
            throw new IllegalArgumentException("fd " + fd + " ist nicht offen");
        }
        return offset;
    }

    public void advanceOffset(int fd, long bytes) {
        fileOffsets.put(fd, getOffset(fd) + bytes);
    }

    public boolean hasFileOpen(int inodeNum) {
        return openFiles.containsValue(inodeNum);
    }

    public int openFileCount() {
        return openFiles.size();
    }

    @Override
    public String toString() {
        return String.format(
            "PCB[pid=%d, name='%s', state=%s, cwd=Inode#%d, openFds=%d]",
            pid, name, state, currentDirectory, openFiles.size());
    }
}
