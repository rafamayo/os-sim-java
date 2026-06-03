# Woche 13 — Verzeichnisse, Links & Pfadauflösung

**Betriebssysteme — Praktikum**
**Hochschule Kempten, Fakultät Informatik**
**Prof. Dr. Rafael Mayoral Malmström — Sommersemester 2026**

---

## Lernziele

Nach dieser Übung können Sie:

- Ein **Verzeichnis** als spezielle Datei (Name → Inode-Nummer) implementieren
- Eine **Pfadauflösung** Schritt für Schritt programmieren
- Den Unterschied zwischen **Hard Links** und **Symbolic Links** praktisch nachvollziehen
- **linkCount** korrekt verwalten und verstehen wann Ressourcen freigegeben werden
- Die **Atomizität von `rename()`** implementieren und erklären

---

## Hintergrund

In Woche 12 haben wir ein flaches Dateisystem gebaut — alle Dateien lagen direkt in einer Map. Echte Dateisysteme verwenden **Verzeichnisse**: spezielle Dateien deren Inhalt eine Liste von Name → Inode-Nummer Paaren ist.

Ein Verzeichnis ist selbst eine Datei: es hat einen Inode, es belegt Datenblöcke, es hat Zugriffsrechte. Das Dateisystem behandelt Verzeichnisse und Dateien fast identisch — der einzige Unterschied ist `mode = MODE_DIRECTORY`.

Die Pfadauflösung traversiert diese Verzeichnis-Kette:

```
/home/user/hallo.txt
  ↓
Root-Dir (Inode #0) → lookup("home") → Inode #5
  ↓
Inode #5 → lookup("user") → Inode #17
  ↓
Inode #17 → lookup("hallo.txt") → Inode #42
```

---

## Aufgaben

### Aufgabe 1 — Directory verstehen (10 Minuten)

Öffnen Sie `Directory.java`. Die Klasse ist vollständig — lesen Sie sie.

**Fragen:**

a) Was sind die Einträge `.` und `..` in jedem Verzeichnis? Welche Inode-Nummern haben sie?

b) Warum hat das Wurzelverzeichnis `/` für beide Einträge dieselbe Inode-Nummer?

c) `lookup("xyz")` gibt `-1` zurück wenn der Name nicht gefunden wird. Welchem Unix-Fehlercode entspricht das?

d) Ein Verzeichnis hat `linkCount = 3`. Wieviele Unterverzeichnisse hat es mindestens? *(Hinweis: Denken Sie an `..`)*

---

### Aufgabe 2 — resolve() implementieren (20 Minuten)

`resolve()` ist die **Grundlage aller weiteren Methoden** — `mkdir()`,
`hardLink()` und `rename()` rufen alle intern `resolve()` auf.
Implementieren Sie sie zuerst.

Implementieren Sie `resolve(String path)` in `DirectoryFileSystem.java`:

```java
// Beispiel: resolve("/home/user/hallo.txt") → Inode #42
public int resolve(String path) {
    // Sonderfall: path.equals("/") → return ROOT_INODE
    // Pfad bei "/" aufteilen: String[] parts = path.split("/")
    // int currentIno = ROOT_INODE
    // Für jede nicht-leere Komponente:
    //   - Verzeichnis laden: Directory dir = getDirectory(currentIno)
    //   - lookup(komponente) aufrufen → nextIno
    //   - Falls nextIno == -1: FileNotFoundException werfen
    //   - currentIno = nextIno
    // return currentIno
}
```

**Testen Sie sofort mit `DirectoryDemo` — Abschnitt "Aufgabe 2":**
```
resolve("/")                   → Inode #0
resolve("/home")               → Inode #1  (nach mkdir)
resolve("/home/user")          → Inode #2
resolve("/nicht/vorhanden")    → FileNotFoundException ✓
```

---

### Aufgabe 3 — DirectoryFileSystem.mkdir() implementieren (15 Minuten)

`mkdir()` baut auf `resolve()` auf — erst wenn Aufgabe 2 funktioniert,
kann diese Methode getestet werden.

Öffnen Sie `DirectoryFileSystem.java` und implementieren Sie `mkdir(String path)`:

```
1. Pfad parsen: parentPath + dirName trennen
   Beispiel: "/home/user" → parent="/home", name="user"
2. Eltern-Verzeichnis über resolve(parentPath) finden
3. Freien Inode allozieren (inodeBitmap.allocate())
4. Inode als Verzeichnis initialisieren (MODE_DIRECTORY)
5. Directory-Objekt erstellen mit "." und ".." Einträgen
6. Eintrag im Eltern-Verzeichnis anlegen
7. linkCount des Eltern-Verzeichnisses erhöhen (wegen "..")
```

**Testen Sie mit `DirectoryDemo` — Abschnitt "Aufgabe 3":**
Bauen Sie die Struktur `/home/user/` mit Dateien auf und
prüfen Sie den `tree()`-Output.

---

### Aufgabe 4 — Hard Link implementieren (15 Minuten)

*(Voraussetzung: Aufgabe 2 resolve() funktioniert)*

Implementieren Sie `hardLink(String existingPath, String newPath)`:

```
1. Quell-Datei über resolve(existingPath) finden → inodeNum
2. linkCount des Inodes erhöhen: inode.linkCount++
3. Neuen Verzeichniseintrag im Ziel-Verzeichnis anlegen
   (Ziel-Verzeichnis = Elternteil von newPath)
```

**Test:** Nach `hardLink("/daten.csv", "/backup/daten.csv")`:
- Beide Pfade zeigen auf denselben Inode
- linkCount ist 2
- `delete("/daten.csv")` löscht nur den Namen, nicht die Daten

---

### Aufgabe 5 — rename() implementieren (15 Minuten)

*(Voraussetzung: Aufgaben 2 und 3 funktionieren)*

Implementieren Sie `rename(String oldPath, String newPath)`:

```
1. Alten Inode über resolve(oldPath) finden
2. Neuen Verzeichniseintrag anlegen (im Ziel-Verzeichnis)
3. Alten Verzeichniseintrag entfernen
   (BEIDE Schritte — in Woche 14 werden sie atomar)
4. linkCount bleibt unverändert
```

**Wichtig:** Die Reihenfolge ist relevant. Warum wird zuerst der neue Eintrag angelegt und dann der alte entfernt?

---

### Aufgabe 6 — Integration testen (15 Minuten)

Führen Sie `DirectoryDemo.main()` vollständig aus. Das Programm
testet alle implementierten Methoden in Sequenz und zeigt
den vollständigen Baum:

```
/
├── home/
│   └── user/
│       ├── readme.txt   (Inode #4)
│       └── daten.csv    (Inode #5)
└── tmp/
    └── work.tmp         (Inode #6)
```

a) Führen Sie `DirectoryDemo.main()` aus und prüfen Sie ob alle ✓ erscheinen.

b) Rufen Sie `rename("/tmp/work.tmp", "/home/user/work.txt")` auf. Was passiert mit dem Inode?

c) Erstellen Sie einen Hard Link: `hardLink("/home/user/daten.csv", "/home/user/backup.csv")`. Überprüfen Sie `linkCount`.

d) Löschen Sie `/home/user/daten.csv`. Ist `backup.csv` noch erreichbar?

---

## Denkanstöße und Reflexionsfragen

1. **Verzeichnis ist eine Datei**: Ein Verzeichnis belegt Datenblöcke genau wie eine reguläre Datei. Was passiert wenn ein Verzeichnis so viele Einträge hat dass ein Block nicht reicht?

2. **rename() und laufende Prozesse**: Prozess A hat `/tmp/work.tmp` geöffnet. Prozess B ruft `rename("/tmp/work.tmp", "/home/user/work.txt")` auf. Kann Prozess A noch schreiben? Warum?

3. **Symbolic Links**: Unser Simulator hat keine Symlinks. Was müsste man hinzufügen? (Hinweis: Symlink = spezielle Datei deren Inhalt ein Pfadstring ist)

4. **Zyklische Verzeichnisse**: Warum dürfen Hard Links nicht auf Verzeichnisse zeigen? Was würde passieren?

5. **ProcessControlBlock und Verzeichnisse**: Der PCB hat ein `currentDirectory`-Feld. Was entspricht dem Unix-Befehl `cd` in unserem Simulator?

---

## Verwendete Dateien

| Datei | Beschreibung |
|-------|-------------|
| `Directory.java` | Verzeichnis-Datenstruktur (vollständig) |
| `DirEntry.java` | Ein Verzeichniseintrag: name + inodeNumber (vollständig) |
| `DirectoryFileSystem.java` | Erweitertes Dateisystem mit Verzeichnissen (Lücken) |
| `DirectoryDemo.java` | Demonstrationsprogramm |
| `ProcessControlBlock.java` | PCB mit currentDirectory (aus Woche 12) |
| + alle Dateien aus Woche 12 | Inode, BlockBitmap, etc. |

---

## Java-Klassen und Dokumentation

### `Directory`

| Methode | Signatur | Beschreibung |
|---|---|---|
| `Directory(int selfIno, int parentIno)` | Konstruktor | Erstellt Dir mit `.` und `..` |
| `lookup(String name)` | `int` | Name → Inode-Nummer (-1 wenn nicht gefunden) |
| `addEntry(String, int)` | `void` | Eintrag hinzufügen |
| `removeEntry(String)` | `void` | Eintrag entfernen (ENOENT wenn nicht gefunden) |
| `entries()` | `List<DirEntry>` | Alle Einträge |
| `size()` | `int` | Anzahl Einträge |

### `DirectoryFileSystem`

| Methode | Signatur | Beschreibung |
|---|---|---|
| `mkdir(String path)` | `int` | Verzeichnis erstellen, gibt Inode zurück |
| `resolve(String path)` | `int` | Pfad → Inode-Nummer |
| `createFile(String path)` | `int` | Datei im angegebenen Pfad erstellen |
| `hardLink(String, String)` | `void` | Hard Link erstellen |
| `rename(String, String)` | `void` | Datei umbenennen/verschieben |
| `listDir(String path)` | `void` | Verzeichnisinhalt ausgeben |
| `tree()` | `void` | Gesamtes Dateisystem als Baum ausgeben |

---

## Weiterführende Ressourcen

- **OSTEP Kapitel 39**: Files and Directories — Verzeichnisse, Links, rename()
  https://pages.cs.wisc.edu/~remzi/OSTEP/file-intro.pdf
- **Linux `ln`-Befehl**: `ln datei hardlink` und `ln -s datei symlink`
- **Linux `stat`**: zeigt linkCount (`Nlinks`) einer Datei
- **Linux `strace`**: `strace ls /home` zeigt alle Systemaufrufe bei Verzeichnislisting