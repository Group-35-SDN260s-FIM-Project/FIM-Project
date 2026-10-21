package my.fim_security_suite.test;

import java.util.Map;
import my.fim_security_suite.core.IntegrityChecker;
import my.fim_security_suite.model.Baseline;
import my.fim_security_suite.model.FileRecord;
import my.fim_security_suite.model.FileStatus;
import my.fim_security_suite.model.ScanResult;

/**
 * Plain Java tests for IntegrityChecker (no JUnit needed, just run the file).
 * Each test builds a small fake baseline and scan by hand and checks the
 * unchanged / modified / new / missing counts.
 */
public class IntegrityCheckerTest {

    private static int passed;
    private static int failed;

    public static void main(String[] args) {
        passed = 0;
        failed = 0;

        testMixedExample();
        testBothEmpty();
        testEmptyBaselineAllNew();
        testEmptyFolderAllMissing();
        testIdenticalAllUnchanged();
        testSingleHashChange();
        testRenameIsMissingPlusNew();

        System.out.println("Passed: " + passed + ", Failed: " + failed);
    }

    private static void testMixedExample() {
        Baseline baseline = baseline(
                rec("a.txt", "h1"),
                rec("b.txt", "h2"),
                rec("c.txt", "h3"));
        Map<String, FileRecord> now = scan(
                rec("a.txt", "h1"),
                rec("b.txt", "XX"),
                rec("d.txt", "h4"));

        ScanResult result = new IntegrityChecker().compare(baseline, now);
        checkCounts("testMixedExample", result, 1, 1, 1, 1);
    }

    private static void testBothEmpty() {
        Baseline baseline = baseline();
        Map<String, FileRecord> now = scan();

        ScanResult result = new IntegrityChecker().compare(baseline, now);
        checkCounts("testBothEmpty", result, 0, 0, 0, 0);
    }

    private static void testEmptyBaselineAllNew() {
        Baseline baseline = baseline();
        Map<String, FileRecord> now = scan(
                rec("a.txt", "h1"),
                rec("b.txt", "h2"));

        ScanResult result = new IntegrityChecker().compare(baseline, now);
        checkCounts("testEmptyBaselineAllNew", result, 0, 0, 2, 0);
    }

    private static void testEmptyFolderAllMissing() {
        Baseline baseline = baseline(
                rec("a.txt", "h1"),
                rec("b.txt", "h2"));
        Map<String, FileRecord> now = scan();

        ScanResult result = new IntegrityChecker().compare(baseline, now);
        checkCounts("testEmptyFolderAllMissing", result, 0, 0, 0, 2);
    }

    private static void testIdenticalAllUnchanged() {
        Baseline baseline = baseline(
                rec("a.txt", "h1"),
                rec("b.txt", "h2"));
        Map<String, FileRecord> now = scan(
                rec("a.txt", "h1"),
                rec("b.txt", "h2"));

        ScanResult result = new IntegrityChecker().compare(baseline, now);
        checkCounts("testIdenticalAllUnchanged", result, 2, 0, 0, 0);
    }

    private static void testSingleHashChange() {
        Baseline baseline = baseline(rec("a.txt", "h1"));
        Map<String, FileRecord> now = scan(rec("a.txt", "XX"));

        ScanResult result = new IntegrityChecker().compare(baseline, now);
        checkCounts("testSingleHashChange", result, 0, 1, 0, 0);
    }

    private static void testRenameIsMissingPlusNew() {
        Baseline baseline = baseline(rec("old.txt", "h1"));
        Map<String, FileRecord> now = scan(rec("new.txt", "h1"));

        ScanResult result = new IntegrityChecker().compare(baseline, now);
        checkCounts("testRenameIsMissingPlusNew", result, 0, 0, 1, 1);
    }

    // FileRecord and Baseline take the path as a String, not a Path
    private static FileRecord rec(String path, String hash) {
        return new FileRecord(path, hash, 0L, 0L);
    }

    private static Map<String, FileRecord> scan(FileRecord... records) {
        Map<String, FileRecord> map = new java.util.HashMap<>();
        for (FileRecord r : records) {
            map.put(r.getPath(), r);
        }
        return map;
    }

    private static Baseline baseline(FileRecord... records) {
        Map<String, FileRecord> map = scan(records);
        return new Baseline(".", java.time.LocalDateTime.now(), map);
    }

    private static void checkCounts(String name, ScanResult result, int u, int m, int n, int x) {
        int unchanged = result.countOf(FileStatus.UNCHANGED);
        int modified = result.countOf(FileStatus.MODIFIED);
        int added = result.countOf(FileStatus.NEW);
        int missing = result.countOf(FileStatus.MISSING);

        check(name, unchanged == u && modified == m && added == n && missing == x);
    }

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("PASS: " + name);
        } else {
            failed++;
            System.out.println("FAIL: " + name);
        }
    }
}
