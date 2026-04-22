package woche08;

/**
 * Process Control Block (PCB) — Woche 08
 * Unverändert gegenüber den Scheduling-Wochen.
 * Wird in Woche 09 um Speicherverwaltungs-Attribute erweitert.
 */
public class ProcessControlBlock {

    private int pid;
    private String name;
    private ProcessState state;
    private int priority;
    private int arrivalTime;
    private int burstTime;
    private int remainingTime;
    private int startTime;
    private int finishTime;
    private int usedTime;

    public ProcessControlBlock(int pid, String name, int arrivalTime, int burstTime) {
        this.pid        = pid;
        this.name       = name;
        this.arrivalTime = arrivalTime;
        this.burstTime   = burstTime;
        this.remainingTime = burstTime;
        this.state      = ProcessState.NEW;
        this.priority   = 0;
        this.startTime  = -1;
        this.finishTime = -1;
        this.usedTime   = 0;
    }

    // --- Getter / Setter ---
    public int    getPid()            { return pid; }
    public String getName()           { return name; }
    public ProcessState getState()    { return state; }
    public void   setState(ProcessState s) { this.state = s; }
    public int    getPriority()       { return priority; }
    public void   setPriority(int p)  { this.priority = p; }
    public int    getArrivalTime()    { return arrivalTime; }
    public int    getBurstTime()      { return burstTime; }
    public int    getRemainingTime()  { return remainingTime; }
    public void   setRemainingTime(int r) { this.remainingTime = r; }
    public int    getStartTime()      { return startTime; }
    public void   setStartTime(int t) { this.startTime = t; }
    public int    getFinishTime()     { return finishTime; }
    public void   setFinishTime(int t){ this.finishTime = t; }
    public int    getUsedTime()       { return usedTime; }
    public void   setUsedTime(int u)  { this.usedTime = u; }

    @Override
    public String toString() {
        return String.format(
            "PCB{pid=%d, name='%s', arrivalTime=%d, burstTime=%d, "
          + "remainingTime=%d, state=%s, priority=%d, startTime=%d, finishTime=%d}",
            pid, name, arrivalTime, burstTime,
            remainingTime, state, priority, startTime, finishTime);
    }
}
