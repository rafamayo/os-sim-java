package woche02;

/**
 * Hauptprogramm zum Testen der Implementierungen aus Woche 2.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Aufgabe 1: Prozesszustände ===");
        testProcessManager();

        System.out.println("\n=== Aufgabe 2 & 3: Process Control Block (PCB) ===");
        testPCBSimulator();
    }

    private static void testProcessManager() {
        ProcessManager manager = new ProcessManager();
        int pid1 = manager.createProcess("Editor");
        int pid2 = manager.createProcess("Compiler");

        manager.blockProcess(pid1);
        System.out.println("Process " + pid1 + " state: " + manager.getProcessState(pid1));

        manager.terminateProcess(pid2);
        manager.listProcesses();
    }

    private static void testPCBSimulator() {
        PCBSimulator simulator = new PCBSimulator();
        int pid = simulator.createProcess("Database");
        simulator.setPriority(pid, 5);
        simulator.blockProcess(pid);

        ProcessControlBlock pcb = simulator.getPCB(pid);
        pcb.setRegister(0, "R0=42"); // Simuliere einen Registerwert
        System.out.println("PCB after updates: " + pcb);

        simulator.listPCBs();
    }
}