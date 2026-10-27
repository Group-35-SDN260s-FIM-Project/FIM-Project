package my.fim_security_suite.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Rebecca Mandlazi
 */
public class ScanResult {
   
    private final List<FileChange> changes = new ArrayList<FileChange>();

    public void add(FileChange change) {
        changes.add(change);
    }

    public void sort() {
        Collections.sort(changes);
    }

    public List<FileChange> getChanges() {
        return Collections.unmodifiableList(changes);
    }

    public int countOf(FileStatus status) {
        int count = 0;
        for (FileChange change : changes) {
            if (change.getStatus() == status) {
                count++;
            }
        }
        return count;
    }

    public int size() {
        return changes.size();
    }

    public boolean isClean() {
        return countOf(FileStatus.UNCHANGED) == changes.size();
    }
}



