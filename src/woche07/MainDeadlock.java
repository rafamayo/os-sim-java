package woche07;

import java.util.ArrayList;
import java.util.List;

/**
 * Aufgabe 1 + 3 – Deadlock provozieren und mit dem Watchdog erkennen.
 *
 * Jeder Philosoph nimmt zuerst die LINKE, dann die RECHTE Gabel.
 * Die Coffman-Bedingung "Circular Wait" ist damit erfüllbar – bei genügend
 * Pech (oder Pause zwischen den beiden pickUp()-Aufrufen) entsteht ein
 * echter Deadlock, den der {@link DeadlockWatchdog} (Aufgabe 3) erkennt
 * und über Thread-Interrupts auflöst.
 *
 * Start:
 *   java woche07.MainDeadlock
 */
public class MainDeadlock {

    public static void main(String[] args) throws InterruptedException {
        header("TEIL A – Szenario 1: Naive Strategie (Deadlock möglich)");
        System.out.println("Jeder Philosoph nimmt zuerst die LINKE, dann die RECHTE Gabel.");
        System.out.println("Coffman-Bedingung 'Circular Wait' ist erfüllbar!\n");

        printTable();

        int n = SimConfig.NUM_PHILOSOPHERS;
        Fork[] forks = createForks(n);

        List<DeadlockPhilosopher> philosophers = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            philosophers.add(new DeadlockPhilosopher(
                    i, forks[i], forks[(i + 1) % n], SimConfig.ROUNDS));
        }

        DeadlockWatchdog watchdog = new DeadlockWatchdog(philosophers);
        watchdog.start();

        long t0 = System.currentTimeMillis();
        philosophers.forEach(Thread::start);
        for (DeadlockPhilosopher p : philosophers) {
            p.join(SimConfig.WATCHDOG_TIMEOUT_MS * 2);
        }
        watchdog.stopWatchdog();

        printSummary(philosophers.stream().mapToInt(DeadlockPhilosopher::getMealsEaten).sum(),
                System.currentTimeMillis() - t0);
    }

    // =========================================================================
    //  Hilfsmethoden (fertig – nicht ändern)
    // =========================================================================

    private static Fork[] createForks(int n) {
        Fork[] forks = new Fork[n];
        for (int i = 0; i < n; i++) forks[i] = new Fork(i);
        return forks;
    }

    private static void printTable() {
        System.out.println("Tischanordnung:");
        System.out.println("            P0");
        System.out.println("           /  \\");
        System.out.println("         G4    G0");
        System.out.println("         /      \\");
        System.out.println("       P4        P1");
        System.out.println("       |          |");
        System.out.println("       G3        G1");
        System.out.println("         \\      /");
        System.out.println("          P3--G2--P2");
        System.out.println("  Pi hält G(i) links, G((i+1)%5) rechts\n");
    }

    private static void printSummary(int totalMeals, long elapsed) {
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.printf( "║  Laufzeit:        %6d ms           ║%n", elapsed);
        System.out.printf( "║  Mahlzeiten ges.: %6d              ║%n", totalMeals);
        System.out.println("╚══════════════════════════════════════╝\n");
    }

    private static void header(String title) {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.printf( "║  %-52s  ║%n", title);
        System.out.println("╚══════════════════════════════════════════════════════╝\n");
    }
}
