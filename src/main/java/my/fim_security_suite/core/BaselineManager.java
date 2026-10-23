package my.fim_security_suite.core;

import my.fim_security_suite.model.Baseline;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;
import my.fim_security_suite.model.FileRecord;

/**
 * Creates, saves and loads Baseline snapshots.
 * Uses FileScanner to read a folder and Java serialization to store the result.
 */
public class BaselineManager {

    private FileScanner scanner;
    private Path storeFile;

    public BaselineManager(FileScanner scanner, Path storeFile) {
        this.scanner = scanner;
        this.storeFile = storeFile;
    }

    /** Scans the folder and builds a new Baseline (does not save it). */
    public Baseline create(Path folder) {
        Map<String, FileRecord> records = scanner.scan(folder);
        return new Baseline(folder.toString(), LocalDateTime.now(), records);
    }

    /** Saves the baseline to the store file. */
    public void save(Baseline baseline) {
        try {
            Path parent = storeFile.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(storeFile))) {
                out.writeObject(baseline);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save baseline to " + storeFile, e);
        }
    }

    /** Loads the baseline from the default store file. */
    public Baseline load() {
        return load(storeFile);
    }

    /** Loads a baseline from the given file. */
    public Baseline load(Path file) {
        if (!Files.exists(file)) {
            throw new IllegalStateException("No baseline found at " + file);
        }
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(file))) {
            return (Baseline) in.readObject();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read baseline from " + file, e);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Baseline file is corrupt or from another version", e);
        }
    }

    /** True if a saved baseline exists. */
    public boolean exists() {
        return Files.exists(storeFile);
    }

    public Path getStoreFile() {
        return storeFile;
    }
}
