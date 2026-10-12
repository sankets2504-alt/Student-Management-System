package com.sms.ui;

import com.sms.database.DatabaseConfig;
import com.sms.database.DatabaseManager;

import javax.swing.*;
import java.awt.*;

public class DatabaseSettingsDialog extends JDialog {
    private JTextField hostField;
    private JTextField portField;
    private JTextField dbNameField;
    private JTextField userField;
    private JPasswordField passwordField;
    private JLabel statusLabel;
    private boolean connectedSuccessfully = false;

    public DatabaseSettingsDialog(Frame parent) {
        super(parent, "MySQL Database Connection Settings", true);
        initComponents();
        loadCurrentConfig();
        setSize(480, 420);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout(10, 5));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        headerPanel.setBackground(new Color(245, 247, 250));

        JLabel titleLabel = new JLabel("Database Configuration");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(33, 37, 41));

        JLabel subLabel = new JLabel("Enter your MySQL database connection credentials below.");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(new Color(108, 117, 125));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        hostField = new JTextField("localhost", 18);
        portField = new JTextField("3306", 18);
        dbNameField = new JTextField("student_management_db", 18);
        userField = new JTextField("root", 18);
        passwordField = new JPasswordField(18);

        addFormField(formPanel, gbc, 0, "Host / Server:", hostField);
        addFormField(formPanel, gbc, 1, "Port:", portField);
        addFormField(formPanel, gbc, 2, "Database Name:", dbNameField);
        addFormField(formPanel, gbc, 3, "Username:", userField);
        addFormField(formPanel, gbc, 4, "Password:", passwordField);

        // Status Label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        formPanel.add(statusLabel, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Bottom Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 15, 15));

        JButton testBtn = new JButton("Test Connection");
        JButton saveBtn = new JButton("Save & Connect");
        JButton cancelBtn = new JButton("Cancel");

        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        saveBtn.setBackground(new Color(13, 110, 253));
        saveBtn.setForeground(Color.WHITE);

        testBtn.addActionListener(e -> onTestConnection());
        saveBtn.addActionListener(e -> onSaveAndConnect());
        cancelBtn.addActionListener(e -> dispose());

        buttonPanel.add(testBtn);
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridy = row;
        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.weightx = 0.3;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(field, gbc);
    }

    private void loadCurrentConfig() {
        DatabaseConfig config = DatabaseConfig.getInstance();
        hostField.setText(config.getHost());
        portField.setText(String.valueOf(config.getPort()));
        dbNameField.setText(config.getDbName());
        userField.setText(config.getUser());
        passwordField.setText(config.getPassword());
    }

    private void onTestConnection() {
        String host = hostField.getText().trim();
        String portStr = portField.getText().trim();
        String user = userField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (host.isEmpty() || portStr.isEmpty() || user.isEmpty()) {
            setStatus("Please fill in host, port, and username.", false);
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException ex) {
            setStatus("Invalid port number.", false);
            return;
        }

        setStatus("Testing connection...", true);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingUtilities.invokeLater(() -> {
            try {
                boolean valid = DatabaseManager.getInstance().testConnection(host, port, user, password);
                if (valid) {
                    setStatus("Connection successful!", true);
                } else {
                    setStatus("Connection failed. Check host, port, or password.", false);
                }
            } catch (Exception ex) {
                setStatus("Connection error: " + ex.getMessage(), false);
            } finally {
                setCursor(Cursor.getDefaultCursor());
            }
        });
    }

    private void onSaveAndConnect() {
        String host = hostField.getText().trim();
        String portStr = portField.getText().trim();
        String dbName = dbNameField.getText().trim();
        String user = userField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (host.isEmpty() || portStr.isEmpty() || dbName.isEmpty() || user.isEmpty()) {
            setStatus("Please fill in all configuration fields.", false);
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException ex) {
            setStatus("Invalid port number.", false);
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setStatus("Connecting and initializing schema...", true);

        SwingUtilities.invokeLater(() -> {
            try {
                DatabaseConfig config = DatabaseConfig.getInstance();
                config.setHost(host);
                config.setPort(port);
                config.setDbName(dbName);
                config.setUser(user);
                config.setPassword(password);

                // Initialize Database
                DatabaseManager.getInstance().initializeDatabase();
                config.save();

                connectedSuccessfully = true;
                JOptionPane.showMessageDialog(this, 
                    "Successfully connected to MySQL database: " + dbName, 
                    "Database Connected", 
                    JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (Exception ex) {
                setStatus("Setup failed: " + ex.getMessage(), false);
                JOptionPane.showMessageDialog(this, 
                    "Failed to initialize database:\n" + ex.getMessage(), 
                    "Connection Error", 
                    JOptionPane.ERROR_MESSAGE);
            } finally {
                setCursor(Cursor.getDefaultCursor());
            }
        });
    }

    private void setStatus(String message, boolean success) {
        statusLabel.setText(message);
        statusLabel.setForeground(success ? new Color(25, 135, 84) : new Color(220, 53, 69));
    }

    public boolean isConnectedSuccessfully() {
        return connectedSuccessfully;
    }
}
