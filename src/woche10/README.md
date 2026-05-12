# Woche 10 — Virtueller Speicher: TLB & Hierarchische Tabellen

## Lernziele

Nach dieser Übung können Sie:
- Die Funktion eines TLB (Translation Lookaside Buffer) erklären
- Lookup, Insert (mit LRU-Verdrängung) und Flush eines TLB implementieren
- Die Adressübersetzung mit TLB-Integration (Hit- und Miss-Pfad) implementieren
- Die Effective Access Time (EAT) berechnen und interpretieren
- Den Einfluss von TLB-Größe und Zugriffsmuster auf die Hit-Rate messen

## Bezug zu Woche 09

In Woche 09 wurde jede Adressübersetzung direkt in der Seitentabelle nachgeschlagen.
Das bedeutet bei einem 2-Level-Walk bis zu 3 Speicherzugriffe pro VA.
Der TLB cached häufig genutzte Übersetzungen im schnellen Hardware-Speicher.
Bei einem Hit entfällt der Page Table Walk vollständig.

---

## Aufgaben

| Aufgabe | Implementieren in | Testen mit |
| --- | --- | --- |
| 1 | `TLB.java` | `MainAufgabe1.java` |
| 2 | `MMUWithTLB.java` | `MainAufgabe2.java` |
| Gesamt | — | `Main.java` |

---

### Aufgabe 1 — TLB: lookup(), insert(), flush() (`TLB.java`)

**`lookup(int pid, int vpn)`**
Durchsucht alle Einträge. Treffer wenn `entry.isValid() && entry.getPid()==pid && entry.getVpn()==vpn`.
Bei Treffer: `entry.setLast(++clock)`, `hits++`, Frame zurückgeben.
Bei Miss: `misses++`, `-1` zurückgeben.

**`insert(int pid, int vpn, int frame)`**
Sucht einen freien (ungültigen) Eintrag. Falls keiner frei: LRU-Eintrag
(kleinster `lastUsed`-Wert) verdrängen, `evictions++`.
Eintrag befüllen: `setValid(true)`, `setPid`, `setVpn`, `setFrame`, `setLast(++clock)`.

**`flush(int pid)`**
Invalidiert alle Einträge mit `entry.getPid() == pid`.
Bei `pid == -1`: vollständiger Flush aller Einträge. `flushes++`.

---

### Aufgabe 2 — MMU mit TLB: translate() (`MMUWithTLB.java`)

Erweitern Sie die Adressübersetzung aus Woche 09 um den TLB:

```
1. VA validieren, VPN und Offset berechnen (wie Woche 09).

2. TLB-Lookup: frame = tlb.lookup(pid, vpn)

   Hit (frame != -1):
     tlbHits++
     Protection-Check (wie Woche 09)
     pte.setReferenced(true), bei write pte.setDirty(true)
     return physMem.physicalAddress(frame, offset)

   Miss (frame == -1):
     tlbMisses++
     pte = pcb.getPTE(vpn)
     Falls !pte.isValid(): throw new PageFaultException(pid, vpn, va)
     Protection-Check, Bits setzen
     tlb.insert(pid, vpn, pte.getFrameNumber())
     return physMem.physicalAddress(pte.getFrameNumber(), offset)
```

**EAT-Formel (aus den Folien):**
```
EAT = (1 - p_miss) × t_hit + p_miss × t_miss
```
Beispiel (t_hit=1ns, t_miss=301ns, missrate=1%):
`EAT = 0.99 × 1 + 0.01 × 301 = 4.01 ns`

---

## Kompilieren und Ausführen

```bash
javac woche10/*.java

# Nach Aufgabe 1:
java woche10.MainAufgabe1

# Nach Aufgabe 2:
java woche10.MainAufgabe2

# Gesamttest:
java woche10.Main
```

---

## Erwartete Ausgabe (Auszug)

**MainAufgabe1** — TLB Lookup:
```
  lookup(pid=1, vpn=0) = 5   [erwartet: 5]
  lookup(pid=2, vpn=0) = -1  [erwartet: -1 (anderer Prozess)]
```

**MainAufgabe2** — EAT-Vergleich:
```
  TLB capacity= 1: HitRate= 0.0%, EAT=201.0 ns
  TLB capacity= 4: HitRate=87.5%, EAT=26.1 ns
  TLB capacity= 8: HitRate=90.0%, EAT=21.1 ns
```

---

## Denkanstöße

- Warum ist ein TLB-Miss bei einem 2-Level-Walk teurer als bei einem 1-Level-Walk?
- Was passiert mit dem TLB bei einem Kontextwechsel ohne ASID-Unterstützung?
- Ab welcher TLB-Größe ist weiteres Vergrößern kaum noch gewinnbringend?
- Warum hilft räumliche Lokalität dem TLB besonders?

---

## Verbindung zu Woche 11

Die `dirty`- und `referenced`-Bits im PTE, die Sie in Woche 09 gesetzt haben
und die in Woche 10 weiterhin gesetzt werden, sind die Grundlage für den
**Clock-Algorithmus** in Woche 11: das `referenced`-Bit entspricht dem
Reference-Bit im Clock-Zeiger.

---

## 📂 Dateien in diesem Verzeichnis

| Datei | Beschreibung |
| --- | --- |
| `ProcessState.java` | Prozesszustände (unverändert). |
| `PageTableEntry.java` | PTE mit `valid`, `frameNumber`, `dirty`, `referenced`, `protection` (aus Woche 09). |
| `ProcessControlBlock.java` | Erweiterter PCB mit einstufiger Seitentabelle (aus Woche 09). |
| `PhysicalMemory.java` | Physischer Speicher als Frame-Array (aus Woche 09). |
| `PageFaultException.java` | Exception für fehlende Seiten (aus Woche 09). |
| `TLBEntry.java` | Ein Eintrag des TLB: `valid`, `vpn`, `frame`, `lastUsed`, `pid`. |
| `TLB.java` | Gerüst **(Aufgabe 1)**: fully-associative TLB mit LRU-Verdrängung und ASID-Simulation. |
| `MMUWithTLB.java` | Gerüst **(Aufgabe 2)**: Adressübersetzung mit TLB-Integration. Enthält fertige Methoden `handlePageFault()`, `contextSwitch()`, `translateWithFaultHandling()`. |
| `MainAufgabe1.java` | Testet `TLB` direkt: Lookup, LRU-Verdrängung, Flush, EAT-Berechnung (Aufgabe 1). |
| `MainAufgabe2.java` | Testet `MMUWithTLB`: Hit vs. Miss, Kontextwechsel, EAT mit echten Zugriffen (Aufgabe 2). |
| `Main.java` | Gesamttest **(alle Aufgaben)**: 4 Szenarien mit verschiedenen Zugriffsmustern und EAT-Vergleich. |

---

## 📚 Verwendete Java-Klassen und ihre Dokumentation

| Klasse | Zweck |
| --- | --- |
| **[`Arrays`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Arrays.html)** | `Arrays.fill()` initialisiert `TLBEntry[]` im TLB-Konstruktor. |
| **[`RuntimeException`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/RuntimeException.html)** | Basisklasse für `PageFaultException` und `IllegalStateException`. |
| **[`IllegalArgumentException`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/IllegalArgumentException.html)** | Für ungültige Adressen und Schutzverletzungen in `MMUWithTLB`. |
| **[`IllegalStateException`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/IllegalStateException.html)** | Out-of-Memory im Page Fault Handler. |

---

## 🔗 Weiterführende Ressourcen

### TLB und Adressübersetzung

* **OSTEP — Paging: Faster Translations (TLBs)** *(empfohlene Pflichtlektüre)*
  <https://pages.cs.wisc.edu/~remzi/OSTEP/vm-tlbs.pdf>

* **OSTEP — Advanced Page Tables**
  <https://pages.cs.wisc.edu/~remzi/OSTEP/vm-smalltables.pdf>
  Mehrstufige Seitentabellen und deren Kosten beim Page Table Walk.

### Konzepte

* **Translation Lookaside Buffer (Wikipedia)**
  <https://en.wikipedia.org/wiki/Translation_lookaside_buffer>

* **Effective Access Time**
  <https://en.wikipedia.org/wiki/Effective_memory_access_time>

* **Address Space Layout (Wikipedia)**
  <https://en.wikipedia.org/wiki/Address_space_layout_randomization>
  Praxisbezug: wie ASLR mit TLBs interagiert.

### Praxisbezug

* **Linux — mm/tlb.c**
  <https://elixir.bootlin.com/linux/latest/source/arch/x86/mm/tlb.c>
  TLB-Flush-Implementierung im Linux-Kernel für x86.

---

> **Hinweis:** In dieser Übung simulieren wir den TLB in Software.
> In echter Hardware ist der TLB in der CPU integriert und arbeitet
> in Nanosekunden — der Simulator zeigt die Logik, nicht die Latenz.
