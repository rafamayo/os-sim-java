# Woche 11 — Seitenersetzungsalgorithmen: FIFO, LRU, Clock, OPT

## Lernziele

Nach dieser Übung können Sie:
- FIFO, LRU und Clock als Seitenersetzungsalgorithmen implementieren
- OPT (Belady) als theoretisches Optimum verstehen und als Baseline nutzen
- Page-Fault-Raten messen und Algorithmen vergleichen
- Beladys Anomalie bei FIFO beobachten und erklären
- Den Einfluss von Rahmenzahl und Zugriffsmuster auf die Fault-Rate analysieren

## Bezug zu früheren Wochen

In Woche 09 wurde Out-of-Memory als offenes Problem gezeigt.
In Woche 10 sorgte der TLB für schnellere Übersetzung, löste aber
das Grundproblem nicht: wenn kein freier Frame mehr vorhanden ist,
muss eine vorhandene Seite verdrängt werden.
Diese Woche beantwortet die Frage: **welche Seite?**

Die `dirty`- und `referenced`-Bits im `PageTableEntry` aus Woche 09
sind die Hardware-Grundlage für den Clock-Algorithmus.

---

## Aufgaben

| Aufgabe | Implementieren in | Testen mit |
| --- | --- | --- |
| 1 | `FIFOReplacer.java` | `MainAufgabe1.java` |
| 2 | `LRUReplacer.java` | `MainAufgabe2.java` |
| 3 | `ClockReplacer.java` | `MainAufgabe3.java` |
| Gesamt | — | `Main.java` |

> `OPTReplacer.java` und `ReplacementSimulator.java` sind bereits vollständig
> implementiert und können von Anfang an verwendet werden.

---

### Aufgabe 1 — FIFO (`FIFOReplacer.java`)

**Algorithmus:**
```
1. Hit-Check: page bereits in frames[]? → return 0

2. Page Fault: pageFaults++

3. Freier Frame (frames[i] == -1)?
     frames[i] = page, order.add(page), return 1

4. Verdrängen:
     victim = order.poll()           ← älteste Seite
     frame des victim überschreiben: frames[i] = page
     order.add(page)                 ← neue Seite hinten einreihen
     return 1
```

---

### Aufgabe 2 — LRU (`LRUReplacer.java`)

**Algorithmus:**
```
1. Hit-Check: frames[i] == page?
     lastUsed[i] = ++clock, return 0

2. Page Fault: pageFaults++

3. Freier Frame?
     frames[i] = page, lastUsed[i] = ++clock, return 1

4. LRU-Opfer: Frame mit kleinstem lastUsed-Wert
     frames[lru] = page, lastUsed[lru] = ++clock, return 1
```

---

### Aufgabe 3 — Clock (`ClockReplacer.java`)

**Algorithmus:**
```
1. Hit-Check: frames[i] == page?
     referenced[i] = true, return 0

2. Page Fault: pageFaults++

3. Freier Frame (frames[hand] == -1)?
     frames[hand] = page, referenced[hand] = true
     hand = (hand+1) % frames.length, return 1

4. Clock-Schleife: solange referenced[hand] == true:
     referenced[hand] = false       ← zweite Chance vergeben
     hand = (hand+1) % frames.length
   Verdrängen: frames[hand] = page, referenced[hand] = true
     hand = (hand+1) % frames.length, return 1
```

**Visualisierung:**
```
Frames:  [ 1* | 2* | 3* ]     (* = referenced=true)
Zugriff auf neue Seite 4:

Zeiger → Frame 0: referenced=true  → löschen, weiter
Zeiger → Frame 1: referenced=true  → löschen, weiter
Zeiger → Frame 2: referenced=true  → löschen, weiter
Zeiger → Frame 0: referenced=false → verdrängen! → Frame 0 = 4
```

---

## Kompilieren und Ausführen

```bash
javac woche11/*.java

# Nach Aufgabe 1:
java woche11.MainAufgabe1

# Nach Aufgabe 2:
java woche11.MainAufgabe2

# Nach Aufgabe 3:
java woche11.MainAufgabe3

# Gesamtvergleich (alle Aufgaben):
java woche11.Main
```

---

## Erwartete Ausgabe (Auszug)

**MainAufgabe1** — FIFO mit 3 Frames:
```
Seite    1   2   3   4   1   2   5   1   2   3   4   5
  -------------------------------------------------------
F0       1   1   1   4   4   4   5   5   5   5   4   4
F1       .   2   2   2   1   1   1   1   1   1   1   5
F2       .   .   3   3   3   2   2   2   2   3   3   3
Fault    *   *   *   *   *   *   *               *   *
Page Faults: 9 / 12
```

**Main** — Gesamtvergleich:
```
Algorithmus  Page Faults  Rate
------------------------------------
FIFO         9            75.0%
LRU          8            66.7%
Clock        8            66.7%
OPT          6            50.0%
```

---

## Denkanstöße

- Warum tritt Beladys Anomalie bei FIFO auf, aber nicht bei LRU und OPT?
- Was ist der Unterschied zwischen einem "Stack-Algorithmus" und anderen?
- Clock ist eine Approximation von LRU — wann liefert er dieselben Ergebnisse?
- Warum ist OPT in der Praxis nicht einsetzbar?
- Bei welchem Zugriffsmuster versagt LRU besonders? *(Hinweis: sequenzieller Scan)*

---

## 📂 Dateien in diesem Verzeichnis

| Datei | Beschreibung |
| --- | --- |
| `ProcessState.java` | Prozesszustände (aus früheren Wochen, Referenz). |
| `PageTableEntry.java` | PTE mit `dirty` und `referenced` (aus Woche 09, Referenz). |
| `ProcessControlBlock.java` | PCB (aus Woche 09, Referenz). |
| `PhysicalMemory.java` | Physischer Speicher (aus Woche 09, Referenz). |
| `PageFaultException.java` | Exception für fehlende Seiten (aus Woche 09, Referenz). |
| `PageReplacer.java` | Interface: `access(page)`, `getPageFaults()`, `getFrames()`, `name()`. |
| `FIFOReplacer.java` | Gerüst **(Aufgabe 1)**: FIFO mit Queue. |
| `LRUReplacer.java` | Gerüst **(Aufgabe 2)**: LRU mit Zeitstempeln. |
| `ClockReplacer.java` | Gerüst **(Aufgabe 3)**: Clock mit Reference-Bits und zirkulärem Zeiger. |
| `OPTReplacer.java` | **Vollständig implementiert** (Referenz/Baseline): OPT/Belady mit Vorausschau. |
| `ReplacementSimulator.java` | **Vollständig implementiert**: Trace-Ausgabe, Algorithmen-Vergleich, Fault-vs-Frames-Kurven. |
| `MainAufgabe1.java` | Testet FIFO: Schritt-für-Schritt-Trace und Beladys Anomalie (Aufgabe 1). |
| `MainAufgabe2.java` | Testet LRU: Vergleich mit FIFO, kein Belady (Aufgabe 2). |
| `MainAufgabe3.java` | Testet Clock: Reference-Bits und zweite Chance (Aufgabe 3). |
| `Main.java` | Gesamtvergleich **(alle Aufgaben)**: 4 Szenarien, Fault-vs-Frames, OPT-Baseline. |

---

## 📚 Verwendete Java-Klassen und ihre Dokumentation

| Klasse | Zweck |
| --- | --- |
| **[`LinkedList<E>`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/LinkedList.html)** | Implementiert `Queue<Integer>` im `FIFOReplacer`: `add()` hinten, `poll()` vorne. |
| **[`Queue<E>`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Queue.html)** | Interface für FIFO-Warteschlangen. `poll()` entnimmt das älteste Element. |
| **[`List<E>`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/List.html)** | Referenzfolge als `List<Integer>` — einfache Iteration und Index-Zugriff für OPT. |
| **[`Arrays`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Arrays.html)** | `Arrays.fill(frames, -1)` initialisiert alle Frames als frei. |
| **[`Integer.MAX_VALUE`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Integer.html#MAX_VALUE)** | Sentinel-Wert in `OPTReplacer`: Seite nie wieder benutzt → optimaler Kandidat für Verdrängung. |

---

## 🔗 Weiterführende Ressourcen

### Seitenersetzungsalgorithmen

* **OSTEP — Beyond Physical Memory: Policies** *(empfohlene Pflichtlektüre)*
  <https://pages.cs.wisc.edu/~remzi/OSTEP/vm-beyondphys-policy.pdf>
  Erklärt FIFO, LRU, Clock, OPT und Thrashing mit Beispielen.

* **OSTEP — Beyond Physical Memory: Mechanisms**
  <https://pages.cs.wisc.edu/~remzi/OSTEP/vm-beyondphys.pdf>
  Hintergrund zu Swap-Space und Page Fault Handling.

### Konzepte

* **Page Replacement Algorithms (Wikipedia)**
  <https://en.wikipedia.org/wiki/Page_replacement_algorithm>
  Überblick über alle gängigen Algorithmen inkl. Working-Set-Modell.

* **Beladys Anomalie (Wikipedia)**
  <https://en.wikipedia.org/wiki/B%C3%A9l%C3%A1dy%27s_anomaly>
  Formale Erklärung und Beweis warum FIFO kein Stack-Algorithmus ist.

* **Thrashing (Wikipedia)**
  <https://en.wikipedia.org/wiki/Thrashing_(computer_science)>
  Was passiert wenn das Working Set die verfügbaren Frames übersteigt.

### Praxisbezug

* **Linux Kernel — mm/vmscan.c**
  <https://elixir.bootlin.com/linux/latest/source/mm/vmscan.c>
  Der Linux Page-Reclaim-Code: eine Variante des Clock-Algorithmus (LRU-Listen).

* **Linux — The Linux VM subsystem**
  <https://www.kernel.org/doc/html/latest/admin-guide/mm/concepts.html>
  Konzepte der Linux-Speicherverwaltung: Anonymous Pages, File-backed Pages, Swap.

---

> **Hinweis:** In dieser Übung simulieren wir Seitenersetzung deterministisch
> mit abstrakten Seitennummern. In einem echten System arbeitet der Ersetzungsalgorithmus
> auf physischen Frames, kennt dirty/referenced-Bits aus der Hardware und muss
> Schreibzugriffe auf verdrängte dirty Pages auf den Swap-Speicher berücksichtigen.
