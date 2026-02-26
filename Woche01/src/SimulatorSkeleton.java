/**
 * Eine einfache Klasse, die grundlegende Operationen eines Betriebssystem-Simulators nachbildet.
 * Erweitere diese Klasse um weitere Methoden (z. B. pauseProcess, resumeProcess).
 */
 
package Woche01;

public class SimulatorSkeleton {
    /**
     * Simuliert das Starten eines Prozesses.
     * @param processName Der Name des Prozesses (z. B. "Process 1").
     */
    public void startProcess(String processName) {
        System.out.println("Starting process: " + processName);
    }

    /**
     * Simuliert das Beenden eines Prozesses.
     * @param processName Der Name des Prozesses.
     */
    public void stopProcess(String processName) {
        System.out.println("Stopping process: " + processName);
    }

    // TODO: Füge hier eine neue Methode hinzu (Aufgabe 2)
    // Beispiel:
    /*
    public void pauseProcess(String processName) {
        System.out.println("Pausing process: " + processName);
    }
    */
}
