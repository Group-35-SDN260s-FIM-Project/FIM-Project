package my.fim_security_suite.model;

 import java.time.LocalDateTime;
 import java.util.ArrayList;
 import java.util.List;

/**
 *
 * @author Rebecca Mandlazi
 */
public class ScanResult {
    private String scannedFolder;
    private LocalDateTime timestamp;
    private List<FileChange> changes;
    
    
    public ScanResult(String scannedFolder){
        this.scannedFolder = scannedFolder;
        this.timestamp = LocalDateTime.now();
        this.changes = new ArrayList<>();
         }
  public List<FileChange> 
        getChanges(){
            return changes;
        }
        public void addChange(FileChange 
                change) {
            this.changes.add(change);
        }
        }



