package my.fim_security_suite;

import my.fim_security_suite.cli.CliMenu;
import my.fim_security_suite.controller.FIMController;

/**
 * Entry point of the File Integrity Monitor.
 *
 * Starts the Swing window. Run it with the argument --cli to get the
 * console menu instead. Both use the same FIMController.
 */
public class FIM_Security_Suite {

    public static void main(String[] args) {
        FIMController controller = new FIMController();
        if (args.length > 0 && args[0].equals("--cli")) {
            new CliMenu(controller).run();
        } else {
            // Swing windows should be created on the event thread
            java.awt.EventQueue.invokeLater(
                    () -> new my.fim_security_suite.ui.MainDashBoard(controller).setVisible(true));
        }
    }
}
