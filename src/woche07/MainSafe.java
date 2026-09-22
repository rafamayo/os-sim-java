package woche07;

import java.util.ArrayList;
import java.util.List;

/**
 * Aufgabe 2 – Deadlock verhindern durch Lock Ordering.
 *
 * Gabeln werden immer in aufsteigender ID-Reihenfolge aufgenommen.
 * Damit ist die Coffman-Bedingung "Circular Wait" strukturell
 * ausgeschlossen – ein Deadlock kann nicht mehr entstehen.
 *
 * Start:
 *   java woche07.MainSafe
 */
public class MainSafe {

    public static void main(String[] args) throws InterruptedException {
        header("TEIL A – Szenario 2: Lock Ordering (kein Deadlock)");
        System.out.println("Gabeln immer in aufsteigender ID-Reihenfolge aufnehmen.");
        System.out.println("'Circular Wait' ist strukturell ausgeschlossen.\n");

        printTable();

        int n = SimConfig.NUM_PHILOSOPHERS;
        Fork[] forks = createForks(n);

        List<SafePhilosopher> philosophers = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            philosophers.add(new SafePhilosopher(
                    i, forks[i], forks[(i + 1) % n], SimConfig.ROUNDS));
        }

        long t0 = System.currentTimeMillis();
        philosophers.forEach(Thread::start);
        for (SafePhilosopher p : philosophers) p.join();

        printSummary(philosophers.stream().mapToInt(SafePhilosopher::getMealsEaten).sum(),
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
