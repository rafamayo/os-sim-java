# Woche 12 — Dateisystem-Grundlagen: Inodes & Block Bitmap

**Betriebssysteme — Praktikum**
**Hochschule Kempten, Fakultät Informatik**
**Prof. Dr. Rafael Mayoral Malmström — Sommersemester 2026**

---

## Lernziele

Nach dieser Übung können Sie:

- Die Struktur eines **Inodes** beschreiben und in Java implementieren
- Eine **Block Bitmap** zur Freiraumverwaltung einsetzen
- Die Operationen `create()`, `write()` und `delete()` auf Inode-Ebene implementieren
- Den Unterschied zwischen **Dateiname** und **Inode** erklären
- Das **Disk-Layout** eines einfachen Dateisystems (Superblock, Bitmaps, Inode-Tabelle, Datenblöcke) nachvollziehen

---

## Hintergrund

Ein Dateisystem speichert für jede Datei zwei Dinge getrennt:

1. **Den Inhalt** — die eigentlichen Bytes, verteilt auf Datenblöcke
2. **Die Metadaten** — Größe, Zeitstempel, Zugriffsrechte, Zeiger auf die Blöcke

Die Metadaten leben im **Inode** (Index Node). Der Dateiname gehört nicht zum Inode — er ist ein Eintrag im Verzeichnis (Woche 13). Das ermöglicht, dass eine Datei mehrere Namen haben kann (Hard Links), und dass eine Datei weiter existiert solange noch ein Prozess sie geöffnet hat, auch wenn ihr Name bereits gelöscht wurde.

Das **Disk-Layout** unseres Simulators:

```
| Superblock | Inode Bitmap | Block Bitmap | Inode Tabelle | Datenblöcke |
  Block 0      Block 1        Block 2        Blöcke 3-18    Blöcke 19-...
```

---

## Aufgaben

### Aufgabe 1 — Inode verstehen (10 Minuten)

Öffnen Sie `Inode.java`. Die Klasse ist vollständig — lesen Sie sie sorgfältig.

**Fragen:**

a) Welche Felder des Inodes beschreiben die **Größe** der Datei, welche beschreiben **wo** die Daten liegen?

b) Warum sind die direkten Zeiger ein Array mit 12 Einträgen? Was passiert wenn eine Datei größer als `12 × BLOCK_SIZE` Bytes ist?

c) Ein Inode hat `linkCount = 0`. Was bedeutet das? Darf die Datei noch gelesen werden?

d) Berechnen Sie: Bei `BLOCK_SIZE = 512` Bytes und 4-Byte-Zeigern — wie groß kann eine Datei maximal werden wenn nur direkte und einfach indirekte Zeiger genutzt werden?

---

### Aufgabe 2 — Block Bitmap implementieren (15 Minuten)

Öffnen Sie `BlockBitmap.java`. Implementieren Sie die fehlenden Methoden:

**`allocate()`** — sucht den ersten freien Block, markiert ihn als belegt und gibt seine Nummer zurück. Wirft `OutOfSpaceException` wenn kein freier Block vorhanden.

**`free(int blockNum)`** — gibt einen Block frei (Bit auf 0 setzen). Wirft `IllegalArgumentException` wenn der Block nicht belegt war.

**`isFree(int blockNum)`** — gibt `true` zurück wenn der Block frei ist.

Testen Sie mit `BlockBitmapTest.main()`.

**Erwartete Ausgabe:**
```
╔══════════════════════════════════════════╗
║  BlockBitmap Test (Woche 12, Aufgabe 2)  ║
╚══════════════════════════════════════════╝

--- Test: allocate() ---
  ✓ Erster freier Block ist 0
  ✓ Zweiter freier Block ist 1
  ✓ Dritter freier Block ist 2
  ✓ Nach 3 allocate(): freeCount() == 5
  TestBitmap [8 Einträge]: 11100000 (5 frei)

--- Test: free() ---
  ✓ Nach free(0): Block 0 ist wieder frei
  ✓ Nach free(0): freeCount() == 6
  ✓ allocate() nach free(0) gibt Block 0 zurück
  ...

══════════════════════════════════════════
  Ergebnis: 14 bestanden, 0 fehlgeschlagen
══════════════════════════════════════════
  ✓ Alle Tests bestanden — BlockBitmap korrekt!
```

---

### Aufgabe 3 — FileSystem.create() implementieren (15 Minuten)

Öffnen Sie `SimpleFileSystem.java`. Implementieren Sie `create(String name)`:

```
1. Freien Inode aus der Inode Bitmap suchen (inodeBitmap.allocate())
2. Inode initialisieren (mode, timestamps, linkCount = 1)
3. Inode in die Inode Tabelle eintragen
4. Verzeichniseintrag anlegen: name → inodeNumber
   (Vereinfachung: Woche 12 verwendet eine flache Map statt Verzeichnisse)
5. Inode-Nummer zurückgeben
```

---

### Aufgabe 4 — FileSystem.writeBlock() implementieren (20 Minuten)

Implementieren Sie `writeBlock(int inodeNum, int blockIndex, byte[] data)`:

```
1. Inode laden
2. Prüfen ob blockIndex < DIRECT_BLOCKS
   (einfach indirekte Zeiger: optionale Erweiterung)
3. Falls direct[blockIndex] == -1:
   a. Neuen Datenblock aus Block Bitmap allozieren
   b. direct[blockIndex] = neue Blocknummer
4. Daten in den Block schreiben
5. Inode aktualisieren: size, mtime
```

---

### Aufgabe 5 — FileSystem.delete() implementieren (15 Minuten)

Implementieren Sie `delete(String name)`:

```
1. name → inodeNumber im Verzeichnis nachschlagen
2. linkCount dekrementieren
3. Falls linkCount == 0:
   a. Alle belegten Datenblöcke freigeben (blockBitmap.free())
   b. Inode freigeben (inodeBitmap.free())
4. Verzeichniseintrag entfernen
```

**Wichtig:** Wenn `linkCount` nach dem Dekrementieren noch > 0 ist, werden die Blöcke **nicht** freigegeben — die Datei lebt unter einem anderen Namen weiter.

---

### Aufgabe 6 — Integration testen (15 Minuten)

Führen Sie `FileSystemDemo.main()` aus. Das Programm:

1. Erstellt drei Dateien
2. Schreibt Daten in eine Datei
3. Löscht eine Datei
4. Gibt den Zustand der Bitmaps aus

**Analysieren Sie die Ausgabe:**

a) Welche Blöcke sind nach dem Löschen wieder frei?

b) Was passiert wenn Sie dieselbe Datei zweimal erstellen?

c) Führen Sie `fsInfo()` aus — welche Informationen enthält der simulierte Superblock?

---

## Denkanstöße und Reflexionsfragen

1. **Fragmentierung**: Wenn viele Dateien erstellt und gelöscht werden, verteilen sich belegte und freie Blöcke über die ganze Disk. Warum ist das bei unserem Simulator kein Problem, auf einer echten HDD aber schon?

2. **Atomizität**: `create()` muss drei Dinge tun — Inode Bitmap setzen, Inode schreiben, Verzeichniseintrag anlegen. Was passiert wenn das Programm nach dem ersten Schritt abstürzt? (Vorschau auf Woche 14)

3. **Inode-Bitmap vs. Block-Bitmap**: Warum brauchen wir zwei separate Bitmaps? Könnten wir mit einer auskommen?

4. **linkCount und Prozesse**: Ein Prozess öffnet eine Datei. Ein anderer Prozess löscht sie (`unlink`). Wann werden die Datenblöcke wirklich freigegeben?

5. **ProcessControlBlock**: In unserem Simulator hält der PCB auch den **aktuellen Arbeitskontext** — welche Datei gerade geöffnet ist, welcher Block zuletzt geschrieben wurde. Wie passt das zum Konzept des File Descriptors aus der Vorlesung?

---

## Verwendete Dateien

| Datei | Beschreibung |
|-------|-------------|
| `Inode.java` | Inode-Datenstruktur (vollständig) |
| `BlockBitmap.java` | Freiraumverwaltung (Lücken) |
| `InodeBitmap.java` | Inode-Freiraumverwaltung (vollständig, analog zu BlockBitmap) |
| `SimpleFileSystem.java` | Dateisystem-Hauptklasse (Lücken) |
| `FileSystemDemo.java` | Demonstrationsprogramm |
| `BlockBitmapTest.java` | Testprogramm für Aufgabe 2 |
| `ProcessControlBlock.java` | PCB — hält geöffnete Datei-Handles |
| `SuperBlock.java` | Superblock-Metadaten (vollständig) |
| `DiskSimulator.java` | Simulierte Disk (vollständig) |

---

## Java-Klassen und Dokumentation

### `Inode`
Repräsentiert die Metadaten einer Datei auf dem simulierten Dateisystem.

| Feld/Methode | Typ | Beschreibung |
|---|---|---|
| `mode` | `int` | Dateityp und Zugriffsrechte (vereinfacht: 0=Datei, 1=Verzeichnis) |
| `size` | `long` | Dateigröße in Bytes |
| `linkCount` | `int` | Anzahl der Verzeichniseinträge die auf diesen Inode zeigen |
| `direct[]` | `int[12]` | Direkte Blockzeiger (-1 = nicht belegt) |
| `singleIndirect` | `int` | Zeiger auf einfach indirekten Block (-1 = nicht belegt) |
| `atime`, `mtime` | `long` | Letzter Zugriff, letzte Änderung (System.currentTimeMillis()) |
| `isAllocated()` | `boolean` | True wenn linkCount > 0 |

### `BlockBitmap`
Verwaltet freie und belegte Blöcke auf der simulierten Disk.

| Methode | Signatur | Beschreibung |
|---|---|---|
| `allocate()` | `int` | Alloziert ersten freien Block, gibt Blocknummer zurück |
| `free(int)` | `void` | Gibt Block frei |
| `isFree(int)` | `boolean` | Prüft ob Block frei ist |
| `freeCount()` | `int` | Anzahl freier Blöcke |
| `toString()` | `String` | Visualisiert Bitmap (0=frei, 1=belegt) |

### `SimpleFileSystem`
Hauptklasse des Dateisystem-Simulators.

| Methode | Signatur | Beschreibung |
|---|---|---|
| `create(String)` | `int` | Erstellt neue Datei, gibt Inode-Nummer zurück |
| `writeBlock(int,int,byte[])` | `void` | Schreibt Datenblock in Datei |
| `readBlock(int,int)` | `byte[]` | Liest Datenblock aus Datei |
| `delete(String)` | `void` | Löscht Datei (dekrementiert linkCount) |
| `fsInfo()` | `void` | Gibt Superblock-Informationen aus |
| `dump()` | `void` | Gibt vollständigen Dateisystem-Zustand aus |

### `ProcessControlBlock`
Repräsentiert einen simulierten Prozess mit Dateisystem-Kontext.

| Feld | Typ | Beschreibung |
|---|---|---|
| `pid` | `int` | Prozess-ID |
| `openFileDescriptors` | `Map<Integer,Integer>` | fd → inodeNumber |
| `currentDirectory` | `int` | Inode des aktuellen Verzeichnisses |
| `open(int)` | `int` | Öffnet Datei, gibt File Descriptor zurück |
| `close(int)` | `void` | Schließt File Descriptor |

---

## Weiterführende Ressourcen

- **OSTEP Kapitel 40**: File System Implementation — sehr anschauliche Erklärung von Inodes, Block Bitmap und Disk-Layout
  https://pages.cs.wisc.edu/~remzi/OSTEP/file-implementation.pdf
- **OSTEP Kapitel 39**: Files and Directories — die API (open/read/write/close)
  https://pages.cs.wisc.edu/~remzi/OSTEP/file-intro.pdf
- **Linux `stat`-Befehl**: `stat dateiname` zeigt alle Inode-Felder einer echten Datei
- **Linux `/proc/self/fdinfo`**: zeigt offene File Descriptors des aktuellen Prozesses
