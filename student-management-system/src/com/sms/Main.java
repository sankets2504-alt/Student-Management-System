package com.sms;

import com.formdev.flatlaf.FlatLightLaf;
import com.sms.database.DatabaseManager;
import com.sms.ui.DatabaseSettingsDialog;
import com.sms.ui.LoginDialog;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Set modern FlatLaf Look and Feel
        try {
            FlatLightLaf.setup();
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("ProgressBar.arc", 6);
            UIManager.put("TextComponent.arc", 6);
        } catch (Exception e) {
            System.err.println("Could not apply FlatLaf theme, using system default: " + e.getMessage());
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }

        SwingUtilities.invokeLater(() -> {
            boolean connected = false;

            // Attempt connection with saved credentials
            try {
                DatabaseManager.getInstance().initializeDatabase();
                connected = true;
            } catch (Exception ex) {
                System.out.println("Initial connection failed, opening setup dialog: " + ex.getMessage());
            }

            if (!connected) {
                DatabaseSettingsDialog dialog = new DatabaseSettingsDialog(null);
                dialog.setVisible(true);
                connected = dialog.isConnectedSuccessfully();
            }

            if (connected) {
                // Show role-based Login Dialog (Admin vs Student / User)
                LoginDialog.showLogin();
            } else {
                int choice = JOptionPane.showConfirmDialog(
                    null, 
                    "Database connection was not established.\nWould you like to exit the application?", 
                    "Exit Application", 
                    JOptionPane.YES_NO_OPTION, 
                    JOptionPane.WARNING_MESSAGE
                );
                if (choice == JOptionPane.YES_OPTION) {
                    System.exit(0);
                } else {
                    main(args);
                }
            }
        });
    }
}
