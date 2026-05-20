# Linux-Praxisblatt — Betriebssystem-Konzepte live beobachten

**Betriebssysteme — Abschlussübung**
**Hochschule Kempten, Fakultät Informatik**
**Prof. Dr. Rafael Mayoral Malmström — Sommersemester 2026**

---

> **Hinweis macOS**: Alle Aufgaben sind auf Ubuntu/Linux konzipiert.
> Wo macOS abweicht, ist ein Hinweis `🍎 macOS:` angegeben.
> `/proc` existiert auf macOS nicht — dort wird `sysctl` oder
> `Activity Monitor` verwendet. Kernkonzepte sind dieselben,
> nur die Werkzeuge unterscheiden sich.

---

## Lernziele

Nach dieser Übung können Sie:

- **Prozesse und Scheduling**: laufende Prozesse inspizieren,
  PCB-Felder in `/proc` wiedererkennen, Scheduler-Informationen lesen
- **Speicherverwaltung**: virtuellen Adressraum eines Prozesses beobachten,
  Paging-Strukturen und Speicheraufteilung verstehen
- **Dateisystem**: Inode-Metadaten auslesen, Hard Links und Symbolic Links
  anlegen und ihr Verhalten beobachten, Disk-Layout nachvollziehen
- **Journaling**: Dateisystem-Typ und Journal-Parameter lesen
- **IPC**: aktive Pipes, Sockets und IPC-Ressourcen anzeigen

---

## Block A — Prozesse & Prozessverwaltung

### A1 — Prozessliste und Prozessbaum

```bash
# Alle laufenden Prozesse anzeigen
ps aux

# Prozessbaum: wer hat wen gestartet?
ps axjf

# Kompakter Baum (falls pstree installiert)
pstree -p
```

🍎 macOS: `ps aux` und `ps axjf` funktionieren identisch.
`pstree` ist nicht vorinstalliert — Alternative: `Activity Monitor → Ansicht → Alle Prozesse hierarchisch`

**Beobachten und erklären:**

a) Starten Sie ein neues Terminal und führen Sie `ps aux` aus.
   Welche PID hat Ihre Shell (`bash` oder `zsh`)? Welche PID hat `ps` selbst?
   Warum ist die PID von `ps` höher als die der Shell?

b) Führen Sie `ps axjf` aus und suchen Sie Ihren Terminal-Prozess.
   Welcher Prozess ist sein Elternprozess (PPID)?
   Was sagt das über den Systemstart aus?

c) Finden Sie in der `ps aux`-Ausgabe einen Prozess im Zustand `S` und einen
   im Zustand `R`. Was bedeuten diese Zustände? *(Spalte `STAT`)*

d) Der Init-Prozess hat PID 1. Was ist seine Aufgabe, und warum beendet er sich nie?

---

### A2 — Der Process Control Block in `/proc`

```bash
# PID der eigenen Shell herausfinden
echo $$

# PCB-Felder des eigenen Prozesses
cat /proc/$$/status

# Offene File Descriptors
ls -la /proc/$$/fd

# Umgebungsvariablen (getrennt durch \0)
cat /proc/$$/environ | tr '\0' '\n' | head -10

# Befehlszeile mit der der Prozess gestartet wurde
cat /proc/$$/cmdline | tr '\0' ' '
echo
```

🍎 macOS: `/proc` existiert nicht. Alternative:
```bash
# Prozessstatus
ps -p $$ -o pid,ppid,stat,rss,vsz,comm

# Offene File Descriptors
lsof -p $$
```

**Beobachten und erklären:**

a) Öffnen Sie `cat /proc/$$/status`. Identifizieren Sie folgende Felder
   und ordnen Sie sie den Inode-Feldern aus dem Kurs zu:

   | `/proc`-Feld | Bedeutung | Kurs-Konzept |
   |---|---|---|
   | `Pid` | | |
   | `PPid` | | |
   | `State` | | |
   | `Threads` | | |
   | `VmRSS` | | |
   | `VmSize` | | |

b) Führen Sie `ls -la /proc/$$/fd` aus. Sie sehen fd 0, 1, 2 — was sind das?
   Worauf zeigen sie? *(Hinweis: schauen Sie auf das Pfeilziel des Symlinks)*

c) Öffnen Sie eine Datei in einem anderen Terminal:
   ```bash
   tail -f /etc/hostname &
   ls -la /proc/$!/fd
   ```
   Welchen File Descriptor hat die geöffnete Datei? Was sehen Sie?

---

### A3 — Scheduling-Informationen

```bash
# Scheduling-Klasse und Priorität der Shell
chrt -p $$

# Prozesspriorität (nice-Wert)
ps -o pid,ni,pri,comm -p $$

# Scheduler-Details aus /proc
cat /proc/$$/sched | head -20

# CPU-Affinität: auf welchen Kernen darf der Prozess laufen?
taskset -p $$
```

🍎 macOS: `chrt` und `taskset` nicht verfügbar.
```bash
# Priorität auf macOS
ps -o pid,nice,pri,comm -p $$

# Scheduling-Statistiken (macOS)
sudo sysctl kern.sched
```

**Beobachten und erklären:**

a) Was gibt `chrt -p $$` aus? Welche Scheduling-Policy hat Ihre Shell?
   Was bedeutet das für die Prioritätsbehandlung?

b) Starten Sie einen Prozess mit reduzierter Priorität:
   ```bash
   nice -n 10 sleep 100 &
   ps -o pid,ni,comm -p $!
   ```
   Was ist der nice-Wert? Wie wirkt sich ein höherer nice-Wert auf
   die Scheduling-Priorität aus?

c) Schauen Sie in `cat /proc/$$/sched`: Finden Sie das Feld
   `nr_switches` — was zählt dieser Wert?
   Führen Sie mehrere Befehle aus und prüfen Sie ob der Wert steigt.

---

## Block B — Speicherverwaltung

### B1 — Systemweiter Speicherüberblick

```bash
# RAM-Auslastung
free -h

# Detaillierte Speicherinformationen
cat /proc/meminfo

# Swap-Nutzung
swapon --show
```

🍎 macOS:
```bash
# RAM auf macOS
vm_stat
sysctl hw.memsize
top -l 1 | grep PhysMem
```

**Beobachten und erklären:**

a) In `free -h`: Was ist der Unterschied zwischen `used` und `available`?
   Warum ist `available` oft größer als `free`?

b) In `cat /proc/meminfo`: Finden Sie `Cached` und `Buffers`.
   Was cacht der Kernel hier? Welches Kurskonzept steckt dahinter?

c) Was ist `SwapTotal` und `SwapFree`? In welcher Situation aktiviert
   der Kernel Swap? Welcher Algorithmus (aus Woche 11) entscheidet,
   welche Seiten ausgelagert werden?

---

### B2 — Virtueller Adressraum eines Prozesses

```bash
# Adressraum der eigenen Shell
cat /proc/$$/maps

# Kompaktere Darstellung mit pmap
pmap $$

# Gesamtgröße aller Segmente
pmap -x $$ | tail -3
```

🍎 macOS:
```bash
# Virtueller Adressraum auf macOS
vmmap $$
# oder
leaks $$
```

**Beobachten und erklären:**

a) Öffnen Sie `cat /proc/$$/maps`. Identifizieren Sie die folgenden Segmente
   und ordnen Sie sie dem Adressraummodell aus der Vorlesung zu:

   | Segment | Adressbereich | Berechtigungen | Kurs-Begriff |
   |---|---|---|---|
   | Code (`.text`) | | `r-xp` | |
   | Daten (`.data`) | | `rw-p` | |
   | Heap | `[heap]` | | |
   | Stack | `[stack]` | | |
   | vDSO | `[vdso]` | | |

b) Die Berechtigungsspalte zeigt z.B. `r-xp` oder `rw-p`.
   Was bedeutet das `p` am Ende? *(Hinweis: p = private, s = shared)*

c) Starten Sie ein einfaches Java-Programm (`java -version &`) und
   vergleichen Sie seinen Adressraum mit dem der Shell:
   ```bash
   pmap $(pgrep java) | tail -5
   ```
   Warum hat ein Java-Prozess typischerweise viel mehr Speicher-Regionen?

d) Öffnen Sie `pmap -x $$`. Die Spalte `RSS` zeigt den tatsächlich
   im RAM belegten Speicher, `Size` den reservierten.
   Warum ist `RSS` oft viel kleiner als `Size`? Welches Konzept steckt dahinter?

---

### B3 — Page-Faults beobachten

```bash
# Page-Fault-Statistiken des eigenen Prozesses
cat /proc/$$/status | grep -i fault

# Page-Faults live beobachten (Ctrl+C zum Beenden)
while true; do
    cat /proc/$$/status | grep -i fault
    sleep 2
done
```

🍎 macOS:
```bash
# Page Faults auf macOS
vm_stat 2  # alle 2 Sekunden
```

**Beobachten und erklären:**

a) Was ist der Unterschied zwischen `voluntary_ctxt_switches` und
   `nonvoluntary_ctxt_switches` in `/proc/$$/status`?
   Welcher entspricht einer blockierenden Operation (z.B. `read()`),
   welcher dem Preemption durch den Scheduler?

b) Führen Sie einen speicherintensiven Befehl aus:
   ```bash
   dd if=/dev/zero of=/dev/null bs=1M count=1000 &
   PID=$!
   cat /proc/$PID/status | grep -i fault
   sleep 2
   cat /proc/$PID/status | grep -i fault
   ```
   Steigt die Anzahl der Page Faults? Welche Art (minor/major)?

---

## Block C — Dateisystem & Inodes

### C1 — Inode-Metadaten inspizieren

```bash
# Inode-Nummer einer Datei anzeigen
ls -i /etc/hostname

# Alle Inode-Felder einer Datei
stat /etc/hostname

# Inode-Nummer im ls-Format (für ein Verzeichnis)
ls -lai /etc/ | head -10
```

🍎 macOS: `stat` hat ein anderes Format:
```bash
stat -x /etc/hostname   # macOS: -x für lesbares Format
stat -f "%i" /etc/hostname  # nur Inode-Nummer
```

**Beobachten und erklären:**

a) Führen Sie `stat /etc/hostname` aus. Identifizieren Sie die Felder:

   | `stat`-Feld | Kurs-Begriff | Bedeutung |
   |---|---|---|
   | `Inode` | | |
   | `Links` | | |
   | `Access` | `atime` | |
   | `Modify` | `mtime` | |
   | `Change` | `ctime` | |
   | `Blocks` | | |

b) Was ist der Unterschied zwischen `mtime` (Modify) und `ctime` (Change)?
   Können Sie `ctime` ändern ohne `mtime` zu ändern?

c) Vergleichen Sie die Inode-Nummer von `/etc/hostname` mit
   dem Inode des Verzeichnisses `/etc`:
   ```bash
   ls -id /etc
   ls -i /etc/hostname
   ```
   Sind es verschiedene Inodes? Warum?

---

### C2 — Hard Links und Symbolic Links

```bash
# Eine Testdatei erstellen
echo "Hallo Welt" > /tmp/original.txt
stat /tmp/original.txt | grep -E "Inode|Links"

# Hard Link anlegen
ln /tmp/original.txt /tmp/hardlink.txt
stat /tmp/original.txt | grep -E "Inode|Links"
stat /tmp/hardlink.txt | grep -E "Inode|Links"

# Symbolic Link anlegen
ln -s /tmp/original.txt /tmp/symlink.txt
stat /tmp/symlink.txt | grep -E "Inode|Links|File"
ls -lai /tmp/*.txt
```

🍎 macOS: Befehle identisch.

**Beobachten und erklären:**

a) Vergleichen Sie die Inode-Nummer von `original.txt` und `hardlink.txt`.
   Was stellen Sie fest? Was sagt das über ihre Beziehung aus?

b) Hat sich `Links` bei `original.txt` nach dem Hard Link verändert?
   Was bedeutet dieser Wert?

c) Löschen Sie jetzt die Originaldatei:
   ```bash
   rm /tmp/original.txt
   cat /tmp/hardlink.txt   # Funktioniert das noch?
   cat /tmp/symlink.txt    # Und das?
   ```
   Erklären Sie den Unterschied. Wann werden die Datenblöcke freigegeben?

d) Was zeigt `ls -lai` für den Symlink? Welche Inode-Nummer hat er?
   Was enthält sein "Dateiinhalt"?

---

### C3 — Verzeichnisse sind Dateien

```bash
# Inode und Größe eines Verzeichnisses
stat /etc

# Anzahl Links eines Verzeichnisses
ls -ld /home

# Links eines leeren vs. befüllten Verzeichnisses
mkdir /tmp/testdir
stat /tmp/testdir | grep Links
mkdir /tmp/testdir/subdir1
stat /tmp/testdir | grep Links
mkdir /tmp/testdir/subdir2
stat /tmp/testdir | grep Links
```

🍎 macOS: identisch.

**Beobachten und erklären:**

a) Warum hat ein neu erstelltes Verzeichnis `Links: 2`?
   *(Hinweis: Denken Sie an `.` und `..`)*

b) Warum erhöht sich `Links` um 1 für jedes neu erstellte Unterverzeichnis?
   Welcher Eintrag im Unterverzeichnis zeigt auf das Elternverzeichnis?

c) Warum sind Hard Links auf Verzeichnisse nicht erlaubt?
   *(Denken Sie an den Algorithmus für Pfadauflösung aus Woche 13)*

---

### C4 — Disk-Layout und freier Speicher

```bash
# Dateisystem-Überblick
df -h

# Mit Inode-Nutzung
df -ih

# Dateisystem-Typ anzeigen
df -T

# Superblock-Informationen (ext4)
sudo tune2fs -l /dev/sda1 2>/dev/null | head -30
# Falls sda1 nicht existiert:
lsblk
sudo tune2fs -l $(df / | tail -1 | cut -d' ' -f1) | head -30
```

🍎 macOS: `tune2fs` nicht verfügbar (APFS statt ext4).
```bash
# Disk-Überblick auf macOS
df -h
diskutil info /
```

**Beobachten und erklären:**

a) In `df -ih`: Was bedeutet `IUse%`? Was passiert wenn die Inode-Nutzung
   100% erreicht, obwohl noch Speicherplatz frei ist?

b) Suchen Sie in `tune2fs -l` folgende Felder und ordnen Sie sie dem
   Disk-Layout aus der Vorlesung zu:

   | `tune2fs`-Feld | Kurs-Begriff |
   |---|---|
   | `Inode count` | |
   | `Block count` | |
   | `Block size` | |
   | `Inode size` | |
   | `Journal inode` | |

c) Steht bei `Filesystem features` der Eintrag `has_journal`?
   Was bedeutet das, und welchem Journaling-Modus aus Woche 14 entspricht das?

---

### C5 — Journaling beobachten

```bash
# Journal-Modus des Root-Dateisystems
sudo tune2fs -l $(df / | tail -1 | cut -d' ' -f1) | grep -i journal

# Ext4-Statistiken aus /proc
cat /proc/fs/ext4/*/stats 2>/dev/null | head -20

# Alternativ: dmesg beim Mounten
dmesg | grep -i "ext4\|journal" | head -10
```

🍎 macOS: APFS verwendet Copy-on-Write statt Journaling:
```bash
# Dateisystem-Typ auf macOS
diskutil info / | grep "File System"
# Zeigt: APFS — kein Journal, stattdessen CoW
```

**Beobachten und erklären:**

a) Welchen Journaling-Modus verwendet Ihr Ubuntu-System?
   (`data=ordered`, `data=journal` oder `data=writeback`)?
   Woran erkennen Sie das?

b) Was ist der Vorteil von `data=ordered` gegenüber `data=writeback`?
   Was ist der Vorteil gegenüber `data=journal`?

c) macOS-Nutzer: APFS verwendet Copy-on-Write statt Journaling.
   Was ist der prinzipielle Unterschied? Welche Vorteile hat CoW
   bezüglich Snapshots?

---

## Block D — Interprozesskommunikation (IPC)

### D1 — Pipes in der Shell

```bash
# Einfache Pipe: stdout von ls → stdin von grep
ls /etc | grep conf

# Pipe-Kette: mehrere Schritte
cat /proc/meminfo | grep -i mem | sort | head -5

# Pipe im Hintergrund beobachten
sleep 100 | sleep 100 &
# Pipe-File-Descriptor finden
ls -la /proc/$!/fd
```

🍎 macOS: Pipes funktionieren identisch.

**Beobachten und erklären:**

a) Führen Sie `ls /etc | grep conf | wc -l` aus.
   Wie viele Prozesse sind an dieser Pipe-Kette beteiligt?
   Laufen sie sequenziell oder parallel? *(Hinweis: `ps aux | grep sleep`)*

b) Schauen Sie in `/proc/$!/fd` nach dem `sleep 100 | sleep 100`-Befehl.
   Welche File Descriptors sehen Sie? Wohin zeigen fd 0 und fd 1?
   *(Hinweis: fd 1 des linken sleep = fd 0 des rechten sleep)*

c) Was passiert mit dem rechten `sleep` wenn der linke endet?
   (Testen Sie mit `echo "test" | sleep 100` — beendet sich sleep sofort?)

---

### D2 — IPC-Ressourcen: Message Queues und Shared Memory

```bash
# Systemweite IPC-Ressourcen anzeigen
ipcs

# Nur Message Queues
ipcs -q

# Nur Shared Memory Segmente
ipcs -m

# Nur Semaphore
ipcs -s

# Detaillierte Infos
ipcs -a
```

🍎 macOS: `ipcs` funktioniert identisch (System-V IPC auch auf macOS).

**Beobachten und erklären:**

a) Welche IPC-Ressourcen sind auf Ihrem System aktiv?
   Haben Sie Shared Memory Segmente ohne selbst welche angelegt?
   Welche Anwendungen könnten diese erstellt haben?

b) Erstellen Sie selbst ein Shared Memory Segment:
   ```bash
   # Python3 (falls verfügbar)
   python3 -c "
   import sysv_ipc
   shm = sysv_ipc.SharedMemory(1234, sysv_ipc.IPC_CREAT, size=4096)
   print('Shared Memory Key:', shm.key, 'ID:', shm.id)
   input('Drücken Sie Enter zum Beenden...')
   shm.detach()
   shm.remove()
   " &
   # In zweitem Terminal:
   ipcs -m
   ```
   Sehen Sie das neue Segment in `ipcs -m`?

---

### D3 — Offene Dateien und Sockets

```bash
# Offene Dateien aller Prozesse (braucht lsof)
# lsof installieren falls nötig: sudo apt install lsof
lsof -p $$

# Alle offenen Netzwerk-Verbindungen
ss -tuln

# Alternativer Befehl (überall verfügbar)
cat /proc/net/tcp | head -5

# Named Pipes (FIFOs) im System finden
find /tmp -type p 2>/dev/null
find /run -type p 2>/dev/null | head -5
```

🍎 macOS:
```bash
lsof -p $$        # lsof auf macOS vorinstalliert
netstat -tuln     # statt ss
```

**Beobachten und erklären:**

a) In `lsof -p $$`: Welche Typen sehen Sie in der Spalte `TYPE`?
   Was bedeuten `REG`, `DIR`, `CHR`, `unix`?

b) In `ss -tuln`: Was sind die Ports unter `Local Address`?
   Welche Dienste lauschen auf diesen Ports?
   *(Typisch: 22 = SSH, 53 = DNS, 631 = CUPS)*

c) Finden Sie in `/proc/net/tcp` eine aktive Verbindung.
   Die Adressen sind hexadezimal kodiert. Decodieren Sie die lokale Adresse:
   ```bash
   # Beispiel: 0100007F:0035 → 127.0.0.1:53
   # Byte-Reihenfolge: Little-Endian → umkehren
   python3 -c "print('.'.join(str(int('0100007F'[i:i+2],16))
       for i in [6,4,2,0]))"
   ```

---

## Block E — Alles zusammen: ein Prozess von innen

### E1 — Einen einfachen Prozess vollständig inspizieren

Starten Sie einen Hintergrundprozess und untersuchen Sie ihn vollständig:

```bash
# Prozess starten
sleep 999 &
PID=$!
echo "PID: $PID"

# --- Identität ---
echo "=== Identität ==="
cat /proc/$PID/status | grep -E "^Name|^Pid|^PPid|^State"

# --- Speicher ---
echo "=== Speicher ==="
cat /proc/$PID/status | grep -E "^VmSize|^VmRSS|^VmStk"

# --- Adressraum ---
echo "=== Adressraum ==="
cat /proc/$PID/maps

# --- File Descriptors ---
echo "=== File Descriptors ==="
ls -la /proc/$PID/fd

# --- Scheduling ---
echo "=== Scheduling ==="
chrt -p $PID
cat /proc/$PID/status | grep ctxt

# --- Aufräumen ---
kill $PID
```

🍎 macOS:
```bash
sleep 999 &
PID=$!
ps -p $PID -o pid,ppid,stat,rss,vsz,nice,comm
lsof -p $PID
kill $PID
```

**Beobachten und erklären:**

a) Was zeigt `cat /proc/$PID/maps` für `sleep`?
   Wie viele Segmente hat dieser einfache Prozess?
   Finden Sie Code, Stack und Heap.

b) Welche File Descriptors hat `sleep` geöffnet (fd 0, 1, 2)?
   Worauf zeigen sie? Was würde passieren wenn Sie fd 0 schließen?

c) Was zeigt `chrt -p $PID`? Warum läuft `sleep` im SCHED_OTHER-Scheduler
   und nicht in einem Echtzeit-Scheduler?

---

## Denkanstöße und Reflexionsfragen

1. **PCB und `/proc`**: Sie haben den PCB in Java implementiert (Woche 2).
   In `/proc/$PID/status` sehen Sie den echten PCB des Linux-Kernels.
   Welche Felder aus Ihrer Java-Implementierung finden Sie wieder?
   Welche gibt es im echten System zusätzlich?

2. **Inode und Dateiname**: Führen Sie `ls -i /bin/sh` und `ls -i /bin/bash` aus.
   Haben sie dieselbe Inode-Nummer? Was sagt das über die Beziehung
   zwischen diesen beiden "Dateien" aus?

3. **Virtueller Adressraum**: In `/proc/$$/maps` sehen Sie eine Adresse
   wie `7fff...` für den Stack und eine niedrige Adresse wie `0040...`
   für den Code. Warum liegen sie so weit auseinander?
   Was liegt dazwischen, und warum?

4. **Swap und Thrashing**: Mit `free -h` können Sie prüfen ob Ihr System
   Swap verwendet. Wann würde Swap aktiv werden?
   Welcher Algorithmus aus Woche 11 entscheidet, welche Seiten in den Swap wandern?

5. **Pipe und Synchronisation**: Sie haben gesehen dass Pipes blockieren
   wenn leer oder voll. Wer implementiert diese Synchronisation?
   Vergleichen Sie mit Ihrem `BlockingRingBuffer` aus Woche 15.

6. **Hard Link Zähler**: Erstellen Sie ein neues Verzeichnis mit
   `mkdir /tmp/links_test`. `stat /tmp/links_test` zeigt `Links: 2`.
   Erstellen Sie dann 3 Unterverzeichnisse. Warum steigt `Links` auf 5?
   Zeichnen Sie das `..`-Geflecht auf.

---

## Nützliche Befehle — Kurzreferenz

| Befehl | Zweck | Kurs-Bezug |
|--------|-------|-----------|
| `ps aux` | Alle Prozesse | PCB, Wochen 1–4 |
| `ps axjf` | Prozessbaum | fork(), parent/child |
| `cat /proc/PID/status` | PCB-Felder | PCB, Woche 2 |
| `cat /proc/PID/maps` | Adressraum | Paging, Woche 9 |
| `pmap PID` | Adressraum kompakt | Segmente, Woche 9 |
| `free -h` | RAM-Auslastung | Speicherverwaltung, Woche 8 |
| `cat /proc/meminfo` | Kernel-Speicherdetails | Buffer Cache, Woche 11 |
| `stat DATEI` | Inode-Metadaten | Inode, Woche 12 |
| `ls -i DATEI` | Inode-Nummer | Inode, Woche 12 |
| `ln / ln -s` | Hard/Soft Link anlegen | Links, Woche 13 |
| `df -ih` | Inode-Auslastung | Disk-Layout, Woche 12 |
| `tune2fs -l` | Superblock/Journal | Journaling, Woche 14 |
| `ipcs` | IPC-Ressourcen | IPC, Woche 15 |
| `ss -tuln` | Sockets/Ports | IPC, Woche 15 |
| `lsof -p PID` | Offene Dateien | File Descriptors, Woche 12 |
| `chrt -p PID` | Scheduling-Policy | Scheduler, Wochen 3–4 |
| `nice -n N CMD` | Priorität setzen | Scheduling, Woche 3 |

---

## Weiterführende Ressourcen

- **OSTEP** (alle relevanten Kapitel):
  https://pages.cs.wisc.edu/~remzi/OSTEP/
- **Linux `man`-Pages**: `man proc`, `man stat`, `man lsof`, `man ipcs`
- **`/proc`-Dokumentation**:
  https://www.kernel.org/doc/html/latest/filesystems/proc.html
- **Linux `procfs`** — alle `/proc`-Dateien erklärt:
  `man 5 proc`
- **Brendan Gregg — Linux Performance Tools**:
  https://www.brendangregg.com/linuxperf.html
  *(Überblick über alle Beobachtungswerkzeuge)*
