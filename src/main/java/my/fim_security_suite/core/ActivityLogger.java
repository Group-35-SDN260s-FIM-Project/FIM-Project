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
 * Writes LogEntry lines to a text file and reads the newest ones back.
 *
 * We used a file and not a list in memory so the history is still
 * there after the program is closed.
 * The methods are synchronized because MonitorService will log from a
 * background thread later.
 */
public class ActivityLogger {

    private final Path logFile;

    public ActivityLogger(Path logFile) {
        this.logFile = logFile;
    }

    /** Adds an entry at the end of the log file (the file is made if it isn't there). */
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
     * Gives the last n entries, oldest first.
     * A line that can't be read is skipped so the menu doesn't crash.
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
                    // the log was probably edited by hand, so skip this line
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
