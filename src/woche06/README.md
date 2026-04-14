# Woche 06: Synchronisation – Mutex, Condition Variables und Monitore

## Lernziele

- **Mutual Exclusion** mit einem Mutex verstehen.
- Den Unterschied zwischen **Simulation** und **echter Thread-Synchronisation** erkennen.
- Das Muster `while (!condition) wait()` verstehen und korrekt anwenden.
- Eine einfache Aufgabe mit **Condition Variables** (`wait()` / `notifyAll()`) implementieren.
- Producer–Consumer mit einem **Monitor** (`synchronized`, `wait`, `notifyAll`) umsetzen.

---

## Aufgaben

### **1. Mutex implementieren (20 Min)**

Implementieren Sie die Klasse `SimulatorMutex`. **Diese Klasse wird danach in Aufgabe 2 verwendet!**

**Wichtig:**
Diese Klasse blockiert **keine echten Java-Threads**. Sie ist eine **Simulation**:
- Wartende Prozesse werden in eine Warteschlange gelegt.
- Das PCB wird als `BLOCKED` markiert.
- `release()` weckt den nächsten Prozess.

**Aufgaben:**
1. Vervollständigen Sie `acquire()` und `release()`.
2. Erklären Sie kurz:
   - Warum ist diese Klasse für den Einstieg didaktisch nützlich?
   - Was fehlt noch, damit daraus ein echter Scheduler würde?

---

### **2. Warm-up: Single-Thread Semaphore Demo (5 Min)**

Die Klasse `CountingSemaphore` ist bereits vollständig implementiert.

**Ziel jetzt:**
- Führen Sie `MainSingleThreadSemaphoreDemo` aus.
- Erklären Sie in 2–3 Sätzen:
  - Warum darf der Code nach einem fehlgeschlagenen `acquire()` nicht weiterlaufen?
  - Warum ist das noch **kein echter Scheduler**?

---

### **3. Condition Variables mit `wait()` / `notifyAll()` (25–30 Min)**

Bevor wir Producer–Consumer lösen, isolieren wir das Kernmuster:

> Ein Thread wartet, bis eine Bedingung erfüllt ist.

Wir verwenden dazu eine `TaskQueue`:
- Worker warten in `take()`, solange keine Aufgabe vorhanden ist.
- `submit()` fügt neue Aufgaben ein und weckt Worker.
- `shutdown()` beendet das System sauber.

**Aufgaben:**
1. Vervollständigen Sie `TaskQueue.submit()`, `TaskQueue.take()` und `TaskQueue.shutdown()`.
2. Führen Sie `MainConditionDemo` aus.
3. Begründen Sie:
   - Warum `while(...) wait()` statt `if(...) wait()`?
   - Warum ist `wait()` effizienter als Busy Waiting?

---

### **4. Producer–Consumer mit Monitoren (35–40 Min)**

Implementieren Sie `BoundedBufferMonitor`.

**Aufgaben:**
1. Vervollständigen Sie:
   - `putWithSnapshot(...)`
   - `getWithSnapshot()`
2. Führen Sie `MainProducerConsumerThreads` aus.
3. Beobachten Sie:
   - Producer blockieren bei vollem Puffer.
   - Consumer blockieren bei leerem Puffer.
   - `logical=[...]` zeigt den tatsächlichen logischen Buffer-Inhalt.
4. Erklären Sie:
   - Welche Bedingung entspricht `bufferNotEmpty`?
   - Welche Bedingung entspricht `bufferNotFull`?

---

### **5. Diskussion (10 Min)**

- Was entspricht im Monitor der Semaphor-Idee `full.acquire()`?
- Was entspricht im Monitor der Semaphor-Idee `empty.acquire()`?
- Warum ist `notifyAll()` in Mehr-Thread-Szenarien oft robuster als `notify()`?

---

## 📂 Dateien in diesem Verzeichnis
| Datei | Beschreibung |
|-------|-------------|
| `ProcessState.java` | Prozesszustände für Simulation/Logging. |
| `ProcessControlBlock.java` | PCB mit `blockOn()` und `unblock()`. |
| `SimulatorMutex.java` | Gerüst (Aufgabe 1): Mutex-Simulation. |
| `CountingSemaphore.java` | Vollständige Single-Thread-Semaphore für das Warm-up. |
| `MainSingleThreadSemaphoreDemo.java` | Demonstriert die Semaphor-Logik ohne echten Scheduler. |
| `TaskQueue.java` | Gerüst (Aufgabe 3): Condition Variable Pattern. |
| `Worker.java` | Worker-Thread für Aufgabe 3. |
| `MainConditionDemo.java` | Testprogramm für TaskQueue/Worker. |
| `RingBuffer.java` | Ringpuffer mit `logical`-Snapshot. |
| `BoundedBufferMonitor.java` | Gerüst (Aufgabe 4): Producer–Consumer mit Monitoren. |
| `ProducerThread.java` | Producer für Aufgabe 4. |
| `ConsumerThread.java` | Consumer für Aufgabe 4. |
| `MainProducerConsumerThreads.java` | Startet 2 Producer + 2 Consumer. |

---

# 📚 Verwendete Java-Klassen und ihre Dokumentation

In diesem Projekt werden einige Klassen aus der Java-Standardbibliothek verwendet.
Die folgenden Links führen zur offiziellen Dokumentation der jeweiligen Klasse.

| Klasse                                                                                                                              | Zweck                                                                                                                                                      |
| ----------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **[`Thread`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Thread.html)**                                  | Repräsentiert einen Ausführungs-Thread in Java. Wird verwendet, um Producer- und Consumer-Threads zu starten.                                              |
| **[`Runnable`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Runnable.html)**                              | Ein Interface für Klassen, deren Objekte von einem Thread ausgeführt werden können. `ProducerThread` und `ConsumerThread` implementieren dieses Interface. |
| **[`ThreadLocalRandom`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/concurrent/ThreadLocalRandom.html)** | Liefert zufällige Zahlen pro Thread. Wird hier verwendet, um zufällige Pausen (`sleep`) einzubauen, damit Interleavings sichtbarer werden.                 |
| **[`ArrayDeque<E>`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/ArrayDeque.html)**                       | Eine effiziente doppelt verkettete Warteschlange (Deque). Wird in der `TaskQueue` verwendet, um Aufgaben zu speichern.                                     |
| **[`Deque<E>`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Deque.html)**                                 | Interface für doppelt verkettete Warteschlangen. Ermöglicht FIFO- oder LIFO-Zugriff.                                                                       |
| **[`Arrays`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Arrays.html)**                                  | Hilfsklasse für Array-Operationen. Wird in `RingBuffer.snapshot()` verwendet, um den Inhalt des Puffers als String darzustellen.                           |
| **[`InterruptedException`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/InterruptedException.html)**      | Wird ausgelöst, wenn ein Thread während `sleep()` oder `wait()` unterbrochen wird.                                                                         |
| **[`Object.wait()`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Object.html#wait%28%29)**                | Blockiert einen Thread, bis ein anderer Thread ihn mit `notify()` oder `notifyAll()` weckt.                                                                |
| **[`Object.notifyAll()`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Object.html#notifyAll%28%29)**      | Weckt alle Threads auf, die auf dem gleichen Monitor mit `wait()` warten.                                                                                  |

---

# 🔗 Weiterführende Ressourcen

Die folgenden Ressourcen helfen dabei, das Thema Synchronisation und Nebenläufigkeit in Java besser zu verstehen.

### Java-Tutorials

* **Java Concurrency Tutorial**
  [https://docs.oracle.com/javase/tutorial/essential/concurrency/](https://docs.oracle.com/javase/tutorial/essential/concurrency/)
  Offizielles Tutorial zu Threads, Synchronisation und Parallelität.

* **Java Concurrency (API Übersicht)**
  [https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/concurrent/package-summary.html](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/concurrent/package-summary.html)
  Übersicht über die Klassen des Pakets `java.util.concurrent`.

---

### Konzepte der Synchronisation

* **Producer–Consumer Problem (Wikipedia)**
  [https://en.wikipedia.org/wiki/Producer%E2%80%93consumer_problem](https://en.wikipedia.org/wiki/Producer%E2%80%93consumer_problem)

* **Monitor (Synchronisationskonzept)**
  [https://en.wikipedia.org/wiki/Monitor_(synchronization)](https://en.wikipedia.org/wiki/Monitor_%28synchronization%29)

* **Condition Variables**
  [https://en.wikipedia.org/wiki/Monitor_(synchronization)#Condition_variables](https://en.wikipedia.org/wiki/Monitor_%28synchronization%29#Condition_variables)

---

### Weiterführende Java-Bibliotheken

In professionellen Anwendungen werden oft zusätzliche Synchronisationsklassen verwendet:

* **[`ReentrantLock`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/concurrent/locks/ReentrantLock.html)**
  Erweiterter Lock mit mehr Kontrolle als `synchronized`.

* **[`Condition`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/concurrent/locks/Condition.html)**
  Condition Variables im `java.util.concurrent`-Framework.

* **[`BlockingQueue`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/concurrent/BlockingQueue.html)**
  Eine fertige thread-sichere Warteschlange, die das Producer-Consumer-Problem bereits implementiert.

---

## 💡 **Wichtiger Hinweis**

> In dieser Übung implementieren wir Synchronisationsmechanismen **selbst**, um die Konzepte zu verstehen.
> In der Praxis würde man oft fertige Klassen wie **`BlockingQueue`** verwenden.
