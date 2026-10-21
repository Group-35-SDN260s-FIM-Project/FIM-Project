import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * SandboxTool - a disposable temp directory for safely testing the FIM.
 * Lets you create, modify and delete files to simulate tampering
 * without touching any real files. Use with try-with-resources so the
 * sandbox is always cleaned up.
 *
 * Owner: Mixo Rikhotso
 */
public class SandboxTool implements AutoCloseable {

    private final Path root;

    public SandboxTool() throws IOException {
        this.root = Files.createTempDirectory("fim_sandbox_");
    }

    /** The sandbox root directory (point FileScanner at this). */
    public Path getRoot() {
        return root;
    }

    /** Creates a file (and any parent folders) with the given content. */
    public Path createFile(String relativePath, String content) throws IOException {
        Path file = resolveSafe(relativePath);
        Files.createDirectories(file.getParent());
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
        return file;
    }

    /** Overwrites an existing file, simulating a tampered/modified file. */
    public Path modifyFile(String relativePath, String newContent) throws IOException {
        Path file = resolveSafe(relativePath);
        if (!Files.exists(file)) {
            throw new IOException("Cannot modify, file does not exist: " + relativePath);
        }
        Files.write(file, newContent.getBytes(StandardCharsets.UTF_8));
        return file;
    }

    /** Deletes a file, simulating a removed file. */
    public void deleteFile(String relativePath) throws IOException {
        Files.deleteIfExists(resolveSafe(relativePath));
    }

    /** Fills the sandbox with a small, known set of files. */
    public void populateDefaults() throws IOException {
        createFile("a.txt", "alpha");
        createFile("b.txt", "bravo");
        createFile("docs/c.txt", "charlie");
        createFile("docs/notes/d.txt", "delta");
    }

    /** Deletes the sandbox and everything inside it. */
    public void cleanup() {
        if (!Files.exists(root)) return;
        try (Stream<Path> paths = Files.walk(root)) {
            paths.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ignored) {
                }
            });
        } catch (IOException ignored) {
        }
    }

    @Override
    public void close() {
        cleanup();
    }

    /** Resolves a path and blocks escaping the sandbox (e.g. "../../x"). */
    private Path resolveSafe(String relativePath) throws IOException {
        Path resolved = root.resolve(relativePath).normalize();
        if (!resolved.startsWith(root)) {
            throw new IOException("Path escapes sandbox: " + relativePath);
        }
        return resolved;
    }
}
