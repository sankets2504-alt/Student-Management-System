package com.sms.ui;

import com.sms.dao.CourseDAO;
import com.sms.dao.GradeDAO;
import com.sms.dao.StudentDAO;
import com.sms.model.Course;
import com.sms.model.Grade;
import com.sms.model.Student;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;

public class GradeDialog extends JDialog {
    private final GradeDAO gradeDAO = new GradeDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final CourseDAO courseDAO = new CourseDAO();

    private final Grade grade;
    private final boolean isEditMode;
    private boolean saved = false;

    private JComboBox<Student> studentCombo;
    private JComboBox<Course> courseCombo;
    private JComboBox<String> semesterCombo;
    private JTextField marksField;
    private JLabel gradePreviewLabel;
    private JTextField remarksField;

    public GradeDialog(Frame parent, Grade gradeToEdit) {
        super(parent, gradeToEdit == null ? "Assign Course Grade" : "Edit Grade Record", true);
        this.grade = gradeToEdit != null ? gradeToEdit : new Grade();
        this.isEditMode = gradeToEdit != null;

        initComponents();
        loadStudentsAndCourses();
        if (isEditMode) {
            populateFields();
        }
        pack();
        setMinimumSize(new Dimension(500, 460));
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        headerPanel.setBackground(new Color(245, 247, 250));

        JLabel titleLabel = new JLabel(isEditMode ? "Update Grade Record" : "Assign Grade & Enrollment");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        JLabel subLabel = new JLabel("Record student grades, semester credits, and performance marks.");
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

        studentCombo = new JComboBox<>();
        courseCombo = new JComboBox<>();
        semesterCombo = new JComboBox<>(new String[]{
            "Semester 1", "Semester 2", "Semester 3", "Semester 4", 
            "Semester 5", "Semester 6", "Semester 7", "Semester 8",
            "Fall 2024", "Spring 2025", "Fall 2025", "Spring 2026"
        });
        semesterCombo.setEditable(true);

        marksField = new JTextField("85.0", 18);
        gradePreviewLabel = new JLabel("Letter: A (GPA: 3.7)");
        gradePreviewLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gradePreviewLabel.setForeground(new Color(13, 110, 253));

        remarksField = new JTextField(18);

        // Auto calculate grade on text change
        marksField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateGradePreview(); }
            public void removeUpdate(DocumentEvent e) { updateGradePreview(); }
            public void changedUpdate(DocumentEvent e) { updateGradePreview(); }
        });

        int row = 0;
        addFormField(formPanel, gbc, row++, "Select Student *:", studentCombo);
        addFormField(formPanel, gbc, row++, "Select Course *:", courseCombo);
        addFormField(formPanel, gbc, row++, "Semester *:", semesterCombo);
        addFormField(formPanel, gbc, row++, "Marks (0 - 100) *:", marksField);
        addFormField(formPanel, gbc, row++, "Calculated Grade:", gradePreviewLabel);
        addFormField(formPanel, gbc, row++, "Remarks / Feedback:", remarksField);

        add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 15, 15));

        JButton saveBtn = new JButton(isEditMode ? "Update Grade" : "Save Grade");
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

    private void updateGradePreview() {
        try {
            double marks = Double.parseDouble(marksField.getText().trim());
            if (marks < 0 || marks > 100) {
                gradePreviewLabel.setText("Marks must be between 0 and 100");
                gradePreviewLabel.setForeground(Color.RED);
            } else {
                String letter = Grade.computeLetterGrade(marks);
                double gpa = Grade.computeGradePoint(letter);
                gradePreviewLabel.setText("Grade: " + letter + " (Grade Point: " + gpa + ")");
                gradePreviewLabel.setForeground(new Color(25, 135, 84));
            }
        } catch (NumberFormatException e) {
            gradePreviewLabel.setText("Enter valid number (0 - 100)");
            gradePreviewLabel.setForeground(Color.GRAY);
        }
    }

    private void loadStudentsAndCourses() {
        try {
            List<Student> students = studentDAO.getAllStudents();
            for (Student s : students) {
                studentCombo.addItem(s);
            }

            List<Course> courses = courseDAO.getAllCourses();
            for (Course c : courses) {
                courseCombo.addItem(c);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load students/courses: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateFields() {
        // Select student
        for (int i = 0; i < studentCombo.getItemCount(); i++) {
            if (studentCombo.getItemAt(i).getId() == grade.getStudentId()) {
                studentCombo.setSelectedIndex(i);
                break;
            }
        }
        // Select course
        for (int i = 0; i < courseCombo.getItemCount(); i++) {
            if (courseCombo.getItemAt(i).getId() == grade.getCourseId()) {
                courseCombo.setSelectedIndex(i);
                break;
            }
        }
        semesterCombo.setSelectedItem(grade.getSemester());
        marksField.setText(String.valueOf(grade.getMarks()));
        remarksField.setText(grade.getRemarks() != null ? grade.getRemarks() : "");
        updateGradePreview();
    }

    private void onSave() {
        Student selectedStudent = (Student) studentCombo.getSelectedItem();
        Course selectedCourse = (Course) courseCombo.getSelectedItem();
        String semester = (String) semesterCombo.getSelectedItem();
        String marksStr = marksField.getText().trim();
        String remarks = remarksField.getText().trim();

        if (selectedStudent == null || selectedCourse == null || semester == null || semester.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a student, a course, and a semester.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double marks;
        try {
            marks = Double.parseDouble(marksStr);
            if (marks < 0.0 || marks > 100.0) {
                JOptionPane.showMessageDialog(this, "Marks must be between 0.0 and 100.0", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric value for marks.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int currentId = isEditMode ? grade.getId() : -1;
            if (gradeDAO.isGradeRecordExists(selectedStudent.getId(), selectedCourse.getId(), semester.trim(), currentId)) {
                JOptionPane.showMessageDialog(this, 
                    "A grade record already exists for " + selectedStudent.getFullName() + 
                    " in " + selectedCourse.getCourseCode() + " for " + semester + ".", 
                    "Duplicate Record", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            grade.setStudentId(selectedStudent.getId());
            grade.setCourseId(selectedCourse.getId());
            grade.setSemester(semester.trim());
            grade.setMarks(marks);
            grade.setGrade(Grade.computeLetterGrade(marks));
            grade.setRemarks(remarks);

            boolean success;
            if (isEditMode) {
                success = gradeDAO.updateGrade(grade);
            } else {
                success = gradeDAO.insertGrade(grade);
            }

            if (success) {
                saved = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save grade record.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
