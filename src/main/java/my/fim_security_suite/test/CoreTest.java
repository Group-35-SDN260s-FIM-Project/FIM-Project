import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * CoreTest - tests the core FIM flow (hash -> scan -> compare) inside a SandboxTool.
 *
 * ASSUMED APIs (adjust to match your teammates' actual code):
 *   HashUtil.sha256(Path)                         -> String
 *   FileScanner.scan(Path dir)                    -> List<FileRecord>
 *   IntegrityChecker.compare(List<FileRecord> baseline, List<FileRecord> current)
 *                                                 -> List<FileChange>
 *   FileChange.getStatus()                        -> FileStatus (MODIFIED, NEW, DELETED)
 *
 * Owner: Mixo Rikhotso
 */
class CoreTest {

    private SandboxTool sandbox;
    private final FileScanner scanner = new FileScanner();
    private final IntegrityChecker checker = new IntegrityChecker();

    @BeforeEach
    void setUp() throws Exception {
        sandbox = new SandboxTool();
        sandbox.populateDefaults();
    }

    @AfterEach
    void tearDown() {
        sandbox.cleanup();
    }

    // ---------- HashUtil ----------

    @Test
    void hashIsConsistentForSameContent() throws Exception {
        Path f = sandbox.createFile("same.txt", "hello");
        assertEquals(HashUtil.sha256(f), HashUtil.sha256(f));
    }

    @Test
    void hashChangesWhenContentChanges() throws Exception {
        Path f = sandbox.createFile("x.txt", "before");
        String before = HashUtil.sha256(f);
        sandbox.modifyFile("x.txt", "after");
        assertNotEquals(before, HashUtil.sha256(f));
    }

    // ---------- FileScanner ----------

    @Test
    void scannerFindsAllFilesIncludingSubfolders() throws Exception {
        List<FileRecord> records = scanner.scan(sandbox.getRoot());
        assertEquals(4, records.size()); // a, b, docs/c, docs/notes/d
    }

    // ---------- IntegrityChecker ----------

    @Test
    void noChangesReportedWhenNothingChanged() throws Exception {
        List<FileRecord> baseline = scanner.scan(sandbox.getRoot());
        List<FileRecord> current = scanner.scan(sandbox.getRoot());
        assertTrue(checker.compare(baseline, current).isEmpty());
    }

    @Test
    void detectsModifiedFile() throws Exception {
        List<FileRecord> baseline = scanner.scan(sandbox.getRoot());
        sandbox.modifyFile("a.txt", "tampered");
        List<FileChange> changes = checker.compare(baseline, scanner.scan(sandbox.getRoot()));

        assertEquals(1, changes.size());
        assertEquals(FileStatus.MODIFIED, changes.get(0).getStatus());
    }

    @Test
    void detectsNewFile() throws Exception {
        List<FileRecord> baseline = scanner.scan(sandbox.getRoot());
        sandbox.createFile("intruder.txt", "surprise");
        List<FileChange> changes = checker.compare(baseline, scanner.scan(sandbox.getRoot()));

        assertEquals(1, changes.size());
        assertEquals(FileStatus.NEW, changes.get(0).getStatus());
    }

    @Test
    void detectsDeletedFile() throws Exception {
        List<FileRecord> baseline = scanner.scan(sandbox.getRoot());
        sandbox.deleteFile("b.txt");
        List<FileChange> changes = checker.compare(baseline, scanner.scan(sandbox.getRoot()));

        assertEquals(1, changes.size());
        assertEquals(FileStatus.DELETED, changes.get(0).getStatus());
    }

    @Test
    void detectsMultipleChangesAtOnce() throws Exception {
        List<FileRecord> baseline = scanner.scan(sandbox.getRoot());
        sandbox.modifyFile("a.txt", "changed");
        sandbox.deleteFile("b.txt");
        sandbox.createFile("new.txt", "new");
        List<FileChange> changes = checker.compare(baseline, scanner.scan(sandbox.getRoot()));

        assertEquals(3, changes.size());
    }

    // ---------- SandboxTool itself ----------

    @Test
    void sandboxBlocksPathTraversal() {
        assertThrows(java.io.IOException.class,
                () -> sandbox.createFile("../escape.txt", "nope"));
    }
}
