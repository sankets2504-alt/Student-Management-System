package com.sms.ui;

import com.sms.dao.StudentDAO;
import com.sms.model.Student;

import javax.swing.*;
import java.awt.*;
import java.sql.Date;

public class StudentDialog extends JDialog {
    private final StudentDAO studentDAO = new StudentDAO();
    private final Student student;
    private final boolean isEditMode;
    private boolean saved = false;

    private JTextField studentIdField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JComboBox<String> genderCombo;
    private JTextField dobField;
    private JComboBox<String> deptCombo;
    private JComboBox<String> yearCombo;
    private JComboBox<String> statusCombo;

    public StudentDialog(Frame parent, Student studentToEdit) {
        super(parent, studentToEdit == null ? "Add New Student" : "Edit Student Information", true);
        this.student = studentToEdit != null ? studentToEdit : new Student();
        this.isEditMode = studentToEdit != null;

        initComponents();
        if (isEditMode) {
            populateFields();
        }
        pack();
        setMinimumSize(new Dimension(500, 520));
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        headerPanel.setBackground(new Color(245, 247, 250));

        JLabel titleLabel = new JLabel(isEditMode ? "Update Student Details" : "Register New Student");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        JLabel subLabel = new JLabel("Fill in the student's profile information below.");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(new Color(108, 117, 125));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        studentIdField = new JTextField(18);
        firstNameField = new JTextField(18);
        lastNameField = new JTextField(18);
        emailField = new JTextField(18);
        phoneField = new JTextField(18);
        genderCombo = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        dobField = new JTextField("2004-01-01", 18);
        deptCombo = new JComboBox<>(new String[]{
            "Computer Science", "Information Technology", "Electrical Engineering", 
            "Mechanical Engineering", "Civil Engineering", "Business Administration", "Mathematics"
        });
        deptCombo.setEditable(true);
        yearCombo = new JComboBox<>(new String[]{"1st Year", "2nd Year", "3rd Year", "4th Year", "Postgraduate"});
        statusCombo = new JComboBox<>(new String[]{"Active", "Inactive", "Graduated"});

        int row = 0;
        addFormField(formPanel, gbc, row++, "Student ID / Roll No *:", studentIdField);
        addFormField(formPanel, gbc, row++, "First Name *:", firstNameField);
        addFormField(formPanel, gbc, row++, "Last Name *:", lastNameField);
        addFormField(formPanel, gbc, row++, "Email Address *:", emailField);
        addFormField(formPanel, gbc, row++, "Phone Number:", phoneField);
        addFormField(formPanel, gbc, row++, "Gender:", genderCombo);
        addFormField(formPanel, gbc, row++, "Date of Birth (YYYY-MM-DD):", dobField);
        addFormField(formPanel, gbc, row++, "Department / Major:", deptCombo);
        addFormField(formPanel, gbc, row++, "Year Level:", yearCombo);
        addFormField(formPanel, gbc, row++, "Enrollment Status:", statusCombo);

        add(new JScrollPane(formPanel), BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 15, 15));

        JButton saveBtn = new JButton(isEditMode ? "Update Student" : "Save Student");
        JButton cancelBtn = new JButton("Cancel");

        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        saveBtn.setBackground(new Color(13, 110, 253));
        saveBtn.setForeground(Color.WHITE);

        saveBtn.addActionListener(e -> onSave());
        cancelBtn.addActionListener(e -> dispose());

        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridy = row;

        gbc.gridx = 0;
        gbc.weightx = 0.35;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        panel.add(field, gbc);
    }

    private void populateFields() {
        studentIdField.setText(student.getStudentId());
        firstNameField.setText(student.getFirstName());
        lastNameField.setText(student.getLastName());
        emailField.setText(student.getEmail());
        phoneField.setText(student.getPhone() != null ? student.getPhone() : "");
        genderCombo.setSelectedItem(student.getGender());
        if (student.getDateOfBirth() != null) {
            dobField.setText(student.getDateOfBirth().toString());
        }
        deptCombo.setSelectedItem(student.getDepartment());
        yearCombo.setSelectedItem(student.getYearLevel());
        statusCombo.setSelectedItem(student.getStatus());
    }

    private void onSave() {
        String studentId = studentIdField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String gender = (String) genderCombo.getSelectedItem();
        String dobStr = dobField.getText().trim();
        String dept = (String) deptCombo.getSelectedItem();
        String year = (String) yearCombo.getSelectedItem();
        String status = (String) statusCombo.getSelectedItem();

        // Validation
        if (studentId.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please fill in all mandatory fields (*): Student ID, First Name, Last Name, and Email.", 
                "Validation Error", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Date dob = null;
        if (!dobStr.isEmpty()) {
            try {
                dob = Date.valueOf(dobStr);
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(this, "Invalid Date of Birth format. Please use YYYY-MM-DD.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        try {
            int currentId = isEditMode ? student.getId() : -1;
            if (studentDAO.isStudentIdExists(studentId, currentId)) {
                JOptionPane.showMessageDialog(this, "Student ID / Roll No '" + studentId + "' is already taken. Please use a unique ID.", "Duplicate ID", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (studentDAO.isEmailExists(email, currentId)) {
                JOptionPane.showMessageDialog(this, "Email address '" + email + "' is already registered to another student.", "Duplicate Email", JOptionPane.WARNING_MESSAGE);
                return;
            }

            student.setStudentId(studentId);
            student.setFirstName(firstName);
            student.setLastName(lastName);
            student.setEmail(email);
            student.setPhone(phone);
            student.setGender(gender);
            student.setDateOfBirth(dob);
            student.setDepartment(dept);
            student.setYearLevel(year);
            student.setStatus(status);

            boolean success;
            if (isEditMode) {
                success = studentDAO.updateStudent(student);
            } else {
                success = studentDAO.insertStudent(student);
            }

            if (success) {
                saved = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save student record to database.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
