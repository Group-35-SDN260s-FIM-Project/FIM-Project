package my.fim_security_suite;

import my.fim_security_suite.cli.CliMenu;
import my.fim_security_suite.controller.FIMController;

/**
 * Entry point of the File Integrity Monitor.
 *
 * For Check-In 2 this starts the console menu. Later the Swing GUI
 * (MainDashBoard etc.) will be started from here instead.
 */
public class FIM_Security_Suite {

    public static void main(String[] args) {
        FIMController controller = new FIMController();
        new CliMenu(controller).run();
    }
}
