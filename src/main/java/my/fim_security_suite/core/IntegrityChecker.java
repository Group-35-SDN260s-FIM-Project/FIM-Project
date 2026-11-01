package my.fim_security_suite.core;

import java.util.Map;
import my.fim_security_suite.model.Baseline;
import my.fim_security_suite.model.FileChange;
import my.fim_security_suite.model.FileRecord;
import my.fim_security_suite.model.FileStatus;
import my.fim_security_suite.model.ScanResult;

/**
 * Compares a fresh scan of the folder against the saved baseline and
 * puts every file into one of the four FileStatus groups.
 *
 * It works in two passes over the maps (baseline first, then the new scan),
 * so it is O(n + m) instead of comparing every file with every other file.
 *
 * @author wahin
 */
public class IntegrityChecker {

    /**
     * @param old the saved baseline
     * @param now the current scan, path -> record (from FileScanner.scan)
     * @return a ScanResult with one FileChange per file, sorted by path
     */
    public ScanResult compare(Baseline old, Map<String, FileRecord> now){
        if(old == null || now == null){
            throw new IllegalArgumentException("Baseline and current scan must not be null");
        }
        ScanResult result = new ScanResult();
        Map<String, FileRecord> before = old.getRecords();

        // pass 1: go through what we had in the baseline
        for (Map.Entry<String, FileRecord> entry : before.entrySet()) {
            String path = entry.getKey();
            String oldHash = entry.getValue().getHash();
            FileRecord current = now.get(path);

            if (current == null) {
                // was in the baseline but isn't in the folder anymore
                result.add(new FileChange(path, FileStatus.MISSING, oldHash, null));
            } else if (oldHash.equals(current.getHash())) {
                result.add(new FileChange(path, FileStatus.UNCHANGED, oldHash, current.getHash()));
            } else {
                // same file name, different contents
                result.add(new FileChange(path, FileStatus.MODIFIED, oldHash, current.getHash()));
            }
        }

        // pass 2: anything in the folder that the baseline never saw is new
        for (Map.Entry<String, FileRecord> entry : now.entrySet()) {
            if (!before.containsKey(entry.getKey())) {
                result.add(new FileChange(entry.getKey(), FileStatus.NEW, null, entry.getValue().getHash()));
            }
        }

        result.sort();
        return result;
    }
}
