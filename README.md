# OS-Sim-Java

Ein einfacher Betriebssystem-Simulator in Java

### Syllabus

| Woche | Theorie (90 min)                                              | Übung (90 min) / Projekt-Meilenstein                                     |
|:-----:|---------------------------------------------------------------|---------------------------------------------------------------------------|
| 1     | Einführung in Betriebssysteme: Ziele, Aufgaben, Kursüberblick<br>Architektur der Simulator-API | Einrichtung der Entwicklungsumgebung (Online-IDE); Hello-Simulator:<br> Konsolen-Framework und Skeleton-Projekt |
| 2     | Prozessmodell und Lebenszyklus<br>Process Control Block (PCB)  | PCB-Implementierung: Java-Klasse mit Zustand, Priorität, Register-Image |
| 3     | CPU-Scheduling: FCFS, SJF, Round-Robin                         | Scheduler-Simulator: erste Algorithmen (FCFS, RR)                         |
| 4     | Scheduling-Vertiefung: Prioritäts- und Multi-Level-Queues      | Scheduler-Erweiterung: Prioritäten, Warteschlangen, Messung von Durchsatz |
| 5     | Einführung in Nebenläufigkeit: Threads vs. Prozesse           | Thread-Simulator mit Java-Threads: einfache Synchronisation (join)        |
| 6     | Synchronisation: Locks, Semaphoren, Monitore                  | Bau einer Semaphore-Bibliothek im Simulator; kritische Sektion           |
| 7     | Deadlocks: Bedingungen, Vermeidung, Erkennung und Auflösung   | Deadlock-Simulator: Ressourcenzuweisung, Detection-Algorithmen            |
| 8     | Speicherverwaltung: Contiguous Allocation, Partitionierung     | MemoryManager: einfache Zuweisung/Free (First-Fit, Best-Fit)              |
| 9     | Paged Memory: Seitentabellen, Segmentierung                  | PageTable-Simulator: Abbildung virtueller auf physische Seiten           |
| 10    | Virtueller Speicher: TLB, hierarchische Tabellen              | TLB-Cache im Simulator, Messung von Hit-/Miss-Raten                      |
| 11    | Seitenersetzungsalgorithmen: FIFO, LRU, Optimal               | Implementierung verschiedener Replacement-Strategien; Vergleich          |
| 12    | Dateisystemgrundlagen: Datei-Abstraktion, Metadaten (Inode)    | Einfacher FileSystem-Simulator: Erstellen/Löschen, Inode-Struktur         |
| 13    | Dateisystem-Verzeichnisstrukturen, Journaling                 | Erweiterung: Verzeichnisse, Pfadauflösung, (optionales) Journaling       |
| 14    | Prozesskommunikation: Pipes, Message Queues, Shared Memory     | IPC-Simulator: Nachrichtenversand, Ringpuffer, SharedBuffer              |
| 15    | Kurszusammenfassung und Ausblick                              | Abschluss-Präsentationen: Demo der Simulator-Module, Lessons Learned     |

### Projekt-Meilensteine können sein:

+ Woche 4: funktionaler Scheduler
+ Woche 7: Deadlock-Management
+ Woche 11: Vollständiger VM-Simulator
+ Woche 13: Einfaches Dateisystem
+ Woche 15: Integration aller Module