# Woche 08 — Speicherverwaltung: Zusammenhängende Allokation & Buddy-System

## Lernziele

Nach dieser Übung können Sie:
- First-Fit, Best-Fit und Worst-Fit als Platzierungsstrategien implementieren und vergleichen
- Externe Fragmentierung erkennen, messen und erklären
- Coalescing beim Freigeben von Speicher implementieren
- Den Buddy-Allocator (Allokation und Freigabe mit Coalescing) implementieren

---

## Struktur

```
woche08/
├── AllocationStrategy.java     # Enum: FIRST_FIT, BEST_FIT, WORST_FIT
├── MemoryBlock.java            # Repräsentiert einen Speicherblock (frei oder belegt)
├── ProcessControlBlock.java    # PCB (unverändert gegenüber Scheduling-Wochen)
├── ProcessState.java           # Prozesszustände
├── ContiguousMemoryManager.java # ← HIER arbeiten Sie (Aufgaben 1–3)
├── BuddyAllocator.java         # ← HIER arbeiten Sie (Aufgabe 4)
└── Main.java                   # Testszenarien (bereits vollständig)
```

---

## Aufgaben

Jede Aufgabe hat eine eigene Testdatei, die sofort ausgeführt werden kann sobald
die zugehörigen Methoden implementiert sind. `Main.java` führt am Ende einen
Gesamtvergleich durch und setzt alle Aufgaben voraus.

| Aufgabe | Implementieren in | Testen mit |
| --- | --- | --- |
| 1 | `ContiguousMemoryManager.java` | `MainAufgabe1.java` |
| 2 | `ContiguousMemoryManager.java` | `MainAufgabe2.java` |
| 3 | `ContiguousMemoryManager.java` | `MainAufgabe3.java` |
| 4 | `BuddyAllocator.java` | `MainAufgabe4.java` |
| Gesamt | — | `Main.java` |

---

### Aufgabe 1 — Platzierungsstrategien (`ContiguousMemoryManager.java`)

Implementieren Sie die drei privaten Methoden:

**`firstFit(int size)`**  
Gibt den ersten freien Block zurück, der mindestens `size` Bytes groß ist.

+ 💡 **Bereits im Code implementiert als Vorbild!**

**`bestFit(int size)`**  
Gibt den kleinsten freien Block zurück, der mindestens `size` Bytes groß ist.

**`worstFit(int size)`**  
Gibt den größten freien Block zurück, der mindestens `size` Bytes groß ist.

Jede Methode durchläuft die `blocks`-Liste. Ein Block ist passend wenn
`b.isFree() && b.getSize() >= size`.

---

### Aufgabe 2 — Freigabe und Coalescing (`ContiguousMemoryManager.java`)

**`free(int address)`**  
Sucht den belegten Block, der an `address` beginnt, markiert ihn als frei
(`setFree(true)`, `setPid(-1)`) und ruft `coalesce()` auf.

**`coalesce(MemoryBlock block)`**  
Führt angrenzende freie Blöcke zusammen:
- **Rechter Nachbar** (`idx + 1`): falls frei → Größe addieren, Nachbar aus Liste entfernen
- **Linker Nachbar** (`idx - 1`): falls frei → Größe des linken Blocks addieren, aktuellen Block entfernen

> **Achtung:** Nach dem Zusammenführen mit dem rechten Nachbarn verändert sich
> der Index des linken Nachbarn nicht.

---

### Aufgabe 3 — Fragmentierungsmetriken (`ContiguousMemoryManager.java`)

**`externalFragmentation()`**  
```
EF = 1 – (größter freier Block / gesamter freier Speicher)
```
Gibt `0.0` zurück wenn kein freier Speicher vorhanden ist.

**`numberOfHoles()`**  
Anzahl der freien Blöcke in der Liste.

**`utilization()`**  
```
Auslastung = belegter Speicher / totalSize
```

---

### Aufgabe 4 — Buddy-Allocator (`BuddyAllocator.java`)

**`allocate(int size)`** — Algorithmus:
1. Ordnung berechnen: `k = ceilLog2(size)` (bereits implementiert)
2. Suche die kleinste Ordnung `j >= k` mit einem freien Block in `freeLists.get(j)`
3. Splitten bis Ordnung `k`: solange `j > k`:
   - Block aus `freeLists.get(j)` entnehmen
   - `j--`
   - Buddy-Adresse: `buddy = block + (1 << j)`
   - Beide Hälften in `freeLists.get(j)` eintragen
   - `totalSplits++`
4. Block aus `freeLists.get(k)` entnehmen, `allocated[block] = k`

**`free(int address)`** — Algorithmus:
1. Ordnung `k = allocated[address]` lesen; falls `k < 0`: Fehler
2. Block freigeben: `allocated[address] = -1`, in `freeLists.get(k)` eintragen
3. Coalescing-Schleife (solange `k < maxOrder`):
   - `buddy = buddyAddress(address, k)` (bereits implementiert: `address XOR 2^k`)
   - Falls Buddy **nicht** in `freeLists.get(k)`: Abbruch
   - Beide entfernen, `address = Math.min(address, buddy)`, `k++`
   - Zusammengeführten Block in `freeLists.get(k)` eintragen

---

## Kompilieren und Ausführen

```bash
# Alle Dateien im gleichen Verzeichnis kompilieren
javac woche08/*.java

# Nach Aufgabe 1:
java woche08.MainAufgabe1

# Nach Aufgabe 2:
java woche08.MainAufgabe2

# Nach Aufgabe 3:
java woche08.MainAufgabe3

# Nach Aufgabe 4:
java woche08.MainAufgabe4

# Gesamtvergleich (alle Aufgaben vollständig):
java woche08.Main
```

---

## Erwartete Ausgabe (Auszug)

Nach korrekter Implementierung zeigt `Main` für jede Strategie eine Speicherkarte wie:

```
=== Memory Map (FIRST_FIT) ===
  [   0 –  199 | size= 200 | PID=1  ]
  [ 200 –  349 | size= 150 | PID=2  ]
  [ 350 –  649 | size= 300 | PID=3  ]
  [ 650 –  749 | size= 100 | PID=4  ]
  [ 750 – 1023 | size= 274 | FREE  ]
```

Und nach Freigabe von P2 und P4:

```
  [   0 –  199 | size= 200 | PID=1  ]
  [ 200 –  349 | size= 150 | FREE  ]
  [ 350 –  649 | size= 300 | PID=3  ]
  [ 650 –  749 | size= 100 | FREE  ]
  [ 750 – 1023 | size= 274 | FREE  ]
  Ext. Fragm.:    0.577
```

Der Fragmentierungsvergleich am Ende zeigt, dass First-Fit und Best-Fit in
der Regel geringere externe Fragmentierung als Worst-Fit aufweisen.

---

## Diskussion

- Warum erzeugt Best-Fit trotz kleinster Restfragmente häufig viele unbrauchbare Löcher?
- Wann lohnt sich Coalescing sofort (immediate), wann deferred?
- Welche interne Fragmentierung entsteht beim Buddy-Allocator, wenn Sie 100 Bytes anfordern?
- Warum wird Kompaktierung in echten Betriebssystemen selten eingesetzt?

---

## 📂 Dateien in diesem Verzeichnis

| Datei | Beschreibung |
| --- | --- |
| `ProcessState.java` | Prozesszustände (`NEW`, `READY`, `RUNNING`, `BLOCKED`, `TERMINATED`). |
| `ProcessControlBlock.java` | PCB mit Scheduling-Attributen (unverändert gegenüber früheren Wochen). |
| `AllocationStrategy.java` | Enum für die drei Platzierungsstrategien: `FIRST_FIT`, `BEST_FIT`, `WORST_FIT`. |
| `MemoryBlock.java` | Repräsentiert einen zusammenhängenden Speicherblock (frei oder belegt) mit Start, Größe und Besitzer-PID. |
| `ContiguousMemoryManager.java` | Gerüst **(Aufgaben 1–3)**: verwaltet eine geordnete Freiliste und implementiert die drei Platzierungsstrategien, Coalescing und Fragmentierungsmetriken. |
| `BuddyAllocator.java` | Gerüst **(Aufgabe 4)**: Buddy-Allocator mit Freilisten pro Ordnung, Splitting und Coalescing. |
| `MainAufgabe1.java` | Testet ausschließlich die Platzierungsstrategien (Aufgabe 1). |
| `MainAufgabe2.java` | Testet Freigabe und Coalescing sowie den ersten Strategievergleich (Aufgabe 2). |
| `MainAufgabe3.java` | Testet die Fragmentierungsmetriken schrittweise und im Strategievergleich (Aufgabe 3). |
| `MainAufgabe4.java` | Testet den Buddy-Allocator isoliert: Splitting, Coalescing, interne Fragmentierung (Aufgabe 4). |
| `Main.java` | Gesamttest **(nach Abschluss aller Aufgaben)**: drei Strategievergleiche, Buddy-Demo und Fragmentierungsvergleich. |

---

## 📚 Verwendete Java-Klassen und ihre Dokumentation

| Klasse | Zweck |
| --- | --- |
| **[`ArrayList<E>`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/ArrayList.html)** | Dynamisches Array für die geordnete Freiliste in `ContiguousMemoryManager`. Ermöglicht effizientes Einfügen und Entfernen von Blöcken per Index. |
| **[`List<E>`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/List.html)** | Interface für geordnete Listen. Wird als Typ für `blocks` und `freeLists` verwendet. |
| **[`ArrayDeque<E>`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/ArrayDeque.html)** | Effiziente Deque-Implementierung. Wird pro Ordnung als Freiliste im `BuddyAllocator` verwendet (`freeLists.get(k)`). |
| **[`Deque<E>`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Deque.html)** | Interface für doppelt verkettete Warteschlangen. Ermöglicht `addFirst()`, `removeFirst()` und `contains()` im Buddy-Allocator. |
| **[`Arrays`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Arrays.html)** | Hilfsklasse für Array-Operationen. Wird im `BuddyAllocator` für `Arrays.fill(allocated, -1)` verwendet. |
| **[`Integer.numberOfLeadingZeros(int)`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Integer.html#numberOfLeadingZeros%28int%29)** | Zählt die Anzahl führender Nullbits. Wird in `ceilLog2()` verwendet, um die benötigte Buddy-Ordnung zu berechnen. |

---

## 🔗 Weiterführende Ressourcen

### Grundlagen der Speicherverwaltung

* **OSTEP — Free-Space Management** *(empfohlene Pflichtlektüre)*
  <https://pages.cs.wisc.edu/~remzi/OSTEP/vm-freespace.pdf>
  Erklärt First-Fit, Best-Fit, Worst-Fit, Coalescing und den Buddy-Allocator anschaulich mit Beispielen.

* **OSTEP — Memory API**
  <https://pages.cs.wisc.edu/~remzi/OSTEP/vm-api.pdf>
  Hintergrund zu `malloc` und `free` aus Betriebssystemperspektive.

### Fragmentierung und Allokationsstrategien

* **Memory Fragmentation (Wikipedia)**
  <https://en.wikipedia.org/wiki/Fragmentation_(computing)>
  Erläutert interne und externe Fragmentierung mit Beispielen.

* **Buddy Memory Allocation (Wikipedia)**
  <https://en.wikipedia.org/wiki/Buddy_memory_allocation>
  Beschreibt das Buddy-System mit Diagrammen zu Splitting und Coalescing.

### Praxisbezug: echte Allokatoren

* **jemalloc — A General Purpose malloc Implementation**
  <https://jemalloc.net/>
  Einer der meistgenutzten Allokatoren in der Praxis (Firefox, FreeBSD); zeigt wie reale Systeme Fragmentierung minimieren.

* **ptmalloc / glibc malloc**
  <https://sourceware.org/glibc/wiki/MallocInternals>
  Dokumentation des Standard-Linux-Allokators: Bins, Chunks, Coalescing in der Praxis.

### Weiterführende Java-Klassen

In der Praxis werden Speicherverwaltungsaufgaben in Java oft mit höherwertigen Abstraktionen gelöst:

* **[`java.nio.ByteBuffer`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/ByteBuffer.html)**
  Direkte Pufferverwaltung in Java; relevant wenn physisch zusammenhängende Speicherbereiche benötigt werden.

* **[`sun.misc.Unsafe`](https://docs.oracle.com/en/java/javase/17/docs/api/jdk.unsupported/sun/misc/Unsafe.html)**
  Ermöglicht direkte Speicherzugriffe in Java — nur für fortgeschrittene Anwendungsfälle und nicht für Produktionscode empfohlen.

---

> **Hinweis:** In dieser Übung implementieren wir Allokationsstrategien von Grund auf,
> um die Mechanismen zu verstehen. In der Praxis verwendet man fertige Allokatoren
> (wie `jemalloc` oder den JVM-eigenen Heap-Manager), die jahrelang optimiert wurden.
