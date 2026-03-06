package woche05;

/**
 * Demo:
 * Zwei Threads verändern denselben PCB-Zustand gleichzeitig.
 *
 * Ziel:
 * Sichtbar machen, dass auch gemeinsame Zustandsübergänge
 * durch Nebenläufigkeit nicht deterministisch werden können.
 */
public class MainProcessStateRaceDemo {

    public static void main(String[] args) throws InterruptedException {
        ProcessControlBlock pcb = new ProcessControlBlock(1, "DemoProcess");

        System.out.println("[Main] Initial: " + pcb);

        /**
         * Thread 1 modelliert einen "normalen" Ablauf:
         * NEW -> READY -> RUNNING
         */
        Thread t1 = new Thread(new StateTransitionWorker(
                "T1",
                pcb,
                new ProcessState[]{
                        ProcessState.READY,
                        ProcessState.RUNNING
                }
        ));

        /**
         * Thread 2 setzt den Zustand ebenfalls,
         * z. B. so, als würde ein anderer Teil des Systems
         * parallel eingreifen.
         */
        Thread t2 = new Thread(new StateTransitionWorker(
                "T2",
                pcb,
                new ProcessState[]{
                        ProcessState.READY,
                        ProcessState.TERMINATED
                }
        ));

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.out.println("[Main] Final: " + pcb);
    }
}