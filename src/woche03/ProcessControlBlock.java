package woche03;


/**
 * Process Control Block (PCB) - Erweiterung der Process-Klasse.
 * Enthält zusätzliche Metadaten wie Priorität, Elternprozess und Register.
 */
public class ProcessControlBlock {

    private int pid;
    private String name;
    private ProcessState state; // Zustand
    private int priority;       // Priorität für Scheduling
    private int parentPid;      // Elternprozess-ID (-1 = kein Elternprozess)
    private String[] registers; // Simulierte CPU-Register
 
    // Ab Woche 3 erforderlich
    private int arrivalTime;    // Neu: Ankunftszeit (für Scheduling)
    private int burstTime;      // Neu: Gesamt-Burst-Time (für SJF/SRT)
    // TODO: Zeiten ergänzen!
    // --->

    

    /**
     * Konstruktor mit allen benötigten Parametern für Scheduling. Ab Woche 3
    */
    public ProcessControlBlock(int pid, String name, int arrivalTime, int burstTime) {
        this.pid = pid;
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;  // Initial gleich burstTime
        this.state = ProcessState.NEW;
        this.priority = 0;
        this.parentPid = -1;
        this.registers = new String[16];
        this.startTime = -1;
        this.finishTime = -1;
        this.usedTime = 0;
    }

    public ProcessControlBlock clone() {
        ProcessControlBlock clone = new ProcessControlBlock(
        this.pid,
        this.name,
        this.arrivalTime,
        this.burstTime
        );
        clone.setState(this.state);
        clone.setPriority(this.priority);
        clone.setParentPid(this.parentPid);
        clone.setRemainingTime(this.remainingTime);
        clone.setStartTime(this.startTime);
        clone.setFinishTime(this.finishTime);
        clone.setUsedTime(this.usedTime);
        //clone.setRegisters(this.registers.clone());  // Falls registers geklont werden muss
        return clone;    
    }

    // Getter/Setter für alle Felder
    public int getPid() {
        return pid;
    }

    public String getName() {
        return name;
    }

    public ProcessState getState() {
        return state;
    }

    public void setState(ProcessState state) {
        this.state = state;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getParentPid() {
        return parentPid;
    }

    public void setParentPid(int parentPid) {
        this.parentPid = parentPid;
    }

    public String[] getRegisters() {
        return registers;
    }

    public void setRegister(int index, String value) {
        if (index >= 0 && index < registers.length) {
            registers[index] = value;
        }
    }

    // BEGIN: Erst in Woche 03 erforderlich
    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(int finishTime) {
        this.finishTime = finishTime;
    }
    
    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getBurstTime() {
        return burstTime;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }

    public int getUsedTime() {
    return usedTime;
    }

    public void setUsedTime(int usedTime) {
        this.usedTime = usedTime;
    }
    // END: Erst in Woche 03 erforderlich


    @Override
    public String toString() {
        return String.format(
            "PCB{pid=%d, name='%s', arrivalTime=%d, burstTime=%d, remainingTime=%d, state=%s, priority=%d, parentPid=%d, registers=%s, startTime=%d, finishTime=%d}",
            pid, name, arrivalTime, burstTime, remainingTime, state, priority, parentPid, java.util.Arrays.toString(registers), startTime, finishTime
        );
    }
}