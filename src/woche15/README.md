# Woche 15 — Prozesskommunikation (IPC): Pipes, Message Queues, Shared Memory

**Betriebssysteme — Praktikum**
**Hochschule Kempten, Fakultät Informatik**
**Prof. Dr. Rafael Mayoral Malmström — Sommersemester 2026**

---

## Lernziele

Nach dieser Übung können Sie:

- Die drei IPC-Mechanismen **Pipe**, **Message Queue** und **Shared Memory**
  voneinander unterscheiden und ihren Einsatz begründen
- Das **Blocking-Muster** (`while + wait + notifyAll`) auf IPC-Ebene anwenden
- Erklären warum **Shared Memory** explizites Signaling erfordert
  während Pipe und Message Queue es eingebaut haben
- Den richtigen IPC-Mechanismus für einen gegebenen Anwendungsfall wählen

---

## Hintergrund

Aus den Wochen 3–4 wissen wir: Prozesse sind isoliert — jeder hat seinen eigenen
Adressraum, keiner kann den Speicher eines anderen lesen. Das ist gut für Sicherheit
und Stabilität. Aber manchmal müssen Prozesse zusammenarbeiten.

**IPC (Inter-Process Communication)** bezeichnet alle Mechanismen mit denen
Prozesse Daten austauschen — ohne ihren Adressraum aufzugeben:

| Mechanismus | Kernidee | Kopieren? | Synchronisation |
|---|---|---|---|
| **Pipe** | Kernel-Puffer, Bytestrom | ja (in Kernel) | eingebaut |
| **Message Queue** | Kernel-Warteschlange, Nachrichten | ja (in Kernel) | eingebaut |
| **Shared Memory** | gemeinsamer physischer Block | nein (zero-copy) | **manuell** |

**Das zentrale Muster dieser Woche:**
Alle drei Mechanismen verwenden im Kern dasselbe Synchronisationsmuster
das Sie aus Woche 6 kennen:

```java
// Producer: blockiert wenn kein Platz
synchronized { while (voll) wait(); /* schreiben */ notifyAll(); }

// Consumer: blockiert wenn keine Daten
synchronized { while (leer) wait(); /* lesen  */ notifyAll(); }
```

Bei Pipe und Message Queue ist dieses Muster eingebaut.
Bei Shared Memory müssen Sie es selbst implementieren.

---

## Aufgaben

### Aufgabe 1 — Pipe beobachten (15 Minuten)

Öffnen Sie `Pipe.java`. Die Klasse ist vollständig — lesen Sie sie.
Führen Sie dann `PipeDemo` aus (in `Demos.java`).

**Fragen:**

a) Eine Pipe ist ein **Bytestrom** — keine Nachrichtengrenzen. Was bedeutet
   das konkret? Wenn Prozess A `write("Hallo")` und dann `write("Welt")`
   aufruft, was liest Prozess B mit einem einzigen `read(10)`?

b) Was passiert wenn Prozess B `read()` aufruft aber die Pipe leer ist?
   Suchen Sie die entsprechende Stelle im Code (`Pipe.java`, Methode `read()`).

c) Was bedeutet **EOF** bei einer Pipe? Wann tritt es auf, und wie erkennt
   der Leser es?

d) Beobachten Sie die Demo-Ausgabe: Die Nachrichten "Hallo ", "Welt!" usw.
   werden einzeln geschrieben aber zusammen gelesen.
   Warum ist das bei einer Message Queue anders?

---

### Aufgabe 2 — BlockingRingBuffer implementieren (25 Minuten)

Öffnen Sie `BlockingRingBuffer.java`. Implementieren Sie `put()` und `take()`.

Dies ist das **Kernmuster** der gesamten Woche — alle anderen IPC-Mechanismen
bauen auf denselben Ideen auf.

**`put(T item)`** — Element einlegen:

```java
public synchronized void put(T item) throws InterruptedException {
    while (count == buf.length) wait();  // Puffer voll: warten
    buf[tail] = item;
    tail = (tail + 1) % buf.length;      // Ring-Wrap-around
    count++;
    totalPuts++;
    notifyAll();                          // Consumer aufwecken
}
```

**`take()`** — Element entnehmen:

```java
public synchronized T take() throws InterruptedException {
    while (count == 0) wait();            // Puffer leer: warten
    T item = (T) buf[head];
    buf[head] = null;                     // GC-freundlich
    head = (head + 1) % buf.length;      // Ring-Wrap-around
    count--;
    totalTakes++;
    notifyAll();                          // Producer aufwecken
    return item;
}
```

**Testen:** Führen Sie `RingBufferDemo` aus (in `Demos.java`).
Zwei Producer, zwei Consumer — beobachten Sie die Ausgabe.

**Prüfen Sie Ihre Implementierung:**

- Führt sich der Ring-Wrap-around korrekt durch? (`toString()` zeigt head und tail)
- Blockiert `put()` wirklich wenn der Puffer voll ist?
- Weckt `notifyAll()` immer die richtige Seite auf?

---

### Aufgabe 3 — MessageQueue implementieren (20 Minuten)

Öffnen Sie `MessageQueue.java`. Implementieren Sie `send()` und `receive()`.

Das Muster ist dasselbe wie in Aufgabe 2 — der Unterschied liegt in der
Datenstruktur: statt eines Arrays mit Indizes verwenden wir eine `Queue`.

**`send(Message msg)`** — Nachricht senden:
```
WHILE queue.size() >= capacity: wait()   // Queue voll: warten
queue.add(msg)
totalSent++
notifyAll()                               // Empfänger aufwecken
```

**`receive()`** — Nachricht empfangen:
```
WHILE queue.isEmpty() UND !closed: wait() // Queue leer: warten
Falls queue.isEmpty(): return null         // EOF
Message msg = queue.poll()
totalReceived++
notifyAll()                                // Sender aufwecken
return msg
```

**Testen:** Führen Sie `MessageQueueDemo` aus (in `Demos.java`).

**Beobachten Sie den Unterschied zur Pipe:**
Jedes `send()` entspricht genau einem `receive()` — Nachrichtengrenzen
bleiben erhalten. Bei der Pipe könnte ein `read()` mehrere `write()`-Aufrufe
zusammen lesen.

---

### Aufgabe 4 — SharedMemory: Signaling implementieren (15 Minuten)

Öffnen Sie `SharedMemory.java`. `write()` und `read()` sind bereits
implementiert — lesen Sie sie kurz.

Ihre Aufgabe: `writeAndNotify()` und `waitForData()` implementieren.

**Das Problem ohne Signaling:**
```java
// Producer schreibt
shm.write(0, "Hallo".getBytes());

// Consumer liest sofort — aber hat der Producer schon geschrieben?
String data = shm.readString(5);  // möglicherweise leerer Buffer!
```

Ohne Signaling gibt es keine Garantie dass der Consumer wartet bis
der Producer fertig ist. Das ist der fundamentale Unterschied zu
Pipe und Message Queue.

**`writeAndNotify(int offset, byte[] data)`**:
```
synchronized(lock) {
    System.arraycopy(data, 0, buffer, offset, data.length)
    writes++
    dataAvailable = true
    lock.notifyAll()     // wartenden Consumer aufwecken
}
```

**`waitForData()`**:
```
synchronized(lock) {
    WHILE !dataAvailable: lock.wait()   // schlafen bis Daten da
    dataAvailable = false               // zurücksetzen für nächstes Mal
}
```

**Testen:** Führen Sie `SharedMemoryDemo.main()` aus.

Das Programm zeigt drei Szenarien:
- **Szenario 1** (ohne Signaling): Consumer liest sofort — beobachten Sie was er liest.
- **Szenario 2** (mit Signaling): Consumer wartet korrekt auf den Producer.
- **Szenario 3** (mehrere Nachrichten): Producer-Consumer mit 5 Nachrichten.

**Beobachten Sie Szenario 1 genau:**
Der Consumer startet vor dem Producer. Ohne `waitForData()`
liest er sofort — was steht zu diesem Zeitpunkt im Buffer?
Warum ist das das Grundproblem von Shared Memory ohne Synchronisation?

---

### Aufgabe 5 — Vergleich und Reflexion (15 Minuten)

Führen Sie `IPCComparisonDemo` vollständig aus (alle drei Mechanismen
mit denselben 10 Nachrichten).

**a)** Welcher Mechanismus ist am einfachsten zu verwenden? Warum?

**b)** Welchen Mechanismus würden Sie für folgende Szenarien wählen?

| Szenario | Mechanismus | Begründung |
|---|---|---|
| Webserver verteilt HTTP-Anfragen an Worker-Prozesse | | |
| Zwei Prozesse tauschen einen 50-MB-Videopuffer aus | | |
| Logging-Prozess sammelt Meldungen von 10 Prozessen | | |

**c)** Verbindung zu Woche 6: Der `BlockingRingBuffer` verwendet
`synchronized + while + wait + notifyAll`. In Woche 6 haben wir
`BlockingQueue` aus `java.util.concurrent` verwendet.
Was ist der konzeptuelle Unterschied?
*(Hinweis: Prozess vs. Thread, Adressraum)*

---

## Denkanstöße und Reflexionsfragen

1. **Nachrichtengrenzen**: Eine Pipe ist ein Bytestrom — keine Grenzen.
   Wenn Sie über eine Pipe JSON-Objekte übertragen wollen,
   was müssen Sie selbst implementieren?

2. **Zero-Copy**: Warum ist Shared Memory schneller als Pipe?
   Zählen Sie die Kopiervorgänge für eine 1-MB-Nachricht:
   - via Pipe: Prozess A → Kernel-Puffer → Prozess B = **2 Kopien**
   - via Shared Memory: Prozess A schreibt direkt = **0 Kopien**

3. **Deadlock bei IPC**: Können mit unseren Mechanismen Deadlocks entstehen?
   Skizzieren Sie ein Szenario mit zwei Prozessen und zwei Message Queues
   die beide voll sind.

4. **PCB und Prozess-Ende**: Was passiert mit einer Pipe wenn der
   schreibende Prozess (PCB) terminiert während der Leser noch wartet?
   Welche Meldung sollte der Leser bekommen?

---

## Verwendete Dateien

| Datei | Beschreibung |
|-------|-------------|
| `Pipe.java` | Unidirektionaler Bytestrom (vollständig) |
| `Message.java` | Nachrichtenobjekt mit Typ und Daten (vollständig) |
| `BlockingRingBuffer.java` | Ringpuffer — **Lücken: `put()`, `take()`** |
| `MessageQueue.java` | Nachrichtenwarteschlange — **Lücken: `send()`, `receive()`** |
| `SharedMemory.java` | Gemeinsamer Speicher — **Lücken: `writeAndNotify()`, `waitForData()`** |
| `SharedMemoryDemo.java` | Demo für Aufgabe 4: 3 Szenarien mit/ohne Signaling |
| `ProcessControlBlock.java` | PCB mit IPC-Handles (vollständig) |
| `Demos.java` | Alle Demo-Programme: PipeDemo, MessageQueueDemo, RingBufferDemo, IPCComparisonDemo |

---

## Java-Klassen: Methoden-Übersicht

### `BlockingRingBuffer<T>`
| Methode | Beschreibung |
|---|---|
| `put(T)` | Element einlegen — **implementieren** |
| `take()` | Element entnehmen — **implementieren** |
| `tryTake()` | Non-blocking Entnehmen (bereits implementiert) |
| `size()`, `isEmpty()`, `isFull()` | Zustandsabfragen |

### `MessageQueue`
| Methode | Beschreibung |
|---|---|
| `send(Message)` | Nachricht senden — **implementieren** |
| `receive()` | Nachricht empfangen — **implementieren** |
| `tryReceive()` | Non-blocking Empfang (bereits implementiert) |

### `SharedMemory`
| Methode | Beschreibung |
|---|---|
| `write(int, byte[])` | Schreiben ohne Signaling (bereits implementiert) |
| `read(int, int)` | Lesen ohne Warten (bereits implementiert) |
| `writeAndNotify(int, byte[])` | Schreiben + Leser aufwecken — **implementieren** |
| `waitForData()` | Blockieren bis Daten verfügbar — **implementieren** |

---

## Weiterführende Ressourcen

- **OSTEP Kapitel 48**: Interlude: IPC
- **Java `java.util.concurrent`**: `ArrayBlockingQueue` —
  produktionsreife Implementierung des Ringpuffers
  https://docs.oracle.com/en/java/docs/api/java.base/java/util/concurrent/ArrayBlockingQueue.html
- **Linux `man 7 pipe`**: POSIX-Pipe Semantik
- **Linux `ipcs`**: Zeigt aktive IPC-Ressourcen