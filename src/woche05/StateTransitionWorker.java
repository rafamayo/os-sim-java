package woche05;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Ein Worker-Thread, der denselben PCB-Zustand mehrfach verändert.
 *
 * Idee:
 * Mehrere Threads greifen gleichzeitig auf dasselbe PCB zu und
 * setzen unterschiedliche Zustände.
 *
 * Dadurch kann die beobachtete Zustandsfolge von Lauf zu Lauf variieren.
 */
public class StateTransitionWorker implements Runnable {

    private final String workerName;
    private final ProcessControlBlock pcb;
    private final ProcessState[] transitions;

    public StateTransitionWorker(String workerName,
                                 ProcessControlBlock pcb,
                                 ProcessState[] transitions) {
        this.workerName = workerName;
        this.pcb = pcb;
        this.transitions = transitions;
    }

    @Override
    public void run() {
        try {
            for (ProcessState s : transitions) {
                pcb.setState(s);

                System.out.println("[" + workerName + "] set state -> " + s
                        + " | observed: " + pcb);

                Thread.sleep(ThreadLocalRandom.current().nextInt(10, 50));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}