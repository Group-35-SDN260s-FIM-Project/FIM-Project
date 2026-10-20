package my.fim_security_suite.cli;

import my_fim_securiy_suite.mode.ScanResult;
import controller.FIMController;
import java.util.Scanner;

/**
 *
 * @author Rebecca Mandlazi
 */
public class CliMenu {
    private FIMController  controller;
    private ScanResult scanResult;
    private Scanner scanner;
    
    public CliMenu(FIMController controller){
        this.controller = controller;
        this.scanner = new Scanner(System.in);
    }
    public void start() {
        boolean running = true;
        while (running) { 
            showMenu();
            break;
        }
    }
    public void display(){
        Ststem.out.println("Displaying sytem...");
    }
                   
            
    public void showMenu(){
        System.out.println("===FIM Security Suite ===");
        System.out.println("1. Create Baseline");
        System.out.println("2. Run Integrity Check");
        System.out.println("3. Exit");
    }
    public void handleInput(){
        
    }
}
