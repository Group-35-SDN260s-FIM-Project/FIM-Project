package my.fim_security_suite.controller;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 *
 * @author wahin
 */
public class FIMController {
    private final FileScanner scanner;
    private final BaselineManager baselineManager;
    private final IntegrityChecker checker;
    private final ActivityLogger logger;
    private final Path logFile;
    private final Path selectedFolder;
    
    public FIMController(){
        this(Path.of("fim_baseline.ser"), Path.of("fim_activity.log"));
    }
    public FIMController (Path baselineFile, Path logFile){
        this.scanner = new FileScanner();
        this.baselineManager = new BaselineManager(scanner, baselineFile);
        this.checker = new IntegrityChecker();
        this.logger = new ActivityLogger(logFile);
        this.selectedFolder = null;
    }
    
    
    
    public void selectFolder(Path folder){
        if(folder == null){
            throw new IllegalArgumentException("No folder given.");
        }
        Path clean = folder.toAbsolutePath().normalize();
        if(!Files.exists(clean)){
            throw new IllegalArgumentException("Folder doesn't exixt: " + clean);
        }
        if(!Files.isDirectory(clean)){
            throw new IllegalArgumentException("That is a file not a folder: " + clean);
        }
        if(isInside(clean, logFile) || isInsisde(clean, baselineManager.getStoreFile())){
            throw new IllegalArgumentException("Folder contains the FIM log or baseline file. Choose a different folder");
        }
        
        this.selectedFolder = clean;
        logger.log(new LogEntry("SELECT","Selected folder" + clean));
    }
    
    public BaseLine createBaseline(){
        requireFolder();
        Baseline baseline = baselineManager.create(selectedFolder);
        baselineManager.save(baseline);
        logger.log(new LogEntry("BASELINE", "Baseline created for " + selectedFolder + "(" + baseline.size() + " files)"));
        return baseline;
    }
    
    public ScanResults runCheck(){
        requireFolder();
        if(!hasBaseline()){
            throw new IllegalStateException("No baseline yet. Create a baseline first.");
        }
        
        Baseline old = baselineManager.load();
        
        Path baselineFolder = Path.of(old.getFolder()).toAbsolutePath().normalize();
        
        if(!baselineFolder.equals(selectedFolder)){
            throw new IllegalStateException("Baseline belongs to " + baselineFolder + ", not " + selectedFolder + ". Created a new baseline first"); 
        }
        
        Map<String, FileRecord> now = scan.scan(selectedFolder);
        ScanResult result = checker.compare(old, now);
        
        String summary = result.isClean()
                ? "Check clean - no changes"
                : "Check found " + result.countOf(FileStatus.MODIFIED) + " modified "
                  + result.countOf(FileStatus.NEW) + " new, "
                  + result.countOf(FileStatus.MISSING) + " missing";
        
        logger.log(new LogEntry("CHECK", summary));
        
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
    
    public List<String> getSkippedFiles(){
        return scanner.getSkipped();
    }
    
    private void requireFolder(){
        if(selectedFolder == null){
            throw new IllegalStateException("Select a Folder first");
        }
    }
    
    private static boolean isInside(Path folder, Path file){
       Path f = folder.toAbsolutePath().normalize();
       Path x = file.toAbsolutePath().normalize();
      return x.startsWith(f);
    }
}
