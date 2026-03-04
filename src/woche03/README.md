# Woche 03: CPU-Scheduling mit Process Control Blocks (PCBs)

**Lernziele:**

1. **Wiederverwendung** und **Ergänzung** der `ProcessControlBlock`-Klasse aus Woche 02.
2. Implementierung/Anpassung von **FCFS, SJF, Round-Robin** mit **realistischen PCB-Zustandsübergängen**.
3. Messung von **Waiting Time, Turnaround Time, Response Time** unter Verwendung der PCB-Metadaten.
4. Vergleich der Algorithmen anhand von **Gantt-Diagrammen** und Metriken.

---

## 📌 Vorbereitung

1. **Wiederhole Woche 02:**

    - Die Klassen `ProcessState` (erweitert um `READY`) und `ProcessControlBlock` werden **wiederverwendet**.
    - **Neu in Woche 03:** Der Zustand `READY` wird für Scheduling-Algorithmen benötigt.

2. **Klone das aktualisierte Repo** und wechsle in `woche03/`.
3. Öffne die **Code-Skelette** in VS Code/Eclipse.

---

## 📌 Scheduler-Interface
Alle Scheduling-Algorithmen (FCFS, SJF, Round-Robin) **implementieren das `Scheduler`-Interface**.
Dieses Interface definiert eine einzige Methode:

```java
ProcessControlBlock schedule(List<ProcessControlBlock> readyQueue, int currentTime);
```

### **Aufgaben des Schedulers:**
1. **Auswahl des nächsten Prozesses** aus der `readyQueue` (basierend auf dem Algorithmus).
2. **Aktualisierung des Prozesszustands** (z. B. `READY` → `RUNNING`).
3. **Rückgabe des ausgewählten Prozesses** (oder `null`, falls keine Prozesse bereit sind).
4. **Aktualisierung der Metadaten** (z. B. `startTime`, `remainingTime`).

### **Beispiel: Verwendung des Interfaces**

```java
Scheduler scheduler = new FCFSScheduler();  // oder SJFScheduler, RoundRobinScheduler
ProcessControlBlock nextProcess = scheduler.schedule(readyQueue, currentTime);
```

---

## 📝 Aufgaben

### **Aufgabe 1: `ProcessState` erweitern und `ProcessControlBlock` anpassen**

**Ziel:** Fügen Sie den Zustand `READY` hinzu und passen Sie `ProcessControlBlock` für Scheduling an.

1. Die Dateien `ProcessState.java` und `ProcessControlBlock.java` in `woche03` sind nicht vollständig und müssen ergänzt werden!

2. **Erweitere `ProcessState`** in `ProcessState.java`:
   ```java
   package woche03;

   public enum ProcessState {
       NEW, READY, RUNNING, BLOCKED, TERMINATED  // READY hinzugefügt!
   }
   ```

3. **Passe `ProcessControlBlock` an**: füge `remainingTime`, `startTime`, `finishTime` sowie `usedTime` hinzu.

```java
// In ProcessControlBlock.java (woche03/)
private int remainingTime;  // Verbleibende Zeit (für präemptive Algorithmen)
private int startTime;      // -1 = noch nicht gestartet, vom Scheduler gesetzt
private int finishTime;     // -1 = noch nicht beendet, vom Scheduler gesetzt
private int usedTime;       // Zeit, die im letzten Quantum verbraucht wurde

public ProcessControlBlock(int pid, String name, int arrivalTime, int burstTime) {
    this.pid = pid;
    this.name = name;
    this.arrivalTime = arrivalTime;
    this.burstTime = burstTime;
    this.remainingTime = burstTime;  // Initial gleich burstTime
    this.state = ProcessState.NEW;
    this.priority = 0;
    this.parentPid = -1;
    this.registers = new String[16];
    this.startTime = -1;
    this.finishTime = -1;
    this.usedTime = 0;
}

// Ist der Konstruktor vollständig?
```

---

### **Aufgabe 2: Scheduler-Interface und FCFS implementieren**

**Ziel:** Implementiere das `Scheduler`-Interface und den **FCFS-Algorithmus** mit PCB-Zustandsübergängen.

1. **Implementiere `Scheduler.java`** (Interface):

```java
package woche03;

import java.util.List;

public interface Scheduler {
    /**
     * Wählt den nächsten Prozess aus der Ready-Queue aus und aktualisiert seinen Zustand.
     * @param readyQueue Liste der bereitstehenden PCBs.
     * @param currentTime Aktuelle Simulationszeit.
     * @return Der ausgewählte PCB (oder null, wenn keine Prozesse bereit sind).
     */
    ProcessControlBlock schedule(List<ProcessControlBlock> readyQueue, int currentTime);
}
```
**💡 Hinweis:** Das Interface steht bereits vollstendig zur Verfügung.

2. **Implementiere `FCFSScheduler.java`**:

    - Sortiere die Ready-Queue nach **Ankunftszeit** (`arrivalTime`).
    - Setze den Zustand des ausgewählten PCBs auf **`RUNNING`**.
    - Aktualisiere `startTime` und `finishTime`.

```java
@Override
public ProcessControlBlock schedule(List<ProcessControlBlock> readyQueue, int currentTime) {
    // 1. Filtere nur nicht-terminierte Prozesse mit remainingTime > 0
    List<ProcessControlBlock> filteredQueue = readyQueue.stream()
        .filter(pcb -> pcb.getState() != ProcessState.TERMINATED)
        .filter(pcb -> pcb.getRemainingTime() > 0)
        .collect(Collectors.toList());

    if (filteredQueue.isEmpty()) {
        return null;  // Keine bereiten Prozesse verfügbar
    }

    // 2. Sortiere nach Ankunftszeit (FCFS)
    filteredQueue.sort(Comparator.comparingInt(ProcessControlBlock::getArrivalTime));
    ProcessControlBlock selected = filteredQueue.get(0);

    // 3. Setze Zustand auf RUNNING
    selected.setState(ProcessState.RUNNING);

    // 4. Setze startTime, falls noch nicht gesetzt
    if (selected.getStartTime() == -1) {
        selected.setStartTime(currentTime);
    }

    // 5. FCFS: Prozess läuft bis zum Ende (usedTime = burstTime)
    int usedTime = selected.getBurstTime();
    selected.setUsedTime(usedTime);
    selected.setRemainingTime(0);  // Prozess ist fertig
    selected.setState(ProcessState.TERMINATED);
    selected.setFinishTime(currentTime + usedTime);

    return selected;
}
```

**💡 Hinweis:** Die Implementierung von FCFSScheduler steht bereits zur Verfügung und dient als Vorbild.

---

### **Aufgabe 3: SJF und SRT mit PCB-Zuständen implementieren**

**Ziel:** Nutze die `remainingTime` und Zustände für **nicht-präemptives SJF** und **präemptives SRT**.

1. **SJFScheduler.java** (nicht-präemptiv):
       - Wähle den PCB mit der **kürzesten `burstTime`** (Annahme: Burst-Time ist bekannt).
       - Setze den Zustand auf `RUNNING`.

```java
@Override
public ProcessControlBlock schedule(List<ProcessControlBlock> readyQueue, int currentTime) {
    // 1. Filtere nur nicht-terminierte Prozesse mit remainingTime > 0
    List<ProcessControlBlock> filteredQueue = readyQueue.stream()
        .filter(pcb -> pcb.getState() != ProcessState.TERMINATED)
        .filter(pcb -> pcb.getRemainingTime() > 0)
        .filter(pcb -> pcb.getArrivalTime() <= currentTime)  // Nur angekommene Prozesse
        .collect(Collectors.toList());

    if (filteredQueue.isEmpty()) {
        return null;  // Keine bereiten Prozesse verfügbar
    }

    // 2. Sortiere nach Burst-Time (SJF: kürzeste Job zuerst)
    // TODO: Sortiere nach remainingTime! Vergleiche mit FCFS
    // --->



    // 3. Setze Zustand auf RUNNING
    selected.setState(ProcessState.RUNNING);

    // 4. Setze startTime, falls noch nicht gesetzt
    if (selected.getStartTime() == -1) {
        selected.setStartTime(currentTime);
    }

    // 5. SJF: Prozess läuft bis zum Ende (usedTime = burstTime)
    int usedTime = selected.getBurstTime();
    selected.setUsedTime(usedTime);
    selected.setRemainingTime(0);  // Prozess ist fertig
    selected.setState(ProcessState.TERMINATED);
    selected.setFinishTime(currentTime + usedTime);

    return selected;
}
```

2. **SRTScheduler.java** (präemptiv):
       - Wähle den PCB mit der **kürzesten `remainingTime`**.
       - **Aktualisiere `remainingTime`** nach jedem Quantum.
       - Setze den Zustand auf `RUNNING` oder `READY` (falls präemptiert).

```java
@Override
public ProcessControlBlock schedule(List<ProcessControlBlock> readyQueue, int currentTime) {
    // --- 1. Filtere alle bereiten Prozesse ---
    // Berücksichtige nur Prozesse, die:
    // - bereits angekommen sind (arrivalTime <= currentTime),
    // - nicht terminiert sind (state != TERMINATED),
    // - noch Ausführungszeit benötigen (remainingTime > 0).
    Optional<ProcessControlBlock> shortestOpt = readyQueue.stream()
        .filter(pcb -> pcb.getArrivalTime() <= currentTime)      // Nur angekommene Prozesse
        .filter(pcb -> pcb.getState() != ProcessState.TERMINATED) // Ignoriere terminierte Prozesse
        .filter(pcb -> pcb.getRemainingTime() > 0)               // Nur Prozesse mit verbleibender Zeit
        .min((a, b) -> Integer.compare(a.getRemainingTime(), b.getRemainingTime()));  // Wähle den mit kürzester remainingTime

    // --- 2. Falls keine bereiten Prozesse vorhanden sind ---
    if (shortestOpt.isEmpty()) {
        return null;  // Kein Prozess verfügbar (Leerlauf)
    }

    // --- 3. Wähle den Prozess mit der kürzesten remainingTime ---
    ProcessControlBlock selected = shortestOpt.get();

    // Setze den Zustand auf RUNNING (falls nicht bereits gesetzt).
    // Dies ist besonders wichtig bei Präemption: Ein unterbrochener Prozess wird wieder auf RUNNING gesetzt.
    // TODO: Setze den richtigen Zustand für den ausgewählten Prozess
    // --->


    
    // --- 4. Setze die Startzeit, falls der Prozess zum ersten Mal läuft ---
    if (selected.getStartTime() == -1) {
        selected.setStartTime(currentTime);  // Erste Ausführung: Merke die Startzeit
    }

    // --- 5. Simuliere die Ausführung für 1 Zeiteinheit ---
    // SRT ist präemptiv: Jeder Prozess läuft maximal 1 Zeiteinheit, bevor neu entschieden wird.
    // TODO: Aktualisiere usedTime nach jedem Quantum.
    // TODO: Aktualisiere remainingTime nach jedem Quantum.
    // --->



    // --- 6. Falls der Prozess fertig ist, markiere ihn als TERMINATED ---
    if (selected.getRemainingTime() == 0) {
        selected.setState(ProcessState.TERMINATED);
        selected.setFinishTime(currentTime + 1);  // Fertigstellungszeit = currentTime + 1 (da 1 Zeiteinheit ausgeführt wird)
    }

    // --- 7. Gib den ausgewählten Prozess zurück ---
    // Der Rückgabewert wird in testSRTScheduler verwendet, um den aktuellen Prozess zu aktualisieren.
    return selected;
}
```

---

### **Aufgabe 4: Round-Robin mit PCB-Zuständen und Quantum**

**Ziel:** Verstehen wie *preemption* implementiert wird..

1. **Analysieren** Sie den Code der Methode `schedule()`. Im Abschnitt 2 wird *preemption* implementiert
    - Nutze eine **Queue** für die Ready-Queue.
    - **Präemptive Logik**:
        - Führe einen PCB für **max. `quantum` Zeiteinheiten** aus.
        - Falls `remainingTime > 0`, setze den Zustand auf **`READY`** und füge ihn hinten an die Queue an.
        - Falls `remainingTime = 0`, setze den Zustand auf **`TERMINATED`**.

```java
// 2. Füge neu angekommene Prozesse VOR den zuletzt ausgeführten Prozess ein
if (lastExecutedProcess != null) {
    // Entferne den zuletzt ausgeführten Prozess temporär
    readyQueue.remove(lastExecutedProcess);
    // Füge alle neu angekommenen Prozesse hinzu
    readyQueue.addAll(newProcesses);
    // Füge den zuletzt ausgeführten Prozess hinten wieder ein
    readyQueue.add(lastExecutedProcess);
} else {
    // Falls kein letzter Prozess, einfach alle neu angekommenen Prozesse anfügen
    readyQueue.addAll(newProcesses);
}
```
2. **Frage:** Was wurde passieren, wenn der zuletzt ausgeführte Prozess nicht aus der `readyQueue` entfernt wird? Was wäre der Unterschied?


---

### **Aufgabe 5: Metriken berechnen mit PCB-Daten**

**Ziel:** Nutze die Felder `startTime`, `finishTime` und `burstTime` des PCBs, um Metriken zu berechnen.

1. **Implementiere `SchedulerUtils.java`**:

```java
public static Map<String, Double> calculateMetrics(List<ProcessControlBlock> pcbs) {
    double avgWaitingTime = pcbs.stream()
        .mapToDouble(pcb -> pcb.getFinishTime() - pcb.getArrivalTime() - pcb.getBurstTime())
        .average()
        .orElse(0.0);

    double avgTurnaroundTime = pcbs.stream()
        .mapToDouble(pcb -> pcb.getFinishTime() - pcb.getArrivalTime())
        .average()
        .orElse(0.0);

    // TODO: Implementiere avgResponseTime und throughput
    // Response Time = startTime - arrivalTime
    // Throughput = pcbs.size() / (maxFinishTime - minArrivalTime)

    return Map.of(
        "avgWaitingTime", avgWaitingTime,
        "avgTurnaroundTime", avgTurnaroundTime,
        "avgResponseTime", avgResponseTime,
        "throughput", throughput
    );
}
```

---

### **Aufgabe 6: Experimentdesign und Vergleich anhand von Metriken**

1. In `SchedulerUtils.java` steht die Methode `calculateMetrics` zur Verfügung, die die Werte
    - `avgWaitingTime`,
    - `avgTurnaroundTime`,
    - `avgResponseTime`,
    - `throughput`

berechnet. Auf Basis dieser Werte werden wir die Scheduling-Strategien vergleichen. Wären andere Metriken interessant? Können Sie die Methode ergänzen?

2. **Teste die Algorithmen** mit den folgenden Workloads (in `Main.java`):

```java
// Workload 1: Convoy-Effekt (FCFS leidet)
List<ProcessControlBlock> workload1 = List.of(
    new ProcessControlBlock(1, "P1", 0, 30),  // Langer Job
    new ProcessControlBlock(2, "P2", 1, 3),   // Kurzer Job
    new ProcessControlBlock(3, "P3", 2, 1)    // Kurzer Job
);

// Workload 2: I/O-bound (viele kurze Jobs)
List<ProcessControlBlock> workload2 = List.of(
    new ProcessControlBlock(1, "P1", 0, 2),
    new ProcessControlBlock(2, "P2", 0, 2),
    new ProcessControlBlock(3, "P3", 3, 2),
    new ProcessControlBlock(4, "P4", 4, 2),
    new ProcessControlBlock(5, "P5", 5, 2)
);

// Workload 3: Gemischt
List<ProcessControlBlock> workload3 = List.of(
    new ProcessControlBlock(1, "P1", 0, 5),
    new ProcessControlBlock(2, "P2", 1, 10),
    new ProcessControlBlock(3, "P3", 2, 1),
    new ProcessControlBlock(4, "P4", 3, 4)
);
```
**💡 Hinweis:** Sie können natürlich Ihre eigenen Workloads testen!

### Beachten Sie, dass zwei unterschiedliche Methoden zum testen der Algorithmen zur Verfügung stehen:

- `testScheduler` für FCFS, SJF und RR
- `testSRTScheduler` für SRT

Der Unterschied besteht darin, wie die Zeit simuliert wird. Denken Sie darüber nach!


3. **Vergleiche die Metriken** für jeden Algorithmus und Workload.

3. **Diskutiere die Ergebnisse**:

    - Welcher Algorithmus ist **fairer** (geringere Varianz in Waiting Times)?
    - Welcher Algorithmus hat die **beste Response Time** für interaktive Jobs?
    - Wie beeinflusst das **Quantum** bei Round-Robin die Performance?

---

## 💡 Hinweise
- **Zustandsübergänge**:
      - `NEW` → `READY` (wenn Prozess erstellt wird)
      - `READY` → `RUNNING` (wenn Prozess ausgewählt wird)
      - `RUNNING` → `READY` (bei Präemption, z. B. Round-Robin)
      - `RUNNING` → `TERMINATED` (wenn Prozess fertig ist)
- **Fehlerbehandlung**:
      - Was passiert, wenn die Ready-Queue leer ist? → Gib `null` zurück.
      - Was passiert, wenn ein Prozess nicht existiert? → Werfe `IllegalArgumentException`.
- **Metriken**:
      - **Waiting Time** = `finishTime - arrivalTime - burstTime`
      - **Turnaround Time** = `finishTime - arrivalTime`
      - **Response Time** = `startTime - arrivalTime`

---

## 📂 Dateien in diesem Verzeichnis
| Datei | Beschreibung | Status |
|-------|-------------|--------|
| `ProcessState.java` | Erweitern um `READY`. | **Zu implementieren** |
| `ProcessControlBlock.java` | Zeiten ergänzen. | **Zu implementieren** |
| `Scheduler.java` | Interface für alle Scheduler. | **Fertig** |
| `FCFSScheduler.java` | First-Come, First-Served. | **Fertig** |
| `SJFScheduler.java` | Shortest Job First. | **Zu implementieren** |
| `SRTScheduler.java` | Shortest Remaining Time. | **Zu implementieren** |
| `RoundRobinScheduler.java` | Round-Robin mit Quantum. | **Fertig** |
| `SchedulerUtils.java` | Berechnet Metriken. | **Fertig** |
| `Main.java` | Testklasse mit Workloads. Weitere Workloads, weitere tests | **Zu implementieren** |

---

### **📚 Verwendete Java-Klassen und ihre Dokumentation**

In diesem Projekt werden folgende Klassen aus dem Paket `java.util` verwendet. Klicke auf die Links, um die offizielle Dokumentation zu öffnen:

| Klasse | Zweck |
|--------|-------|
| **[`List<E>`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/List.html)** | Eine **geordnete Sammlung** von Elementen (hier: `ProcessControlBlock`-Objekte). Wird für die Ready-Queue verwendet. |
| **[`Map<K,V>`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Map.html)** | Eine **Abbildung von Schlüsseln auf Werte** (z. B. `Map<Integer, ProcessControlBlock>` für PID → PCB). |
| **[`Comparator<T>`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Comparator.html)** | Ermöglicht das **Sortieren von Objekten** nach bestimmten Kriterien (z. B. nach `arrivalTime` oder `burstTime`). |
| **[`Queue<E>`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Queue.html)** | Eine **Warteschlange** (FIFO-Prinzip), verwendet in `RoundRobinScheduler`. |
| **[`LinkedList<E>`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/LinkedList.html)** | Eine **doppelt verkettete Liste**, die als Queue oder Liste genutzt werden kann. |
| **[`Map.of()`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Map.html#of())** | Erstellt eine **unveränderliche Map** mit vorgegebenen Schlüssel-Wert-Paaren (z. B. für Metriken-Rückgabe). |
| **[`Arrays.toString()`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Arrays.html#toString(java.lang.Object[]))** | Wandelt ein **Array in einen String** um (z. B. für die Ausgabe der `registers` im PCB). |
| **[`OptionalDouble`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/OptionalDouble.html)** | Ein **Container für `double`-Werte**, der `null` vermeidet (z. B. bei `average()`-Berechnungen). |

---

### **💡 Warum werden diese Klassen verwendet?**

1. **`List<E>` und `Queue<E>`**:
    - **Ready-Queue** für Scheduling-Algorithmen (z. B. `List<ProcessControlBlock>` in `FCFSScheduler`).
    - **Dynamische Größe**: Prozesse können während der Simulation hinzugefügt/entfernt werden.

2. **`Map<K,V>`**:

    - **Schneller Zugriff** auf Prozesse über ihre PID (z. B. `Map<Integer, ProcessControlBlock>`).
    - Beispiel: `pcbs.get(pid)` liefert den PCB mit der gegebenen PID in **O(1)-Zeit**.

3. **`Comparator<T>`**:

    - **Sortierung der Ready-Queue** nach verschiedenen Kriterien:
        - FCFS: Nach `arrivalTime`.
        - SJF: Nach `burstTime`.
        - SRT: Nach `remainingTime`.
    - Beispiel:

```java
readyQueue.sort(Comparator.comparingInt(ProcessControlBlock::getBurstTime));
```

4. **`OptionalDouble`**:

    - **Sichere Berechnung von Durchschnitten** (z. B. für Metriken wie `avgWaitingTime`).
    - Vermeidet `NullPointerException`, falls die Liste leer ist:

```java
double avg = pcbs.stream()
    .mapToDouble(ProcessControlBlock::getWaitingTime)
    .average()
    .orElse(0.0);  // Default-Wert 0.0, falls keine Prozesse vorhanden
```

5. **`Arrays.toString()`**:

    - **Lesbare Ausgabe** von Arrays (z. B. für die `registers` im PCB):
```java
System.out.println("Registers: " + Arrays.toString(pcb.getRegisters()));
```

---

### **🔗 Weiterführende Ressourcen**

- **[Java Collections Framework (Überblick)](https://docs.oracle.com/javase/8/docs/technotes/guides/collections/)**
  Erklärt die grundlegenden Datenstrukturen in Java (Listen, Maps, Queues etc.).

- **[Java Streams API](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/stream/Stream.html)**
  Wird für die Berechnung von Metriken (z. B. `average()`) verwendet.

- **[Java Comparator Tutorial](https://www.baeldung.com/java-comparator-comparable)**
  Erklärt, wie man Objekte mit `Comparator` sortiert.

---
### **📌 Hinweis für die Übung**

- **Keine Angst vor `java.util`!**

  Die verwendeten Klassen sind **Standardwerkzeuge** in Java und werden in fast jedem Projekt eingesetzt.


- **Alternativen**:
    - Statt `List` könntest du ein **Array** verwenden (weniger flexibel).
    - Statt `Map` könntest du eine **Liste durchsuchen** (langsamer: O(n) statt O(1)).

