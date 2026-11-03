package my.fim_security_suite;

import my.fim_security_suite.cli.CliMenu;
import my.fim_security_suite.controller.FIMController;

/**
 * Where the File Integrity Monitor starts.
 *
 * It opens the Swing window. If you run it with --cli you get the console
 * menu instead. Both of them use the same FIMController.
 */
public class FIM_Security_Suite {

    public static void main(String[] args) {
        FIMController controller = new FIMController();
        if (args.length > 0 && args[0].equals("--cli")) {
            new CliMenu(controller).run();
        } else {
            // Swing windows must be made on the event thread
            java.awt.EventQueue.invokeLater(
                    () -> new my.fim_security_suite.ui.MainDashBoard(controller).setVisible(true));
        }
    }
}
