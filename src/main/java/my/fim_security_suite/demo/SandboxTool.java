package my.fim_security_suite.demo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Scanner;
import java.util.stream.Stream;

/**
 * SandboxTool - makes a throwaway folder to demo the FIM on, so we never
 * have to point it at real files.
 *
 * Run this class (Run File in NetBeans) and use its menu, or give it one
 * argument: create | tamper | reset | delete
 *   create  - makes the sandbox folder with 4 files (and keeps a copy)
 *   tamper  - changes one file, deletes one and adds a new one
 *   reset   - puts the sandbox back like it was after "create"
 *   delete  - removes the sandbox folder and the backup copy
 *
 * CliMenu (options 6 to 9) calls the same methods.
 *
 * Order for the demo: create -> select the sandbox in the FIM -> create
 * baseline -> run check (clean) -> tamper -> run check again.
 */
public class SandboxTool {

    // both folders are next to the program and not inside each other
    private static final Path SANDBOX = Path.of("sandbox");
    private static final Path ORIGINAL = Path.of("sandbox_original");

    public static void main(String[] args) {
        try {
            if (args.length > 0) {
                if (!runCommand(args[0])) {
                    System.out.println("Usage: SandboxTool create | tamper | reset | delete");
                }
                return;
            }
            menu();
        } catch (IOException e) {
            System.out.println("Sandbox error: " + e.getMessage());
        }
    }

    /** Little menu for when the class is run without an argument. */
    private static void menu() throws IOException {
        Scanner input = new Scanner(System.in);
        while (true) {
            System.out.println();
            System.out.println("=== Sandbox Tool ===");
            System.out.println("  1. Create sandbox");
            System.out.println("  2. Tamper with the files");
            System.out.println("  3. Reset the sandbox");
            System.out.println("  4. Delete the sandbox");
            System.out.println("  0. Exit");
            System.out.print("Choice: ");
            if (!input.hasNextLine()) {
                return;   // input stream was closed
            }
            String choice = input.nextLine().trim();
            try {
                switch (choice) {
                    case "1": runCommand("create"); break;
                    case "2": runCommand("tamper"); break;
                    case "3": runCommand("reset"); break;
                    case "4":
                        System.out.print("Delete the sandbox and its backup copy? (y/n): ");
                        if (input.hasNextLine() && input.nextLine().trim().equalsIgnoreCase("y")) {
                            runCommand("delete");
                        } else {
                            System.out.println("Nothing deleted.");
                        }
                        break;
                    case "0": return;
                    default: System.out.println("Please choose 0 - 4.");
                }
            } catch (IOException e) {
                System.out.println("Sandbox error: " + e.getMessage());
            }
        }
    }

    /** Runs create, tamper, reset or delete by name. Gives false if the name is none of them. */
    private static boolean runCommand(String command) throws IOException {
        switch (command.toLowerCase()) {
            case "create": create(); break;
            case "tamper": tamper(); break;
            case "reset":  reset(); break;
            case "delete": delete(); break;
            default: return false;
        }
        if (!command.equalsIgnoreCase("delete")) {
            System.out.println("Done. Sandbox folder: " + SANDBOX.toAbsolutePath());
        }
        return true;
    }

    /** The sandbox folder, so the console menu can select it. */
    public static Path getSandboxFolder() {
        return SANDBOX;
    }

    /** New sandbox with the same files every time, plus a backup copy for reset(). */
    public static void create() throws IOException {
        deleteRecursively(SANDBOX);
        deleteRecursively(ORIGINAL);
        write("report.txt", "Quarterly report - draft 1");
        write("notes.txt", "Meeting notes for group 35");
        write("config/settings.ini", "theme=dark\nautosave=true");
        write("config/users.csv", "name,role\nadmin,owner");
        copyRecursively(SANDBOX, ORIGINAL);
        System.out.println("Created sandbox with 4 files.");
    }

    /** Does one of each change that the checker should find. */
    public static void tamper() throws IOException {
        if (!Files.exists(SANDBOX)) {
            throw new IOException("Run 'create' first, there is no sandbox yet");
        }
        write("report.txt", "Quarterly report - someone changed this");   // MODIFIED
        Files.deleteIfExists(SANDBOX.resolve("notes.txt"));               // MISSING
        write("intruder.txt", "this file was not here before");           // NEW
        System.out.println("Tampered: modified report.txt, deleted notes.txt, added intruder.txt");
    }

    /** Puts the sandbox back to how it was right after create(). */
    public static void reset() throws IOException {
        if (!Files.exists(ORIGINAL)) {
            throw new IOException("Nothing to reset from, run 'create' first");
        }
        deleteRecursively(SANDBOX);
        copyRecursively(ORIGINAL, SANDBOX);
        System.out.println("Sandbox reset to its original 4 files.");
    }

    /** Removes the sandbox and the backup copy. Does nothing if they are already gone. */
    public static void delete() throws IOException {
        if (!Files.exists(SANDBOX) && !Files.exists(ORIGINAL)) {
            System.out.println("Nothing to delete, there is no sandbox.");
            return;
        }
        deleteRecursively(SANDBOX);
        deleteRecursively(ORIGINAL);
        System.out.println("Sandbox deleted.");
    }

    /** Writes text into a file in the sandbox and makes the folders if they're missing. */
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
        // delete the deepest files first so the folders are empty when we reach them
        try (Stream<Path> paths = Files.walk(path)) {
            for (Path p : (Iterable<Path>) paths.sorted(Comparator.reverseOrder())::iterator) {
                Files.delete(p);
            }
        }
    }
}
