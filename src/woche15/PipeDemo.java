package woche15;

// ============================================================
// PipeDemo.java
// ============================================================

/**
 * Demonstriert die Pipe zwischen zwei simulierten Prozessen.
 * Zeigt: Bytestrom, blocking, EOF.
 */
class PipeDemo {

    public static void main(String[] args) throws InterruptedException {

        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║  Pipe-Demo (Woche 15)                    ║");
        System.out.println("╚══════════════════════════════════════════╝");

        Pipe pipe = new Pipe(64, "Hauptpipe");

        ProcessControlBlock writer = new ProcessControlBlock(1, "TextWriter");
        ProcessControlBlock reader = new ProcessControlBlock(2, "TextReader");
        writer.attachPipe("main", pipe);
        reader.attachPipe("main", pipe);

        // Writer-Thread: schreibt Nachrichten mit kurzer Pause
        Thread writerThread = new Thread(() -> {
            try {
                String[] messages = {
                    "Hallo ", "Welt!", " Dies ", "ist eine ", "Pipe."
                };
                for (String msg : messages) {
                    writer.writePipe("main", msg);
                    Thread.sleep(100);
                }
                pipe.close(); // EOF signalisieren
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Writer");

        // Reader-Thread: liest in Blöcken
        Thread readerThread = new Thread(() -> {
            try {
                StringBuilder sb = new StringBuilder();
                while (true) {
                    byte[] data = pipe.read(8);
                    if (data.length == 0) {
                        System.out.println("[Reader] EOF — Pipe geschlossen");
                        break;
                    }
                    String chunk = new String(data);
                    System.out.printf("[Reader] Gelesen: \"%s\"%n", chunk);
                    sb.append(chunk);
                }
                System.out.println("[Reader] Vollständige Nachricht: \"" + sb + "\"");
                System.out.println("[Reader] Beachte: Bytestrom ohne Grenzen!");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Reader");

        readerThread.start();
        writerThread.start();
        writerThread.join();
        readerThread.join();
    }
}
