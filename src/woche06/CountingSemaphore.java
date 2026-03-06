package woche06;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Single-Thread-Semaphore für das Warm-up.
 *
 * Diese Klasse simuliert das Verhalten einer Counting Semaphore.
 * Das ist didaktisch nützlich, aber noch kein echter Scheduler.
 */
public class CountingSemaphore {

    private final int maxCount;
    private int count;
    private final Deque<ProcessControlBlock> waiters = new ArrayDeque<>();

    public CountingSemaphore(int initial, int max) {
        this.count = initial;
        this.maxCount = max;
    }

    public synchronized int getCount() { return count; }

    public synchronized boolean acquire(ProcessControlBlock pcb) {
        if (count > 0) {
            count--;
            return true;
        }
        waiters.addLast(pcb);
        pcb.blockOn(this);
        return false;
    }

    public synchronized void release() {
        if (!waiters.isEmpty()) {
            ProcessControlBlock next = waiters.removeFirst();
            next.unblock();
            return;
        }
        if (count < maxCount) count++;
    }

    @Override
    public String toString() {
        return "CountingSemaphore{count=" + count + "/" + maxCount + "}";
    }
}
