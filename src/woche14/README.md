# Woche 14 — Crash-Konsistenz & Journaling

**Betriebssysteme — Praktikum**
**Hochschule Kempten, Fakultät Informatik**
**Prof. Dr. Rafael Mayoral Malmström — Sommersemester 2026**

---

## Lernziele

Nach dieser Übung können Sie:

- Das **Crash-Konsistenzproblem** anhand konkreter Absturz-Szenarien erklären
- Den Unterschied zwischen **TxB** und **TxE** erklären und warum TxE zuletzt geschrieben wird
- Den **Recovery-Algorithmus** (zwei Durchläufe) nachvollziehen und implementieren
- Den Begriff **Idempotenz** auf Dateisystem-Operationen anwenden

---

## Hintergrund

Aus Woche 13 wissen wir: `createFile()` muss drei Strukturen aktualisieren:

1. **Inode Bitmap** — neuen Inode als belegt markieren
2. **Inode** — initialisieren
3. **Verzeichniseintrag** — Name → Inode-Nummer

Das Betriebssystem kann diese drei Schritte nicht atomar ausführen —
zwischen jedem Schritt kann ein Stromausfall oder Systemabsturz eintreten.
Wenn das System nach Schritt 1, aber vor Schritt 3 abstürzt, ist das
Dateisystem **inkonsistent**: die Inode Bitmap sagt Inode #3 ist belegt,
aber kein Verzeichnis verweist auf ihn — Speicherleck.

**Journaling** löst dieses Problem: bevor die Strukturen geändert werden,
wird die geplante Änderung ins Journal geschrieben. Nach einem Absturz
kann das System das Journal lesen und unvollständige Operationen entweder
abschließen (committed) oder ignorieren (nicht committed).

### Das Problem mit einer einfachen Simulation

Um das Crash-Konsistenzproblem wirklich sichtbar zu machen, müssten wir
mitten in `create()` einen Absturz simulieren — also nach Schritt 1,
aber vor Schritt 2. Das ist von außen nicht möglich, weil `create()` eine
einzige Methode ist und die internen Strukturen (`inodeBitmap`,
`inodeTable`, `rootDir`) privat sind.

**Lösung: `CrashableFileSystem`**

Für **diese Demo** gibt es eine Hilfsklasse `CrashableFileSystem` die
`SimpleFileSystem` erweitert und die drei Schritte einzeln nach außen freilegt:

```java
public class CrashableFileSystem extends SimpleFileSystem {
    public int  step1_allocateInode()                    // Inode Bitmap setzen
    public void step2_initInode(int inoNum)              // Inode initialisieren
    public void step3_addDirEntry(String name, int ino)  // Verzeichniseintrag
}
```

`CrashableFileSystem` ist **nur für diese Demo** gedacht — in einem echten
System würde man diese Kapselung niemals aufbrechen. Sie macht hier sichtbar
was normalerweise verborgen bleibt: die Nicht-Atomarität der drei Schritte.

---

## Aufgaben

### Aufgabe 1 — Crash-Szenarien beobachten (15 Minuten)

Öffnen Sie `CrashSimulator.java` und `CrashableFileSystem.java`.
Lesen Sie beide Dateien kurz durch — verstehen Sie wie `CrashableFileSystem`
die drei Schritte von `create()` einzeln freilegt.

Führen Sie `CrashSimulator.main()` aus und beobachten Sie die Ausgabe.

**Beobachten Sie in der Ausgabe:**

Jedes Szenario zeigt den Zustand der **Inode Bitmap** vor und nach dem
simulierten Absturz. Achten Sie genau auf den Unterschied zwischen
dem was die Bitmap anzeigt und dem was über `lookup()` erreichbar ist.

**Analysieren Sie:**

a) **Szenario 1 — Absturz nach Schritt 1** (nur Inode Bitmap gesetzt):
   - Welche Inode-Nummer erscheint in der Bitmap als belegt?
   - Was gibt `lookup("wichtig.txt")` zurück? Warum?
   - Was ist das konkrete Problem: welche Strukturen stimmen
     nicht überein?
   - Was würde `fsck` beim nächsten Boot tun?

b) **Szenario 2 — Absturz nach Schritt 2** (Bitmap + Inode, kein Verzeichnis):
   - Warum ist die Datei immer noch nicht erreichbar obwohl
     der Inode vollständig initialisiert ist?
   - Warum ist Szenario 2 "schlimmer" als Szenario 1?
     *(Hinweis: Denken Sie an Datenblöcke)*

c) **Szenario 3 — kein Absturz** (alle 3 Schritte):
   - Welche drei Strukturen stimmen jetzt überein?
   - Warum ist das Dateisystem konsistent?

d) **Vergleich**: Füllen Sie die Tabelle aus:

   | Szenario | Bitmap | Inode | Verzeichnis | Konsistent? |
   |----------|--------|-------|-------------|-------------|
   | Absturz nach Schritt 1 | ✓ | ✗ | ✗ | ✗ |
   | Absturz nach Schritt 2 | | | | |
   | Kein Absturz           | | | | |

---

### Aufgabe 2 — Journal: beginTransaction(), log() und commit() (25 Minuten)

Öffnen Sie `Journal.java`. Die Klasse verwaltet das Write-Ahead Log.
`JournalEntry` und alle Hilfsdatenklassen sind bereits implementiert —
lesen Sie sie zuerst, um die Struktur zu verstehen.

Implementieren Sie die drei Methoden:

**`beginTransaction()`** — startet eine neue Transaktion (TxB):
```
1. int txId = nextTxId++
2. writeEntry(new JournalEntry(txId, "TxB", "begin"))
3. Meldung ausgeben: "[Journal] beginTransaction() -> txId=X"
4. return txId
```

**`log(int txId, String operation, Object data)`** — fügt Dateneintrag hinzu:
```
writeEntry(new JournalEntry(txId, operation, data))
Meldung ausgeben: "[Journal] log(txId=X, operation)"
```

**`commit(int txId)`** — schließt Transaktion ab (TxE):
```
1. Ausgabe: "[Journal] fsync() — Journal-Daten auf Disk gesichert"
2. writeEntry(new JournalEntry(txId))    ← TxE-Konstruktor (nur txId)
3. Ausgabe: "[Journal] fsync() — TxE #X auf Disk gesichert ✓"
4. Ausgabe: "[Journal] Transaktion #X committed"
```

**Warum `fsync()` vor TxE?**
Das `fsync()` stellt sicher dass alle bisherigen Journal-Einträge
wirklich auf Disk sind — bevor TxE geschrieben wird.
TxE darf erst nach diesem `fsync()` auf Disk landen.
Nur so gilt die Invariante: *TxE auf Disk = Transaktion vollständig*.

**Testen:** Fügen Sie am Ende von `JournaledDemo.main()` einen Aufruf
`jfs.journal.dump()` ein — Sie sehen dann alle Journal-Einträge
mit TxB, Daten und TxE.

---

### Aufgabe 3 — Journal.replay() implementieren (25 Minuten)

Implementieren Sie `replay(JournaledFileSystem fs)` in `Journal.java`.

Das ist der Recovery-Algorithmus der nach einem Absturz aufgerufen wird:

```
Schritt 1 — Welche Transaktionen sind committed?
   Set<Integer> committed = new HashSet<>();
   for (JournalEntry e : log)
       if (e.isCommit) committed.add(e.txId);

   Falls committed leer: keine Aktion nötig, return.

Schritt 2 — Committed Einträge auf das Dateisystem anwenden:
   for (JournalEntry e : log)
       if (!e.isCommit
           && committed.contains(e.txId)
           && !e.operation.equals("TxB"))
           fs.applyOperation(e.operation, e.data);
```

**Warum zwei Durchläufe?**
Wir müssen das gesamte Log lesen bevor wir wissen welche Transaktionen
ein TxE haben. Erst dann können wir entscheiden was replayed wird.

**Warum ist Replay sicher?**
`applyOperation()` schreibt immer denselben Wert an dieselbe Stelle —
mehrfaches Ausführen hat dasselbe Ergebnis wie einmaliges. Das nennt man
**Idempotenz**. Ohne Idempotenz wäre Replay gefährlich
(z.B. ein Zähler der zweimal erhöht wird).

---

### Aufgabe 4 — JournaledDemo beobachten (15 Minuten)

Führen Sie `JournaledDemo.main()` aus (setzt Aufgaben 2 und 3 voraus).

Das Programm demonstriert drei Szenarien:

1. **Normaler Betrieb**: Dateien werden mit Journal erstellt.
   Rufen Sie `jfs.journal.dump()` auf — sehen Sie die TxB/TxE-Struktur.

2. **Absturz nach TxE**: Das Journal ist committed, der Checkpoint fehlt.
   → `journal.replay()` stellt die Datei wieder her.

3. **Absturz vor TxE**: Das Journal ist unvollständig.
   → `journal.replay()` ignoriert die Transaktion — FS unverändert.

**Beobachten und erklären:**

a) In Szenario 2: Welche Meldung gibt `replay()` aus?
   Welche Datei existiert nach Recovery?

b) In Szenario 3: Warum wird die Transaktion ignoriert?
   Was ist der Unterschied zu Szenario 2 im Journal-Inhalt?

c) Vergleichen Sie mit dem `CrashSimulator` aus Aufgabe 1:
   Welches Problem aus Aufgabe 1 löst das Journal?
   Welches Szenario aus Aufgabe 1 kann jetzt nicht mehr auftreten?

---

## Denkanstöße und Reflexionsfragen

1. **TxE zuletzt**: Warum wird TxE unbedingt als letztes geschrieben,
   nach einem expliziten `fsync()`? Was würde passieren wenn TxE und
   die anderen Journal-Einträge in beliebiger Reihenfolge auf Disk
   geschrieben werden?

2. **Idempotenz**: Warum muss `applyOperation()` idempotent sein?
   Geben Sie ein Beispiel für eine nicht-idempotente Operation
   die im Journal gefährlich wäre.

3. **Ordered vs. Full Journaling**: Unser Simulator journalt nur Metadaten.
   Was würde man ändern um auch Dateiinhalt zu journaln?
   Was wäre der Nachteil?

4. **ProcessControlBlock und Crash**: Ein Prozess hat eine Datei geöffnet
   (fd im PCB). Nach einem Absturz und Recovery — ist der fd noch gültig?
   Was muss beim Neustart passieren?

---

## Verwendete Dateien

| Datei | Beschreibung |
|-------|-------------|
| `CrashableFileSystem.java` | Erweitert SimpleFileSystem — legt die 3 Schritte von create() frei (nur für Demo) |
| `CrashSimulator.java` | Führt die 3 Schritte einzeln aus und simuliert Absturz nach jedem Schritt |
| `Journal.java` | Write-Ahead Log — **Lücken in Aufgaben 2 und 3** |
| `JournaledFileSystem.java` | Dateisystem mit Journaling (vollständig) |
| `JournaledDemo.java` | Demonstriert normalen Betrieb, Recovery und Absturz-Szenarien |
| + alle Dateien aus Wochen 12 und 13 | |

---

## Java-Klassen und Dokumentation

### `Journal`

| Methode | Signatur | Beschreibung |
|---|---|---|
| `beginTransaction()` | `int` | Startet neue Transaktion, schreibt TxB, gibt txId zurück |
| `log(int, String, Object)` | `void` | Fügt Dateneintrag zur laufenden Transaktion hinzu |
| `commit(int)` | `void` | Schreibt TxE, sichert mit fsync — Transaktion gilt als committed |
| `replay(JournaledFileSystem)` | `void` | Recovery: wiederholt alle committed Transaktionen |
| `dump()` | `void` | Gibt alle Journal-Einträge aus |
| `simulateCrashAfterEntry(int)` | `void` | Für Tests: bricht nach n Einträgen ab |

### `JournaledFileSystem`

| Methode | Signatur | Beschreibung |
|---|---|---|
| `createFileJournaled(String)` | `int` | Erstellt Datei mit Journal-Schutz (vollständig implementiert) |
| `deleteJournaled(String)` | `void` | Löscht Datei mit Journal-Schutz |
| `applyOperation(String, Object)` | `void` | Wendet einen Journal-Eintrag auf das FS an (idempotent) |

---

## Weiterführende Ressourcen

- **OSTEP Kapitel 42**: Crash Consistency: FSCK and Journaling
  https://pages.cs.wisc.edu/~remzi/OSTEP/file-journaling.pdf
- **Linux `lsblk`**: Zeigt alle Block-Devices als Baumstruktur — Festplatten, Partitionen, Loop-Devices
- **Linux `tune2fs -l /dev/sda1`**: Zeigt ext4-Journal-Parameter
- **SQLite WAL-Modus**: Dokumentation unter https://www.sqlite.org/wal.html
  — dasselbe Prinzip wie FS-Journaling, sehr lesenswert
