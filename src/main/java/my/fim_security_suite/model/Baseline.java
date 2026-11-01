package my.fim_security_suite.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * A saved snapshot of a folder: where it was, when we took it and the
 * record (hash, size, date) of every file in it.
 * Serializable so BaselineManager can write it to disk.
 */
public class Baseline implements Serializable {

    private static final long serialVersionUID = 1L;

    private String folder;
    private LocalDateTime createdAt;
    private HashMap<String, FileRecord> records;

    public Baseline(String folder, LocalDateTime createdAt, Map<String, FileRecord> records) {
        this.folder = folder;
        this.createdAt = createdAt;
        // copy the map so changing the original later can't change the baseline
        this.records = new HashMap<>(records);
    }

    public String getFolder() {
        return folder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Map<String, FileRecord> getRecords() {
        return records;
    }

    public int size() {
        return records.size();
    }
}
