package com.sms.ui;

import com.sms.dao.CourseDAO;
import com.sms.model.Course;

import javax.swing.*;
import java.awt.*;

public class CourseDialog extends JDialog {
    private final CourseDAO courseDAO = new CourseDAO();
    private final Course course;
    private final boolean isEditMode;
    private boolean saved = false;

    private JTextField courseCodeField;
    private JTextField courseNameField;
    private JComboBox<String> deptCombo;
    private JSpinner creditsSpinner;
    private JTextField instructorField;
    private JTextArea descArea;

    public CourseDialog(Frame parent, Course courseToEdit) {
        super(parent, courseToEdit == null ? "Add New Course" : "Edit Course Information", true);
        this.course = courseToEdit != null ? courseToEdit : new Course();
        this.isEditMode = courseToEdit != null;

        initComponents();
        if (isEditMode) {
            populateFields();
        }
        pack();
        setMinimumSize(new Dimension(480, 480));
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        headerPanel.setBackground(new Color(245, 247, 250));

        JLabel titleLabel = new JLabel(isEditMode ? "Update Course" : "Register New Course");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        JLabel subLabel = new JLabel("Enter course code, title, credits, and instructor information.");
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

        courseCodeField = new JTextField(18);
        courseNameField = new JTextField(18);
        deptCombo = new JComboBox<>(new String[]{
            "Computer Science", "Information Technology", "Electrical Engineering", 
            "Mechanical Engineering", "Civil Engineering", "Business Administration", "Mathematics"
        });
        deptCombo.setEditable(true);
        creditsSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        instructorField = new JTextField(18);
        descArea = new JTextArea(4, 18);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);

        int row = 0;
        addFormField(formPanel, gbc, row++, "Course Code *:", courseCodeField);
        addFormField(formPanel, gbc, row++, "Course Name *:", courseNameField);
        addFormField(formPanel, gbc, row++, "Department *:", deptCombo);
        addFormField(formPanel, gbc, row++, "Credits *:", creditsSpinner);
        addFormField(formPanel, gbc, row++, "Instructor:", instructorField);

        gbc.gridy = row++;
        gbc.gridx = 0;
        gbc.weightx = 0.35;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.65;
        formPanel.add(new JScrollPane(descArea), gbc);

        add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 15, 15));

        JButton saveBtn = new JButton(isEditMode ? "Update Course" : "Save Course");
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
        courseCodeField.setText(course.getCourseCode());
        courseNameField.setText(course.getCourseName());
        deptCombo.setSelectedItem(course.getDepartment());
        creditsSpinner.setValue(course.getCredits());
        instructorField.setText(course.getInstructor() != null ? course.getInstructor() : "");
        descArea.setText(course.getDescription() != null ? course.getDescription() : "");
    }

    private void onSave() {
        String code = courseCodeField.getText().trim();
        String name = courseNameField.getText().trim();
        String dept = (String) deptCombo.getSelectedItem();
        int credits = (Integer) creditsSpinner.getValue();
        String instructor = instructorField.getText().trim();
        String desc = descArea.getText().trim();

        if (code.isEmpty() || name.isEmpty() || dept == null || dept.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please fill in all mandatory fields (*): Course Code, Course Name, and Department.", 
                "Validation Error", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int currentId = isEditMode ? course.getId() : -1;
            if (courseDAO.isCourseCodeExists(code, currentId)) {
                JOptionPane.showMessageDialog(this, 
                    "Course Code '" + code + "' already exists. Please choose a unique course code.", 
                    "Duplicate Code", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            course.setCourseCode(code);
            course.setCourseName(name);
            course.setDepartment(dept.trim());
            course.setCredits(credits);
            course.setInstructor(instructor);
            course.setDescription(desc);

            boolean success;
            if (isEditMode) {
                success = courseDAO.updateCourse(course);
            } else {
                success = courseDAO.insertCourse(course);
            }

            if (success) {
                saved = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save course to database.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
