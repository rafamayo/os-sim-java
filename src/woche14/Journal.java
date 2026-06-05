package woche14;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Write-Ahead Journal (WAL) für den Dateisystem-Simulator (Woche 14).
 *
 * Prinzip:
 *   1. Alle geplanten Änderungen zuerst ins Journal schreiben (TxB + Daten)
 *   2. fsync() — Journal auf Disk sichern
 *   3. TxE schreiben — erst JETZT gilt die Transaktion als committed
 *   4. fsync() — TxE auf Disk sichern
 *   5. Checkpoint: eigentliche Strukturen im Dateisystem aktualisieren
 *
 * Recovery nach Absturz:
 *   - Transaktion MIT TxE  → Checkpoint wiederholen (Replay)
 *   - Transaktion OHNE TxE → ignorieren (FS bleibt unverändert)
 */
public class Journal {

    // =========================================================
    // Innere Klassen — bereits implementiert, bitte lesen
    // =========================================================

    /**
     * Ein einzelner Journal-Eintrag.
     * Beschreibt eine geplante Änderung ODER einen Commit-Marker (TxE).
     */
    public static class JournalEntry {
        public final int     txId;
        public final String  operation;
        public final Object  data;
        public final boolean isCommit;  // true = TxE

        /** Normaler Dateneintrag (TxD) */
        public JournalEntry(int txId, String operation, Object data) {
            this.txId      = txId;
            this.operation = operation;
            this.data      = data;
            this.isCommit  = false;
        }

        /** Commit-Marker (TxE) — nur txId, kein data */
        public JournalEntry(int txId) {
            this.txId      = txId;
            this.operation = "TxE";
            this.data      = null;
            this.isCommit  = true;
        }

        @Override
        public String toString() {
            return isCommit
                ? String.format("[TxE  #%d]", txId)
                : String.format("[TxD  #%d] %-20s %s",
                    txId, operation, data != null ? data.toString() : "");
        }
    }

    // Hilfs-Datenklassen für Journal-Einträge
    public record InodeInit(int inodeNum, int mode) {}
    public record DirEntryData(int parentIno, String name, int inodeNum) {}
    public record InodeBitmapData(int inodeNum, boolean value) {}
    public record BlockBitmapData(int blockNum, boolean value) {}

    // =========================================================
    // Interner Zustand
    // =========================================================

    private final List<JournalEntry> log = new ArrayList<>();
    private int nextTxId        = 1;
    private int crashAfterEntry = -1;
    private int entryCount      = 0;

    // =========================================================
    // Aufgabe 2a — beginTransaction()
    // =========================================================

    /**
     * Startet eine neue Transaktion und schreibt TxB ins Journal.
     *
     * TODO (Aufgabe 2a):
     *   1. int txId = nextTxId++
     *   2. writeEntry(new JournalEntry(txId, "TxB", "begin"))
     *   3. System.out.printf("[Journal] beginTransaction() -> txId=%d%n", txId)
     *   4. return txId
     *
     * @return txId — wird für alle weiteren log()/commit()-Aufrufe benötigt
     */
    public int beginTransaction() {
        // TODO: Implementieren
        throw new UnsupportedOperationException("beginTransaction() nicht implementiert");
    }

    // =========================================================
    // Aufgabe 2b — log()
    // =========================================================

    /**
     * Fügt einen Dateneintrag zur laufenden Transaktion hinzu.
     *
     * TODO (Aufgabe 2b):
     *   1. writeEntry(new JournalEntry(txId, operation, data))
     *   2. System.out.printf("[Journal] log(txId=%d, %s)%n", txId, operation)
     */
    public void log(int txId, String operation, Object data) {
        // TODO: Implementieren
        throw new UnsupportedOperationException("log() nicht implementiert");
    }

    // =========================================================
    // Aufgabe 2c — commit()
    // =========================================================

    /**
     * Schließt eine Transaktion ab — schreibt TxE.
     *
     * WICHTIG: TxE wird erst nach fsync() geschrieben.
     * Das garantiert: wenn TxE auf Disk ist, sind auch alle
     * Dateneinträge auf Disk. Das ist die zentrale Invariante
     * des Journalings.
     *
     * TODO (Aufgabe 2c):
     *   1. System.out.printf("[Journal] fsync() — Journal-Daten auf Disk gesichert%n")
     *   2. writeEntry(new JournalEntry(txId))      ← TxE (nur txId, kein data)
     *   3. System.out.printf("[Journal] fsync() — TxE #%d auf Disk gesichert ✓%n", txId)
     *   4. System.out.printf("[Journal] Transaktion #%d committed%n", txId)
     *
     * Nach diesem Aufruf gilt: Absturz → Recovery replays diese Transaktion.
     */
    public void commit(int txId) {
        // TODO: Implementieren
        throw new UnsupportedOperationException("commit() nicht implementiert");
    }

    // =========================================================
    // Aufgabe 3 — replay()
    // =========================================================

    /**
     * Recovery: liest das Journal und wiederholt alle committed Transaktionen.
     *
     * Aufgerufen beim Systemstart nach einem Absturz.
     * Transaktionen MIT TxE werden replayed.
     * Transaktionen OHNE TxE werden ignoriert.
     *
     * TODO (Aufgabe 3):
     *
     *   System.out.println("[Journal] === Recovery: Journal-Replay startet ===");
     *
     *   // Durchlauf 1: Welche txIds haben ein TxE?
     *   Set<Integer> committed = new HashSet<>();
     *   for (JournalEntry e : log)
     *       if (e.isCommit) committed.add(e.txId);
     *
     *   // Falls keine committed Transaktionen: nichts zu tun
     *   if (committed.isEmpty()) {
     *       System.out.println("[Journal] Keine committed Transaktionen — FS unverändert");
     *       return;
     *   }
     *
     *   // Durchlauf 2: Einträge committed Transaktionen anwenden
     *   int replayed = 0;
     *   for (JournalEntry e : log)
     *       if (!e.isCommit
     *           && committed.contains(e.txId)
     *           && !e.operation.equals("TxB")) {
     *           System.out.printf("[Journal] Replay: %s%n", e);
     *           fs.applyOperation(e.operation, e.data);
     *           replayed++;
     *       }
     *
     *   System.out.printf("[Journal] Recovery: %d Operationen replayed%n", replayed);
     *
     * @param fs Dateisystem auf das Recovery angewendet wird
     */
    public void replay(JournaledFileSystem fs) {
        // TODO: Implementieren
        System.out.println("[Journal] replay() noch nicht implementiert");
    }

    // =========================================================
    // Bereits implementiert — nicht ändern
    // =========================================================

    /** Gibt den Journal-Inhalt aus. */
    public void dump() {
        System.out.println("=== Journal-Inhalt (" + log.size() + " Einträge) ===");
        for (JournalEntry e : log) {
            System.out.println("  " + e);
        }
    }

    /**
     * Aktiviert Crash-Simulation: bricht nach dem n-ten Eintrag ab.
     * @param n Nach dem n-ten Eintrag abstürzen (-1 = kein Absturz)
     */
    public void simulateCrashAfterEntry(int n) {
        this.crashAfterEntry = n;
        this.entryCount      = 0;
        if (n >= 0)
            System.out.printf(
                "[Journal] Crash-Simulation: Absturz nach %d Einträgen%n", n);
    }

    public int entryCount() { return log.size(); }

    private void writeEntry(JournalEntry entry) {
        if (crashAfterEntry >= 0 && entryCount >= crashAfterEntry) {
            throw new SimulatedCrashException(
                "Simulierter Absturz nach " + entryCount + " Einträgen");
        }
        log.add(entry);
        entryCount++;
    }

    public static class SimulatedCrashException extends RuntimeException {
        public SimulatedCrashException(String msg) { super(msg); }
    }
}
