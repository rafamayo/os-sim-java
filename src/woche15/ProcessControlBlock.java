package woche15;

import java.util.HashMap;
import java.util.Map;

/**
 * Process Control Block — erweitert um IPC-Kontext (Woche 15).
 *
 * Jeder Prozess kann Pipes, Message Queues und Shared Memory
 * halten und darüber mit anderen Prozessen kommunizieren.
 *
 * Die IPC-Ressourcen sind benannt (String-Schlüssel) —
 * beide Prozesse müssen denselben Namen kennen um kommunizieren zu können.
 * Das entspricht dem Konzept benannter Pipes (FIFOs) oder
 * System-V IPC mit Schlüsseln in echten Betriebssystemen.
 */
public class ProcessControlBlock {

    // --- Identifikation ---
    public final int    pid;
    public final String name;

    // --- Prozess-Zustand ---
    public enum State { NEW, READY, RUNNING, WAITING, TERMINATED }
    public State state;

    // --- IPC-Ressourcen ---
    public final Map<String, Pipe>         ownedPipes    = new HashMap<>();
    public final Map<String, MessageQueue> ownedQueues   = new HashMap<>();
    public final Map<String, SharedMemory> sharedSegments = new HashMap<>();

    /**
     * Erstellt einen neuen PCB.
     */
    public ProcessControlBlock(int pid, String name) {
        this.pid   = pid;
        this.name  = name;
        this.state = State.NEW;
    }

    // =========================================================
    // IPC-Operationen
    // =========================================================

    /**
     * Sendet eine Nachricht über eine benannte Message Queue.
     *
     * @param queueName Name der Queue
     * @param msg       Nachricht (Absender-PID wird automatisch gesetzt)
     */
    public void sendMessage(String queueName, Message msg)
            throws InterruptedException {
        MessageQueue q = ownedQueues.get(queueName);
        if (q == null) {
            throw new IllegalArgumentException(
                "PCB " + pid + ": Queue '" + queueName + "' nicht gefunden");
        }
        System.out.printf("[PCB %d (%s)] sendMessage -> %s: %s%n",
            pid, name, queueName, msg);
        q.send(new Message(msg.getType(), msg.getData(), pid));
    }

    /**
     * Empfängt eine Nachricht aus einer benannten Queue (blockierend).
     */
    public Message receiveMessage(String queueName) throws InterruptedException {
        MessageQueue q = ownedQueues.get(queueName);
        if (q == null) {
            throw new IllegalArgumentException(
                "PCB " + pid + ": Queue '" + queueName + "' nicht gefunden");
        }
        Message msg = q.receive();
        if (msg != null) {
            System.out.printf("[PCB %d (%s)] receiveMessage <- %s: %s%n",
                pid, name, queueName, msg);
        }
        return msg;
    }

    /**
     * Schreibt in einen benannten Pipe.
     */
    public void writePipe(String pipeName, String data) throws InterruptedException {
        Pipe pipe = ownedPipes.get(pipeName);
        if (pipe == null) {
            throw new IllegalArgumentException(
                "PCB " + pid + ": Pipe '" + pipeName + "' nicht gefunden");
        }
        System.out.printf("[PCB %d (%s)] writePipe -> %s: \"%s\"%n",
            pid, name, pipeName, data);
        pipe.write(data);
    }

    /**
     * Liest aus einem benannten Pipe.
     */
    public String readPipe(String pipeName, int maxBytes) throws InterruptedException {
        Pipe pipe = ownedPipes.get(pipeName);
        if (pipe == null) {
            throw new IllegalArgumentException(
                "PCB " + pid + ": Pipe '" + pipeName + "' nicht gefunden");
        }
        String data = pipe.readString(maxBytes);
        System.out.printf("[PCB %d (%s)] readPipe <- %s: \"%s\"%n",
            pid, name, pipeName, data);
        return data;
    }

    /**
     * Registriert einen IPC-Kanal bei diesem Prozess.
     * Beide Prozesse müssen denselben Kanal registrieren um kommunizieren zu können.
     */
    public void attachPipe(String name, Pipe pipe) {
        ownedPipes.put(name, pipe);
    }

    public void attachQueue(String name, MessageQueue queue) {
        ownedQueues.put(name, queue);
    }

    public void attachSharedMemory(String name, SharedMemory shm) {
        sharedSegments.put(name, shm);
    }

    @Override
    public String toString() {
        return String.format("PCB[pid=%d, name='%s', state=%s, pipes=%d, queues=%d, shm=%d]",
            pid, name, state,
            ownedPipes.size(), ownedQueues.size(), sharedSegments.size());
    }
}
