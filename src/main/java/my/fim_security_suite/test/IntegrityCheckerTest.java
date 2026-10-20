package my.fim_security_suite.test;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

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

    private static FileRecord rec(String path, String hash) {
        return new FileRecord(Path.of(path), hash, 0L, 0L);
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
        return new Baseline(Path.of("."), java.time.LocalDateTime.now(), map);
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
