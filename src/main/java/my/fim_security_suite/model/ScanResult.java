package my.fim_security_suite.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Everything one integrity check found: a list of FileChange objects
 * with a few helpers to count and sort them.
 *
 * @author Rebecca Mandlazi
 */
public class ScanResult {

    private final List<FileChange> changes = new ArrayList<FileChange>();

    /** Adds one file result to the list. */
    public void add(FileChange change) {
        changes.add(change);
    }

    /** Sorts by path (FileChange is Comparable) so the output is in a steady order. */
    public void sort() {
        Collections.sort(changes);
    }

    /** Read-only view, the list can only be changed through add(). */
    public List<FileChange> getChanges() {
        return Collections.unmodifiableList(changes);
    }

    /** How many files got the given status. */
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

    /** True when every single file is UNCHANGED. */
    public boolean isClean() {
        return countOf(FileStatus.UNCHANGED) == changes.size();
    }
}



