package woche08;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Buddy-Allocator.
 *
 * Heapgröße = 2^maxOrder Bytes.
 * Jede Ordnung k hat eine Freiliste von Blöcken der Größe 2^k.
 * Allokation: auf nächste Zweierpotenz aufrunden, dann splitten bis zur
 *             gewünschten Ordnung.
 * Freigabe:   Buddy-Adresse berechnen, ggf. zusammenführen (coalesce).
 */
public class BuddyAllocator {

    private final int maxOrder;
    private final int heapSize;
    private final List<Deque<Integer>> freeLists;

    // Belegungstabelle: allocated[address] = Ordnung des belegten Blocks, -1 = frei
    private final int[] allocated;

    // Statistik
    private int totalAllocations;
    private int failedAllocations;
    private int totalFrees;
    private int totalSplits;
    private int totalMerges;

    /**
     * @param maxOrder  Heap = 2^maxOrder Bytes (z.B. 10 → 1024 Bytes)
     */
    public BuddyAllocator(int maxOrder) {
        this.maxOrder  = maxOrder;
        this.heapSize  = 1 << maxOrder; // Verschiebung der Zahl 1 in ihrer Binärdarstellung um maxOrder Stellen nach links
        this.freeLists = new ArrayList<>(maxOrder + 1);
        for (int i = 0; i <= maxOrder; i++) {
            freeLists.add(new ArrayDeque<>());
        }
        freeLists.get(maxOrder).add(0);
        this.allocated = new int[heapSize];
        java.util.Arrays.fill(allocated, -1);
    }

    // =========================================================================
    // AUFGABE 4 – Allokation im Buddy-System
    // =========================================================================
    // Implementieren Sie allocate(int size):
    //
    //  Schritt 1: Gewünschte Ordnung berechnen:
    //               k = ceilLog2(size)   (bereits implementiert)
    //             Falls k > maxOrder: Fehlschlag.
    //
    //  Schritt 2: Suche die kleinste Ordnung j >= k,
    //             für die freeLists.get(j) nicht leer ist.
    //             Falls keine gefunden (j > maxOrder): Fehlschlag.
    //
    //  Schritt 3: Splitten bis Ordnung k:
    //             Solange j > k:
    //               - Block aus freeLists.get(j) entnehmen
    //               - j-- (eine Ordnung kleiner)
    //               - Buddy-Adresse: buddy = block + (1 << j)
    //               - Beide Hälften in freeLists.get(j) eintragen
    //               - totalSplits++
    //
    //  Schritt 4: Block aus freeLists.get(k) entnehmen,
    //             allocated[block] = k setzen,
    //             totalAllocations++ und Adresse zurückgeben.
    // =========================================================================

    /**
     * AUFGABE 4a – Allokation
     *
     * @param size  benötigte Größe in Bytes (> 0)
     * @return Startadresse oder -1 bei Misserfolg
     */
    public int allocate(int size) {
        if (size <= 0 || size > heapSize) {
            failedAllocations++;
            return -1;
        }

        // TODO: Implementieren Sie den Buddy-Allokations-Algorithmus
        // Nutzen Sie ceilLog2(size) für die Ordnung.
        // Denken Sie daran: (1 << j) ist die Blockgröße der Ordnung j.
        throw new UnsupportedOperationException("allocate() nicht implementiert");
    }

    // =========================================================================
    // AUFGABE 4b – Freigabe und Coalescing im Buddy-System
    // =========================================================================
    // Implementieren Sie free(int address):
    //
    //  Schritt 1: Ordnung k aus allocated[address] lesen.
    //             Falls k < 0: der Block war nicht belegt → return false.
    //
    //  Schritt 2: allocated[address] = -1 (freigeben)
    //             freeLists.get(k).add(address)
    //             totalFrees++
    //
    //  Schritt 3: Coalescing-Schleife (solange k < maxOrder):
    //               buddy = buddyAddress(address, k)   (bereits implementiert)
    //               Falls buddy NICHT in freeLists.get(k): Abbruch.
    //               Sonst:
    //                 - Beide aus freeLists.get(k) entfernen
    //                 - address = Math.min(address, buddy)  (linker Block)
    //                 - k++
    //                 - freeLists.get(k).add(address)
    //                 - totalMerges++
    // =========================================================================

    /**
     * AUFGABE 4b – Freigabe
     *
     * @param address  Startadresse des freizugebenden Blocks
     * @return true wenn erfolgreich, false sonst
     */
    public boolean free(int address) {
        if (address < 0 || address >= heapSize) return false;

        // TODO: Implementieren Sie die Buddy-Freigabe mit Coalescing
        throw new UnsupportedOperationException("free() nicht implementiert");
    }

    // =========================================================================
    // Hilfsmethoden (bereits implementiert)
    // =========================================================================

    /**
     * Berechnet die Adresse des Buddies.
     * Formel: buddy(address, k) = address XOR 2^k
     */
    private int buddyAddress(int address, int order) {
        return address ^ (1 << order);
    }

    /**
     * Berechnet ceil(log2(n)), mindestens 0.
     * Beispiele: ceilLog2(1)=0, ceilLog2(3)=2, ceilLog2(4)=2, ceilLog2(5)=3
     */
    static int ceilLog2(int n) {
        if (n <= 1) return 0;
        return 32 - Integer.numberOfLeadingZeros(n - 1);
    }

    /** Gibt den Zustand aller Freilisten aus. */
    public void printFreeLists() {
        System.out.println("=== Buddy Freelists (maxOrder=" + maxOrder
                         + ", heapSize=" + heapSize + ") ===");
        for (int k = 0; k <= maxOrder; k++) {
            if (!freeLists.get(k).isEmpty()) {
                System.out.printf("  Order %2d (size=%4d): %s%n",
                    k, (1 << k), freeLists.get(k));
            }
        }
    }

    /** Gibt Statistiken aus. */
    public void printStats() {
        System.out.println("=== Buddy Statistiken ===");
        System.out.println("  Allokationen (erfolgreich): " + totalAllocations);
        System.out.println("  Allokationen (fehlgeschlagen): " + failedAllocations);
        System.out.println("  Freigaben: " + totalFrees);
        System.out.println("  Splits: " + totalSplits);
        System.out.println("  Merges: " + totalMerges);
    }

    // Getter für Tests
    public int getMaxOrder()     { return maxOrder; }
    public int getHeapSize()     { return heapSize; }
    public int getAllocatedOrder(int address) { return allocated[address]; }
    public List<Deque<Integer>> getFreeLists() { return freeLists; }
}
