# Woche 09 — Paged Memory: Adressübersetzung & Page Fault Handling

## Lernziele

Nach dieser Übung können Sie:
- Die Struktur eines Page Table Entry (PTE) erklären und verwenden
- Eine virtuelle Adresse in eine physische Adresse übersetzen (VA → PA)
- Die Schritte einer Adressübersetzung mit einstufiger Seitentabelle durchführen
- Einen Page Fault Handler implementieren (Demand Paging)
- Schutzverletzungen erkennen und behandeln
- Den `ProcessControlBlock` um eine Seitentabelle erweitern

## Bezug zu Woche 08

In Woche 08 haben Sie physischen Speicher als zusammenhängenden Block verwaltet.
Ab dieser Woche trennen wir **virtuelle Adressen** (Sicht des Prozesses) von
**physischen Adressen** (realer RAM). Prozesse sehen einen zusammenhängenden
virtuellen Adressraum, der intern auf beliebige physische Frames verteilt ist.

---

## Struktur

```
woche09/
├── ProcessState.java           # Prozesszustände (unverändert)
├── PageTableEntry.java         # PTE mit valid, frame, dirty, referenced, protection
├── ProcessControlBlock.java    # PCB (neu: pageTable, numPages, pageSize)
├── PhysicalMemory.java         # Physischer Speicher als Frame-Array
├── PageFaultException.java     # Exception für fehlende Seiten
├── MemoryManagementUnit.java   # ← HIER arbeiten Sie (Aufgaben 1–2)
└── Main.java                   # 5 Testszenarien (bereits vollständig)
```

### Neu im PCB

```java
pcb.initPageTable(numPages, pageSize);   // Seitentabelle anlegen
pcb.getPTE(vpn)                          // PTE für eine Seitennummer holen
pcb.setPTE(vpn, pte)                     // PTE setzen
pcb.virtualAddressSpaceSize()            // Größe des virtuellen Adressraums
```

---

## Aufgaben

### Aufgabe 1 — Adressübersetzung (`MemoryManagementUnit.java`)

Implementieren Sie `translate(ProcessControlBlock pcb, int va, boolean write)`.

**Algorithmus (einstufige Seitentabelle):**

```
1. Validierung:  falls va < 0 oder va >= virtualAddressSpaceSize → Exception

2. Dekomposition der virtuellen Adresse:
     pageSize = physMem.getPageSize()
     vpn      = va / pageSize          ← Virtual Page Number
     offset   = va % pageSize          ← Offset innerhalb der Seite

3. Statistik: totalTranslations++

4. PTE holen: pte = pcb.getPTE(vpn)
   Falls pte == null oder !pte.isValid():
     → throw new PageFaultException(pcb.getPid(), vpn, va)

5. Protection-Check (nur bei Schreibzugriff):
   Falls write && !pte.canWrite():
     totalProtectionFaults++
     → throw new IllegalArgumentException(...)

6. Bits aktualisieren:
     pte.setReferenced(true)
     Falls write: pte.setDirty(true)

7. Physische Adresse:
     return physMem.physicalAddress(pte.getFrameNumber(), offset)
```

**Visualisierung:**

```
Virtuelle Adresse (va = 200, pageSize = 64):
┌─────────────────────────────┐
│  VPN = 200 / 64 = 3         │
│  Offset = 200 % 64 = 8      │
└─────────────────────────────┘
          │
          ▼
┌─────────────────────────────┐
│  Seitentabelle (PCB)        │
│  VPN 3 → PTE[frame=5, R/W]  │
└─────────────────────────────┘
          │
          ▼
  PA = 5 × 64 + 8 = 328
```

---

### Aufgabe 2 — Page Fault Handler (`MemoryManagementUnit.java`)

Implementieren Sie `handlePageFault(ProcessControlBlock pcb, PageFaultException fault)`.

**Algorithmus:**

```
1. Freien Frame anfordern:
     frame = physMem.allocateFrame(pcb.getPid())

2. Falls frame == -1:
     → throw new IllegalStateException("Out of Memory: kein freier Frame für PID=...")

3. Neuen PTE anlegen und eintragen:
     newPte = new PageTableEntry(frame, READ | WRITE)
     pcb.setPTE(fault.getVpn(), newPte)

4. totalPageFaults++

5. Ausgabe (zur Nachverfolgung):
     System.out.printf("  [Page Fault] PID=%d VPN=%d → Frame=%d ...%n", ...)
```

**Ablauf bei einem Demand-Paging-Zugriff:**

```
translateWithFaultHandling(pcb, va=80, write=false)
  └─► translate() → PTE invalid → PageFaultException
  └─► handlePageFault() → Frame 3 zugewiesen, PTE gesetzt
  └─► translate() → PTE jetzt valid → PA = 3×64 + 16 = 208 ✓
```

---

## Kompilieren und Ausführen

```bash
javac woche09/*.java
java woche09.Main
```

---

## Erwartete Ausgabe (Auszug)

**Szenario 1** (fertige Seitentabelle):
```
  Adressübersetzungen:
    VA   0  →  PA 128   (VPN=0, Offset=0)
    VA  63  →  PA 191   (VPN=0, Offset=63)
    VA  64  →  PA 320   (VPN=1, Offset=0)
```

**Szenario 2** (Demand Paging):
```
  [Page Fault] PID=2 VPN=0 → Frame=0 (PA=0–63)
    VA  10  →  PA  10
  [Page Fault] PID=2 VPN=1 → Frame=1 (PA=64–127)
    VA  80  →  PA  80
```

**Szenario 5** (Out of Memory):
```
  → Out of Memory: kein freier Frame für PID=6
  → In Woche 11: Seitenersetzungsalgorithmus nötig!
```

---

## Diskussion

- Warum zeigen zwei Prozesse mit VA=0 auf unterschiedliche physische Adressen?
- Was passiert mit `dirty` und `referenced`, wenn eine Seite nur gelesen wird?
- Warum kann Demand Paging den Programmstart beschleunigen?
- Was fehlt noch, damit das System bei vollem Speicher weiterarbeiten kann?
  *(Antwort: Seitenersetzung — kommt in Woche 11)*
- Wofür werden `dirty` und `referenced` in Woche 10/11 benötigt?

---

## Verbindung zu Woche 10

In Woche 10 kommt der **TLB** (Translation Lookaside Buffer) dazu:
ein kleiner Cache, der häufig verwendete VA→PA-Übersetzungen speichert.
Die `dirty`- und `referenced`-Bits im PTE, die Sie hier bereits setzen,
werden für den **Clock-Algorithmus** in Woche 11 verwendet.

---

## 📂 Dateien in diesem Verzeichnis

| Datei | Beschreibung |
| --- | --- |
| `ProcessState.java` | Prozesszustände (`NEW`, `READY`, `RUNNING`, `BLOCKED`, `TERMINATED`). |
| `PageTableEntry.java` | Ein Eintrag der Seitentabelle: `valid`, `frameNumber`, `dirty`, `referenced`, `protection`. Enthält Konstanten `READ`, `WRITE`, `EXEC`. |
| `ProcessControlBlock.java` | Erweiterter PCB: enthält zusätzlich eine einstufige Seitentabelle (`pageTable`), `numPages` und `pageSize`. |
| `PhysicalMemory.java` | Physischer Speicher als Frame-Array. Verwaltet freie Frames und bietet `allocateFrame()`, `freeFrame()` und `physicalAddress()`. |
| `PageFaultException.java` | Wird von `translate()` geworfen, wenn ein PTE ungültig ist. Enthält PID, VPN und virtuelle Adresse. |
| `MemoryManagementUnit.java` | Gerüst **(Aufgaben 1–2)**: implementiert Adressübersetzung (`translate()`) und Page Fault Handling (`handlePageFault()`). Enthält fertige Hilfsmethode `translateWithFaultHandling()`. |
| `Main.java` | Fünf vollständige Testszenarien: einfache Übersetzung, Demand Paging, Prozessisolation, Schutzverletzung, Out-of-Memory. |

---

## 📚 Verwendete Java-Klassen und ihre Dokumentation

| Klasse | Zweck |
| --- | --- |
| **[`RuntimeException`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/RuntimeException.html)** | Basisklasse für unkontrollierte Ausnahmen. `PageFaultException` erweitert `RuntimeException`, damit sie ohne `throws`-Deklaration weitergereicht werden kann. |
| **[`IllegalArgumentException`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/IllegalArgumentException.html)** | Wird für ungültige virtuelle Adressen und Schutzverletzungen verwendet. |
| **[`IllegalStateException`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/IllegalStateException.html)** | Wird im Page Fault Handler geworfen, wenn kein freier Frame mehr verfügbar ist (Out of Memory). |
| **[`Arrays`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Arrays.html)** | Hilfsklasse für Array-Operationen. Wird in `PhysicalMemory` für `Arrays.fill(frameOwner, -1)` verwendet, um alle Frames als frei zu initialisieren. |
| **[`String.format()`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/String.html#format%28java.lang.String,java.lang.Object...%29)** | Formatierte Ausgabe in `PageFaultException` und den `toString()`-Methoden. |

---

## 🔗 Weiterführende Ressourcen

### Grundlagen virtueller Speicher und Paging

* **OSTEP — Introduction to Paging** *(empfohlene Pflichtlektüre)*
  <https://pages.cs.wisc.edu/~remzi/OSTEP/vm-paging.pdf>
  Erklärt Seitentabellen, PTEs, VPN und Offset anschaulich mit Beispielen.

* **OSTEP — Translation Lookaside Buffers**
  <https://pages.cs.wisc.edu/~remzi/OSTEP/vm-tlbs.pdf>
  Vorbereitung auf Woche 10: wie der TLB die Adressübersetzung beschleunigt.

* **OSTEP — Advanced Page Tables**
  <https://pages.cs.wisc.edu/~remzi/OSTEP/vm-smalltables.pdf>
  Mehrstufige Seitentabellen — Vorbereitung auf Woche 10.

### Konzepte

* **Page Table (Wikipedia)**
  <https://en.wikipedia.org/wiki/Page_table>
  Überblick über einstufige, mehrstufige und invertierte Seitentabellen.

* **Page Fault (Wikipedia)**
  <https://en.wikipedia.org/wiki/Page_fault>
  Erklärt Minor Faults, Major Faults und Segmentation Faults.

* **Demand Paging (Wikipedia)**
  <https://en.wikipedia.org/wiki/Demand_paging>
  Hintergrund zu Lazy Loading von Seiten und dessen Auswirkungen auf Startzeiten.

* **Memory Protection (Wikipedia)**
  <https://en.wikipedia.org/wiki/Memory_protection>
  Erklärt Schutzrechte (read/write/exec) und deren Durchsetzung durch die MMU.

### Praxisbezug

* **Linux Kernel — Virtual Memory**
  <https://www.kernel.org/doc/html/latest/admin-guide/mm/index.html>
  Dokumentation der Linux-Speicherverwaltung; zeigt wie die hier erarbeiteten Konzepte in einem echten Kernel umgesetzt sind.

---

> **Hinweis:** In dieser Übung simulieren wir Adressübersetzung und Demand Paging
> deterministisch in Java. In einem echten System übernimmt diese Aufgabe die
> Hardware-MMU, unterstützt vom Betriebssystem-Kernel über den Page Fault Handler.
