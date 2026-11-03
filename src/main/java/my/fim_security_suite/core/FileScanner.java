package my.fim_security_suite.core;

import my.fim_security_suite.model.FileRecord;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Goes through a folder and all its sub folders and makes a FileRecord
 * for each normal file.
 * What we can't or shouldn't read (links, locked files) is not hashed,
 * it goes in getSkipped() and the scan carries on.
 */
public class FileScanner {

    private final Pattern ignore;
    private final List<String> skipped = new ArrayList<>();

    /** Scanner that looks at all the files. */
    public FileScanner() {
        this(null);
    }

    /** Scanner that skips files whose relative path matches the regex (e.g. ".*\\.tmp"). */
    public FileScanner(String ignoreRegex) {
        ignore = (ignoreRegex == null || ignoreRegex.isBlank())
                ? null : Pattern.compile(ignoreRegex);
    }

    /** Gives relative path -> FileRecord for everything inside the folder. */
    public Map<String, FileRecord> scan(Path folder) {
        Objects.requireNonNull(folder, "folder");

        Path root = folder.toAbsolutePath().normalize();

        if (!Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS)) {
            throw new IllegalArgumentException("Not a folder: " + folder);
        }

        skipped.clear();
        Map<String, FileRecord> records = new LinkedHashMap<>();

        walk(root, root, records);

        return records;
    }

    /** Goes through one directory and calls itself again for each sub folder. */
    private void walk(Path root, Path directory,
                      Map<String, FileRecord> records) {

        try (DirectoryStream<Path> children =
                     Files.newDirectoryStream(directory)) {

            for (Path child : children) {

                String relative = root.relativize(child)
                        .toString().replace('\\', '/');

                if (ignore != null &&
                        ignore.matcher(relative).matches()) {
                    continue;
                }

                if (Files.isSymbolicLink(child)) {
                    skipped.add(relative + " (symbolic link skipped)");
                    continue;
                }

                if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                    walk(root, child, records);
                    continue;
                }

                if (!Files.isRegularFile(child, LinkOption.NOFOLLOW_LINKS)) {
                    skipped.add(relative + " (not a regular file)");
                    continue;
                }

                try {
                    String hash = HashUtil.sha256(child);
                    long size = Files.size(child);
                    long modified = Files.getLastModifiedTime(child).toMillis();

                    FileRecord record =
                            new FileRecord(relative, hash, size, modified);

                    records.put(relative, record);

                } catch (IOException | SecurityException ex) {
                    skipped.add(relative + " (" +
                            ex.getClass().getSimpleName() + ")");
                }
            }

        } catch (IOException | SecurityException ex) {

            String name = root.equals(directory) ? "." :
                    root.relativize(directory).toString().replace('\\', '/');

            skipped.add(name + " (directory unreadable: " +
                    ex.getClass().getSimpleName() + ")");
        }
    }

    /** What the last scan skipped and why. */
    public List<String> getSkipped() {
        return new ArrayList<>(skipped);
    }
}
