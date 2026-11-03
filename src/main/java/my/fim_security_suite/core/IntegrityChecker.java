package my.fim_security_suite.core;

import java.util.Map;
import my.fim_security_suite.model.Baseline;
import my.fim_security_suite.model.FileChange;
import my.fim_security_suite.model.FileRecord;
import my.fim_security_suite.model.FileStatus;
import my.fim_security_suite.model.ScanResult;

/**
 * Compares a new scan of the folder with the saved baseline and puts
 * each file in one of the four FileStatus groups.
 *
 * It goes through the maps twice (baseline first, then the new scan),
 * so it is O(n + m) and doesn't compare every file with every other file.
 */
public class IntegrityChecker {

    /**
     * @param old the saved baseline
     * @param now the new scan, path -> record (from FileScanner.scan)
     * @return a ScanResult with a FileChange for every file, sorted by path
     */
    public ScanResult compare(Baseline old, Map<String, FileRecord> now){
        if(old == null || now == null){
            throw new IllegalArgumentException("Baseline and current scan must not be null");
        }
        ScanResult result = new ScanResult();
        Map<String, FileRecord> before = old.getRecords();

        // pass 1: look at everything that was in the baseline
        for (Map.Entry<String, FileRecord> entry : before.entrySet()) {
            String path = entry.getKey();
            String oldHash = entry.getValue().getHash();
            FileRecord current = now.get(path);

            if (current == null) {
                // it was in the baseline but is not in the folder any more
                result.add(new FileChange(path, FileStatus.MISSING, oldHash, null));
            } else if (oldHash.equals(current.getHash())) {
                result.add(new FileChange(path, FileStatus.UNCHANGED, oldHash, current.getHash()));
            } else {
                // same file name but the contents are different
                result.add(new FileChange(path, FileStatus.MODIFIED, oldHash, current.getHash()));
            }
        }

        // pass 2: whatever is in the folder but not in the baseline is new
        for (Map.Entry<String, FileRecord> entry : now.entrySet()) {
            if (!before.containsKey(entry.getKey())) {
                result.add(new FileChange(entry.getKey(), FileStatus.NEW, null, entry.getValue().getHash()));
            }
        }

        result.sort();
        return result;
    }
}
