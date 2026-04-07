package woche07;

/**
 * Philosoph – NAIVE Variante (Deadlock möglich).
 *
 * Strategie: Immer erst linke, dann rechte Gabel aufnehmen.
 *
 * Coffman-Bedingungen, die hier alle gleichzeitig erfüllt sein können:
 *   1. Mutual Exclusion  – Jede Gabel ist ein exklusiver Lock.
 *   2. Hold and Wait     – Hält linke Gabel, wartet auf rechte.
 *   3. No Preemption     – Eine gehaltene Gabel wird nicht entzogen.
 *   4. Circular Wait     – P0 wartet auf Gabel von P1, P1 auf P2, ..., Pn auf P0.
 *                          → Deadlock!
 */
public class DeadlockPhilosopher extends Thread {

    private final int id;
    private final Fork leftFork;
    private final Fork rightFork;
    private final int rounds;
    private volatile ProcessState state;
    private int mealsEaten;

    public DeadlockPhilosopher(int id, Fork leftFork, Fork rightFork, int rounds) {
        super("Philosoph-" + id);
        this.id = id;
        this.leftFork = leftFork;
        this.rightFork = rightFork;
        this.rounds = rounds;
        this.state = ProcessState.NEW;
        this.mealsEaten = 0;
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
            System.out.printf("[P%d] unterbrochen (Deadlock-Recovery?). Mahlzeiten: %d%n",
                    id, mealsEaten);
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

        // TODO (Aufgabe 1a): Nimm die Gabeln in der NAIVEN Reihenfolge auf.
        //   Schritt 1: Nimm die linke Gabel auf  → leftFork.pickUp(id)
        //   Schritt 2: Warte SimConfig.FORK_PICKUP_DELAY_MS ms → Thread.sleep(...)
        //              (Diese Pause vergrößert das Zeitfenster für den Deadlock!)
        //   Schritt 3: Nimm die rechte Gabel auf → rightFork.pickUp(id)
        // --->



        // --- kritischer Abschnitt: beide Gabeln werden gehalten ---
        state = ProcessState.RUNNING;
        System.out.printf("[P%d] ISST (Mahlzeit #%d)%n", id, mealsEaten + 1);
        Thread.sleep(SimConfig.EAT_TIME_MS);
        mealsEaten++;

        // TODO (Aufgabe 1b): Lege die Gabeln wieder ab.
        //   Lege zuerst die rechte, dann die linke Gabel ab → putDown(id)
        // --->

    }

    public int getPhilosopherId()             { return id; }
    public ProcessState getPhilosopherState() { return state; }
    public int getMealsEaten()                { return mealsEaten; }
}
