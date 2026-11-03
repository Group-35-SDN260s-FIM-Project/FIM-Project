package my.fim_security_suite.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * One line of the activity log, for example
 * 2026-10-18 19:57:54 | MODIFIED | text.txt
 *
 * toLine() makes that text and fromLine() reads it back again.
 * ActivityLogger uses these two to save entries in the log file.
 */
public class LogEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final LocalDateTime timestamp;
    private final String action;
    private final String message;

    /** Makes a new entry with the time it was created. */
    public LogEntry(String action, String message) {
        this(LocalDateTime.now(), action, message);
    }

    public LogEntry(LocalDateTime timestamp, String action, String message) {
        // the log file only has whole seconds, so cut off the nanos here as well,
        // if not the entry is not equal to itself after saving and reading it back
        this.timestamp = timestamp.withNano(0);
        this.action = action;
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getAction() {
        return action;
    }

    public String getMessage() {
        return message;
    }

    /** The text that goes into the log file. */
    public String toLine() {
        return FORMAT.format(timestamp) + " | " + action + " | " + message;
    }

    /**
     * Turns a line made by toLine() back into an entry.
     * The split limit is 3 so a message with a " | " in it stays in one piece.
     */
    public static LogEntry fromLine(String line) {
        String[] parts = line.split(" \\| ", 3);
        if (parts.length < 3) {
            throw new IllegalArgumentException("Not a valid log line: " + line);
        }
        LocalDateTime time = LocalDateTime.parse(parts[0].trim(), FORMAT);
        return new LogEntry(time, parts[1].trim(), parts[2]);
    }

    @Override
    public String toString() {
        return toLine();
    }
}
