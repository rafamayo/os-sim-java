public class HelloSimulator {
    public static void main(String[] args) {
        SimulatorSkeleton simulator = new SimulatorSkeleton();

        // Prozesse starten
        int pid1 = simulator.startProcess("Process 1");
        int pid2 = simulator.startProcess("Process 2");

        // Prozess blockieren (z. B. für I/O)
        simulator.blockProcess(pid1);

        // Prozesszustand abfragen
        SimulatorSkeleton.ProcessState state = simulator.getProcessState(pid1);
        System.out.println("Process " + pid1 + " is in state: " + state);

        // Prozess beenden
        simulator.stopProcess(pid1);
        simulator.stopProcess(pid2);
    }
}
