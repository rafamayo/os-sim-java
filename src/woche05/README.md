# Woche 05: Nebenläufigkeit – Threads, Interleavings und Race Conditions

## Lernziele

- Unterschied zwischen **Prozess** und **Thread** beschreiben.
- Verstehen, dass **Thread-Scheduling nicht deterministisch** ist.
- Den Lebenszyklus einfacher Threads mit `start()` und `join()` nachvollziehen.
- **Race Conditions** an einem gemeinsamen Zähler beobachten und erklären.
- Verstehen, warum **geteilte Daten ohne Synchronisation problematisch** sind.

---

## Aufgaben

### **1. Thread-Basics und Interleavings (15–20 Min)**

In dieser Aufgabe starten Sie zwei Threads, die jeweils einige Nachrichten ausgeben.

**Ziel:** Beobachten, dass die Reihenfolge der Ausgaben **nicht fest vorhersagbar** ist.

**Aufgaben:**
1. Vervollständigen Sie die Klasse `MessagePrinter`.
2. Führen Sie `MainThreadBasicsDemo` mehrmals aus.
3. Beschreiben Sie kurz:
   - Warum unterscheiden sich die Ausgaben von Lauf zu Lauf?
   - Welche Aufgabe übernimmt `join()`?

---

### **2. Race Condition am gemeinsamen Zähler (20–25 Min)**

Nun betrachten wir zwei Threads, die beide denselben Zähler erhöhen.

**Ziel:** Erkennen, dass selbst eine scheinbar einfache Operation wie

```java
counter++;
```

nicht atomar ist.

**Aufgaben:**
1. Vervollständigen Sie `SharedCounter.increment()`.
2. Starten Sie `MainRaceConditionDemo`.
3. Vergleichen Sie:
   - den **erwarteten Wert**
   - den **tatsächlich beobachteten Wert**

**Diskussion:**
- Warum ist der Endwert oft kleiner als erwartet?
- Warum tritt der Fehler nicht in jedem Lauf gleich stark auf?

---


### **3. Race Condition bei Prozesszuständen (15–20 Min)**

In den vorherigen Aufgaben haben Sie gesehen, dass ein gemeinsam genutzter **Zähler (`counter`)** durch mehrere Threads inkonsistent werden kann.

In dieser Aufgabe betrachten wir statt einer Zahl einen anderen wichtigen Bestandteil eines Betriebssystems: den **Prozesszustand**.

Ein Betriebssystem verwaltet für jeden Prozess ein sogenanntes **Process Control Block (PCB)**.
Darin wird unter anderem der aktuelle Zustand des Prozesses gespeichert.

Typische Zustände sind zum Beispiel:

```
NEW → READY → RUNNING → TERMINATED
```

Diese Zustände werden von verschiedenen Teilen des Betriebssystems verändert, zum Beispiel:

* Scheduler
* Interrupt-Handler
* I/O-Subsystem

Damit greifen **mehrere Komponenten gleichzeitig auf dieselbe Zustandsinformation zu**.


**Ziel der Aufgabe**

In dieser Demo greifen mehrere Threads gleichzeitig auf dasselbe `ProcessControlBlock`-Objekt zu und verändern dessen Zustand.

Dadurch kann die beobachtete Reihenfolge der Zustandsänderungen **von Lauf zu Lauf unterschiedlich sein**.

Diese Aufgabe soll zeigen:

* Auch **Zustandsinformationen** sind gemeinsam genutzte Daten.
* Nebenläufige Änderungen können zu **unerwarteten oder inkonsistenten Zustandsfolgen** führen.


**Vorgehen**

1. Öffnen Sie die Dateien

```
ProcessControlBlock.java
StateTransitionWorker.java
MainProcessStateRaceDemo.java
```

2. Starten Sie das Programm:

```
MainProcessStateRaceDemo
```

3. Führen Sie das Programm **mehrmals** aus.


**Beobachtung**

Beachten Sie:

* Welche Zustände werden von den Threads gesetzt?
* In welcher Reihenfolge erscheinen die Zustandsänderungen?
* Ist die Reihenfolge in jedem Programmlauf gleich?

Beispielhafte Ausgabe:

```
[T1] set state -> READY
[T2] set state -> READY
[T1] set state -> RUNNING
[T2] set state -> TERMINATED
```

In einem anderen Lauf kann die Reihenfolge anders sein.


**Fragen**

Beantworten Sie kurz:

1. Warum ist das Verhalten dieses Programms **nicht deterministisch**?
2. Welche Threads verändern denselben gemeinsamen Zustand?
3. Warum müssen Betriebssysteme Änderungen an wichtigen Datenstrukturen (z. B. PCB oder Warteschlangen) schützen?


**Wichtige Erkenntnis**

Nicht nur Zahlen können durch Nebenläufigkeit problematisch werden.

Auch **Zustandsinformationen** oder **Systemdatenstrukturen** sind gemeinsame Ressourcen.

Ohne geeignete Synchronisation können mehrere Threads gleichzeitig Änderungen durchführen und damit **inkonsistente Systemzustände** erzeugen.


**Ausblick**

In der nächsten Woche lernen Sie Mechanismen kennen, mit denen solche Probleme verhindert werden können, zum Beispiel:

* **Mutex**
* **Semaphore**
* **Monitore**

Diese Mechanismen sorgen dafür, dass bestimmte Codebereiche nur von **einem Thread gleichzeitig** ausgeführt werden.

Diese Bereiche nennt man **kritische Abschnitte (critical sections)**.

---

### **4. Diskussion (10 Min)**

Beantworten Sie kurz:
- Was ist der wichtigste Unterschied zwischen **Nebenläufigkeit** und **Parallelität**?
- Warum sind Race Conditions so schwer zu debuggen?
- Warum ist ein gemeinsam genutzter Zähler ein gutes Minimalbeispiel?

---

## 📂 Dateien in diesem Verzeichnis
| Datei | Beschreibung |
|-------|-------------|
| `ProcessState.java` | Vereinfachte Prozesszustände für die Demo. |
| `ProcessControlBlock.java` | Vereinfachtes PCB für Logging und Zustandswechsel. |
| `MessagePrinter.java` | Gerüst/Aufgabe 1: Ein Thread gibt mehrere Nachrichten aus. |
| `MainThreadBasicsDemo.java` | Startet zwei `MessagePrinter`-Threads. |
| `SharedCounter.java` | Gerüst/Aufgabe 2: Gemeinsamer Zähler ohne Synchronisation. |
| `CounterWorker.java` | Erhöht einen gemeinsamen Zähler mehrfach. |
| `StateTransitionWorker.java` | Veändert denselben PCB-Zustand mehrfach. |
| `MainRaceConditionDemo.java` | Demonstriert eine Race Condition. |
| `MainProcessStateRaceDemo.java` | Kleine Demo zu Race Conditions bei Prozesszuständen. |

---

### **📚 Verwendete Java-Klassen und ihre Dokumentation**

| Klasse | Zweck |
|--------|-------|
| **[`Thread`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Thread.html)** | Grundlegende Klasse für Threads in Java. |
| **[`Runnable`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Runnable.html)** | Interface für nebenläufig auszuführende Aufgaben. |
| **[`ThreadLocalRandom`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/concurrent/ThreadLocalRandom.html)** | Erzeugt kleine zufällige Pausen, damit Interleavings sichtbarer werden. |

---

### **💡 Warum werden diese Klassen verwendet?**

1. **`Thread`**:
   - Startet echte Ausführungspfade neben dem Main-Thread.
   - Zeigt, dass Scheduling vom Betriebssystem / der JVM gesteuert wird.

2. **`Runnable`**:
   - Trennt die **Aufgabe** vom **Thread-Objekt**.
   - Das ist in Java das typische Muster.

3. **`ThreadLocalRandom`**:
   - Kleine Pausen machen Interleavings leichter sichtbar.
   - Das ist didaktisch hilfreich, weil Race Conditions dadurch häufiger beobachtbar werden.

---

### **🔗 Weiterführende Ressourcen**

- **[Java Tutorials: Concurrency](https://docs.oracle.com/javase/tutorial/essential/concurrency/)**
- **[Thread (Java 17 API)](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Thread.html)**
- **[Runnable (Java 17 API)](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Runnable.html)**

---
