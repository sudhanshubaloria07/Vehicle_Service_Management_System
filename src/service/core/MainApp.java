package service.core;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import service.gui.MainFrame;

/**
 * Main application entry point for the Vehicle Service Management System (VSMS).
 * 
 * OOP CONCEPT: Class & Object (Unit 1)
 * OOP CONCEPT: Multithreading & Event Handling (Unit 3) - SwingUtilities.invokeLater()
 */
public class MainApp {

    public static void main(String[] args) {
        // Set Look and Feel to Nimbus or System default with fallback
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }

        // Launch GUI safely on the Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
