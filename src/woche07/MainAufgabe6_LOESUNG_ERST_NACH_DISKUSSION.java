package woche07;

import java.util.ArrayList;
import java.util.List;

/**
 * ⚠️ LÖSUNG zu Aufgabe 6 (Bonus) – NUR NACH DER DISKUSSION ÖFFNEN!
 *
 * Diese Datei löst die Diskussionsfragen aus Aufgabe 6 auf
 * ("Eigenes Experiment", siehe README.md und das TODO in
 * MainWaitForGraph.java). Bitte erst öffnen, nachdem du das Szenario
 * selbst gebaut und die Fragen im Team besprochen hast – sonst nimmst
 * du dir die Pointe der Übung!
 *
 * Szenario: WFG mit 4 Prozessen (P0–P3):
 *   - P0 → P1 → P2 → P0   (echter Zyklus)
 *   - P3 → P1              (P3 wartet auf P1, ist aber NICHT Teil des Zyklus)
 *
 * Ziel: zeigen, dass WaitForGraph.detectDeadlock() nicht "den Zyklus"
 * zurückgibt, sondern den kompletten DFS-Pfad vom Startknoten bis zur
 * Rückkante. Startet die DFS zufällig bei einem Prozess, der nur IN den
 * Zyklus hineinführt (wie P3), taucht dieser fälschlich in der Ausgabe
 * auf – mit Folgen für die Victim-Auswahl bei der Recovery.
 *
 * Start:
 *   java woche07.MainAufgabe6_LOESUNG_ERST_NACH_DISKUSSION
 */
public class MainAufgabe6_LOESUNG_ERST_NACH_DISKUSSION {

    public static void main(String[] args) {
        header("AUFGABE 6 (LÖSUNG) – Grenzen der Zyklusdetektion");

        WaitForGraph wfg = new WaitForGraph();

        // Bewusste Einfüge-Reihenfolge: P3 zuerst, damit detectDeadlock()
        // seine DFS deterministisch bei P3 startet (WaitForGraph iteriert
        // intern über eine LinkedHashMap in Einfüge-Reihenfolge – ohne
        // diesen Trick wäre das "falsche" Ergebnis reiner Zufall).
        wfg.addProcess(3);
        wfg.addProcess(0);
        wfg.addProcess(1);
        wfg.addProcess(2);

        wfg.addWaitEdge(0, 1);   // Teil des Zyklus
        wfg.addWaitEdge(1, 2);   // Teil des Zyklus
        wfg.addWaitEdge(2, 0);   // schließt den Zyklus
        wfg.addWaitEdge(3, 1);   // P3 wartet auf P1 – NICHT Teil des Zyklus

        wfg.printGraph();

        // --- Naives Ergebnis: unveränderte WaitForGraph.detectDeadlock() ---
        System.out.println("── Naive Ausgabe von detectDeadlock() ────────────────");
        List<Integer> rawPath = wfg.detectDeadlock();
        printDetection(rawPath);
        System.out.println("→ P3 taucht in der Liste auf, obwohl P3 an keiner");
        System.out.println("  Rückkante beteiligt ist. Es ist nur der DFS-Startknoten");
        System.out.println("  und 'läuft' zufällig in den bestehenden Zyklus hinein.\n");

        // --- Bereinigtes Ergebnis: nur echte Zyklus-Mitglieder ---
        System.out.println("── Bereinigt: nur echte Zyklus-Mitglieder ────────────");
        List<Integer> trueCycle = trimToCycle(rawPath);
        printDetection(trueCycle);

        // --- Antworten auf die Diskussionsfragen aus der README ---
        System.out.println("── Antworten auf die Diskussionsfragen ───────────────");
        System.out.println("Wird P3 als Teil des Deadlocks gemeldet?");
        System.out.println("  Kommt auf die DFS-Startreihenfolge an – hier: JA (fälschlich),");
        System.out.println("  weil P3 zufällig zuerst besucht wird. Bei anderer Reihenfolge");
        System.out.println("  (z. B. P0 zuerst, wie in B1–B4) würde P3 gar nicht auftauchen.");
        System.out.println("  Der rohe Rückgabewert ist also nicht robust gegenüber der");
        System.out.println("  Traversierungsreihenfolge.\n");

        System.out.println("Welche Prozesse sind tatsächlich blockiert? Kann P3 fortfahren?");
        System.out.println("  Blockiert sind alle vier (P0, P1, P2 UND P3). Aber nur P0, P1, P2");
        System.out.println("  stehen in echtem Circular Wait. P3 hängt nur transitiv daran:");
        System.out.println("  es wartet auf P1, das nie freigegeben wird, solange der Zyklus");
        System.out.println("  besteht. Sobald der Zyklus aufgelöst ist (z. B. P1 wird Victim),");
        System.out.println("  kann P3 automatisch weiterlaufen – ohne dass man P3 anfassen muss.\n");

        System.out.println("Wie würde ein reales OS mit P3 umgehen?");
        System.out.println("  Ein reales OS würde den Victim ausschließlich aus der bereinigten");
        System.out.println("  Zyklus-Liste wählen (hier: P0, P1 oder P2), NIE aus dem bloßen");
        System.out.println("  DFS-Einstiegspfad. P3 als Victim zu wählen wäre wirkungslos:");
        System.out.println("  P3 hält keine Ressource, auf die P0/P1/P2 warten – der Deadlock");
        System.out.println("  bliebe bestehen, und ein unbeteiligter Prozess wäre unnötig");
        System.out.println("  abgebrochen worden.\n");
    }

    // =========================================================================
    //  Die eigentliche "Lösung": Pfad → echten Zyklus zurechtstutzen
    //
    //  WaitForGraph.detectDeadlock() bleibt dabei komplett unverändert
    //  (siehe WaitForGraph.java) – das Zurechtstutzen passiert erst hier,
    //  bei der Auswertung des öffentlichen Rückgabewerts.
    // =========================================================================

    /**
     * detectDeadlock() liefert den kompletten DFS-Pfad vom Startknoten bis
     * zur Rückkante zurück, nicht nur den Zyklus selbst. Der Knoten, an dem
     * die Rückkante erkannt wurde, taucht darin zweimal auf: einmal beim
     * ersten Besuch, einmal am Listenende. Alles VOR dem ersten Vorkommen
     * ist nur "Einstiegspfad" (wie P3 hier) und gehört nicht zum
     * eigentlichen Circular Wait – wir schneiden es einfach ab.
     */
    private static List<Integer> trimToCycle(List<Integer> path) {
        if (path.isEmpty()) return path;
        int closingNode = path.get(path.size() - 1);
        int start = path.indexOf(closingNode);      // erstes Vorkommen
        return new ArrayList<>(path.subList(start, path.size()));
    }

    // =========================================================================
    //  Hilfsmethoden (identisch zu MainWaitForGraph.java)
    // =========================================================================

    private static void printDetection(List<Integer> cycle) {
        if (cycle.isEmpty()) {
            System.out.println("✅ Kein Deadlock.\n");
        } else {
            String s = cycle.stream().map(i -> "P" + i).reduce((a, b) -> a + "→" + b).orElse("");
            System.out.println("🔴 DEADLOCK! Zyklus: " + s + "\n");
        }
    }

    private static void header(String title) {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.printf( "║  %-52s  ║%n", title);
        System.out.println("╚══════════════════════════════════════════════════════╝\n");
    }
}
