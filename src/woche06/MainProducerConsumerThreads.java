package woche06;

/**
 * Aufgabe 4: Producer–Consumer mit echten Threads.
 *
 * Diese Demo zeigt das klassische Producer–Consumer-Problem:
 *
 * - Producer erzeugen Daten (Items)
 * - Consumer verbrauchen Daten
 * - beide greifen auf einen gemeinsamen, begrenzten Puffer zu
 *
 * Der Puffer ist hier ein sogenannter "bounded buffer":
 *
 *     Er hat nur eine feste Kapazität.
 *
 * Dadurch entstehen zwei wichtige Bedingungen:
 *
 * 1. Producer dürfen nur dann einfügen, wenn noch Platz vorhanden ist.
 * 2. Consumer dürfen nur dann entnehmen, wenn mindestens ein Item vorhanden ist.
 *
 * Genau diese Bedingungen werden in der Klasse BoundedBufferMonitor
 * mit einem Monitor gelöst:
 *
 *     synchronized
 *     wait()
 *     notifyAll()
 *
 * Wichtige didaktische Idee:
 *
 * Diese Demo zeigt sehr schön, dass Synchronisation nicht nur
 * "Daten schützen" bedeutet, sondern auch
 *
 *     "Threads koordinieren"
 *
 * also festlegen:
 * - wann ein Thread arbeiten darf
 * - und wann er warten muss
 */
public class MainProducerConsumerThreads {

    public static void main(String[] args) throws InterruptedException {

        /**
         * Maximale Größe des Puffers.
         *
         * Da die Kapazität klein ist (3),
         * sieht man im Lauf der Demo besonders gut:
         *
         * - Producer blockieren, wenn der Buffer voll ist
         * - Consumer blockieren, wenn der Buffer leer ist
         *
         * Für Vorlesungs- und Übungszwecke ist eine kleine Kapazität
         * daher oft didaktisch besser als ein großer Puffer.
         */
        final int capacity = 3;

        /**
         * Anzahl der Items, die jeder Producer erzeugt.
         *
         * Hier erzeugen wir zwei Producer mit je 10 Items.
         * Insgesamt werden also 20 Items produziert.
         */
        final int itemsPerProducer = 10;

        /**
         * Anzahl der Items, die jeder Consumer konsumiert.
         *
         * Hier konsumieren zwei Consumer ebenfalls je 10 Items.
         * Insgesamt werden also 20 Items verbraucht.
         *
         * Das ist wichtig:
         * Produktion und Konsum müssen hier insgesamt zusammenpassen,
         * sonst würden Producer oder Consumer am Ende dauerhaft warten.
         */
        final int itemsPerConsumer = 10;

        /**
         * Gemeinsamer Puffer, auf den ALLE Producer und Consumer zugreifen.
         *
         * Die Klasse BoundedBufferMonitor implementiert die eigentliche Synchronisation.
         *
         * Intern kümmert sich der Monitor darum:
         *
         * - dass nie zwei Threads gleichzeitig den kritischen Abschnitt betreten
         * - dass Producer bei vollem Buffer warten
         * - dass Consumer bei leerem Buffer warten
         *
         * Also:
         *
         *     Mutual Exclusion + Condition Synchronization
         */
        BoundedBufferMonitor buffer = new BoundedBufferMonitor(capacity);

        /**
         * Erzeuge zwei Producer-Threads.
         *
         * Parameter:
         *
         * - Name des Threads ("P1", "P2")
         * - gemeinsamer Buffer
         * - Startwert
         * - Anzahl zu produzierender Items
         *
         * Die Startwerte 100 und 200 sind didaktisch nützlich:
         *
         * - P1 produziert: 100, 101, 102, ...
         * - P2 produziert: 200, 201, 202, ...
         *
         * Dadurch erkennt man in der Ausgabe sehr leicht,
         * welcher Producer welches Item erzeugt hat.
         */
        Thread p1 = new Thread(new ProducerThread("P1", buffer, 100, itemsPerProducer));
        Thread p2 = new Thread(new ProducerThread("P2", buffer, 200, itemsPerProducer));

        /**
         * Erzeuge zwei Consumer-Threads.
         *
         * Beide greifen auf denselben Buffer zu und entnehmen Items.
         *
         * Auch hier ist es didaktisch hilfreich,
         * dass die Consumer unterschiedliche Namen haben ("C1", "C2"),
         * damit man in der Ausgabe Interleavings besser verfolgen kann.
         */
        Thread c1 = new Thread(new ConsumerThread("C1", buffer, itemsPerConsumer));
        Thread c2 = new Thread(new ConsumerThread("C2", buffer, itemsPerConsumer));

        /**
         * Start aller Threads.
         *
         * Ab diesem Moment laufen:
         *
         * - zwei Producer
         * - zwei Consumer
         *
         * parallel zum Main-Thread.
         *
         * Wichtig:
         * Die Reihenfolge der späteren Ausgaben ist NICHT deterministisch.
         *
         * Das bedeutet:
         * Unterschiedliche Programmläufe können unterschiedliche
         * Interleavings erzeugen.
         */
        p1.start();
        p2.start();
        c1.start();
        c2.start();

        /**
         * join() wartet, bis der jeweilige Thread beendet wurde.
         *
         * Ohne join() könnte das Main-Programm zu früh fertig werden,
         * obwohl Producer oder Consumer noch laufen.
         *
         * Durch die vier join()-Aufrufe stellen wir sicher:
         *
         *     Erst wenn alle Producer und Consumer fertig sind,
         *     gibt main den Endzustand aus.
         */
        p1.join();
        p2.join();
        c1.join();
        c2.join();

        /**
         * Ausgabe des finalen Buffer-Zustands.
         *
         * Wenn alles korrekt funktioniert hat, sollte der Buffer
         * am Ende typischerweise leer sein, weil:
         *
         * - insgesamt 20 Items produziert wurden
         * - insgesamt 20 Items konsumiert wurden
         *
         * Der Snapshot dient vor allem dem Debugging und der Verifikation.
         */
        System.out.println("[Main] done. Final: " + buffer.snapshot());
    }
}