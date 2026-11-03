package my.fim_security_suite.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Everything one check found: a list of FileChange objects plus
 * a few small helpers to count and sort them.
 */
public class ScanResult {

    private final List<FileChange> changes = new ArrayList<FileChange>();

    /** Adds the result of one file. */
    public void add(FileChange change) {
        changes.add(change);
    }

    /** Sorts by path (FileChange is Comparable) so the output always comes in the same order. */
    public void sort() {
        Collections.sort(changes);
    }

    /** Read only view, the list can only be changed with add(). */
    public List<FileChange> getChanges() {
        return Collections.unmodifiableList(changes);
    }

    /** Counts how many files have the given status. */
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

    /** True if all the files are UNCHANGED. */
    public boolean isClean() {
        return countOf(FileStatus.UNCHANGED) == changes.size();
    }
}



