package my.fim_security_suite.cli;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;
import my.fim_security_suite.controller.FIMController;
import my.fim_security_suite.demo.SandboxTool;
import my.fim_security_suite.model.Baseline;
import my.fim_security_suite.model.FileChange;
import my.fim_security_suite.model.FileStatus;
import my.fim_security_suite.model.LogEntry;
import my.fim_security_suite.model.ScanResult;

/**
 * Console menu for the Check-In 2 prototype.
 * It just reads what the user types and prints results, the real work
 * is done by FIMController.
 */
public class CliMenu {
    private final FIMController controller;
    private final Scanner input;

    public CliMenu(FIMController controller){
        this.controller = controller;
        this.input = new Scanner(System.in);
    }

    /**
     * Lets you run the console version straight from this file
     * (right click, Run File in NetBeans) without the --cli argument.
     */
    public static void main(String[] args) {
        new CliMenu(new FIMController()).run();
    }

    /** Keeps showing the menu until the user picks 0. */
    public void run() {
        System.out.println("=== FIM Security Suite (console version) ===");
        boolean running = true;
        while (running) {
            printMenu();
            String choice = readLine();
            if (choice == null) {
                break;   // input stream was closed
            }
            // errors from the controller (wrong folder, no baseline...) get printed
            // for the user so the program doesn't crash
            try {
                switch (choice.trim()) {
                    case "1": chooseFolder(); break;
                    case "2": createBaseline(); break;
                    case "3": runCheck(); break;
                    case "4": showLog(); break;
                    case "5": openDashboard(); break;
                    case "6": sandboxCreate(); break;
                    case "7": sandboxTamper(); break;
                    case "8": sandboxReset(); break;
                    case "9": sandboxDelete(); break;
                    case "0": running = false; break;
                    default: System.out.println("Please choose 0 - 9.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (RuntimeException e) {
                System.out.println("Something went wrong: " + e.getMessage());
            }
        }
        System.out.println("Goodbye.");
    }

    private void printMenu(){
        Path folder = controller.getSelectedFolder();
        System.out.println();
        System.out.println("Folder:   " + (folder == null ? "(none chosen)" : folder));
        System.out.println("Baseline: " + (controller.hasBaseline() ? "saved" : "none"));
        System.out.println("  1. Choose folder to monitor");
        System.out.println("  2. Create baseline");
        System.out.println("  3. Run integrity check");
        System.out.println("  4. View recent activity log");
        System.out.println("  5. Open GUI dashboard");
        System.out.println("  6. Sandbox: create demo folder and select it");
        System.out.println("  7. Sandbox: tamper with the demo files");
        System.out.println("  8. Sandbox: reset the demo files");
        System.out.println("  9. Sandbox: delete the demo folder");
        System.out.println("  0. Exit");
        System.out.print("Choice: ");
    }

    private void chooseFolder(){
        System.out.print("Folder path: ");
        String text = readLine();
        if (text == null || text.isBlank()) {
            System.out.println("No folder entered.");
            return;
        }
        // Windows Explorer pastes the path with quotes around it, so remove them
        text = text.trim().replace("\"", "");
        controller.selectFolder(Path.of(text));
        System.out.println("Now monitoring: " + controller.getSelectedFolder());
    }

    private void createBaseline(){
        Baseline baseline = controller.createBaseline();
        System.out.println("Baseline saved: " + baseline.size() + " files recorded.");
        printSkipped();
    }

    private void runCheck(){
        ScanResult result = controller.runCheck();

        System.out.println();
        System.out.println("--- Integrity check result ---");
        System.out.println("Unchanged: " + result.countOf(FileStatus.UNCHANGED));
        System.out.println("Modified:  " + result.countOf(FileStatus.MODIFIED));
        System.out.println("New:       " + result.countOf(FileStatus.NEW));
        System.out.println("Missing:   " + result.countOf(FileStatus.MISSING));

        if (result.isClean()) {
            System.out.println("No changes detected.");
        } else {
            System.out.println();
            for (FileChange change : result.getChanges()) {
                if (change.getStatus() == FileStatus.UNCHANGED) {
                    continue;   // only show the files that changed
                }
                System.out.printf("  [%-8s] %s   (old: %s  new: %s)%n",
                        change.getStatus(), change.getPath(),
                        shortHash(change.getOldHash()), shortHash(change.getNewHash()));
            }
        }
        printSkipped();
    }

    /** Opens the Swing dashboard with the same controller, so both views share the same state. */
    private void openDashboard(){
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            System.out.println("No display available for the GUI.");
            return;
        }
        java.awt.EventQueue.invokeLater(() -> {
            my.fim_security_suite.ui.MainDashBoard dashboard =
                    new my.fim_security_suite.ui.MainDashBoard(controller);
            dashboard.setLocationRelativeTo(null);
            dashboard.setVisible(true);
        });
        System.out.println("Dashboard opened.");
    }

    /** Makes the demo folder and selects it, so nobody has to type a path during the demo. */
    private void sandboxCreate(){
        try {
            SandboxTool.create();
            controller.selectFolder(SandboxTool.getSandboxFolder());
            System.out.println("Now monitoring: " + controller.getSelectedFolder());
            System.out.println("Next: option 2 to create a baseline.");
        } catch (IOException e) {
            System.out.println("Sandbox error: " + e.getMessage());
        }
    }

    /** Changes one file, deletes one and adds one so the next check has something to find. */
    private void sandboxTamper(){
        try {
            SandboxTool.tamper();
            System.out.println("Next: option 3 to run a check.");
        } catch (IOException e) {
            System.out.println("Sandbox error: " + e.getMessage());
        }
    }

    /** Puts the demo files back like they were after option 6. */
    private void sandboxReset(){
        try {
            SandboxTool.reset();
            System.out.println("Next: option 3 to run a check (it should be clean again).");
        } catch (IOException e) {
            System.out.println("Sandbox error: " + e.getMessage());
        }
    }

    /** Deletes the demo folder and its backup, but asks first so it can't happen by mistake. */
    private void sandboxDelete(){
        System.out.print("Delete the sandbox and its backup copy? (y/n): ");
        String answer = readLine();
        if (answer == null || !answer.trim().equalsIgnoreCase("y")) {
            System.out.println("Nothing deleted.");
            return;
        }
        try {
            SandboxTool.delete();
        } catch (IOException e) {
            System.out.println("Sandbox error: " + e.getMessage());
        }
    }

    private void showLog(){
        List<LogEntry> entries = controller.getRecentLog(10);
        System.out.println("--- Last 10 log entries ---");
        if (entries.isEmpty()) {
            System.out.println("(log is empty)");
        }
        for (LogEntry entry : entries) {
            System.out.println(entry.toLine());
        }
    }

    /** Prints the files the scanner couldn't read, if there are any. */
    private void printSkipped(){
        List<String> skipped = controller.getSkippedFiles();
        if (!skipped.isEmpty()) {
            System.out.println("Skipped " + skipped.size() + " item(s):");
            for (String s : skipped) {
                System.out.println("  - " + s);
            }
        }
    }

    /** First 8 characters of a hash, or "-" if there is none (new / missing files). */
    private static String shortHash(String hash){
        if (hash == null) {
            return "-";
        }
        return hash.length() > 8 ? hash.substring(0, 8) + "..." : hash;
    }

    /** Gives null when the input has ended instead of throwing an exception. */
    private String readLine(){
        return input.hasNextLine() ? input.nextLine() : null;
    }
}
