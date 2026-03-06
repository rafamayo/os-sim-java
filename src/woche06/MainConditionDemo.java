package woche06;

/**
 * Aufgabe 3: Demo für Condition Variables mit TaskQueue + Worker.
 *
 * Ziel dieser Demo:
 *
 * Wir wollen zeigen, wie Threads mit
 *
 *     wait()
 *     notifyAll()
 *
 * koordiniert werden können.
 *
 * Die zentrale Idee ist:
 *
 * - Worker-Threads holen Aufgaben aus einer gemeinsamen Queue.
 * - Wenn keine Aufgaben vorhanden sind, sollen sie NICHT ständig aktiv prüfen
 *   (kein Busy Waiting), sondern blockieren.
 * - Sobald neue Aufgaben verfügbar sind, werden sie geweckt.
 *
 * Damit modellieren wir ein sehr typisches Synchronisationsmuster:
 *
 *     "Warte, bis eine Bedingung erfüllt ist."
 *
 * Die Bedingung hier lautet:
 *
 *     "Es gibt mindestens eine Aufgabe in der Queue."
 *
 * Außerdem gibt es eine zweite Bedingung:
 *
 *     "Das System soll heruntergefahren werden."
 *
 * In diesem Fall sollen wartende Worker sauber beendet werden.
 */
public class MainConditionDemo {

    public static void main(String[] args) throws InterruptedException {

        /**
         * Gemeinsame TaskQueue.
         *
         * Alle Worker-Threads greifen auf dieselbe Queue zu.
         *
         * Die Synchronisation steckt NICHT hier im Main-Programm,
         * sondern in der Klasse TaskQueue:
         *
         * - submit(...) fügt Aufgaben ein
         * - take() entnimmt Aufgaben
         * - shutdown() signalisiert das Ende
         *
         * Intern verwendet TaskQueue:
         *
         *     synchronized
         *     wait()
         *     notifyAll()
         */
        TaskQueue q = new TaskQueue();

        /**
         * Zwei Worker-Threads erzeugen.
         *
         * Jeder Worker bekommt:
         * - einen Namen (nur für Logging)
         * - dieselbe gemeinsame Queue
         *
         * Wichtig:
         * Der Konstruktor new Thread(...) startet den Thread noch NICHT.
         * Erst start() startet die run()-Methode.
         */
        Thread w1 = new Thread(new Worker("W1", q));
        Thread w2 = new Thread(new Worker("W2", q));

        /**
         * Beide Worker starten.
         *
         * Ab jetzt laufen sie parallel zum Main-Thread.
         *
         * Typischer Ablauf:
         *
         * 1. Worker ruft queue.take() auf
         * 2. Queue ist zunächst leer
         * 3. Worker blockiert intern mit wait()
         *
         * Das bedeutet:
         * Die Worker schlafen effizient und verbrauchen keine CPU,
         * bis neue Aufgaben in die Queue gelegt werden.
         */
        w1.start();
        w2.start();

        /**
         * Kleine Pause.
         *
         * Warum?
         *
         * Wir möchten den Workern Zeit geben, tatsächlich zu starten
         * und vermutlich bereits in queue.take() zu blockieren.
         *
         * Didaktisch ist das nützlich, weil wir dann klarer beobachten können:
         *
         * - zuerst warten die Worker
         * - später werden sie durch submit(...) geweckt
         *
         * Ohne diese Pause könnte es sein, dass Main sofort Aufgaben einfügt,
         * bevor die Worker überhaupt angefangen haben zu warten.
         *
         * Dann wäre der Ablauf weniger anschaulich.
         */
        Thread.sleep(200);

        /**
         * Jetzt werden zwei Aufgaben eingereiht.
         *
         * submit(...) macht in TaskQueue typischerweise zwei Dinge:
         *
         * 1. Aufgabe in die Queue einfügen
         * 2. notifyAll() aufrufen
         *
         * Dadurch werden wartende Worker geweckt.
         *
         * Die Aufgabe selbst ist hier ein Lambda-Ausdruck:
         *
         *     () -> System.out.println(...)
         *
         * Das ist eine sehr kurze Form eines Runnable-Objekts.
         */
        q.submit(() -> System.out.println("[Task] Hello from task 1"));
        q.submit(() -> System.out.println("[Task] Hello from task 2"));

        /**
         * Noch eine kleine Pause.
         *
         * Diese Pause gibt den Workern Zeit,
         * die beiden Aufgaben tatsächlich auszuführen.
         *
         * Typischer Ablauf:
         *
         * - Worker wird geweckt
         * - Worker entnimmt eine Aufgabe
         * - Worker führt task.run() aus
         *
         * Dadurch wird die Ausgabe übersichtlicher und der Demo-Ablauf klarer.
         */
        Thread.sleep(200);

        /**
         * Shutdown des Systems.
         *
         * Die Methode shutdown() in TaskQueue soll:
         *
         * 1. ein internes shutdown-Flag setzen
         * 2. notifyAll() aufrufen
         *
         * Warum notifyAll() auch hier?
         *
         * Möglicherweise warten Worker gerade wieder in take().
         * Diese Worker müssen geweckt werden, damit sie bemerken:
         *
         *     "Es gibt keine Aufgaben mehr und das System wird beendet."
         *
         * Danach können sie sauber terminieren.
         */
        q.shutdown();

        /**
         * join() wartet, bis die Worker-Threads vollständig beendet sind.
         *
         * Das ist wichtig:
         *
         * Ohne join() könnte der Main-Thread sofort das Ende ausgeben,
         * obwohl die Worker noch laufen oder gerade erst beendet werden.
         *
         * Durch join() stellen wir sicher:
         *
         *     Erst wenn beide Worker fertig sind,
         *     endet auch das Main-Programm.
         */
        w1.join();
        w2.join();

        /**
         * Abschlussmeldung.
         *
         * Wenn diese Zeile erscheint, wissen wir:
         *
         * - beide Worker wurden beendet
         * - das Shutdown-Protokoll hat funktioniert
         * - keine Aufgabe wird mehr verarbeitet
         */
        System.out.println("[Main] done.");
    }
}