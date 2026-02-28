# Woche 01: Einführung in Betriebssysteme

**Lernziele:**
- Einrichtung des lokalen Eclipse-Projekts für den Kurs.
- Erstellen eines einfachen Konsolen-Frameworks für den Betriebssystem-Simulator.

---

## 📌 Vorbereitung: Eclipse-Projekt einrichten



### 1. Repo klonen (falls noch nicht geschehen)
```bash
git clone https://github.com/rafamayo/os-sim-java
```

### 2. Eclipse-Projekt aus bestehendem Verzeichnis erstellen

1. Öffne Eclipse.
2. Erstelle ein neues Java-Projekt:
    - File → New → Java Project.
    - Projektname: os-java-sim (wie dein Hauptverzeichnis).
    - Setze den Pfad explizit zum Projekt-Hauptverzeichnis
    - Nicht markieren: `Create module-info.java file`
    - Klicke auf `Finish`.

### 3. Programm ausführen
- Rechtsklick auf `HelloSimulator.java` → **Run As → Java Application**.
- **Erwartete Ausgabe**:

```
  Starting process: Process 1
  Stopping process: Process 1
```

---

## 📝 Aufgaben

### Aufgabe 1: Hello-Simulator erweitern
1. Öffne die Datei `src/HelloSimulator.java` und führe das Programm aus.
2. **Erweitere die `main`-Methode**:
   - Füge zwei weitere Prozesse hinzu:
```java
     simulator.startProcess("Process 2");
     simulator.stopProcess("Process 2");
```
3. Führe das Programm erneut aus und überprüfe die Ausgabe.

### Aufgabe 2: Simulator-Skeleton anpassen
1. Öffne die Datei `src/SimulatorSkeleton.java`.
2. **Implementiere die Methode `pauseProcess`**:
   - Entferne die Kommentarzeichen (`//`) vor der Methode `pauseProcess` und implementiere sie.
3. Rufe die neue Methode in `HelloSimulator.java` auf:
   ```java
   simulator.pauseProcess("Process 1");
   ```
### Aufgabe 3: Prozess-ID verwalten

1. Füge der SimulatorSkeleton-Klasse ein Attribut int `nextId` hinzu, das bei jedem `startProcess` inkrementiert wird.

```java
    private int nextId = 1;

    public int startProcess(String name) {
        int pid = nextId++;
        System.out.println("Starting process " + pid + ": " + name);
        return pid;
    }
```
2. Der zurückgegebene Wert `pid` muss nun verwendet werden, um den Prozess zu pausieren und zu stopeen stoppen (Code anpassen!)



---

## 💡 Hinweise
- **Fehlerbehebung**:
  - Falls Eclipse Fehler anzeigt:
    - Überprüfe, ob die Dateien **im `src`-Ordner** liegen.
    - Achte auf **Groß-/Kleinschreibung** (Java ist case-sensitive!).
    - Jede Anweisung endet mit einem **Semikolon (`;`)**.
- **Fragen?**
  - Nutze die Kommentare im Code oder wende dich an deinen Tutor.

---

## 📂 Dateien in diesem Verzeichnis

```
src/
└── woche01/
    ├── HelloSimulator.java    # Hauptprogramm mit `main`-Methode
    └── SimulatorSkeleton.java # Skeleton-Klasse für den Simulator
```

---



