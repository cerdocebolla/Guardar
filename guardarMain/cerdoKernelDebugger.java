import java.util.List;

public class KernelDebugger {
    private final ProcessManager processManager;
    private final MemoryManager memoryManager;

    public KernelDebugger(ProcessManager pm, MemoryManager mm) {
        this.processManager = pm;
        this.memoryManager = mm;
    }

    // Perform a debugging check
    public void runDiagnostics() {
        System.out.println("🔍 Running Kernel Diagnostics...");

        List<Process> processes = processManager.getAllProcesses();

        // Check for hung processes
        for (Process p : processes) {
            if (!p.isRunning()) {
                System.out.println("⚠️ Process PID " + p.getPid() + " is not running. Marked for cleanup.");
            }
        }

        // Check memory usage
        int used = memoryManager.getUsedMemory();
        int total = memoryManager.getTotalMemory();
        if (used > total * 0.9) {
            System.out.println("⚠️ Memory usage critical: " + used + "/" + total + " bytes.");
        } else {
            System.out.println("✅ Memory usage OK: " + used + "/" + total + " bytes.");
        }

        System.out.println("✅ Diagnostics complete.");
    }
}