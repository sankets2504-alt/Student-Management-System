package com.sms.ui;

import com.sms.database.DatabaseConfig;
import com.sms.database.DatabaseManager;
import com.sms.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainFrame extends JFrame {
    private final User currentUser;
    private JTabbedPane tabbedPane;
    private DashboardPanel dashboardPanel;
    private StudentPanel studentPanel;
    private CoursePanel coursePanel;
    private GradePanel gradePanel;
    private UserAccountsPanel userAccountsPanel;
    private JLabel statusDbLabel;

    public MainFrame() {
        this(new User(1, "admin", "admin123", "ADMIN", null, "System Administrator", null));
    }

    public MainFrame(User user) {
        super("Administrator Portal - Student Management System");
        this.currentUser = user != null ? user : new User(1, "admin", "admin123", "ADMIN", null, "System Administrator", null);

        initComponents();
        setSize(1150, 740);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Setup Menu Bar
        setupMenuBar();

        // Top Admin Header Bar
        JPanel topHeader = new JPanel(new BorderLayout(15, 10));
        topHeader.setBackground(new Color(24, 43, 73));
        topHeader.setBorder(new EmptyBorder(12, 20, 12, 20));

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBox.setOpaque(false);

        JLabel titleLbl = new JLabel("Administrator Dashboard");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 19));
        titleLbl.setForeground(Color.WHITE);

        JLabel subLbl = new JLabel("Logged in as: " + currentUser.getFullName() + " (" + currentUser.getUsername() + ")  •  Full System Control");
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLbl.setForeground(new Color(185, 203, 230));

        titleBox.add(titleLbl);
        titleBox.add(subLbl);
        topHeader.add(titleBox, BorderLayout.WEST);

        // Header Right: Logout button
        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        headerRight.setOpaque(false);

        JButton logoutBtn = new JButton("Switch User / Logout");
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoutBtn.setBackground(new Color(220, 53, 69));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.addActionListener(e -> onLogout());

        headerRight.add(logoutBtn);
        topHeader.add(headerRight, BorderLayout.EAST);

        add(topHeader, BorderLayout.NORTH);

        // Setup Tabbed Pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        dashboardPanel = new DashboardPanel(this);
        studentPanel = new StudentPanel(this);
        coursePanel = new CoursePanel(this);
        gradePanel = new GradePanel(this);
        userAccountsPanel = new UserAccountsPanel(this);

        tabbedPane.addTab("Admin Dashboard", dashboardPanel);
        tabbedPane.addTab("Student Directory", studentPanel);
        tabbedPane.addTab("Course Catalog", coursePanel);
        tabbedPane.addTab("Grades & Records", gradePanel);
        tabbedPane.addTab("User Accounts", userAccountsPanel);

        tabbedPane.addChangeListener(e -> {
            int selected = tabbedPane.getSelectedIndex();
            switch (selected) {
                case 0 -> dashboardPanel.loadDashboardData();
                case 1 -> studentPanel.loadStudents();
                case 2 -> coursePanel.loadCourses();
                case 3 -> gradePanel.loadGrades();
                case 4 -> userAccountsPanel.loadUsers();
            }
        });

        add(tabbedPane, BorderLayout.CENTER);

        // Bottom Status Bar
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(222, 226, 230)),
            BorderFactory.createEmptyBorder(6, 15, 6, 15)
        ));
        statusBar.setBackground(new Color(248, 249, 250));

        statusDbLabel = new JLabel();
        updateDbStatusText();
        statusBar.add(statusDbLabel, BorderLayout.WEST);

        JLabel appInfo = new JLabel("Java Swing Student Management System - Admin Mode");
        appInfo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        appInfo.setForeground(new Color(108, 117, 125));
        statusBar.add(appInfo, BorderLayout.EAST);

        add(statusBar, BorderLayout.SOUTH);
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File Menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem refreshItem = new JMenuItem("Refresh All Data");
        refreshItem.addActionListener(e -> reloadAllPanels());
        JMenuItem logoutItem = new JMenuItem("Log Out / Switch User");
        logoutItem.addActionListener(e -> onLogout());
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(refreshItem);
        fileMenu.add(logoutItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        // Database Menu
        JMenu dbMenu = new JMenu("Database");
        JMenuItem dbSettingsItem = new JMenuItem("Connection Settings...");
        dbSettingsItem.addActionListener(e -> onOpenDbSettings());

        JMenuItem testConnItem = new JMenuItem("Test Connection");
        testConnItem.addActionListener(e -> {
            try {
                DatabaseConfig cfg = DatabaseConfig.getInstance();
                boolean ok = DatabaseManager.getInstance().testConnection(cfg.getHost(), cfg.getPort(), cfg.getUser(), cfg.getPassword());
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Connection to MySQL Server is active and healthy!", "Connection OK", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Connection test failed.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Connection error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JMenuItem reseedItem = new JMenuItem("Re-initialize Database Tables");
        reseedItem.addActionListener(e -> {
            int opt = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to verify or re-initialize tables in the database?", 
                "Database Initialization", 
                JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                try {
                    DatabaseManager.getInstance().initializeDatabase();
                    reloadAllPanels();
                    JOptionPane.showMessageDialog(this, "Database schema verified and initialized.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error initializing database: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        dbMenu.add(dbSettingsItem);
        dbMenu.add(testConnItem);
        dbMenu.addSeparator();
        dbMenu.add(reseedItem);
        menuBar.add(dbMenu);

        // Help Menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                "Student Management System (SMS) - Administrator Portal\n\n" +
                "Features:\n" +
                "• Complete Student Directory & Profile Management\n" +
                "• Course Catalog Management\n" +
                "• Enrollment & Letter Grade Recording with GPA\n" +
                "• User Accounts Management & Roles\n" +
                "• Multi-Filter Search & Live Sorting\n" +
                "• CSV Export for Records\n" +
                "• Full Visual Analytics Dashboard\n",
                "About Admin Portal",
                JOptionPane.INFORMATION_MESSAGE);
        });
        helpMenu.add(aboutItem);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    public void onOpenDbSettings() {
        DatabaseSettingsDialog dlg = new DatabaseSettingsDialog(this);
        dlg.setVisible(true);
        if (dlg.isConnectedSuccessfully()) {
            updateDbStatusText();
            reloadAllPanels();
        }
    }

    public void updateDbStatusText() {
        DatabaseConfig config = DatabaseConfig.getInstance();
        statusDbLabel.setText("● Connected to MySQL (" + config.getHost() + ":" + config.getPort() + " / " + config.getDbName() + ")");
        statusDbLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        statusDbLabel.setForeground(new Color(25, 135, 84));
    }

    public void reloadAllPanels() {
        dashboardPanel.loadDashboardData();
        studentPanel.loadStudents();
        coursePanel.loadCourses();
        gradePanel.loadGrades();
        userAccountsPanel.loadUsers();
    }

    private void onLogout() {
        int opt = JOptionPane.showConfirmDialog(this, "Do you want to log out of the Administrator Portal?", "Logout", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            dispose();
            LoginDialog.showLogin();
        }
    }
}
