# OS-Sim-Java

Ein einfacher Betriebssystem-Simulator in Java

### Syllabus

| Woche | Theorie (90 min) | Übung (90 min) |
|-------|------------------|----------------|
| 1  | Einführung in Betriebssysteme: Ziele, Aufgaben, Kursüberblick; Architektur der Simulator-API | Einrichtung der Entwicklungsumgebung; Hello-Simulator: Konsolen-Framework und Skeleton-Projekt |
| 2  | Prozessmodell und Lebenszyklus; Process Control Block (PCB) | PCB-Implementierung: Java-Klasse mit Zustand, Priorität, Register-Image |
| 3  | CPU-Scheduling: FCFS, SJF, Round-Robin | Scheduler-Simulator: FCFS und Round-Robin implementieren und vergleichen |
| 4  | Scheduling-Vertiefung: Prioritäts-Scheduling, Multi-Level Feedback Queue (MLFQ) | Scheduler-Erweiterung: Prioritäten, MLFQ-Simulator, Messung von Wartezeit und Durchsatz |
| 5  | Nebenläufigkeit: Threads vs. Prozesse; Race Conditions | Thread-Simulator: einfache Synchronisation mit `join()`, Race Conditions beobachten |
| 6  | Synchronisation: Mutex, Semaphor, Monitor; `wait()`/`notify()` | Semaphor-Bibliothek und Producer-Consumer-Simulator; kritische Sektion schützen |
| 7  | Deadlocks: Coffman-Bedingungen, Wait-for-Graph, Erkennung und Vermeidung | Deadlock-Simulator: Dining Philosophers, Wait-for-Graph mit DFS |
| 8  | Speicherverwaltung: Contiguous Allocation, Buddy-Allocator | MemoryManager: First-Fit, Best-Fit, Buddy-Allocator implementieren |
| 9  | Paging: Seitentabellen, Adressübersetzung (VPN → Frame) | PageTable-Simulator: Abbildung virtueller auf physische Adressen |
| 10 | Virtueller Speicher: TLB, mehrstufige Seitentabellen, EAT | TLB-Cache im Simulator: Hit-/Miss-Raten messen und vergleichen |
| 11 | Seitenersetzungsalgorithmen: FIFO, LRU, OPT, Clock (Second Chance) | Replacement-Simulator: FIFO, LRU, Clock implementieren und vergleichen; Enhanced Clock mit Dirty-Bit |
| 12 | Dateisystem-Grundlagen: Dateiabstraktion, Inodes, Blockzeiger, Disk-Layout | FileSystem-Simulator: Inodes, Block Bitmap, `create()`/`writeBlock()`/`delete()` |
| 13 | Verzeichnisse: Verzeichnis als Datei, Pfadauflösung, Hard Links, Symbolic Links, `rename()` | DirectoryFileSystem: `resolve()`, `mkdir()`, `hardLink()`, `rename()` implementieren |
| 14 | Crash-Konsistenz: fsck, Journaling (WAL, TxB/TxE), Copy-on-Write | Journal-Simulator: Crash-Szenarien, `beginTransaction()`/`commit()`/`replay()` implementieren |
| 15 | Prozesskommunikation (IPC): Pipes, Message Queues, Shared Memory, Ringpuffer | IPC-Simulator: `BlockingRingBuffer`, `MessageQueue`, `SharedMemory` mit `wait()`/`notifyAll()` |
| 16 | Kurszusammenfassung und Ausblick | Rückblick: alle Simulator-Module, Verbindungen zwischen den Themen, Prüfungsvorbereitung |
