# Woche 04: Multi-Level Feedback Queue (MLFQ)

## Lernziele

- Verständnis der **MLFQ-Scheduling-Policy** und ihrer Parameter.
- Implementierung/Anpassung eines **präemptiven MLFQ-Schedulers** in Java.
- Simulation von **Prozessausführung, Demotion und Aging**.

---

## Aufgabenstellung

### **1. MLFQ-Scheduler implementieren**

In diesem Verzeichnis finden Sie eine fast fertige Implementierung des MLFQ-Verfahrens und eine Main-Klasse zum testen, inklusive eines beispielhaften Workloads.

1. Damit Sie den Code ausführen können, müssen Sie die erforderlichen Klassen aus `woche03` importieren (z.B. `ProcessControlBlock`, `ProcessState`). Fügen Sie die fehlenden Imports hinzu und führen Sie den Code aus. Beispiel:

```java
import woche03.ProcessControlBlock
```

2. Testen Sie den Scheduler mit dem angegebenen Workload. Es handelt sich um einen lang laufenden Prozess. Untersuchen Sie die Ausgabe. Erhalten Sie das erwartete Ergebnis? Können Sie das Ergebnis erklären?

---

### **2. Verschiedene Workloads erstellen und testen**

1. Erstellen Sie folgende Workloads:

    - 1 Lang laufender Prozess und ein kurzer Prozess, der ankommt wenn der erste Prozess bereits die niedrigste Priorität hat.
    - 1 Lang laufender Prozess und mehrere kurze Prozess, die in regelmäßigen Abständen ankommen.
    - 1 Lang laufender Prozess und mehrere kurze Prozess, die in regelmäßigen Abständen so ankommen, dass das Risiko von Starvation für den ersten Prozess besteht.

2. Testen Sie den MLFQ-Scheduler mit den Workloads aus 3. Eventuell müssen Sie die Parameter des Schedulers (Quanta der Queues, agingThreshold) verändern. Erhalten Sie die erwarteten Ergebnisse? Lassen sich die Ergebnisse mmit Hilfe der Scheduling-Regeln des MLFQ-Verfahrens?

---

### **3. Priority Boost**

1. In der verfügbaren Version des MLFQ-Schedulers wird das *Priority-Boost* so implementiert, dass Prozesse, die zu lang warten (`agingThreshold`) in die nächsthöhere Priorität gebracht werden. Verändern Sie den Code der Methode `schedule` so, dass Prozesse, die zu lange warten in die höchste Prioritätsebene gebracht werden. Finden Sie die Stelle im Code, das *Priority-Boost* stattfindet und passen Sie den Code an. **Hinweis:** die Queue mit der höchsten Priorität ist die Queue `0`. 

2. Testen Sie Ihre Workloads nach dieser Änderung erneut. Wie ändern sich die Ergebnisse. Eventuell brauchen Sie weitere Workloads, um den Effekt deutlich zu merken.

---

### **4. Vergleich mit anderen Scheduling-Verfahren**

1. Erstellen und testen Sie weitere Workloads. Vergleichen Sie MLFQ mit den anderen Scheduling-Verfahren. Wie beurteilen Sie die verschiedenen Strategien? Passen die Ergebnisse zur Theorie?

---

### **5. Diskussion der Ergebnisse**

- Welcher Algorithmus ist **fairer** (geringere Varianz in Waiting Times)?
    - Welcher Prozess hat die längste Wartezeit? Warum?
    - Wie wirkt sich das agingThreshold auf die Fairness aus?

- Welcher Algorithmus hat die **beste Response Time** für interaktive Jobs?
    - Wie schnell werden kurze Prozesse ausgeführt?
    - Was passiert, wenn Sie das Quantum der höchsten Queue verkleinern?

- Wie beeinflussen die **Quanta** und das **agingThreshold** die Performance?
    - Welcher Algorithmus eignet sich besser für interaktive Anwendungen? Warum?
    - Wie wirkt sich MLFQ auf die Durchschnitts-Wartezeit aus?

---

## 📂 Dateien in diesem Verzeichnis
| Datei | Beschreibung |
|-------|-------------|
| `MLFQScheduler.java` | Multi-Level Feedback Queues |
| `Main.java` | Testklasse mit Workloads. |