package woche02;

/**
 * Process Control Block (PCB) - Vervollständige diese Klasse!
 * Implementiere die fehlenden Methoden und Felder gemäß der Aufgabenstellung.
 */
public class ProcessControlBlock {
    // Das ProcessState-Enum steht in seiner eigenen Datei zur Verfügung!

    // TODO: Deklariere die folgenden Felder (gemäß Aufgabenstellung)
    // private int pid;
    // private String name;
    // private ProcessState state;
    // private int priority;
    // private int parentPid;
    // private String[] registers;

    public ProcessControlBlock(int pid, String name) {
        // TODO: Initialisiere alle Felder im Konstruktor
        // this.pid = pid;
        // this.name = name;
        // this.state = ProcessState.NEW;
        // this.priority = 1;
        // this.parentPid = -1;
        // this.registers = new String[16];
    }

    // TODO: Füge Getter/Setter für alle Felder hinzu
    // public int getPid() { return pid; }
    // public void setState(ProcessState state) { this.state = state; }
    // ...
    public void setRegister(int index, String value) {
        if (index >= 0 && index < registers.length) {
            registers[index] = value;
        }
    }

    @Override
    public String toString() {
        // TODO: Implementiere eine toString()-Methode, die alle Felder ausgibt
        return "ProcessControlBlock{...}";
    }
}

