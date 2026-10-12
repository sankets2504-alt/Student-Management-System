package com.sms.ui;

import com.sms.dao.UserDAO;
import com.sms.model.User;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.SQLException;

public class LoginDialog extends JDialog {
    private final UserDAO userDAO = new UserDAO();

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel errorLabel;
    private User authenticatedUser;

    public LoginDialog(Frame parent) {
        super(parent, "Student Management System - Sign In", true);
        initComponents();
        setSize(460, 520);
        setResizable(false);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        mainPanel.setBackground(Color.WHITE);

        // Header Title
        JLabel appTitle = new JLabel("Student Management System");
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        appTitle.setForeground(new Color(24, 43, 73));
        appTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subTitle = new JLabel("Select role or enter credentials to sign in");
        subTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subTitle.setForeground(new Color(108, 117, 125));
        subTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(appTitle);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        mainPanel.add(subTitle);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Inputs Card
        JPanel card = new JPanel(new GridLayout(4, 1, 6, 6));
        card.setOpaque(false);
        card.setMaximumSize(new Dimension(400, 160));

        JLabel uLabel = new JLabel("Username:");
        uLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        usernameField = new JTextField(15);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JLabel pLabel = new JLabel("Password:");
        pLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        card.add(uLabel);
        card.add(usernameField);
        card.add(pLabel);
        card.add(passwordField);

        mainPanel.add(card);

        // Error Label
        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorLabel.setForeground(new Color(220, 53, 69));
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        mainPanel.add(errorLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        // Login Button
        JButton loginBtn = new JButton("Sign In");
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        loginBtn.setBackground(new Color(13, 110, 253));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setMaximumSize(new Dimension(400, 38));
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.setFocusPainted(false);
        loginBtn.addActionListener(e -> onLogin());

        // Press Enter to login
        passwordField.addActionListener(e -> onLogin());
        usernameField.addActionListener(e -> onLogin());

        mainPanel.add(loginBtn);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        // New Student Self-Registration Button
        JButton registerBtn = new JButton("New Student? Create Account");
        registerBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        registerBtn.setForeground(new Color(25, 135, 84));
        registerBtn.setBackground(new Color(235, 247, 238));
        registerBtn.setMaximumSize(new Dimension(400, 34));
        registerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerBtn.setFocusPainted(false);
        registerBtn.addActionListener(e -> onOpenRegistration());

        mainPanel.add(registerBtn);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 16)));

        // Quick One-Click Demo Logins Box
        JPanel quickBox = new JPanel();
        quickBox.setLayout(new BoxLayout(quickBox, BoxLayout.Y_AXIS));
        quickBox.setBorder(new CompoundBorder(
            new LineBorder(new Color(222, 226, 230), 1, true),
            new EmptyBorder(10, 12, 10, 12)
        ));
        quickBox.setBackground(new Color(248, 249, 250));
        quickBox.setMaximumSize(new Dimension(400, 120));

        JLabel quickTitle = new JLabel("Quick Demo Access:");
        quickTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        quickTitle.setForeground(new Color(108, 117, 125));
        quickBox.add(quickTitle);
        quickBox.add(Box.createRigidArea(new Dimension(0, 8)));

        JPanel btnRow = new JPanel(new GridLayout(1, 2, 8, 8));
        btnRow.setOpaque(false);

        JButton adminQuickBtn = new JButton("Admin Portal");
        adminQuickBtn.setToolTipText("Login as admin / admin123");
        adminQuickBtn.addActionListener(e -> quickLogin("admin", "admin123"));

        JButton studentQuickBtn = new JButton("Student Portal");
        studentQuickBtn.setToolTipText("Login as student / student123");
        studentQuickBtn.addActionListener(e -> quickLogin("student", "student123"));

        btnRow.add(adminQuickBtn);
        btnRow.add(studentQuickBtn);
        quickBox.add(btnRow);

        mainPanel.add(quickBox);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Bottom link for Database Settings
        JButton dbSettingsBtn = new JButton("Configure MySQL Database...");
        dbSettingsBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        dbSettingsBtn.setForeground(new Color(108, 117, 125));
        dbSettingsBtn.setBorderPainted(false);
        dbSettingsBtn.setContentAreaFilled(false);
        dbSettingsBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        dbSettingsBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        dbSettingsBtn.addActionListener(e -> {
            DatabaseSettingsDialog dlg = new DatabaseSettingsDialog(null);
            dlg.setVisible(true);
        });

        mainPanel.add(dbSettingsBtn);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void onOpenRegistration() {
        StudentRegistrationDialog regDialog = new StudentRegistrationDialog(null);
        regDialog.setVisible(true);
        if (regDialog.isRegistrationSuccessful()) {
            usernameField.setText(regDialog.getRegisteredUsername());
            passwordField.setText(regDialog.getRegisteredPassword());
            errorLabel.setText("Account created! Click Sign In to enter.");
            errorLabel.setForeground(new Color(25, 135, 84));
        }
    }

    private void quickLogin(String u, String p) {
        usernameField.setText(u);
        passwordField.setText(p);
        onLogin();
    }

    private void onLogin() {
        String u = usernameField.getText().trim();
        String p = new String(passwordField.getPassword()).trim();

        if (u.isEmpty() || p.isEmpty()) {
            errorLabel.setText("Please enter username and password.");
            return;
        }

        try {
            User user = userDAO.authenticate(u, p);
            if (user != null) {
                this.authenticatedUser = user;
                dispose();
            } else {
                errorLabel.setText("Invalid username or password.");
            }
        } catch (SQLException ex) {
            errorLabel.setText("Database error: " + ex.getMessage());
            int opt = JOptionPane.showConfirmDialog(this, 
                "Could not connect to database:\n" + ex.getMessage() + "\n\nWould you like to open Database Settings?", 
                "Connection Issue", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.ERROR_MESSAGE);
            if (opt == JOptionPane.YES_OPTION) {
                DatabaseSettingsDialog dlg = new DatabaseSettingsDialog(null);
                dlg.setVisible(true);
            }
        }
    }

    public static void showLogin() {
        SwingUtilities.invokeLater(() -> {
            LoginDialog dialog = new LoginDialog(null);
            dialog.setVisible(true);

            User user = dialog.getAuthenticatedUser();
            if (user != null) {
                if (user.isAdmin()) {
                    MainFrame adminFrame = new MainFrame(user);
                    adminFrame.setVisible(true);
                } else {
                    UserDashboardFrame studentFrame = new UserDashboardFrame(user);
                    studentFrame.setVisible(true);
                }
            } else {
                System.exit(0);
            }
        });
    }

    public User getAuthenticatedUser() {
        return authenticatedUser;
    }
}
