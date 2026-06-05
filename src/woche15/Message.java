package woche15;

/**
 * Unveränderliches Nachrichtenobjekt für Message Queues.
 *
 * Eine Nachricht hat:
 *   - einen Typ  (z.B. "ORDER", "STATUS", "SHUTDOWN")
 *   - einen Inhalt (beliebiger String)
 *   - einen Zeitstempel (automatisch gesetzt)
 *
 * Nachrichten sind immutable — nach dem Erstellen können sie
 * nicht mehr geändert werden. Das ist wichtig für Thread-Sicherheit:
 * mehrere Threads können dieselbe Nachricht lesen ohne Synchronisation.
 */
public final class Message {

    private final String type;
    private final String data;
    private final long   timestamp;
    private final int    senderId;

    /**
     * Erstellt eine neue Nachricht.
     *
     * @param type     Nachrichtentyp (z.B. "ORDER", "STATUS")
     * @param data     Nachrichteninhalt
     */
    public Message(String type, String data) {
        this(type, data, -1);
    }

    /**
     * Erstellt eine neue Nachricht mit Absender-ID.
     *
     * @param type     Nachrichtentyp
     * @param data     Nachrichteninhalt
     * @param senderId PID des sendenden Prozesses (-1 = unbekannt)
     */
    public Message(String type, String data, int senderId) {
        this.type      = type;
        this.data      = data;
        this.senderId  = senderId;
        this.timestamp = System.currentTimeMillis();
    }

    public String getType()      { return type; }
    public String getData()      { return data; }
    public long   getTimestamp() { return timestamp; }
    public int    getSenderId()  { return senderId; }

    @Override
    public String toString() {
        return String.format("Msg{type=%s, data=\"%s\"%s}",
            type, data,
            senderId >= 0 ? ", from=" + senderId : "");
    }
}
