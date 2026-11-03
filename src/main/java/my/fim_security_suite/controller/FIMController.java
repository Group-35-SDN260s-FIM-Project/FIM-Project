package my.fim_security_suite.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import my.fim_security_suite.core.ActivityLogger;
import my.fim_security_suite.core.BaselineManager;
import my.fim_security_suite.core.FileScanner;
import my.fim_security_suite.core.IntegrityChecker;
import my.fim_security_suite.model.Baseline;
import my.fim_security_suite.model.FileChange;
import my.fim_security_suite.model.FileRecord;
import my.fim_security_suite.model.FileStatus;
import my.fim_security_suite.model.LogEntry;
import my.fim_security_suite.model.ScanResult;

/**
 * Sits between the user interface and the core classes.
 * The console menu and the GUI only talk to this class, so changing
 * the interface never touches the core code.
 */
public class FIMController {
    private final FileScanner scanner;
    private final BaselineManager baselineManager;
    private final IntegrityChecker checker;
    private final ActivityLogger logger;
    private final Path logFile;
    private Path selectedFolder;   // not final because it changes when the user picks a folder

    /** Uses the default baseline and log files in the folder the program is run from. */
    public FIMController(){
        this(Path.of("fim_baseline.ser"), Path.of("fim_activity.log"));
    }

    public FIMController (Path baselineFile, Path logFile){
        this.scanner = new FileScanner();
        this.baselineManager = new BaselineManager(scanner, baselineFile);
        this.checker = new IntegrityChecker();
        this.logFile = logFile;
        this.logger = new ActivityLogger(logFile);
        this.selectedFolder = null;
    }

    /**
     * Sets the folder to monitor.
     * A folder that contains our own baseline or log is refused, if not
     * every check would say those two files changed.
     */
    public void selectFolder(Path folder){
        if(folder == null){
            throw new IllegalArgumentException("No folder given.");
        }
        Path clean = folder.toAbsolutePath().normalize();
        if(!Files.exists(clean)){
            throw new IllegalArgumentException("Folder doesn't exist: " + clean);
        }
        if(!Files.isDirectory(clean)){
            throw new IllegalArgumentException("That is a file not a folder: " + clean);
        }
        if(isInside(clean, logFile) || isInside(clean, baselineManager.getStoreFile())){
            throw new IllegalArgumentException("Folder contains the FIM log or baseline file. Choose a different folder");
        }

        this.selectedFolder = clean;
        logger.log(new LogEntry("SELECT", "Selected folder " + clean));
    }

    /** Scans the selected folder and saves the result as the new baseline. */
    public Baseline createBaseline(){
        requireFolder();
        Baseline baseline = baselineManager.create(selectedFolder);
        baselineManager.save(baseline);
        logger.log(new LogEntry("BASELINE", "Baseline created for " + selectedFolder + " (" + baseline.size() + " files)"));
        return baseline;
    }

    /** Scans the folder again, compares it to the baseline and logs what changed. */
    public ScanResult runCheck(){
        requireFolder();
        if(!hasBaseline()){
            throw new IllegalStateException("No baseline yet. Create a baseline first.");
        }

        Baseline old = baselineManager.load();

        Path baselineFolder = Path.of(old.getFolder()).toAbsolutePath().normalize();

        if(!baselineFolder.equals(selectedFolder)){
            throw new IllegalStateException("Baseline belongs to " + baselineFolder + ", not " + selectedFolder + ". Create a new baseline first");
        }

        Map<String, FileRecord> now = scanner.scan(selectedFolder);
        ScanResult result = checker.compare(old, now);

        String summary = "unchanged=" + result.countOf(FileStatus.UNCHANGED)
                + " modified=" + result.countOf(FileStatus.MODIFIED)
                + " new=" + result.countOf(FileStatus.NEW)
                + " missing=" + result.countOf(FileStatus.MISSING);

        logger.log(new LogEntry("CHECK_RUN", summary));

        // one log line for every changed file so the log shows what exactly happened
        for (FileChange change : result.getChanges()) {
            if (change.getStatus() != FileStatus.UNCHANGED) {
                logger.log(new LogEntry(change.getStatus().name(), change.getPath()));
            }
        }

        return result;
    }

    public List<LogEntry> getRecentLog(int n){
        return logger.recent(n);
    }

    public Path getSelectedFolder(){
        return selectedFolder;
    }

    public boolean hasBaseline(){
        return baselineManager.exists();
    }

    /** Files the last scan could not read (locked, no permission, links...). */
    public List<String> getSkippedFiles(){
        return scanner.getSkipped();
    }

    private void requireFolder(){
        if(selectedFolder == null){
            throw new IllegalStateException("Select a folder first");
        }
    }

    private static boolean isInside(Path folder, Path file){
       Path f = folder.toAbsolutePath().normalize();
       Path x = file.toAbsolutePath().normalize();
       return x.startsWith(f);
    }
}
