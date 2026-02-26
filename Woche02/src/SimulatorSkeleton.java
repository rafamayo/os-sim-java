public class SimulatorSkeleton {
    // Prozesszustände (vereinfacht)
    public enum ProcessState {
        NEW,      // Prozess wurde erstellt, aber noch nicht gestartet
        RUNNING,  // Prozess wird ausgeführt
        BLOCKED,  // Prozess wartet auf eine Ressource (z. B. I/O)
        TERMINATED // Prozess wurde beendet
    }

    // Klasse zur Darstellung eines Prozesses
    private static class Process {
        int pid;
        String name;
        ProcessState state;

        public Process(int pid, String name) {
            this.pid = pid;
            this.name = name;
            this.state = ProcessState.NEW; // Initialzustand
        }
    }

    private int nextId = 1;
    private Map<Integer, Process> processes = new HashMap<>(); // Speichert alle Prozesse

    // Startet einen Prozess und setzt den Zustand auf RUNNING
    public int startProcess(String name) {
        int pid = nextId++;
        Process process = new Process(pid, name);
        process.state = ProcessState.RUNNING;
        processes.put(pid, process);
        System.out.println("Starting process " + pid + " (" + name + "): State = " + process.state);
        return pid;
    }

    // Stoppt einen Prozess und setzt den Zustand auf TERMINATED
    public void stopProcess(int pid) {
        Process process = processes.get(pid);
        if (process == null) {
            System.out.println("Error: Process " + pid + " not found!");
            return;
        }
        process.state = ProcessState.TERMINATED;
        System.out.println("Stopping process " + pid + ": State = " + process.state);
    }

    // Blockiert einen Prozess (z. B. für I/O)
    public void blockProcess(int pid) {
        Process process = processes.get(pid);
        if (process == null) {
            System.out.println("Error: Process " + pid + " not found!");
            return;
        }
        process.state = ProcessState.BLOCKED;
        System.out.println("Blocking process " + pid + ": State = " + process.state);
    }

    // Gibt den Zustand eines Prozesses zurück
    public ProcessState getProcessState(int pid) {
        Process process = processes.get(pid);
        return (process != null) ? process.state : null;
    }
}
