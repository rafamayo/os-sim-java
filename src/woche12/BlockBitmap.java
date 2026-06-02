package woche12;

/**
 * Verwaltet freie und belegte Datenblöcke auf der simulierten Disk
 * mittels einer Bitmap (1 Bit pro Block).
 *
 * Bit = 0: Block ist frei
 * Bit = 1: Block ist belegt
 */
public class BlockBitmap {

    private final boolean[] bits;
    private final int       capacity;
    private final String    name;

    public BlockBitmap(int capacity, String name) {
        this.capacity = capacity;
        this.name     = name;
        this.bits     = new boolean[capacity];
    }

    /**
     * Alloziert den ersten freien Block.
     *
     * TODO (Aufgabe 2a):
     *   Durchsuche das bits-Array von Index 0 bis capacity-1.
     *   Wenn bits[i] == false (= frei):
     *     - Setze bits[i] = true  (als belegt markieren)
     *     - Gib i zurück
     *   Falls kein freier Block gefunden: wirf OutOfSpaceException
     *
     * @return Nummer des allozierten Blocks
     * @throws OutOfSpaceException wenn kein freier Block vorhanden
     */
    public int allocate() throws OutOfSpaceException {
        // TODO: Implementieren
        throw new OutOfSpaceException(name + ": noch nicht implementiert");
    }

    /**
     * Gibt einen Block frei.
     *
     * TODO (Aufgabe 2b):
     *   1. Prüfe ob num im gültigen Bereich liegt (checkRange)
     *   2. Prüfe ob bits[num] == true (war belegt)
     *      Falls nicht: wirf IllegalArgumentException
     *   3. Setze bits[num] = false
     *
     * @param num Nummer des freizugebenden Blocks
     */
    public void free(int num) {
        checkRange(num);
        // TODO: Implementieren
    }

    /**
     * Prüft ob ein Block frei ist.
     *
     * TODO (Aufgabe 2c):
     *   Gib !bits[num] zurück (frei = nicht belegt)
     *
     * @param num Block-Nummer
     * @return true wenn der Block frei ist
     */
    public boolean isFree(int num) {
        checkRange(num);
        // TODO: Implementieren
        return false;
    }

    /**
     * Gibt die Anzahl der freien Einträge zurück.
     * (Bereits implementiert — als Referenz)
     */
    public int freeCount() {
        int count = 0;
        for (boolean b : bits) if (!b) count++;
        return count;
    }

    public int capacity() { return capacity; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" [").append(capacity).append(" Einträge]: ");
        for (boolean b : bits) sb.append(b ? "1" : "0");
        sb.append(" (").append(freeCount()).append(" frei)");
        return sb.toString();
    }

    // --- Hilfsmethoden ---

    private void checkRange(int num) {
        if (num < 0 || num >= capacity) {
            throw new IllegalArgumentException(
                name + ": ungültige Nummer " + num);
        }
    }

    // --- Exception ---

    public static class OutOfSpaceException extends Exception {
        public OutOfSpaceException(String msg) { super(msg); }
    }
}
