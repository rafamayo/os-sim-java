package woche11;

/**
 * Repräsentiert den physischen Hauptspeicher als Array von Frames.
 *
 * Jeder Frame kann frei oder von einem Prozess (PID) belegt sein.
 * Frames werden über ihre Nummer (0 bis numFrames-1) adressiert.
 */
public class PhysicalMemory {

    public static final int DEFAULT_NUM_FRAMES = 16;
    public static final int DEFAULT_PAGE_SIZE  = 256;  // Bytes

    private final int   numFrames;
    private final int   pageSize;
    private final int[] frameOwner;   // frameOwner[i] = PID des Besitzers, -1 = frei

    /**
     * Erzeugt einen physischen Speicher mit den angegebenen Parametern.
     *
     * @param numFrames  Anzahl physischer Frames
     * @param pageSize   Größe eines Frames/einer Seite in Bytes
     */
    public PhysicalMemory(int numFrames, int pageSize) {
        this.numFrames  = numFrames;
        this.pageSize   = pageSize;
        this.frameOwner = new int[numFrames];
        java.util.Arrays.fill(frameOwner, -1);   // alle Frames frei
    }

    /** Erzeugt einen physischen Speicher mit Standardwerten. */
    public PhysicalMemory() {
        this(DEFAULT_NUM_FRAMES, DEFAULT_PAGE_SIZE);
    }

    /**
     * Sucht und reserviert einen freien Frame für den angegebenen Prozess.
     *
     * @param pid  PID des Prozesses
     * @return Framenummer oder -1 wenn kein Frame frei ist
     */
    public int allocateFrame(int pid) {
        for (int i = 0; i < numFrames; i++) {
            if (frameOwner[i] == -1) {
                frameOwner[i] = pid;
                return i;
            }
        }
        return -1;   // kein freier Frame
    }

    /**
     * Gibt den angegebenen Frame frei.
     *
     * @param frame  Framenummer
     * @return true wenn erfolgreich, false wenn der Frame bereits frei war
     */
    public boolean freeFrame(int frame) {
        if (frame < 0 || frame >= numFrames) return false;
        if (frameOwner[frame] == -1) return false;
        frameOwner[frame] = -1;
        return true;
    }

    /**
     * Gibt alle Frames frei, die dem angegebenen Prozess gehören.
     *
     * @param pid  PID des Prozesses
     * @return Anzahl der freigegebenen Frames
     */
    public int freeAllFrames(int pid) {
        int count = 0;
        for (int i = 0; i < numFrames; i++) {
            if (frameOwner[i] == pid) {
                frameOwner[i] = -1;
                count++;
            }
        }
        return count;
    }

    /** Gibt die physische Adresse für Frame + Offset zurück. */
    public int physicalAddress(int frame, int offset) {
        return frame * pageSize + offset;
    }

    /** Gibt die Anzahl freier Frames zurück. */
    public int freeFrames() {
        int count = 0;
        for (int f : frameOwner) {
            if (f == -1) count++;
        }
        return count;
    }

    /** Gibt den Besitzer eines Frames zurück (-1 = frei). */
    public int getFrameOwner(int frame) {
        if (frame < 0 || frame >= numFrames) return -1;
        return frameOwner[frame];
    }

    /** Gibt eine Übersicht aller Frames auf der Konsole aus. */
    public void printFrameMap() {
        System.out.printf("=== Physical Memory (%d frames × %d bytes = %d bytes total) ===%n",
            numFrames, pageSize, numFrames * pageSize);
        System.out.printf("    Free frames: %d / %d%n", freeFrames(), numFrames);
        for (int i = 0; i < numFrames; i++) {
            String owner = (frameOwner[i] == -1)
                ? "FREE"
                : "PID=" + frameOwner[i];
            System.out.printf("    Frame %3d [%4d – %4d]: %s%n",
                i, i * pageSize, (i + 1) * pageSize - 1, owner);
        }
        System.out.println();
    }

    // --- Getter ---
    public int getNumFrames()  { return numFrames; }
    public int getPageSize()   { return pageSize; }
}
