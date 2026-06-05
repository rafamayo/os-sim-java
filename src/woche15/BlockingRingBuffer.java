package woche15;

/**
 * Thread-sicherer Ringpuffer mit Blocking-Semantik (Woche 15).
 *
 * Struktur:
 *   buf[]  — festes Array der Größe capacity
 *   head   — nächste Leseposition  (Consumer)
 *   tail   — nächste Schreibposition (Producer)
 *   count  — aktuelle Anzahl Elemente
 *
 * Modulo-Arithmetik erzeugt den Ring:
 *   tail = (tail + 1) % buf.length
 *   head = (head + 1) % buf.length
 *
 *   Beispiel (capacity=4):
 *   put("A"): buf[0]="A", tail=1, count=1
 *   put("B"): buf[1]="B", tail=2, count=2
 *   take():   return buf[0], head=1, count=1
 *   put("C"): buf[2]="C", tail=3, count=2
 *   put("D"): buf[3]="D", tail=0, count=3  ← Wrap-around!
 */
public class BlockingRingBuffer<T> {

    private final Object[] buf;
    private int            head  = 0;
    private int            tail  = 0;
    private int            count = 0;

    private final String name;
    private int totalPuts = 0, totalTakes = 0, totalWaits = 0;

    public BlockingRingBuffer(int capacity, String name) {
        this.buf  = new Object[capacity];
        this.name = name;
    }

    public BlockingRingBuffer(int capacity) { this(capacity, "RingBuffer"); }

    // =========================================================
    // Aufgabe 2a — put()
    // =========================================================

    /**
     * Legt ein Element ein. Blockiert wenn Puffer voll.
     *
     * TODO (Aufgabe 2a) — Methode muss synchronized sein:
     *
     *   Schritt 1: WHILE count == buf.length: wait()
     *              (WHILE, nicht IF — mehrere Producer könnten gleichzeitig
     *               aufwachen; nur einer bekommt Platz, die anderen müssen
     *               erneut prüfen)
     *
     *   Schritt 2: buf[tail] = item
     *
     *   Schritt 3: tail = (tail + 1) % buf.length
     *              (Ring-Wrap-around: nach dem letzten Index kommt Index 0)
     *
     *   Schritt 4: count++
     *
     *   Schritt 5: totalPuts++
     *
     *   Schritt 6: notifyAll()
     *              (schlafende Consumer aufwecken)
     */
    public synchronized void put(T item) throws InterruptedException {
        // TODO: Implementieren
        throw new UnsupportedOperationException("put() nicht implementiert");
    }

    // =========================================================
    // Aufgabe 2b — take()
    // =========================================================

    /**
     * Entnimmt ein Element. Blockiert wenn Puffer leer.
     *
     * TODO (Aufgabe 2b) — Methode muss synchronized sein:
     *
     *   Schritt 1: WHILE count == 0: wait()
     *
     *   Schritt 2: T item = (T) buf[head]
     *
     *   Schritt 3: buf[head] = null
     *              (Referenz löschen — hilft dem Garbage Collector)
     *
     *   Schritt 4: head = (head + 1) % buf.length
     *
     *   Schritt 5: count--
     *
     *   Schritt 6: totalTakes++
     *
     *   Schritt 7: notifyAll()
     *              (schlafende Producer aufwecken)
     *
     *   Schritt 8: return item
     */
    @SuppressWarnings("unchecked")
    public synchronized T take() throws InterruptedException {
        // TODO: Implementieren
        throw new UnsupportedOperationException("take() nicht implementiert");
    }

    // =========================================================
    // Bereits implementiert — nicht ändern
    // =========================================================

    @SuppressWarnings("unchecked")
    public synchronized T tryTake() {
        if (count == 0) return null;
        T item = (T) buf[head];
        buf[head] = null;
        head = (head + 1) % buf.length;
        count--;
        totalTakes++;
        notifyAll();
        return item;
    }

    public synchronized int     size()     { return count; }
    public int                  capacity() { return buf.length; }
    public synchronized boolean isEmpty()  { return count == 0; }
    public synchronized boolean isFull()   { return count == buf.length; }
    public String               getName()  { return name; }

    public void printStats() {
        System.out.printf("[%s] Statistik: puts=%d, takes=%d, waits=%d%n",
            name, totalPuts, totalTakes, totalWaits);
    }

    @Override
    public String toString() {
        return String.format("%s[%d/%d, head=%d, tail=%d]",
            name, count, buf.length, head, tail);
    }
}
