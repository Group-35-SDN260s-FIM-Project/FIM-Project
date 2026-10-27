/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.fim_security_suite;

public class FIM_Security_Suite {

    public static void main(String[] args) {
        // Check-In 2: keep the original GUI as default; --cli opens the console.
        if (args != null && java.util.Arrays.asList(args).contains("--cli")) {
            my.fim_security_suite.FIM_Security_Suite.main(args);
            return;
        }
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    // This explicitly creates and launches your dashboard window canvas
                    // Create and display the main dashboard window
                    MainDashBoard dashboard = new MainDashBoard();
                    dashboard.setLocationRelativeTo(null); // Centers the app on your screen
                    dashboard.setVisible(true);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
