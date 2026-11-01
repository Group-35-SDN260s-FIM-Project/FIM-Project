package my.fim_security_suite.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * One line in the activity log, e.g.
 * 2026-10-18 19:57:54 | MODIFIED | text.txt
 *
 * toLine() turns it into that text and fromLine() reads it back,
 * which is how ActivityLogger stores entries in the log file.
 *
 * @author wahin
 */
public class LogEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final LocalDateTime timestamp;
    private final String action;
    private final String message;

    /** New entry stamped with the current time. */
    public LogEntry(String action, String message) {
        this(LocalDateTime.now(), action, message);
    }

    public LogEntry(LocalDateTime timestamp, String action, String message) {
        // the log only keeps whole seconds, so drop the nanos here too
        // otherwise an entry wouldn't equal itself after being saved and read back
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

    /** The text written to the log file. */
    public String toLine() {
        return FORMAT.format(timestamp) + " | " + action + " | " + message;
    }

    /**
     * Reads a line written by toLine().
     * Split limit is 3 so a message that has a " | " in it stays in one piece.
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
