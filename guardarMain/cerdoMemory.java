import java.util.*;
import java.util.concurrent.*;

// Simulated OS Kernel
public class Kernel {
    public static void main(String[] args) {
        System.out.println("Booting up SimOS v1.0...");
        KernelCore kernel = new KernelCore();
        kernel.start();
    }
}

class KernelCore {
    private ProcessManager processManager;
    private MemoryManager memoryManager;
    private Shell shell;
    private MemoryLogger memoryLogger;

    public KernelCore() {
        this.memoryManager = new MemoryManager(1024); // 1KB of simulated RAM
        this.processManager = new ProcessManager(memoryManager);
        this.shell = new Shell(processManager);
        this.memoryLogger = new MemoryLogger(memoryManager);
        memoryLogger.startLogging();
    }

    public void start() {
        shell.run();
        memoryLogger.stopLogging();
    }
}

// Shell for user input
class Shell {
    private Scanner scanner;
    private ProcessManager processManager;

    public Shell(ProcessManager pm) {
        this.scanner = new Scanner(System.in);
        this.processManager = pm;
    }

    public void run() {
        System.out.println("Welcome to SimOS shell. Type 'help' for commands.");
        while (true) {
            System.out.print("simos> ");
            String input = scanner.nextLine();
            if (input.trim().equals("exit")) {
                System.out.println("Shutting down SimOS...");
                break;
            }
            handleCommand(input);
        }
    }

    private void handleCommand(String input) {
        String[] tokens = input.split(" ");
        String cmd = tokens[0];

        switch (cmd) {
            case "help":
                System.out.println("Available commands:");
                System.out.println("  ps             - list processes");
                System.out.println("  exec <name>    - run process");
                System.out.println("  kill <pid>     - kill process");
                System.out.println("  mem            - show memory usage");
                System.out.println("  exit           - shutdown OS");
                break;
            case "ps":
                processManager.listProcesses();
                break;
            case "exec":
                if (tokens.length < 2) {
                    System.out.println("Usage: exec <name>");
                } else {
                    processManager.createProcess(tokens[1]);
                }
                break;
            case "kill":
                if (tokens.length < 2) {
                    System.out.println("Usage: kill <pid>");
                } else {
                    try {
                        int pid = Integer.parseInt(tokens[1]);
                        processManager.killProcess(pid);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid PID");
                    }
                }
                break;
            case "mem":
                processManager.showMemory();
                break;
            default:
                System.out.println("Unknown command. Type 'help' for help.");
        }
    }
}

// Process class
class Process {
    private static int nextPid = 1;
    private int pid;
    private String name;
    private int memory;
    private Thread thread;
    private boolean running = true;

    public Process(String name, int memory, Runnable task) {
        this.pid = nextPid++;
        this.name = name;
        this.memory = memory;
        this.thread = new Thread(() -> {
            task.run();
            running = false;
        });
    }

    public void start() {
        thread.start();
    }

    public void kill() {
        thread.interrupt();
        running = false;
    }

    public int getPid() {
        return pid;
    }

    public String getName() {
        return name;
    }

    public int getMemory() {
        return memory;
    }

    public boolean isRunning() {
        return running && thread.isAlive();
    }
}

// Process Manager
class ProcessManager {
    private List<Process> processes = new ArrayList<>();
    private MemoryManager memoryManager;

    public ProcessManager(MemoryManager mm) {
        this.memoryManager = mm;
    }

    public void createProcess(String name) {
        Random rand = new Random();
        int mem = 50 + rand.nextInt(200); // allocate 50-250 bytes
        if (memoryManager.allocate(mem)) {
            Runnable task = () -> {
                try {
                    for (int i = 0; i < 5; i++) {
                        System.out.println(name + " [PID] Running step " + (i+1));
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                    System.out.println(name + " [PID] Terminated.");
                }
            };
            Process proc = new Process(name, mem, task);
            processes.add(proc);
            proc.start();
            System.out.println("Process " + name + " started with PID " + proc.getPid() + " and " + mem + " bytes.");
        } else {
            System.out.println("Not enough memory to start process " + name);
        }
    }

    public void listProcesses() {
        System.out.println("PID\tName\t\tMemory\tStatus");
        for (Process p : processes) {
            System.out.printf("%d\t%s\t\t%d\t%s\n", p.getPid(), p.getName(), p.getMemory(), p.isRunning() ? "Running" : "Stopped");
        }
    }

    public void killProcess(int pid) {
        for (Process p : processes) {
            if (p.getPid() == pid) {
                p.kill();
                memoryManager.free(p.getMemory());
                System.out.println("Process " + pid + " killed.");
                return;
            }
        }
        System.out.println("No process found with PID " + pid);
    }

    public void showMemory() {
        memoryManager.printMemoryStatus();
    }
}

// Memory Manager
class MemoryManager {
    private int totalMemory;
    private int usedMemory = 0;

    public MemoryManager(int size) {
        this.totalMemory = size;
    }

    public synchronized boolean allocate(int size) {
        if (usedMemory + size > totalMemory) return false;
        usedMemory += size;
        return true;
    }

    public synchronized void free(int size) {
        usedMemory -= size;
        if (usedMemory < 0) usedMemory = 0;
    }

    public synchronized void printMemoryStatus() {
        System.out.println("Memory Usage: " + usedMemory + "/" + totalMemory + " bytes");
    }

    public synchronized int getUsedMemory() {
        return usedMemory;
    }

    public synchronized int getTotalMemory() {
        return totalMemory;
    }
}

// Memory Logger that logs memory usage over time
class MemoryLogger {
    private final MemoryManager memoryManager;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> task;

    public MemoryLogger(MemoryManager memoryManager) {
        this.memoryManager = memoryManager;
    }

    public void startLogging() {
        task = scheduler.scheduleAtFixedRate(() -> {
            int used = memoryManager.getUsedMemory();
            int total = memoryManager.getTotalMemory();
            System.out.println("[MEMLOG] Used: " + used + "/" + total + " bytes");
        }, 0, 3, TimeUnit.SECONDS);
    }

    public void stopLogging() {
        if (task != null) task.cancel(true);
        scheduler.shutdown();
    }
}