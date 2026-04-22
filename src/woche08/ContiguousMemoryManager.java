package woche08;

import java.util.ArrayList;
import java.util.List;

/**
 * Simulator für zusammenhängende Speicherverwaltung.
 *
 * Verwaltet eine geordnete Liste von MemoryBlocks (freie und belegte).
 * Unterstützt First-Fit, Best-Fit und Worst-Fit Platzierungsstrategien
 * sowie Coalescing beim Freigeben.
 * 
 * Die Klasse MemoryBlock repräsentiert einen zusammenhängenden Speicherblock (-> siehe MemoryBlock.java)
 */
public class ContiguousMemoryManager {

    private final int totalSize;
    private final AllocationStrategy strategy;
    private final List<MemoryBlock> blocks;   // nach Startadresse geordnet

    // Statistiken
    private int totalAllocations;
    private int failedAllocations;
    private int totalFrees;
    private int totalCoalesces;

    /**
     * Erzeugt einen neuen Speicher-Manager.
     *
     * @param totalSize  Gesamtgröße des verwalteten Speichers in Bytes
     * @param strategy   Platzierungsstrategie (FIRST_FIT, BEST_FIT, WORST_FIT)
     */
    public ContiguousMemoryManager(int totalSize, AllocationStrategy strategy) {
        this.totalSize  = totalSize;
        this.strategy   = strategy;
        this.blocks     = new ArrayList<>();
        blocks.add(new MemoryBlock(0, totalSize));
    }

    // =========================================================================
    // AUFGABE 1 – Platzierungsstrategien
    // =========================================================================
    // Implementieren Sie die drei Hilfsmethoden firstFit(), bestFit() und
    // worstFit(). Jede Methode soll die blocks-Liste durchlaufen und den
    // passenden freien MemoryBlock gemäß ihrer Strategie zurückgeben,
    // oder null wenn kein passender Block gefunden wird.
    //
    // Hinweis: Ein Block ist passend wenn b.isFree() && b.getSize() >= size.
    // =========================================================================

    /**
     * Versucht, einen Block der angegebenen Größe für den Prozess (pid) zu
     * allozieren. Die Platzzuweisung erfolgt gemäß der konfigurierten Strategie.
     *
     * @param size  benötigte Größe in Bytes
     * @param pid   PID des anfragenden Prozesses
     * @return Startadresse des allozierten Blocks, oder -1 bei Misserfolg
     */
    public int allocate(int size, int pid) {
        if (size <= 0) return -1;

        MemoryBlock chosen = null;

        switch (strategy) {
            case FIRST_FIT -> chosen = firstFit(size);
            case BEST_FIT  -> chosen = bestFit(size);
            case WORST_FIT -> chosen = worstFit(size);
        }

        if (chosen == null) {
            failedAllocations++;
            return -1;
        }

        totalAllocations++;
        return placeBlock(chosen, size, pid);
    }

    /**
     * AUFGABE 1a – First-Fit
     * Gibt den ERSTEN freien Block zurück, der groß genug ist.
     */

    // Bereits implementiert: Vorbild!
    private MemoryBlock firstFit(int size) {
        for (MemoryBlock b : blocks) {
            if (b.isFree() && b.getSize() >= size) {
                return b;
            }
        }
        return null;
    }

    /**
     * AUFGABE 1b – Best-Fit
     * Gibt den KLEINSTEN freien Block zurück, der groß genug ist.
     */
    private MemoryBlock bestFit(int size) {
        // TODO: Implementieren Sie Best-Fit
        // Durchlaufen Sie blocks und merken Sie sich den kleinsten passenden Block.
        // "Passend" bedeutet: b.isFree() && b.getSize() >= size
        // "Kleinster" bedeutet: b.getSize() ist minimal unter allen passenden Blöcken.
        throw new UnsupportedOperationException("bestFit() nicht implementiert");
    }

    /**
     * AUFGABE 1c – Worst-Fit
     * Gibt den GRÖSSTEN freien Block zurück.
     */
    private MemoryBlock worstFit(int size) {
        // TODO: Implementieren Sie Worst-Fit
        // Durchlaufen Sie blocks und merken Sie sich den größten passenden Block.
        throw new UnsupportedOperationException("worstFit() nicht implementiert");
    }

    /**
     * Platziert den Prozess im gewählten Block.
     * Falls der Block größer als benötigt ist, wird er gesplittet.
     * Diese Methode ist bereits vollständig implementiert.
     */
    private int placeBlock(MemoryBlock block, int size, int pid) {
        int start     = block.getStart();
        int remainder = block.getSize() - size;

        if (remainder > 0) {
            int idx = blocks.indexOf(block);
            MemoryBlock freeRemainder = new MemoryBlock(start + size, remainder);
            blocks.add(idx + 1, freeRemainder);
        }

        block.setSize(size);
        block.setFree(false);
        block.setPid(pid);
        return start;
    }

    // =========================================================================
    // AUFGABE 2 – Freigabe und Coalescing
    // =========================================================================
    // Implementieren Sie die Methode free() und die Hilfsmethode coalesce().
    //
    // free() soll:
    //   1. Den Block an der angegebenen Adresse in der blocks-Liste suchen.
    //   2. Ihn als frei markieren (setFree(true), setPid(-1)).
    //   3. coalesce() aufrufen.
    //
    // coalesce() soll:
    //   1. Den rechten Nachbarn prüfen: falls frei, beide zusammenführen
    //      (Größe addieren, rechten Block aus der Liste entfernen).
    //   2. Den linken Nachbarn prüfen: falls frei, beide zusammenführen.
    // =========================================================================

    /**
     * AUFGABE 2a – Freigabe
     * Gibt den Speicherblock frei, der an der angegebenen Adresse beginnt.
     *
     * @param address  Startadresse des freizugebenden Blocks
     * @return true wenn erfolgreich, false wenn die Adresse nicht gefunden wurde
     */
    public boolean free(int address) {
        // TODO: Suchen Sie den belegten Block mit block.getStart() == address
        // Markieren Sie ihn als frei: setFree(true), setPid(-1)
        // Rufen Sie coalesce(target) auf
        // Geben Sie true zurück bei Erfolg, false wenn nicht gefunden
        throw new UnsupportedOperationException("free() nicht implementiert");
    }

    /**
     * AUFGABE 2b – Coalescing
     * Führt angrenzende freie Blöcke zusammen.
     * Prüft rechten und linken Nachbarn von 'block'.
     */
    private void coalesce(MemoryBlock block) {
        // TODO: Holen Sie den Index des Blocks: blocks.indexOf(block)
        //
        // Rechter Nachbar (idx + 1):
        //   Falls vorhanden und frei:
        //     block.setSize(block.getSize() + right.getSize())
        //     blocks.remove(right)
        //     totalCoalesces++
        //
        // Linker Nachbar (idx - 1):
        //   Falls vorhanden und frei:
        //     left.setSize(left.getSize() + block.getSize())
        //     blocks.remove(block)
        //     totalCoalesces++
        //
        // ACHTUNG: Nach dem Zusammenführen mit dem rechten Nachbarn hat sich
        // der Index des linken Nachbarn nicht verändert.
        throw new UnsupportedOperationException("coalesce() nicht implementiert");
    }

    // =========================================================================
    // AUFGABE 3 – Fragmentierungsmetriken
    // =========================================================================
    // Implementieren Sie die drei Methoden externalFragmentation(),
    // numberOfHoles() und utilization().
    // =========================================================================

    /**
     * AUFGABE 3a – Externe Fragmentierung
     * EF = 1 – (größter freier Block / gesamter freier Speicher)
     * Gibt 0.0 zurück wenn kein freier Speicher vorhanden ist.
     */
    public double externalFragmentation() {
        // TODO: Durchlaufen Sie blocks
        // Berechnen Sie totalFree (Summe aller freien Blockgrößen)
        // und largestFree (Maximum aller freien Blockgrößen).
        // Geben Sie 0.0 zurück wenn totalFree == 0.
        // Sonst: return 1.0 - (double) largestFree / totalFree;
        throw new UnsupportedOperationException("externalFragmentation() nicht implementiert");
    }

    /**
     * AUFGABE 3b – Anzahl freier Löcher
     * Zählt zusammenhängende freie Bereiche.
     */
    public int numberOfHoles() {
        // TODO: Zählen Sie die freien Blöcke in der Liste
        throw new UnsupportedOperationException("numberOfHoles() nicht implementiert");
    }

    /**
     * AUFGABE 3c – Auslastung
     * Utilization = belegter Speicher / Gesamtspeicher
     */
    public double utilization() {
        // TODO: Summieren Sie die Größen aller belegten Blöcke
        // und teilen Sie durch totalSize
        throw new UnsupportedOperationException("utilization() nicht implementiert");
    }

    // =========================================================================
    // Hilfsmethoden (bereits implementiert)
    // =========================================================================

    /** Gibt den aktuellen Zustand des Speichers auf der Konsole aus. */
    public void printMemoryMap() {
        System.out.println("=== Memory Map (" + strategy + ") ===");
        for (MemoryBlock b : blocks) {
            System.out.println("  " + b);
        }
        System.out.printf("  Utilization:    %.1f%%%n", utilization() * 100);
        System.out.printf("  Ext. Fragm.:    %.3f%n",  externalFragmentation());
        System.out.printf("  Holes:          %d%n",    numberOfHoles());
        System.out.println();
    }

    /** Gibt eine Zusammenfassung der Statistiken aus. */
    public void printStats() {
        System.out.println("=== Statistiken ===");
        System.out.println("  Allokationen (erfolgreich): " + totalAllocations);
        System.out.println("  Allokationen (fehlgeschlagen): " + failedAllocations);
        System.out.println("  Freigaben: " + totalFrees);
        System.out.println("  Coalescing-Operationen: " + totalCoalesces);
    }

    // Getter für Tests
    public int  getTotalSize()           { return totalSize; }
    public List<MemoryBlock> getBlocks() { return blocks; }
    public int  getTotalAllocations()    { return totalAllocations; }
    public int  getFailedAllocations()   { return failedAllocations; }
    public AllocationStrategy getStrategy() { return strategy; }
}
