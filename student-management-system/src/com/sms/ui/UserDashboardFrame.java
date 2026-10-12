package com.sms.ui;

import com.sms.dao.CourseDAO;
import com.sms.dao.GradeDAO;
import com.sms.dao.StudentDAO;
import com.sms.dao.UserDAO;
import com.sms.model.Course;
import com.sms.model.Grade;
import com.sms.model.Student;
import com.sms.model.User;
import com.sms.util.CsvExporter;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public class UserDashboardFrame extends JFrame {
    private final User currentUser;
    private final StudentDAO studentDAO = new StudentDAO();
    private final CourseDAO courseDAO = new CourseDAO();
    private final GradeDAO gradeDAO = new GradeDAO();
    private final UserDAO userDAO = new UserDAO();

    private Student currentStudent;
    private List<Grade> studentGrades;

    // UI elements
    private JLabel welcomeTitle;
    private JLabel welcomeSub;
    private JLabel gpaLabel;
    private JLabel enrolledCoursesLabel;
    private JLabel totalCreditsLabel;
    private JLabel avgMarksLabel;
    private JTable gradesTable;
    private DefaultTableModel gradesModel;
    private JTable catalogTable;
    private DefaultTableModel catalogModel;
    private JPanel profileTabContainer;

    public UserDashboardFrame(User user) {
        super("Student Portal - " + user.getFullName());
        this.currentUser = user;

        loadStudentProfile();
        initComponents();
        loadStudentData();

        setSize(1100, 720);
        setMinimumSize(new Dimension(880, 580));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void loadStudentProfile() {
        if (currentUser.getStudentId() != null) {
            try {
                this.currentStudent = studentDAO.getStudentById(currentUser.getStudentId());
            } catch (SQLException e) {
                System.err.println("Failed to load student profile: " + e.getMessage());
            }
        }
        if (this.currentStudent == null) {
            this.currentStudent = new Student();
            this.currentStudent.setStudentId("STU-GUEST");
            this.currentStudent.setFirstName(currentUser.getFullName());
            this.currentStudent.setLastName("");
            this.currentStudent.setEmail(currentUser.getUsername() + "@university.edu");
            this.currentStudent.setDepartment("Computer Science");
            this.currentStudent.setYearLevel("1st Year");
            this.currentStudent.setStatus("Active");
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Top Header Banner
        JPanel headerPanel = new JPanel(new BorderLayout(15, 10));
        headerPanel.setBackground(new Color(24, 43, 73));
        headerPanel.setBorder(new EmptyBorder(18, 24, 18, 24));

        JPanel welcomeBox = new JPanel(new GridLayout(2, 1, 0, 4));
        welcomeBox.setOpaque(false);

        welcomeTitle = new JLabel("Welcome back, " + currentStudent.getFullName() + "!");
        welcomeTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        welcomeTitle.setForeground(Color.WHITE);

        welcomeSub = new JLabel(getHeaderSubtitleText());
        welcomeSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        welcomeSub.setForeground(new Color(185, 203, 230));

        welcomeBox.add(welcomeTitle);
        welcomeBox.add(welcomeSub);
        headerPanel.add(welcomeBox, BorderLayout.WEST);

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
        headerPanel.add(headerRight, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Center Content inside Tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Tab 1: Dashboard & Academic Transcript
        JPanel transcriptTab = createTranscriptTab();
        tabbedPane.addTab("My Grades & Transcript", transcriptTab);

        // Tab 2: Course Catalog
        JPanel catalogTab = createCatalogTab();
        tabbedPane.addTab("Course Catalog", catalogTab);

        // Tab 3: My Profile
        profileTabContainer = new JPanel(new BorderLayout());
        buildProfileContent();
        tabbedPane.addTab("My Profile", profileTabContainer);

        add(tabbedPane, BorderLayout.CENTER);

        // Bottom Status Bar
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(222, 226, 230)),
            new EmptyBorder(6, 15, 6, 15)
        ));
        statusBar.setBackground(new Color(248, 249, 250));

        JLabel sessionLabel = new JLabel("● Logged in as Student: " + currentUser.getFullName() + " (" + currentUser.getUsername() + ")");
        sessionLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        sessionLabel.setForeground(new Color(25, 135, 84));
        statusBar.add(sessionLabel, BorderLayout.WEST);

        JLabel portalInfo = new JLabel("Student Portal - Academic Year 2026");
        portalInfo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        portalInfo.setForeground(new Color(108, 117, 125));
        statusBar.add(portalInfo, BorderLayout.EAST);

        add(statusBar, BorderLayout.SOUTH);
    }

    private String getHeaderSubtitleText() {
        return "Roll No: " + currentStudent.getStudentId() + "  |  " +
               "Department: " + currentStudent.getDepartment() + "  |  " +
               "Year: " + currentStudent.getYearLevel() + "  |  " +
               "Status: " + currentStudent.getStatus();
    }

    private JPanel createTranscriptTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        // 1. Top KPI Row
        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 15, 15));
        kpiRow.setPreferredSize(new Dimension(800, 95));

        gpaLabel = new JLabel("0.00");
        enrolledCoursesLabel = new JLabel("0");
        totalCreditsLabel = new JLabel("0");
        avgMarksLabel = new JLabel("0.0%");

        kpiRow.add(createCard("Cumulative GPA", gpaLabel, new Color(13, 110, 253)));
        kpiRow.add(createCard("Enrolled Courses", enrolledCoursesLabel, new Color(111, 66, 193)));
        kpiRow.add(createCard("Credits Completed", totalCreditsLabel, new Color(25, 135, 84)));
        kpiRow.add(createCard("Average Score", avgMarksLabel, new Color(253, 126, 20)));

        panel.add(kpiRow, BorderLayout.NORTH);

        // 2. Center: Grades Table
        JPanel tableContainer = new JPanel(new BorderLayout(10, 10));

        JPanel tableHeader = new JPanel(new BorderLayout());
        JLabel tTitle = new JLabel("Enrolled Courses & Academic Performance");
        tTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        tableHeader.add(tTitle, BorderLayout.WEST);

        JPanel tableActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton exportBtn = new JButton("Download Transcript (CSV)");
        exportBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        exportBtn.setBackground(new Color(13, 110, 253));
        exportBtn.setForeground(Color.WHITE);
        exportBtn.addActionListener(e -> onExportTranscript());

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadStudentData());

        tableActions.add(exportBtn);
        tableActions.add(refreshBtn);
        tableHeader.add(tableActions, BorderLayout.EAST);

        tableContainer.add(tableHeader, BorderLayout.NORTH);

        String[] cols = {"Course Code", "Course Title", "Semester", "Credits", "Marks", "Letter Grade", "Grade Points", "Remarks"};
        gradesModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        gradesTable = new JTable(gradesModel);
        gradesTable.setRowHeight(28);
        gradesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        gradesTable.getColumnModel().getColumn(0).setCellRenderer(center);
        gradesTable.getColumnModel().getColumn(2).setCellRenderer(center);
        gradesTable.getColumnModel().getColumn(3).setCellRenderer(center);
        gradesTable.getColumnModel().getColumn(4).setCellRenderer(center);
        gradesTable.getColumnModel().getColumn(6).setCellRenderer(center);

        // Colored Grade Letter renderer
        gradesTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSel, boolean hasFocus, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tbl, val, isSel, hasFocus, r, c);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                if (val != null && !isSel) {
                    String g = val.toString();
                    if (g.startsWith("A")) l.setForeground(new Color(25, 135, 84));
                    else if (g.startsWith("B")) l.setForeground(new Color(13, 110, 253));
                    else if (g.startsWith("C")) l.setForeground(new Color(255, 193, 7));
                    else if (g.startsWith("D")) l.setForeground(new Color(253, 126, 20));
                    else if (g.equalsIgnoreCase("F")) l.setForeground(new Color(220, 53, 69));
                }
                return l;
            }
        });

        tableContainer.add(new JScrollPane(gradesTable), BorderLayout.CENTER);
        panel.add(tableContainer, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCatalogTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        // Filter Bar
        JPanel filterBar = new JPanel(new BorderLayout(10, 10));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        left.add(new JLabel("Search Courses:"));
        JTextField searchField = new JTextField(18);
        searchField.putClientProperty("JTextField.placeholderText", "Code, Name, Instructor...");
        left.add(searchField);

        left.add(new JLabel("Department:"));
        JComboBox<String> deptFilter = new JComboBox<>(new String[]{
            "All Departments", "Computer Science", "Electrical Engineering", 
            "Mechanical Engineering", "Civil Engineering", "Mathematics"
        });
        left.add(deptFilter);

        filterBar.add(left, BorderLayout.WEST);

        JButton refreshCatalogBtn = new JButton("Refresh Catalog");
        refreshCatalogBtn.addActionListener(e -> loadCatalog(searchField.getText().trim(), (String) deptFilter.getSelectedItem()));
        filterBar.add(refreshCatalogBtn, BorderLayout.EAST);

        panel.add(filterBar, BorderLayout.NORTH);

        // Catalog Table
        String[] cols = {"Course Code", "Course Title", "Department", "Credits", "Instructor", "Description"};
        catalogModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        catalogTable = new JTable(catalogModel);
        catalogTable.setRowHeight(28);
        catalogTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        catalogTable.getColumnModel().getColumn(0).setCellRenderer(center);
        catalogTable.getColumnModel().getColumn(3).setCellRenderer(center);

        panel.add(new JScrollPane(catalogTable), BorderLayout.CENTER);

        // Search action
        searchField.addActionListener(e -> loadCatalog(searchField.getText().trim(), (String) deptFilter.getSelectedItem()));
        deptFilter.addActionListener(e -> loadCatalog(searchField.getText().trim(), (String) deptFilter.getSelectedItem()));

        return panel;
    }

    private void buildProfileContent() {
        profileTabContainer.removeAll();

        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Profile Card
        JPanel card = new JPanel(new GridBagLayout());
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(222, 226, 230), 1, true),
            new EmptyBorder(20, 25, 20, 25)
        ));
        card.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);

        JLabel header = new JLabel("Official Student Profile Details");
        header.setFont(new Font("Segoe UI", Font.BOLD, 18));
        topHeader.add(header, BorderLayout.WEST);

        // Buttons: Edit Profile and Change Password
        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionBtns.setOpaque(false);

        JButton editProfileBtn = new JButton("Edit My Profile");
        editProfileBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        editProfileBtn.setBackground(new Color(13, 110, 253));
        editProfileBtn.setForeground(Color.WHITE);
        editProfileBtn.addActionListener(e -> onEditProfile());

        JButton changePwdBtn = new JButton("Change Password");
        changePwdBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        changePwdBtn.setBackground(new Color(108, 117, 125));
        changePwdBtn.setForeground(Color.WHITE);
        changePwdBtn.addActionListener(e -> onChangePassword());

        actionBtns.add(editProfileBtn);
        actionBtns.add(changePwdBtn);
        topHeader.add(actionBtns, BorderLayout.EAST);

        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        card.add(topHeader, gbc);

        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        addProfileRow(card, gbc, row++, "Full Name:", currentStudent.getFullName());
        addProfileRow(card, gbc, row++, "Student ID / Roll No:", currentStudent.getStudentId());
        addProfileRow(card, gbc, row++, "Email Address:", currentStudent.getEmail());
        addProfileRow(card, gbc, row++, "Phone Number:", currentStudent.getPhone() != null && !currentStudent.getPhone().isEmpty() ? currentStudent.getPhone() : "Not provided");
        addProfileRow(card, gbc, row++, "Department / Major:", currentStudent.getDepartment());
        addProfileRow(card, gbc, row++, "Academic Year Level:", currentStudent.getYearLevel());
        addProfileRow(card, gbc, row++, "Gender:", currentStudent.getGender() != null ? currentStudent.getGender() : "-");
        addProfileRow(card, gbc, row++, "Date of Birth:", currentStudent.getDateOfBirth() != null ? currentStudent.getDateOfBirth().toString() : "-");
        addProfileRow(card, gbc, row++, "Enrollment Status:", currentStudent.getStatus());

        panel.add(card, BorderLayout.NORTH);
        profileTabContainer.add(panel, BorderLayout.CENTER);
        profileTabContainer.revalidate();
        profileTabContainer.repaint();
    }

    private void addProfileRow(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0.25;
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(new Color(108, 117, 125));
        panel.add(l, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.75;
        JLabel v = new JLabel(value != null ? value : "-");
        v.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        v.setForeground(new Color(33, 37, 41));
        panel.add(v, gbc);
    }

    private void onEditProfile() {
        JTextField fNameField = new JTextField(currentStudent.getFirstName(), 15);
        JTextField lNameField = new JTextField(currentStudent.getLastName(), 15);
        JTextField emailField = new JTextField(currentStudent.getEmail(), 15);
        JTextField phoneField = new JTextField(currentStudent.getPhone() != null ? currentStudent.getPhone() : "", 15);
        JComboBox<String> genderCombo = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        genderCombo.setSelectedItem(currentStudent.getGender());
        JTextField dobField = new JTextField(currentStudent.getDateOfBirth() != null ? currentStudent.getDateOfBirth().toString() : "2004-01-01", 15);

        JPanel p = new JPanel(new GridLayout(6, 2, 8, 8));
        p.add(new JLabel("First Name *:"));
        p.add(fNameField);
        p.add(new JLabel("Last Name *:"));
        p.add(lNameField);
        p.add(new JLabel("Email Address *:"));
        p.add(emailField);
        p.add(new JLabel("Phone Number:"));
        p.add(phoneField);
        p.add(new JLabel("Gender:"));
        p.add(genderCombo);
        p.add(new JLabel("Date of Birth (YYYY-MM-DD):"));
        p.add(dobField);

        int opt = JOptionPane.showConfirmDialog(this, p, "Update My Student Profile", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opt == JOptionPane.OK_OPTION) {
            String fn = fNameField.getText().trim();
            String ln = lNameField.getText().trim();
            String em = emailField.getText().trim();
            String ph = phoneField.getText().trim();
            String gen = (String) genderCombo.getSelectedItem();
            String dobStr = dobField.getText().trim();

            if (fn.isEmpty() || ln.isEmpty() || em.isEmpty()) {
                JOptionPane.showMessageDialog(this, "First Name, Last Name, and Email are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!em.contains("@") || !em.contains(".")) {
                JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Date dob = null;
            if (!dobStr.isEmpty()) {
                try {
                    dob = Date.valueOf(dobStr);
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid Date of Birth format. Please use YYYY-MM-DD.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            try {
                if (studentDAO.isEmailExists(em, currentStudent.getId())) {
                    JOptionPane.showMessageDialog(this, "Email address is already in use by another student.", "Duplicate Email", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                currentStudent.setFirstName(fn);
                currentStudent.setLastName(ln);
                currentStudent.setEmail(em);
                currentStudent.setPhone(ph);
                currentStudent.setGender(gen);
                currentStudent.setDateOfBirth(dob);

                boolean updated = studentDAO.updateStudent(currentStudent);
                if (updated) {
                    currentUser.setFullName(currentStudent.getFullName());
                    userDAO.updateUserFullName(currentUser.getId(), currentStudent.getFullName());

                    // Refresh Header & Profile
                    welcomeTitle.setText("Welcome back, " + currentStudent.getFullName() + "!");
                    welcomeSub.setText(getHeaderSubtitleText());
                    buildProfileContent();

                    JOptionPane.showMessageDialog(this, "Your student profile has been updated successfully!", "Profile Updated", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update profile.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onChangePassword() {
        JPasswordField curPwdField = new JPasswordField(15);
        JPasswordField newPwdField = new JPasswordField(15);
        JPasswordField confirmPwdField = new JPasswordField(15);

        JPanel p = new JPanel(new GridLayout(3, 2, 8, 8));
        p.add(new JLabel("Current Password:"));
        p.add(curPwdField);
        p.add(new JLabel("New Password (min 4 chars):"));
        p.add(newPwdField);
        p.add(new JLabel("Confirm New Password:"));
        p.add(confirmPwdField);

        int opt = JOptionPane.showConfirmDialog(this, p, "Change Account Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opt == JOptionPane.OK_OPTION) {
            String curPwd = new String(curPwdField.getPassword()).trim();
            String newPwd = new String(newPwdField.getPassword()).trim();
            String confPwd = new String(confirmPwdField.getPassword()).trim();

            if (!curPwd.equals(currentUser.getPassword())) {
                JOptionPane.showMessageDialog(this, "Current password is incorrect.", "Authentication Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (newPwd.length() < 4) {
                JOptionPane.showMessageDialog(this, "New password must be at least 4 characters long.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!newPwd.equals(confPwd)) {
                JOptionPane.showMessageDialog(this, "New passwords do not match.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                boolean ok = userDAO.updateUserPassword(currentUser.getId(), newPwd);
                if (ok) {
                    currentUser.setPassword(newPwd);
                    JOptionPane.showMessageDialog(this, "Password changed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to change password.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(222, 226, 230), 1, true),
            new EmptyBorder(12, 14, 12, 14)
        ));
        card.setBackground(Color.WHITE);

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setForeground(new Color(108, 117, 125));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(accentColor);

        JPanel stripe = new JPanel();
        stripe.setPreferredSize(new Dimension(10, 4));
        stripe.setBackground(accentColor);

        card.add(stripe, BorderLayout.NORTH);
        card.add(t, BorderLayout.CENTER);
        card.add(valueLabel, BorderLayout.SOUTH);
        return card;
    }

    private void loadStudentData() {
        if (currentStudent == null || currentStudent.getId() <= 0) return;

        try {
            studentGrades = gradeDAO.getGradesByStudentId(currentStudent.getId());
            gradesModel.setRowCount(0);

            double totalGradePointsTimesCredits = 0.0;
            int totalCredits = 0;
            double sumMarks = 0.0;

            for (Grade g : studentGrades) {
                double gp = Grade.computeGradePoint(g.getGrade());
                int credits = g.getCredits() > 0 ? g.getCredits() : 3;

                totalGradePointsTimesCredits += (gp * credits);
                totalCredits += credits;
                sumMarks += g.getMarks();

                gradesModel.addRow(new Object[]{
                    g.getCourseCode(),
                    g.getCourseName(),
                    g.getSemester(),
                    credits,
                    String.format("%.2f", g.getMarks()),
                    g.getGrade(),
                    String.format("%.1f", gp),
                    g.getRemarks() != null ? g.getRemarks() : "-"
                });
            }

            int count = studentGrades.size();
            enrolledCoursesLabel.setText(count + (count == 1 ? " Course" : " Courses"));
            totalCreditsLabel.setText(totalCredits + " Credits");

            if (totalCredits > 0) {
                double gpa = totalGradePointsTimesCredits / totalCredits;
                gpaLabel.setText(String.format("%.2f", gpa));
            } else {
                gpaLabel.setText("N/A");
            }

            if (count > 0) {
                avgMarksLabel.setText(String.format("%.1f%%", sumMarks / count));
            } else {
                avgMarksLabel.setText("0.0%");
            }

            loadCatalog("", "All Departments");

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load grades: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCatalog(String query, String dept) {
        try {
            List<Course> courses = courseDAO.searchCourses(query, dept);
            catalogModel.setRowCount(0);
            for (Course c : courses) {
                catalogModel.addRow(new Object[]{
                    c.getCourseCode(),
                    c.getCourseName(),
                    c.getDepartment(),
                    c.getCredits(),
                    c.getInstructor() != null ? c.getInstructor() : "-",
                    c.getDescription() != null ? c.getDescription() : "-"
                });
            }
        } catch (SQLException e) {
            System.err.println("Error loading courses: " + e.getMessage());
        }
    }

    private void onExportTranscript() {
        if (gradesModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No grades found to export.", "Transcript", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Transcript to CSV");
        fileChooser.setSelectedFile(new File(currentStudent.getStudentId() + "_transcript.csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fileChooser.getSelectedFile();
            if (!f.getName().toLowerCase().endsWith(".csv")) {
                f = new File(f.getAbsolutePath() + ".csv");
            }
            try {
                CsvExporter.exportTableToCsv(gradesTable, f);
                JOptionPane.showMessageDialog(this, "Transcript saved successfully to:\n" + f.getAbsolutePath(), "Export Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to export: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onLogout() {
        int opt = JOptionPane.showConfirmDialog(this, "Do you want to log out and switch user?", "Logout", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            dispose();
            LoginDialog.showLogin();
        }
    }
}
