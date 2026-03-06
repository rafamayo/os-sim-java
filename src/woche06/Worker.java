package woche06;

/**
 * Worker-Thread für die TaskQueue-Demo.
 *
 * Idee:
 * Mehrere Worker können parallel laufen und aus einer gemeinsamen Queue arbeiten.
 * Falls keine Aufgaben vorhanden sind, blockieren sie in queue.take().
 */
public class Worker implements Runnable {

    private final String workerName;
    private final TaskQueue queue;

    public Worker(String workerName, TaskQueue queue) {
        this.workerName = workerName;
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Runnable task = queue.take();
                if (task == null) {
                    System.out.println("[" + workerName + "] shutdown, exit.");
                    return;
                }
                System.out.println("[" + workerName + "] run task...");
                task.run();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("[" + workerName + "] interrupted, exit.");
        }
    }
}
