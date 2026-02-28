# Woche 02: Von Prozesszuständen zum Process Control Block (PCB)

**Lernziele:**
1. Prozesszustände mit `Map` und `enum` **selbst implementieren**.
2. Eine **`ProcessControlBlock`**-Klasse **von Grund auf** erstellen.
3. Einen **PCB-basierten Simulator** **selbst implementieren** und testen.

---

## 📌 Vorbereitung
1. Klone das Repo und wechsle in das Verzeichnis `woche02/`.
2. Öffne die **unvollständigen Skelett-Dateien** in VS Code oder Eclipse.
3. **Vervollständige die Methoden** gemäß den Aufgabenstellungen.

---

## 📝 Aufgaben

### **Aufgabe 1: Prozesszustände mit `Map` und `enum` verwalten**
**Ziel:** Implementiere die fehlenden Methoden in `ProcessManager.java`.

1. Vervollständige die **Getter/Setter** für den Prozesszustand.
2. Implementiere die Methoden:
   - `blockProcess`: Setzt den Zustand auf `BLOCKED`.
   - `terminateProcess`: Setzt den Zustand auf `TERMINATED`.
   - `getProcessState`: Gibt den Zustand zurück.
   - `listProcesses`: Gibt alle Prozesse aus.

**Tipp:** `createProcess`: steht fertig zur Verfügung als Vorbild.

---

### **Aufgabe 2: `ProcessControlBlock` (PCB) implementieren**
**Ziel:** Erstelle die PCB-Klasse **von Grund auf** in `ProcessControlBlock.java`.

1. Deklariere **alle Felder** gemäß der Aufgabenstellung:
   - `pid`, `name`, `state`, `priority`, `parentPid`, `registers`.
2. Implementiere den **Konstruktor** und **alle Getter/Setter**.
3. Vervollständige die **`toString()`**-Methode.

**Hinweis:** Die `registers`-Variable soll ein Array der Größe 16 sein (simulierte CPU-Register).

---

### **Aufgabe 3: PCB-basierten Simulator implementieren**
**Ziel:** Ersetze die `Map<Integer, Process>` durch eine `Map<Integer, ProcessControlBlock>` in `PCBSimulator.java`.

1. Implementiere die Methoden:
   - `createProcess`: Erstellt einen neuen `ProcessControlBlock`.
   - `blockProcess`: Setzt den Zustand eines PCB auf `BLOCKED`.
   - `terminateProcess`: Setzt den Zustand eines PCB auf `TERMINATED`.
   - `setPriority`: Setzt die Priorität eines PCB.
   - `getPCB`: Gibt den PCB eines Prozesses zurück.
   - `listPCBs`: Gibt alle PCBs aus.

**Tipps:**
   - Nutze die Methoden aus `ProcessControlBlock`, um die Zustände und Prioritäten zu setzen.
   - `createProcess` steht zur Verfügung als Vorbild.

---

### **Aufgabe 4: Testen und Reflektieren**
1. Führe `Main.java` aus und überprüfe, ob deine Implementierungen funktionieren.
2. **Vergleiche** die beiden Ansätze:
   - `ProcessManager` (nur Zustände) vs. `PCBSimulator` (PCB mit Metadaten).
3. **Beantworte die Fragen:**
   - Warum ist der PCB **realistischer** als die einfache Zustandverwaltung?
   - Welche **Vor- und Nachteile** haben beide Ansätze?
   - Welche **weiteren Felder** könntest du dem PCB hinzufügen?

---

## 💡 Hinweise
- **Prozess-ID (PID):** Jeder Prozess erhält eine **einzigartige ID** (`nextId`).
- **Zustandsübergänge:**
  - `NEW` → `RUNNING` (bei `createProcess`).
  - `RUNNING` → `BLOCKED` (bei `blockProcess`).
  - `RUNNING`/`BLOCKED` → `TERMINATED` (bei `terminateProcess`).
- **Fehlerbehandlung:** Was passiert, wenn du einen nicht existierenden Prozess blockierst?
  → Füge eine **Prüfung** in `blockProcess` hinzu!

---

## 📂 Dateien in diesem Verzeichnis
| Datei | Beschreibung | Status |
|-------|-------------|--------|
| `ProcessManager.java` | Unvollständiges Skelett für Prozesszustände. | **Zu vervollständigen** |
| `ProcessControlBlock.java` | Unvollständiges Skelett für den PCB. | **Zu vervollständigen** |
| `PCBSimulator.java` | Unvollständiger PCB-basierter Simulator. | **Zu vervollständigen** |
| `Main.java` | Fertige Testklasse (nicht ändern!). | ✅ Fertig |

---




# Woche 02: Von Prozesszuständen zum Process Control Block (PCB)

**Lernziele:**

1. Prozesszustände mit `Map` und `enum` verwalten (Vertiefung von Woche 1).
2. Eine **`ProcessControlBlock`**-Klasse implementieren, die **Zustände + Metadaten** speichert.
3. Einen **PCB-basierten Simulator** erstellen und testen.

---

## 📌 Vorbereitung

1. Klone das Repo (falls noch nicht geschehen) und wechsle in das Verzeichnis `woche02/`.
2. Öffne die Dateien in **VS Code** oder **Eclipse** (siehe Woche 1).

---

## 📝 Aufgaben

### **Aufgabe 1: Prozesszustände mit `Map` und `enum` verwalten**
**Ziel:** Vertiefe die Verwaltung von Prozesszuständen.

1. Öffne `ProcessManager.java` und vervollständige die Methoden:
   - `createProcess`: Erstellt einen neuen Prozess im Zustand `RUNNING`.
   - `blockProcess`: Setzt den Zustand eines Prozesses auf `BLOCKED`.
   - `terminateProcess`: Setzt den Zustand auf `TERMINATED`.
   - `getProcessState`: Gibt den Zustand eines Prozesses zurück.

2. Teste die Implementierung mit `Main.java`:

```java
ProcessManager manager = new ProcessManager();
int pid1 = manager.createProcess("Editor");
manager.blockProcess(pid1);
System.out.println("Process " + pid1 + " state: " + manager.getProcessState(pid1));
```

---

### **Aufgabe 2: `ProcessControlBlock` (PCB) implementieren**
**Ziel:** Erweitere die einfache `Process`-Klasse aus Woche 1 zu einem **vollständigen PCB**.

1. Öffne `ProcessControlBlock.java` und implementiere die Klasse mit folgenden Feldern:
   - `pid` (Prozess-ID)
   - `name` (Prozessname)
   - `state` (Prozesszustand, `ProcessState` aus Woche 1)
   - `priority` (Priorität, `int`, Default: 1)
   - `parentPid` (Elternprozess-ID, `int`, Default: -1 für keinen Elternprozess)
   - `registers` (Simulierte CPU-Register, `String[16]`)

2. Füge **Getter/Setter** für alle Felder hinzu.
3. Implementiere eine `toString()`-Methode, die alle Felder ausgibt.

**Beispiel:**

```java
public class ProcessControlBlock {
    private int pid;
    private String name;
    private ProcessState state;
    private int priority;
    private int parentPid;
    private String[] registers;

    public ProcessControlBlock(int pid, String name) {
        this.pid = pid;
        this.name = name;
        this.state = ProcessState.NEW;
        this.priority = 1;
        this.parentPid = -1;
        this.registers = new String[16];
    }

    // Getter/Setter hier einfügen...
}
```

---

### **Aufgabe 3: PCB-basierten Simulator implementieren**
**Ziel:** Ersetze die `Map<Integer, Process>` aus Aufgabe 1 durch eine `Map<Integer, ProcessControlBlock>`.

1. Öffne `PCBSimulator.java` und implementiere die Methoden:
   - `createProcess`: Erstellt einen neuen `ProcessControlBlock` und fügt ihn der `Map` hinzu.
   - `blockProcess`: Setzt den Zustand eines PCB auf `BLOCKED`.
   - `setPriority`: Setzt die Priorität eines PCB.
   - `getPCB`: Gibt den PCB eines Prozesses zurück.

2. Teste die Implementierung mit `Main.java`:

```java
   PCBSimulator simulator = new PCBSimulator();
   int pid = simulator.createProcess("Compiler");
   simulator.setPriority(pid, 5);
   simulator.blockProcess(pid);
   System.out.println(simulator.getPCB(pid));
```

---

### **Aufgabe 4: Vergleich der Implementierungen (Reflexion)**
1. **Vergleiche** die beiden Ansätze:
   - `ProcessManager` (nur Zustände) vs. `PCBSimulator` (PCB mit Metadaten).

2. **Beantworte die Fragen:**
   - Warum ist der PCB **realistischer** als die einfache Zustandverwaltung?
   - Welche **Vor- und Nachteile** haben beide Ansätze?
   - Welche **weiteren Felder** könntest du dem PCB hinzufügen (z. B. für Speicherverwaltung in Woche 8)?

---

## 💡 Hinweise
- **Prozess-ID (PID)**: Jeder Prozess erhält eine **einzigartige ID** (`nextId`).
- **Zustandsübergänge**:
    - `NEW` → `RUNNING` (bei `createProcess`).
    - `RUNNING` → `BLOCKED` (bei `blockProcess`).
    - `RUNNING`/`BLOCKED` → `TERMINATED` (bei `terminateProcess`).

- **Fehlerbehandlung**: Was passiert, wenn du einen nicht existierenden Prozess blockierst?
    - Füge eine **Prüfung** in `blockProcess` hinzu!

---

## 📂 Dateien in diesem Verzeichnis
<br>

| Datei | Beschreibung |
|-------|-------------|
| `ProcessManager.java` | Verwaltung von Prozesszuständen (Map + enum). |
| `ProcessControlBlock.java` | PCB-Klasse mit Zuständen und Metadaten. |
| `PCBSimulator.java` | Simulator, der PCBs statt einfacher Prozesse verwendet. |
| `Main.java` | Hauptprogramm zum Testen der Implementierungen. |

