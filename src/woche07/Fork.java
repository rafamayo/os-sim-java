package woche07;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Repräsentiert eine Gabel (geteilte Ressource) am Tisch der Philosophen.
 *
 * Eine Gabel ist ein nicht-teilbarer Mutex – sie verkörpert die
 * Coffman-Bedingung "Mutual Exclusion" direkt im Code.
 */
public class Fork {

    private final int id;
    private final ReentrantLock lock;

    public Fork(int id) {
        this.id = id;
        this.lock = new ReentrantLock();
    }

    /**
     * Nimmt die Gabel auf (blockierend, unterbrechbar).
     *
     * @param philosopherId ID des anfordernden Philosophen (für Log-Ausgaben)
     * @throws InterruptedException wenn der Thread unterbrochen wird
     */
    public void pickUp(int philosopherId) throws InterruptedException {
        System.out.printf("  [P%d] versucht Gabel %d aufzunehmen...%n", philosopherId, id);
        lock.lockInterruptibly();
        System.out.printf("  [P%d] hat Gabel %d ▶ AUFGENOMMEN%n", philosopherId, id);
    }

    /**
     * Legt die Gabel ab und gibt den Lock frei.
     *
     * @param philosopherId ID des ablegenden Philosophen (für Log-Ausgaben)
     */
    public void putDown(int philosopherId) {
        lock.unlock();
        System.out.printf("  [P%d] hat Gabel %d ◀ ABGELEGT%n", philosopherId, id);
    }

    public int getId() { return id; }

    public boolean isHeld() { return lock.isLocked(); }

    @Override
    public String toString() { return "Gabel-" + id; }
}
