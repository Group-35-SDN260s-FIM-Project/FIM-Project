package my.fim_security_suite.core;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import my.fim_security_suite.model.LogEntry;

/**
 * Writes LogEntry lines to a text file and reads the latest ones back.
 *
 * We went with a file instead of keeping a list in memory so the history
 * is still there after the program is closed.
 * The methods are synchronized because MonitorService will log from a
 * background thread later on.
 *
 * @author Lenovo
 */
public class ActivityLogger {

    private final Path logFile;

    public ActivityLogger(Path logFile) {
        this.logFile = logFile;
    }

    /** Adds one entry to the end of the log file (creates the file if needed). */
    public synchronized void log(LogEntry entry) {
        try {
            Path parent = logFile.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(logFile,
                    (entry.toLine() + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write to log " + logFile, e);
        }
    }

    /**
     * Returns the last n entries, oldest first.
     * Lines that can't be read are skipped instead of crashing the menu.
     */
    public synchronized List<LogEntry> recent(int n) {
        List<LogEntry> entries = new ArrayList<>();
        if (n <= 0 || !Files.exists(logFile)) {
            return entries;
        }
        try {
            List<String> lines = Files.readAllLines(logFile, StandardCharsets.UTF_8);
            int start = Math.max(0, lines.size() - n);
            for (int i = start; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.isBlank()) {
                    continue;
                }
                try {
                    entries.add(LogEntry.fromLine(line));
                } catch (RuntimeException badLine) {
                    // someone edited the log by hand, just skip that line
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read log " + logFile, e);
        }
        return entries;
    }

    public Path getLogFile() {
        return logFile;
    }
}
