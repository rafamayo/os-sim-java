# Woche 07: Deadlocks – Erkennung und Prävention

**Lernziele:**

1. Die vier **Coffman-Bedingungen** am laufenden Programm identifizieren und erklären.
2. Einen **Deadlock provozieren** (Dining Philosophers, naive Variante) und beobachten.
3. **Lock Ordering** als Prevention-Strategie implementieren und den Effekt messen.
4. *(Bonus)* Einen **Wait-for Graph** mit DFS-Zyklusdetektion implementieren.
5. *(Bonus)* Eine einfache **Recovery-Strategie** (Victim Selection, Process Abort) anwenden.

---

## 📌 Vorbereitung

1. **Wiederhole Woche 02/03:**
   - `ProcessState` wird aus Woche 03 wiederverwendet (inkl. `BLOCKED`-Zustand).
   - In Woche 07 ist `BLOCKED` besonders wichtig: ein Prozess im `BLOCKED`-Zustand wartet auf eine Ressource.

2. **Klone das aktualisierte Repo** und öffne den Ordner `os-sim/` in VS Code.

3. **Starte die Simulation** über das Run-&-Debug-Panel (▶) oder im Terminal:
   ```bash
   # vom Verzeichnis os-sim/src aus:
   javac woche07/*.java
   java woche07.Main partA_deadlock
   ```

4. **Mache dich mit der Tischanordnung vertraut:**

```
            P0
           /  \
         G0    G1
         /      \
       P4        P1
       |          |
       G4        G2
         \       /
          P3    P2
            \  /
             G3

  Pi hält G(i) links, G((i+1)%5) rechts
```

Jeder Philosoph Pi benötigt zum Essen genau **zwei Gabeln**: G(i) links und G((i+1)%5) rechts. Gabeln sind geteilte Ressourcen – sie können nur von **einem** Philosophen gleichzeitig gehalten werden.

---

## ⏱ Zeitplan (90 Minuten)

| Zeit | Inhalt |
|------|--------|
| 0–10 min | Vorbereitung, Projekt öffnen, Tischanordnung verstehen |
| 10–20 min | Aufgabe 1: Deadlock provozieren |
| 20–30 min | Aufgabe 1: Beobachten und Fragen beantworten |
| 30–45 min | Aufgabe 2: Lock Ordering implementieren und vergleichen |
| 45–60 min | Aufgabe 3: Watchdog-Monitor ergänzen |
| 60–90 min | **Bonus** Aufgaben 4–6 (für schnelle Gruppen) |

---

## 📝 Pflichtaufgaben

### **Aufgabe 1: Deadlock provozieren – `DeadlockPhilosopher.java`**

**Ziel:** Verstehen, wie die vier Coffman-Bedingungen gleichzeitig erfüllt werden.

Implementiere die naive Gabel-Strategie in der Methode `dine()`:

```java
// TODO (Aufgabe 1a): Nimm die Gabeln in der NAIVEN Reihenfolge auf.
//   Schritt 1: Nimm die linke Gabel auf  → leftFork.pickUp(id)
//   Schritt 2: Warte SimConfig.FORK_PICKUP_DELAY_MS ms → Thread.sleep(...)
//   Schritt 3: Nimm die rechte Gabel auf → rightFork.pickUp(id)
```

Lege danach die Gabeln wieder ab (rechte zuerst, dann linke).

**💡 Hinweis:** Die `FORK_PICKUP_DELAY_MS`-Pause zwischen den beiden `pickUp()`-Aufrufen ist entscheidend: Sie vergrößert das Zeitfenster, in dem alle Philosophen gleichzeitig ihre erste Gabel halten und auf die zweite warten – genau die Situation, die zum Deadlock führt.

**Starte die Simulation:**
```
java woche07.Main partA_deadlock
```

**Beobachtungsfragen:**
- Tritt der Deadlock sofort auf oder erst nach einigen Runden? Starte die Simulation mehrfach – was fällt auf?
- Zeige für jede der vier Coffman-Bedingungen die konkrete Stelle im Quelltext.
- Was meldet der Watchdog? Nach wie vielen Millisekunden schlägt er an?

---

### **Aufgabe 2: Deadlock verhindern – `SafePhilosopher.java`**

**Ziel:** Lock Ordering als Prevention-Strategie implementieren.

Im Konstruktor müssen `firstFork` und `secondFork` korrekt belegt werden:

```java
// TODO (Aufgabe 2): Implementiere Lock Ordering.
//   Weise firstFork und secondFork so zu, dass firstFork IMMER
//   die Gabel mit der kleineren ID ist.
//   Nutze leftFork.getId() und rightFork.getId() zum Vergleich.
this.firstFork  = null; // TODO: ersetzen
this.secondFork = null; // TODO: ersetzen
```

**Starte die Simulation:**
```
java woche07.Main partA_safe
```

**Beobachtungsfragen:**
- Terminiert die Simulation diesmal ohne Watchdog-Eingriff?
- Welche Coffman-Bedingung wird durch Lock Ordering eliminiert?
- Vergleiche die Gesamtlaufzeit zwischen `partA_deadlock` und `partA_safe`. Was fällt auf?
- **Grenzfall P4:** Welche Gabeln nimmt P4 in welcher Reihenfolge? Erkläre, warum P4 in der naiven Variante der „kritische" Philosoph ist, der den Zyklus schließt.

---

### **Aufgabe 3: Watchdog-Monitor – `DeadlockWatchdog.java`**

**Ziel:** Timeout-basierte Deadlock-Heuristik verstehen und ergänzen.

**3a)** Zähle die lebenden Philosophen im BLOCKED-Zustand:

```java
// TODO (Aufgabe 3a): Zähle lebende Philosophen im BLOCKED-Zustand.
//   filter 1: p.isAlive()
//   filter 2: p.getPhilosopherState() == ProcessState.BLOCKED
long blockedCount = 0; // TODO: ersetzen
```

**3b)** Zähle alle noch lebenden Philosophen (`aliveCount`).

**3c)** Unterbreche alle Philosophen-Threads als Recovery-Maßnahme:

```java
// TODO (Aufgabe 3c): philosophers.forEach(Thread::interrupt)
```

**💡 Hinweis:** `Thread::interrupt` ist eine Methoden-Referenz. Wenn ein Thread in `lockInterruptibly()` wartet, wird er sofort mit einer `InterruptedException` unterbrochen – das ist die Recovery-Maßnahme (Process Abort).

**Diskussionsfrage:**
- Der Watchdog meldet einen Deadlock, wenn alle Threads BLOCKED sind. Kann das auch ohne echten Deadlock passieren (False Positive)? Nenne ein konkretes Beispiel.
- Vergleiche diesen Ansatz mit der exakten Zyklusdetektion (Teil B): Was sind die Vor- und Nachteile jedes Ansatzes?

---

## 🏆 Bonusaufgaben *(für schnelle Gruppen)*

> Die folgenden Aufgaben bauen auf den Pflichtaufgaben auf. `WaitForGraph.java` und `ResourceManager.java` sind bereits vollständig implementiert – lese sie durch, bevor du beginnst.

### **Aufgabe 4: DFS-Zyklusdetektion verstehen – `WaitForGraph.java`**

**Ziel:** Den implementierten Algorithmus nachvollziehen und testen.

`WaitForGraph.java` ist fertig. Lies die Methoden `detectDeadlock()` und `dfs()` und beantworte:

- Was ist der Unterschied zwischen `visited` und `recursionStack`?
- Warum reicht es nicht, nur `visited` zu prüfen?
- Verfolge den Algorithmus manuell für den Graphen `P0→P1→P2→P0`:
  In welchem Schritt wird die Rückkante erkannt?

Starte Teil B und prüfe alle Szenarien:
```
java woche07.Main partB
```

Erwartete Ausgaben:
- B1: `✅ Kein Deadlock.`
- B2: `✅ Kein Deadlock.`
- B3: `🔴 DEADLOCK! Zyklus: P0→P1→P0`
- B4: `🔴 DEADLOCK! Zyklus: P0→P1→P2→P0`

---

### **Aufgabe 5: ResourceManager verstehen – `ResourceManager.java`**

**Ziel:** Die Integration von WFG in einen echten Ressourcenmanager nachvollziehen.

`ResourceManager.java` ist fertig. Lies `requestResource()` und beantworte:

- Wann wird eine Wartekante in den WFG eingefügt, wann wieder entfernt?
- Was passiert, wenn `checkDeadlock()` einen Zyklus findet?

Szenario B5 in `Main.runPartB()` baut folgenden Zyklus auf:
```
P0 hält R1, wartet auf R2 (hält P1)  →  WFG: P0 → P1
P1 hält R2, wartet auf R3 (hält P2)  →  WFG: P1 → P2
P2 hält R3, wartet auf R1 (hält P0)  →  WFG: P2 → P0  ← ZYKLUS!
```

Du solltest sehen:
```
Exception: 🔴 DEADLOCK erkannt! Zyklus: P0 → P1 → P2 → P0
```

---

### **Aufgabe 6: Eigenes Experiment – `Main.java`**

**Ziel:** Grenzen der Zyklusdetektion verstehen.

Ergänze das Szenario am Ende von `runPartB()` (der `TODO`-Kommentar):

Erstelle einen WFG mit 4 Prozessen (P0–P3):
- P0→P1, P1→P2, P2→P0 (Zyklus)
- P3→P1 (P3 wartet auf P1, ist aber nicht Teil des Zyklus)

**Fragen:**
- Wird P3 als Teil des Deadlocks gemeldet?
- Welche Prozesse sind tatsächlich blockiert? Kann P3 jemals fortfahren?
- Wie würde ein reales OS mit P3 umgehen?

---

## 💡 Hinweise

- **Zustandsübergänge in Woche 07:**
  - `NEW` → `READY` (Thread startet)
  - `READY` / `RUNNING` ↔ `BLOCKED` (hungrig / Gabel erhalten)
  - `RUNNING` → `TERMINATED` (alle Runden abgeschlossen)

- **`lockInterruptibly()` vs. `lock()`:**
  - `lock()` blockiert ununterbrechbar.
  - `lockInterruptibly()` wirft `InterruptedException` bei `Thread.interrupt()` – unverzichtbar für Recovery.

- **Nicht-Determinismus:** Der Deadlock in Aufgabe 1 tritt nicht bei jedem Lauf auf. Starte die Simulation mehrmals – das ist normales Verhalten bei nebenläufigen Fehlern.

- **`volatile` bei `state`:** Ohne `volatile` könnte der Watchdog-Thread einen gecachten, veralteten Zustand lesen.

---

## 📂 Dateien in diesem Verzeichnis

| Datei | Beschreibung | Status |
|-------|-------------|--------|
| `ProcessState.java` | Prozesszustände (inkl. `BLOCKED`). | **Fertig** |
| `SimConfig.java` | Simulationsparameter. | **Fertig** |
| `Fork.java` | Gabel als `ReentrantLock`. | **Fertig** |
| `DeadlockPhilosopher.java` | Philosoph, naive Strategie. | **Aufgabe 1** |
| `SafePhilosopher.java` | Philosoph, Lock Ordering. | **Aufgabe 2** |
| `DeadlockWatchdog.java` | Timeout-basierter Deadlock-Monitor. | **Aufgabe 3** |
| `WaitForGraph.java` | Wait-for Graph mit DFS. | **Fertig** *(Bonus: Aufgabe 4)* |
| `ResourceManager.java` | Ressourcenmanager mit WFG. | **Fertig** *(Bonus: Aufgabe 5)* |
| `Main.java` | Testszenarien Teil A + B. | **Bonus: Aufgabe 6* |

---

## 📚 Verwendete Java-Klassen

| Klasse | Zweck |
|--------|-------|
| **[`ReentrantLock`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/concurrent/locks/ReentrantLock.html)** | Expliziter Mutex; `lockInterruptibly()` für unterbrechbares Warten. |
| **[`Thread`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Thread.html)** | Nebenläufige Ausführung; `interrupt()` für Recovery. |
| **[`Map<K,V>`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Map.html)** | Adjazenzliste im WFG: `Map<Integer, Set<Integer>>`. |
| **[`Set<E>`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Set.html)** | `visited` und `recursionStack` in DFS – O(1) für `contains()`. |
| **[`LinkedHashMap`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/LinkedHashMap.html)** | HashMap mit stabiler Einfügereihenfolge (konsistente Ausgaben). |
| **[`volatile`](https://docs.oracle.com/javase/specs/jls/se17/html/jls-8.html#jls-8.3.1.4)** | Sichtbarkeit von `state` über Thread-Grenzen (Watchdog liest aus anderem Thread). |

---

### 💡 Warum werden diese Klassen verwendet?

1. **`ReentrantLock`** statt `synchronized`

Das klassische `synchronized`-Schlüsselwort in Java ist nicht unterbrechbar: ein Thread, der auf einen `synchronized`-Block wartet, ignoriert `Thread.interrupt()` vollständig. Für die Simulation ist das fatal – der Watchdog könnte deadlocked Threads niemals beenden. `ReentrantLock` mit `lockInterruptibly()` löst genau dieses Problem: sobald der Watchdog `interrupt()` aufruft, wird der wartende Thread sofort mit einer `InterruptedException` aufgeweckt und kann die Gabel loslassen.

2. **`Thread`**

Jeder Philosoph ist ein eigenständiger `Thread`, weil er unabhängig und nebenläufig zu den anderen denkt und isst – genau wie Prozesse in einem echten Betriebssystem. Die Vererbung von `Thread` (statt `Runnable`) macht den Code kompakter und erlaubt direkten Zugriff auf `Thread.interrupt()` von außen, was für die Recovery-Logik im Watchdog benötigt wird.

3. **`Map<Integer, Set<Integer>>`**

Der Wait-for Graph wird als Adjazenzliste gespeichert: jeder Prozess (Integer-Schlüssel) zeigt auf die Menge der Prozesse, auf die er wartet (Set als Wert). Eine Adjazenzmatrix wäre einfacher zu verstehen, aber unpraktisch: sie hat feste Größe und wäre für 3 Prozesse schon eine 3×3-Matrix, die bei jedem Hinzufügen eines Prozesses neu alloziert werden müsste. Die Map wächst dynamisch und erlaubt beliebige Prozess-IDs als Schlüssel.

4. **`Set<E>`** für `visited` und `recursionStack`

Die DFS braucht in jedem Schritt die Antwort auf „Ist Knoten X bereits besucht?" – und das sehr oft. Mit einer `List` wäre das O(n) pro Abfrage, mit einem `HashSet` ist es O(1). Bei einem Graphen mit 100 Prozessen macht das den Unterschied zwischen einem schnellen und einem praktisch unbenutzbar langsamen Algorithmus. Außerdem sind Mengen semantisch präziser: `visited` und `recursionStack` sind per Definition keine geordneten Sequenzen.

5. **`LinkedHashMap`** statt `HashMap`

Beide haben die gleiche O(1)-Performance für `get` und `put`. Der Unterschied liegt in der Iteration: `HashMap` liefert Einträge in undefinierter Reihenfolge, `LinkedHashMap` in der Reihenfolge, in der sie eingefügt wurden. Das hat keinen Einfluss auf die Korrektheit des Algorithmus, aber auf die Ausgabe: ohne `LinkedHashMap` würde der Wait-for Graph bei jedem Programmstart in zufälliger Reihenfolge gedruckt, was die Ausgaben schwer vergleichbar macht – besonders in einer Lehrumgebung, wo Studierende ihre Ausgabe mit einer Erwartung abgleichen.

6. **`volatile`**

Java-Threads dürfen aus Performance-Gründen Variablen in CPU-Register oder Thread-lokale Caches kopieren und dort veraltet belassen. Ohne `volatile` könnte der Watchdog-Thread die Variable `state` eines Philosophen-Threads lesen und dabei einen gecachten, längst veralteten Wert sehen – z.B. `RUNNING`, obwohl der Philosoph schon seit Sekunden `BLOCKED` ist. `volatile` erzwingt, dass jeder Lesezugriff direkt aus dem Hauptspeicher erfolgt. Eine Alternative wäre `AtomicReference<ProcessState>`, die aber für diesen einfachen Fall überdimensioniert ist.

---

### 🔗 Weiterführende Ressourcen

- **[OSTEP – Common Concurrency Problems (Kapitel 32)](https://pages.cs.wisc.edu/~remzi/OSTEP/threads-bugs.pdf)**
- **[Java Concurrency in Practice – Kapitel 10](https://jcip.net/)**: Deadlocks, Lock Ordering, `tryLock`
- **[ReentrantLock Tutorial (Baeldung)](https://www.baeldung.com/java-concurrent-locks)**
- **Tanenbaum**: Modern Operating Systems, Kapitel 6
