package my.fim_security_suite.test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;
import my.fim_security_suite.core.BaselineManager;
import my.fim_security_suite.core.FileScanner;
import my.fim_security_suite.core.HashUtil;
import my.fim_security_suite.model.Baseline;
import my.fim_security_suite.model.FileRecord;
import my.fim_security_suite.model.LogEntry;

/**
 * CoreTest - normal Java tests (no JUnit) for what IntegrityCheckerTest
 * leaves out: HashUtil, saving and loading the baseline, the log line
 * format and the ignore pattern of FileScanner.
 *
 * Just run the file. It prints PASS or FAIL for every check and a total
 * at the end. The temp files are deleted when it finishes.
 */
public class CoreTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        // every test has its own try so one crash doesn't stop the others
        try {
            testHashOfAbc();
        } catch (Exception e) {
            check("hash of abc threw " + e, false);
        }
        try {
            testSaveAndLoadRoundTrip();
        } catch (Exception e) {
            check("save/load round trip threw " + e, false);
        }
        try {
            testLogLineRoundTrip();
        } catch (Exception e) {
            check("log line round trip threw " + e, false);
        }
        try {
            testScannerIgnoresTmp();
        } catch (Exception e) {
            check("scanner ignores .tmp threw " + e, false);
        }

        System.out.println();
        System.out.println(passed + " checks passed, " + failed + " failed.");
    }

    /**
     * The SHA-256 of "abc" is a known test value, so if we get the same
     * answer then our hashing is really correct and not only consistent.
     */
    private static void testHashOfAbc() throws Exception {
        Path dir = Files.createTempDirectory("fim_core_hash");
        dir.toFile().deleteOnExit();
        Path file = dir.resolve("abc.txt");
        Files.write(file, "abc".getBytes(StandardCharsets.UTF_8));
        file.toFile().deleteOnExit();

        String expected = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad";
        check("sha256 of abc", expected.equals(HashUtil.sha256(file)));
    }

    /** Saves a baseline, loads it again and checks that nothing got lost. */
    private static void testSaveAndLoadRoundTrip() throws Exception {
        // the monitored folder and the baseline file are kept apart, like in the real program
        Path folder = Files.createTempDirectory("fim_core_folder");
        Path dataDir = Files.createTempDirectory("fim_core_data");
        folder.toFile().deleteOnExit();
        dataDir.toFile().deleteOnExit();

        Path a = folder.resolve("a.txt");
        Path b = folder.resolve("b.txt");
        Files.write(a, "alpha".getBytes(StandardCharsets.UTF_8));
        Files.write(b, "bravo".getBytes(StandardCharsets.UTF_8));
        a.toFile().deleteOnExit();
        b.toFile().deleteOnExit();

        Path store = dataDir.resolve("baseline.ser");
        store.toFile().deleteOnExit();

        BaselineManager manager = new BaselineManager(new FileScanner(), store);
        Baseline original = manager.create(folder);
        manager.save(original);
        Baseline loaded = manager.load(store);

        check("round trip: file count", loaded.size() == original.size());
        check("round trip: folder", loaded.getFolder().equals(original.getFolder()));

        boolean sameHashes = true;
        for (Map.Entry<String, FileRecord> entry : original.getRecords().entrySet()) {
            FileRecord copy = loaded.getRecords().get(entry.getKey());
            if (copy == null || !copy.getHash().equals(entry.getValue().getHash())) {
                sameHashes = false;
            }
        }
        check("round trip: hashes", sameHashes);
    }

    /** toLine() followed by fromLine() must give the same entry back. */
    private static void testLogLineRoundTrip() {
        LocalDateTime time = LocalDateTime.of(2026, 10, 18, 19, 57, 54);
        LogEntry original = new LogEntry(time, "MODIFIED", "text.txt");

        LogEntry copy = LogEntry.fromLine(original.toLine());

        check("log line: timestamp", copy.getTimestamp().equals(time));
        check("log line: action", copy.getAction().equals("MODIFIED"));
        check("log line: message", copy.getMessage().equals("text.txt"));
    }

    /** A scanner with an ignore pattern should skip the .tmp files. */
    private static void testScannerIgnoresTmp() throws Exception {
        Path folder = Files.createTempDirectory("fim_core_scan");
        folder.toFile().deleteOnExit();

        Path keep = folder.resolve("keep.txt");
        Path junk = folder.resolve("junk.tmp");
        Files.write(keep, "keep me".getBytes(StandardCharsets.UTF_8));
        Files.write(junk, "ignore me".getBytes(StandardCharsets.UTF_8));
        keep.toFile().deleteOnExit();
        junk.toFile().deleteOnExit();

        FileScanner scanner = new FileScanner(".*\\.tmp");
        Map<String, FileRecord> records = scanner.scan(folder);

        check("scanner: keeps .txt", records.containsKey("keep.txt"));
        check("scanner: skips .tmp", !records.containsKey("junk.tmp"));
        check("scanner: only one file", records.size() == 1);
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
