class KernelCore {
    private ProcessManager processManager;
    private MemoryManager memoryManager;
    private Shell shell;
    private MemoryLogger memoryLogger;
    private KernelDebugger debugger;

    public KernelCore() {
        this.memoryManager = new MemoryManager(1024); // 1KB simulated RAM
        this.processManager = new ProcessManager(memoryManager);
        this.shell = new Shell(processManager);
        this.memoryLogger = new MemoryLogger(memoryManager);
        this.debugger = new KernelDebugger(processManager, memoryManager);

        memoryLogger.startLogging();
    }

    public void start() {
        debugger.runDiagnostics(); // Run before shell
        shell.run();
        debugger.runDiagnostics(); // Run after shell
        memoryLogger.stopLogging();
    }
}