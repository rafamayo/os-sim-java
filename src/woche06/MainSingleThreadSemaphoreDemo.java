package woche06;

/** Warm-up-Demo für eine Single-Thread-Semaphore. */
public class MainSingleThreadSemaphoreDemo {
    public static void main(String[] args) {
        int max = 3;
        RingBuffer rb = new RingBuffer(max);
        CountingSemaphore empty = new CountingSemaphore(max, max);
        CountingSemaphore full = new CountingSemaphore(0, max);
        SimulatorMutex mutex = new SimulatorMutex();
        ProcessControlBlock producer = new ProcessControlBlock(1, "Producer");
        ProcessControlBlock consumer = new ProcessControlBlock(2, "Consumer");

        System.out.println("[Producer] trying to put 5 items into capacity 3...");
        for (int i = 1; i <= 5; i++) {
            if (!empty.acquire(producer)) {
                System.out.println("[Producer] blocked because buffer is full.");
                break;
            }
            mutex.acquire(producer);
            rb.put(i);
            mutex.release(producer);
            full.release();
        }

        System.out.println("[Consumer] trying to get 5 items...");
        for (int i = 1; i <= 5; i++) {
            if (!full.acquire(consumer)) {
                System.out.println("[Consumer] blocked because buffer is empty.");
                break;
            }
            mutex.acquire(consumer);
            int v = rb.get();
            mutex.release(consumer);
            empty.release();
            System.out.println("[Consumer] got " + v);
        }

        System.out.println("[Main] final buffer: " + rb.snapshot());
    }
}
