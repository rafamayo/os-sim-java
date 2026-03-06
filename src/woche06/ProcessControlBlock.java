package woche06;

/** Vereinfachtes PCB für Logging und Zustandssimulation. */
public class ProcessControlBlock {
    private final int pid;
    private final String name;
    private ProcessState state;
    private Object blockedOn;

    public ProcessControlBlock(int pid, String name) {
        this.pid = pid;
        this.name = name;
        this.state = ProcessState.NEW;
        this.blockedOn = null;
    }

    public int getPid() { return pid; }
    public String getName() { return name; }
    public ProcessState getState() { return state; }
    public void setState(ProcessState state) { this.state = state; }
    public Object getBlockedOn() { return blockedOn; }

    /** Markiert einen Prozess als blockiert (nur Simulation/Logging). */
    public void blockOn(Object resource) {
        this.state = ProcessState.BLOCKED;
        this.blockedOn = resource;
        System.out.println("[" + name + "] Blockiert auf " + resource);
    }

    /** Markiert einen Prozess als wieder bereit. */
    public void unblock() {
        this.state = ProcessState.READY;
        this.blockedOn = null;
        System.out.println("[" + name + "] Wurde geweckt");
    }

    @Override
    public String toString() {
        return "PCB{pid=" + pid + ", name='" + name + "', state=" + state + "}";
    }
}
