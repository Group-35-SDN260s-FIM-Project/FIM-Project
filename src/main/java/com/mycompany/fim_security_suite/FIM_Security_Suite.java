/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.fim_security_suite;

public class FIM_Security_Suite {

    public static void main(String[] args) {
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
