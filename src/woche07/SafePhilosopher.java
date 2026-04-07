package woche07;

/**
 * Philosoph – SAFE Variante mit Lock Ordering (Prevention).
 *
 * Strategie: Gabeln immer in aufsteigender ID-Reihenfolge aufnehmen.
 *
 * Warum funktioniert das?
 *   Alle Philosophen nehmen die Gabel mit der kleineren ID zuerst.
 *   Dadurch kann kein Zyklus entstehen: Um einen Zyklus zu bilden,
 *   müsste irgendein Prozess eine Ressource mit HÖHERER ID zuerst nehmen –
 *   das ist hier strukturell ausgeschlossen.
 *   → Coffman-Bedingung "Circular Wait" ist verletzt → kein Deadlock möglich.
 */
public class SafePhilosopher extends Thread {

    private final int id;
    private final Fork firstFork;   // Gabel mit niedrigerer ID (immer zuerst)
    private final Fork secondFork;  // Gabel mit höherer ID (immer danach)
    private final int rounds;
    private volatile ProcessState state;
    private int mealsEaten;

    public SafePhilosopher(int id, Fork leftFork, Fork rightFork, int rounds) {
        super("SafePhilosoph-" + id);
        this.id = id;
        this.rounds = rounds;
        this.state = ProcessState.NEW;
        this.mealsEaten = 0;

        // TODO (Aufgabe 2): Implementiere Lock Ordering.
        //   Weise firstFork und secondFork so zu, dass firstFork IMMER
        //   die Gabel mit der kleineren ID ist.
        //   Nutze leftFork.getId() und rightFork.getId() zum Vergleich.
        //
        //   Tipp: if (leftFork.getId() < rightFork.getId()) { ... } else { ... }
        // --->
        this.firstFork  = null; // TODO: ersetzen
        this.secondFork = null; // TODO: ersetzen
    }

    @Override
    public void run() {
        state = ProcessState.READY;
        try {
            for (int i = 0; i < rounds; i++) {
                think();
                dine();
            }
            state = ProcessState.TERMINATED;
            System.out.printf("[P%d] fertig. Mahlzeiten insgesamt: %d%n", id, mealsEaten);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void think() throws InterruptedException {
        state = ProcessState.RUNNING;
        System.out.printf("[P%d] DENKT%n", id);
        Thread.sleep(SimConfig.THINK_TIME_MS);
    }

    private void dine() throws InterruptedException {
        state = ProcessState.BLOCKED;
        System.out.printf("[P%d] ist HUNGRIG%n", id);

        // Lock Ordering: firstFork (niedrigere ID) wird IMMER zuerst genommen.
        // Dieser Code ist fertig – er funktioniert korrekt, sobald firstFork
        // und secondFork oben richtig zugewiesen wurden.
        firstFork.pickUp(id);
        Thread.sleep(SimConfig.FORK_PICKUP_DELAY_MS);
        secondFork.pickUp(id);

        state = ProcessState.RUNNING;
        System.out.printf("[P%d] ISST (Mahlzeit #%d)%n", id, mealsEaten + 1);
        Thread.sleep(SimConfig.EAT_TIME_MS);
        mealsEaten++;

        secondFork.putDown(id);
        firstFork.putDown(id);
    }

    public int getPhilosopherId()             { return id; }
    public ProcessState getPhilosopherState() { return state; }
    public int getMealsEaten()                { return mealsEaten; }
}
