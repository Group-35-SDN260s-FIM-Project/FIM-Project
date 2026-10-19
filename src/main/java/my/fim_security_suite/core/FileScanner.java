/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package my.fim_security_suite.core;

import my.fim_security_suite.model.FileRecord;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;

public class FileScanner {

    private final Pattern ignore;
    private final List<String> skipped = new ArrayList<>();

    public FileScanner() {
        this(null);
    }

    public FileScanner(String ignoreRegex) {
        ignore = (ignoreRegex == null || ignoreRegex.isBlank())
                ? null : Pattern.compile(ignoreRegex);
    }

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

    public List<String> getSkipped() {
        return new ArrayList<>(skipped);
    }
}
