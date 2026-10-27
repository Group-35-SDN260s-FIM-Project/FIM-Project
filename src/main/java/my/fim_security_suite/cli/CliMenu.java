package my.fim_security_suite.cli;

import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;
import my.fim_security_suite.controller.FIMController;
import my.fim_security_suite.model.Baseline;
import my.fim_security_suite.model.FileChange;
import my.fim_security_suite.model.FileStatus;
import my.fim_security_suite.model.LogEntry;
import my.fim_security_suite.model.ScanResult;

/**
 * Console menu for the Check-In 2 prototype.
 * It only reads input and prints results, all the actual work is done
 * by FIMController.
 *
 * @author Rebecca Mandlazi
 */
public class CliMenu {
    private final FIMController controller;
    private final Scanner input;

    public CliMenu(FIMController controller){
        this.controller = controller;
        this.input = new Scanner(System.in);
    }

    /** Shows the menu over and over until the user picks 0. */
    public void run() {
        System.out.println("=== FIM Security Suite (console version) ===");
        boolean running = true;
        while (running) {
            printMenu();
            String choice = readLine();
            if (choice == null) {
                break;   // input was closed
            }
            // errors from the controller (wrong folder, no baseline...) are shown
            // to the user instead of crashing the program
            try {
                switch (choice.trim()) {
                    case "1": chooseFolder(); break;
                    case "2": createBaseline(); break;
                    case "3": runCheck(); break;
                    case "4": showLog(); break;
                    case "5": openDashboard(); break;
                    case "0": running = false; break;
                    default: System.out.println("Please choose 0 - 5.");
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
        // people often paste the path with quotes from Windows Explorer
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
                    continue;   // only list the files that actually changed
                }
                System.out.printf("  [%-8s] %s   (old: %s  new: %s)%n",
                        change.getStatus(), change.getPath(),
                        shortHash(change.getOldHash()), shortHash(change.getNewHash()));
            }
        }
        printSkipped();
    }

    /** Opens the Swing dashboard on the same controller, so both views share state. */
    private void openDashboard(){
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            System.out.println("No display available for the GUI.");
            return;
        }
        java.awt.EventQueue.invokeLater(() -> {
            com.mycompany.fim_security_suite.MainDashBoard dashboard =
                    new com.mycompany.fim_security_suite.MainDashBoard(controller);
            dashboard.setLocationRelativeTo(null);
            dashboard.setVisible(true);
        });
        System.out.println("Dashboard opened.");
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

    /** Lists files the scanner couldn't read, if there were any. */
    private void printSkipped(){
        List<String> skipped = controller.getSkippedFiles();
        if (!skipped.isEmpty()) {
            System.out.println("Skipped " + skipped.size() + " item(s):");
            for (String s : skipped) {
                System.out.println("  - " + s);
            }
        }
    }

    /** First 8 characters of a hash, or "-" when there isn't one (new / missing files). */
    private static String shortHash(String hash){
        if (hash == null) {
            return "-";
        }
        return hash.length() > 8 ? hash.substring(0, 8) + "..." : hash;
    }

    /** Returns null if there is no more input instead of throwing an exception. */
    private String readLine(){
        return input.hasNextLine() ? input.nextLine() : null;
    }
}
