package com.sms.ui;

import com.sms.dao.StudentDAO;
import com.sms.dao.UserDAO;
import com.sms.model.Student;
import com.sms.model.User;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.Date;
import java.sql.SQLException;

public class StudentRegistrationDialog extends JDialog {
    private final StudentDAO studentDAO = new StudentDAO();
    private final UserDAO userDAO = new UserDAO();

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;

    private JTextField studentIdField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JComboBox<String> genderCombo;
    private JTextField dobField;
    private JComboBox<String> deptCombo;
    private JComboBox<String> yearCombo;

    private boolean registrationSuccessful = false;
    private String registeredUsername = "";
    private String registeredPassword = "";

    public StudentRegistrationDialog(Frame parent) {
        super(parent, "New Student Self-Registration", true);
        initComponents();
        setSize(540, 680);
        setMinimumSize(new Dimension(500, 620));
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout(10, 4));
        headerPanel.setBackground(new Color(24, 43, 73));
        headerPanel.setBorder(new EmptyBorder(16, 22, 16, 22));

        JLabel titleLabel = new JLabel("Create Student Account");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 19));
        titleLabel.setForeground(Color.WHITE);

        JLabel subLabel = new JLabel("Register your student profile and set up your student login credentials");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(new Color(185, 203, 230));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Content form inside scroll pane
        JPanel formContainer = new JPanel();
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setBorder(new EmptyBorder(15, 20, 15, 20));

        // 1. Account Credentials Box
        JPanel credBox = createSectionBox("1. Login Credentials");
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);
        confirmPasswordField = new JPasswordField(15);

        addFormField(credBox, gbc, 0, "Choose Username *:", usernameField);
        addFormField(credBox, gbc, 1, "Password *:", passwordField);
        addFormField(credBox, gbc, 2, "Confirm Password *:", confirmPasswordField);

        formContainer.add(credBox);
        formContainer.add(Box.createRigidArea(new Dimension(0, 12)));

        // 2. Student Profile Details Box
        JPanel profileBox = createSectionBox("2. Student Profile Information");
        studentIdField = new JTextField(15);
        firstNameField = new JTextField(15);
        lastNameField = new JTextField(15);
        emailField = new JTextField(15);
        phoneField = new JTextField(15);
        genderCombo = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        dobField = new JTextField("2004-01-01", 15);
        deptCombo = new JComboBox<>(new String[]{
            "Computer Science", "Information Technology", "Electrical Engineering", 
            "Mechanical Engineering", "Civil Engineering", "Business Administration", "Mathematics"
        });
        deptCombo.setEditable(true);
        yearCombo = new JComboBox<>(new String[]{"1st Year", "2nd Year", "3rd Year", "4th Year", "Postgraduate"});

        int r = 0;
        addFormField(profileBox, gbc, r++, "Student ID / Roll No *:", studentIdField);
        addFormField(profileBox, gbc, r++, "First Name *:", firstNameField);
        addFormField(profileBox, gbc, r++, "Last Name *:", lastNameField);
        addFormField(profileBox, gbc, r++, "Email Address *:", emailField);
        addFormField(profileBox, gbc, r++, "Phone Number:", phoneField);
        addFormField(profileBox, gbc, r++, "Gender:", genderCombo);
        addFormField(profileBox, gbc, r++, "Date of Birth (YYYY-MM-DD):", dobField);
        addFormField(profileBox, gbc, r++, "Department / Major *:", deptCombo);
        addFormField(profileBox, gbc, r++, "Year Level *:", yearCombo);

        formContainer.add(profileBox);

        add(new JScrollPane(formContainer), BorderLayout.CENTER);

        // Buttons Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        bottomBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(222, 226, 230)));

        JButton registerBtn = new JButton("Complete Registration");
        registerBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        registerBtn.setBackground(new Color(13, 110, 253));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.addActionListener(e -> onRegister());

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        bottomBar.add(registerBtn);
        bottomBar.add(cancelBtn);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private JPanel createSectionBox(String title) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new CompoundBorder(
            BorderFactory.createTitledBorder(
                new LineBorder(new Color(222, 226, 230), 1, true),
                " " + title + " ",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 13),
                new Color(33, 37, 41)
            ),
            new EmptyBorder(8, 12, 8, 12)
        ));
        p.setMaximumSize(new Dimension(800, 320));
        return p;
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridy = row;

        gbc.gridx = 0;
        gbc.weightx = 0.38;
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(l, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.62;
        panel.add(comp, gbc);
    }

    private void onRegister() {
        String u = usernameField.getText().trim();
        String p1 = new String(passwordField.getPassword()).trim();
        String p2 = new String(confirmPasswordField.getPassword()).trim();

        String rollNo = studentIdField.getText().trim();
        String fName = firstNameField.getText().trim();
        String lName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String gender = (String) genderCombo.getSelectedItem();
        String dobStr = dobField.getText().trim();
        String dept = (String) deptCombo.getSelectedItem();
        String year = (String) yearCombo.getSelectedItem();

        // 1. Validate Credentials
        if (u.isEmpty() || p1.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please choose a username and password.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (p1.length() < 4) {
            JOptionPane.showMessageDialog(this, "Password must be at least 4 characters long.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!p1.equals(p2)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match. Please re-enter confirm password.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. Validate Student Details
        if (rollNo.isEmpty() || fName.isEmpty() || lName.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required student details (*).", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this, "Please provide a valid email address.", "Validation Error", JOptionPane.WARNING_MESSAGE);
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

        // 3. Database uniqueness checks
        try {
            if (userDAO.isUsernameExists(u)) {
                JOptionPane.showMessageDialog(this, "Username '" + u + "' is already taken. Please choose another username.", "Username Taken", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (studentDAO.isStudentIdExists(rollNo, -1)) {
                JOptionPane.showMessageDialog(this, "Student ID / Roll No '" + rollNo + "' already exists.", "Duplicate Roll No", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (studentDAO.isEmailExists(email, -1)) {
                JOptionPane.showMessageDialog(this, "Email address '" + email + "' is already registered.", "Duplicate Email", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 4. Create Student
            Student newStudent = new Student();
            newStudent.setStudentId(rollNo);
            newStudent.setFirstName(fName);
            newStudent.setLastName(lName);
            newStudent.setEmail(email);
            newStudent.setPhone(phone);
            newStudent.setGender(gender);
            newStudent.setDateOfBirth(dob);
            newStudent.setDepartment(dept != null ? dept.trim() : "General");
            newStudent.setYearLevel(year != null ? year : "1st Year");
            newStudent.setStatus("Active");

            boolean studentSaved = studentDAO.insertStudent(newStudent);
            if (!studentSaved || newStudent.getId() <= 0) {
                JOptionPane.showMessageDialog(this, "Failed to create student profile.", "Database Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 5. Create Linked User Account
            User newUser = new User();
            newUser.setUsername(u);
            newUser.setPassword(p1);
            newUser.setRole("STUDENT");
            newUser.setStudentId(newStudent.getId());
            newUser.setFullName(newStudent.getFullName());

            boolean userSaved = userDAO.createUser(newUser);
            if (!userSaved) {
                JOptionPane.showMessageDialog(this, "Student profile saved, but failed to create login account.", "Database Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            registrationSuccessful = true;
            registeredUsername = u;
            registeredPassword = p1;

            JOptionPane.showMessageDialog(this, 
                "Registration Successful!\n\n" +
                "Welcome, " + newStudent.getFullName() + "!\n" +
                "Student ID: " + newStudent.getStudentId() + "\n" +
                "Username: " + u + "\n\n" +
                "You can now sign in to your student dashboard.", 
                "Account Created", 
                JOptionPane.INFORMATION_MESSAGE);

            dispose();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isRegistrationSuccessful() {
        return registrationSuccessful;
    }

    public String getRegisteredUsername() {
        return registeredUsername;
    }

    public String getRegisteredPassword() {
        return registeredPassword;
    }
}
