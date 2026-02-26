/**
 * Ein einfaches Programm, das den Betriebssystem-Simulator startet.
 * Erweitere diese Klasse, um weitere Prozesse zu simulieren.
 */
 
package Woche01;
 
public class HelloSimulator {
    public static void main(String[] args) {
        // Erzeuge eine Instanz des Simulators
        SimulatorSkeleton simulator = new SimulatorSkeleton();

        // Starte und stoppe einen Prozess
        simulator.startProcess("Process 1");
        simulator.stopProcess("Process 1");

        // TODO: Füge hier weitere Prozesse hinzu (Aufgabe 1)
    }
}
