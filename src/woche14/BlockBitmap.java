package woche14;

/**
 * Verwaltet freie und belegte Datenblöcke auf der simulierten Disk
 * mittels einer Bitmap (1 Bit pro Block).
 *
 * Bit = 0: Block ist frei
 * Bit = 1: Block ist belegt
 *
 * Dieselbe Klasse wird auch als InodeBitmap verwendet (Inode-Nummern
 * statt Block-Nummern).
 */
public class BlockBitmap {

    private final boolean[] bits;
    private final int       capacity;
    private final String    name;     // "Block Bitmap" oder "Inode Bitmap"

    /**
     * Erstellt eine neue Bitmap mit der angegebenen Kapazität.
     * Alle Bits werden auf 0 (frei) gesetzt.
     *
     * @param capacity Anzahl der verwalteten Blöcke/Inodes
     * @param name     Bezeichnung für die Ausgabe
     */
    public BlockBitmap(int capacity, String name) {
        this.capacity = capacity;
        this.name     = name;
        this.bits     = new boolean[capacity];
        // Alle Bits initial 0 = frei
    }

    /**
     * Alloziert den ersten freien Block.
     *
     * @return Nummer des allozierten Blocks
     * @throws OutOfSpaceException wenn kein freier Block vorhanden
     */
    public int allocate() throws OutOfSpaceException {
        for (int i = 0; i < capacity; i++) {
            if (!bits[i]) {
                bits[i] = true;
                return i;
            }
        }
        throw new OutOfSpaceException(name + ": keine freien Einträge mehr");
    }

    /**
     * Gibt einen Block frei.
     *
     * @param num Nummer des freizugebenden Blocks
     * @throws IllegalArgumentException wenn der Block nicht belegt war
     *         oder außerhalb des gültigen Bereichs liegt
     */
    public void free(int num) {
        checkRange(num);
        if (!bits[num]) {
            throw new IllegalArgumentException(
                name + ": Block " + num + " war nicht belegt");
        }
        bits[num] = false;
    }

    /**
     * Prüft ob ein Block frei ist.
     *
     * @param num Block-Nummer
     * @return true wenn der Block frei ist
     */
    public boolean isFree(int num) {
        checkRange(num);
        return !bits[num];
    }

    /**
     * Gibt die Anzahl der freien Einträge zurück.
     */
    public int freeCount() {
        int count = 0;
        for (boolean b : bits) if (!b) count++;
        return count;
    }

    /**
     * Gibt die Gesamtkapazität zurück.
     */
    public int capacity() {
        return capacity;
    }

    /**
     * Visualisiert die Bitmap als Zeichenkette.
     * 0 = frei, 1 = belegt
     */
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
                name + ": ungültige Nummer " + num
                + " (Bereich: 0-" + (capacity - 1) + ")");
        }
    }

    // --- Exception ---

    public static class OutOfSpaceException extends Exception {
        public OutOfSpaceException(String msg) { super(msg); }
    }
}
