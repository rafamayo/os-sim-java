package woche09;

/**
 * Wird ausgelöst wenn eine virtuelle Adresse auf eine Seite zeigt,
 * die nicht im physischen Speicher vorhanden ist (valid == false).
 */
public class PageFaultException extends RuntimeException {

    private final int pid;
    private final int vpn;
    private final int virtualAddress;

    public PageFaultException(int pid, int vpn, int virtualAddress) {
        super(String.format("Page Fault: PID=%d, VPN=%d, VA=0x%X", pid, vpn, virtualAddress));
        this.pid            = pid;
        this.vpn            = vpn;
        this.virtualAddress = virtualAddress;
    }

    public int getPid()            { return pid; }
    public int getVpn()            { return vpn; }
    public int getVirtualAddress() { return virtualAddress; }
}
