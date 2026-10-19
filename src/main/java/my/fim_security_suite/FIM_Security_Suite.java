package my.fim_security_suite;

import my.fim_security_suite.controller.FIMController;

public class FIM_Security_Suite {

    public static void main(String[] args) {
        FIMController controller = new FIMController();
        CliMenu menu = new CliMenu(controller);
        menu.run();
    }
}
