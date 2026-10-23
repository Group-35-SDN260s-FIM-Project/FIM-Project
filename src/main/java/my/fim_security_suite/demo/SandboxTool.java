package my.fim_security_suite.demo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * SandboxTool - sets up a throwaway folder to demo the FIM on, so we never
 * point it at real files during a demo.
 *
 * Run it with one argument:
 *   create  - makes the sandbox folder with 4 files (and keeps a copy of them)
 *   tamper  - modifies one file, deletes one and adds a new one
 *   reset   - puts the sandbox back to how it was after "create"
 *
 * Demo order: create -> pick the sandbox in the menu -> create baseline
 * -> run check (clean) -> tamper -> run check again.
 *
 * Owner: Mixo Rikhotso
 */
public class SandboxTool {

    // both folders sit next to the program, not inside each other
    private static final Path SANDBOX = Path.of("sandbox");
    private static final Path ORIGINAL = Path.of("sandbox_original");

    public static void main(String[] args) {
        String command = args.length > 0 ? args[0].toLowerCase() : "";
        try {
            switch (command) {
                case "create": create(); break;
                case "tamper": tamper(); break;
                case "reset":  reset(); break;
                default:
                    System.out.println("Usage: SandboxTool create | tamper | reset");
                    return;
            }
            System.out.println("Done. Sandbox folder: " + SANDBOX.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("Sandbox error: " + e.getMessage());
        }
    }

    /** Fresh sandbox with a known set of files, plus a backup copy for reset(). */
    static void create() throws IOException {
        deleteRecursively(SANDBOX);
        deleteRecursively(ORIGINAL);
        write("report.txt", "Quarterly report - draft 1");
        write("notes.txt", "Meeting notes for group 35");
        write("config/settings.ini", "theme=dark\nautosave=true");
        write("config/users.csv", "name,role\nadmin,owner");
        copyRecursively(SANDBOX, ORIGINAL);
        System.out.println("Created sandbox with 4 files.");
    }

    /** Makes one of each kind of change the checker should catch. */
    static void tamper() throws IOException {
        if (!Files.exists(SANDBOX)) {
            throw new IOException("Run 'create' first, there is no sandbox yet");
        }
        write("report.txt", "Quarterly report - someone changed this");   // MODIFIED
        Files.deleteIfExists(SANDBOX.resolve("notes.txt"));               // MISSING
        write("intruder.txt", "this file was not here before");           // NEW
        System.out.println("Tampered: modified report.txt, deleted notes.txt, added intruder.txt");
    }

    /** Puts the sandbox back to the state right after create(). */
    static void reset() throws IOException {
        if (!Files.exists(ORIGINAL)) {
            throw new IOException("Nothing to reset from, run 'create' first");
        }
        deleteRecursively(SANDBOX);
        copyRecursively(ORIGINAL, SANDBOX);
        System.out.println("Sandbox reset to its original 4 files.");
    }

    /** Writes text to a file inside the sandbox, creating folders if needed. */
    private static void write(String relativePath, String text) throws IOException {
        Path file = SANDBOX.resolve(relativePath);
        Files.createDirectories(file.getParent());
        Files.write(file, text.getBytes(StandardCharsets.UTF_8));
    }

    private static void copyRecursively(Path from, Path to) throws IOException {
        try (Stream<Path> paths = Files.walk(from)) {
            for (Path source : (Iterable<Path>) paths::iterator) {
                Path target = to.resolve(from.relativize(source).toString());
                if (Files.isDirectory(source)) {
                    Files.createDirectories(target);
                } else {
                    Files.copy(source, target);
                }
            }
        }
    }

    private static void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }
        // delete the deepest files first so folders are empty when we get to them
        try (Stream<Path> paths = Files.walk(path)) {
            for (Path p : (Iterable<Path>) paths.sorted(Comparator.reverseOrder())::iterator) {
                Files.delete(p);
            }
        }
    }
}
