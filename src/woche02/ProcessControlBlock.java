package woche02;


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

    public ProcessControlBlock(int pid, String name) {
        this.pid = pid;
        this.name = name;
        this.state = ProcessState.NEW;
        this.priority = 1;      // Default-Priorität
        this.parentPid = -1;    // Kein Elternprozess
        this.registers = new String[16]; // 16 Register (wie in echten CPUs)
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

    @Override
    public String toString() {
        return String.format(
            "PCB{pid=%d, name='%s', state=%s, priority=%d, parentPid=%d, registers=%s}",
            pid, name, state, priority, parentPid, java.util.Arrays.toString(registers)
        );
    }
}