package com.sms.ui;

import com.sms.dao.StudentDAO;
import com.sms.model.Student;
import com.sms.util.CsvExporter;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.SQLException;
import java.util.List;

public class StudentPanel extends JPanel {
    private final StudentDAO studentDAO = new StudentDAO();
    private final Frame parentFrame;

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> deptFilter;
    private JComboBox<String> yearFilter;
    private JComboBox<String> statusFilter;
    private JLabel countLabel;

    private JButton editBtn;
    private JButton deleteBtn;

    private List<Student> currentStudentList;

    public StudentPanel(Frame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        initComponents();
        loadStudents();
    }

    private void initComponents() {
        // Top Filter Bar
        JPanel filterPanel = new JPanel(new BorderLayout(10, 10));

        JPanel leftFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        leftFilters.add(new JLabel("Search:"));
        searchField = new JTextField(15);
        searchField.putClientProperty("JTextField.placeholderText", "Name, ID, Email...");
        leftFilters.add(searchField);

        leftFilters.add(new JLabel("Department:"));
        deptFilter = new JComboBox<>(new String[]{"All Departments"});
        leftFilters.add(deptFilter);

        leftFilters.add(new JLabel("Year:"));
        yearFilter = new JComboBox<>(new String[]{"All Years", "1st Year", "2nd Year", "3rd Year", "4th Year", "Postgraduate"});
        leftFilters.add(yearFilter);

        leftFilters.add(new JLabel("Status:"));
        statusFilter = new JComboBox<>(new String[]{"All Status", "Active", "Inactive", "Graduated"});
        leftFilters.add(statusFilter);

        JButton resetFiltersBtn = new JButton("Reset");
        resetFiltersBtn.addActionListener(e -> resetFilters());
        leftFilters.add(resetFiltersBtn);

        filterPanel.add(leftFilters, BorderLayout.WEST);

        // Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        JButton addBtn = new JButton("+ Add Student");
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addBtn.setBackground(new Color(13, 110, 253));
        addBtn.setForeground(Color.WHITE);

        editBtn = new JButton("Edit");
        deleteBtn = new JButton("Delete");
        deleteBtn.setForeground(new Color(220, 53, 69));

        JButton exportBtn = new JButton("Export to CSV");
        JButton refreshBtn = new JButton("Refresh");

        editBtn.setEnabled(false);
        deleteBtn.setEnabled(false);

        actionPanel.add(addBtn);
        actionPanel.add(editBtn);
        actionPanel.add(deleteBtn);
        actionPanel.add(exportBtn);
        actionPanel.add(refreshBtn);

        filterPanel.add(actionPanel, BorderLayout.EAST);
        add(filterPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Roll Number", "Name", "Email", "Phone", "Gender", "DOB", "Department", "Year", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Center align roll number, gender, year, status
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setPreferredWidth(160);
        table.getColumnModel().getColumn(4).setPreferredWidth(90);
        table.getColumnModel().getColumn(5).setPreferredWidth(60);
        table.getColumnModel().getColumn(6).setPreferredWidth(80);
        table.getColumnModel().getColumn(7).setPreferredWidth(140);
        table.getColumnModel().getColumn(8).setPreferredWidth(80);
        table.getColumnModel().getColumn(9).setPreferredWidth(70);

        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(8).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(9).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Bar
        JPanel bottomBar = new JPanel(new BorderLayout());
        countLabel = new JLabel("Total Students: 0");
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        bottomBar.add(countLabel, BorderLayout.WEST);
        add(bottomBar, BorderLayout.SOUTH);

        // Event Listeners
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterStudents(); }
            public void removeUpdate(DocumentEvent e) { filterStudents(); }
            public void changedUpdate(DocumentEvent e) { filterStudents(); }
        });

        deptFilter.addActionListener(e -> filterStudents());
        yearFilter.addActionListener(e -> filterStudents());
        statusFilter.addActionListener(e -> filterStudents());

        table.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = table.getSelectedRow() != -1;
            editBtn.setEnabled(hasSelection);
            deleteBtn.setEnabled(hasSelection);
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    onEditStudent();
                }
            }
        });

        addBtn.addActionListener(e -> onAddStudent());
        editBtn.addActionListener(e -> onEditStudent());
        deleteBtn.addActionListener(e -> onDeleteStudent());
        exportBtn.addActionListener(e -> onExportCsv());
        refreshBtn.addActionListener(e -> loadStudents());
    }

    public void loadStudents() {
        try {
            // Populate department filter choices
            List<String> depts = studentDAO.getDistinctDepartments();
            deptFilter.removeAllItems();
            deptFilter.addItem("All Departments");
            for (String d : depts) {
                deptFilter.addItem(d);
            }

            filterStudents();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load students: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterStudents() {
        String query = searchField.getText().trim();
        String dept = (String) deptFilter.getSelectedItem();
        String year = (String) yearFilter.getSelectedItem();
        String status = (String) statusFilter.getSelectedItem();

        try {
            currentStudentList = studentDAO.searchStudents(query, dept, year, status);
            tableModel.setRowCount(0);

            for (Student s : currentStudentList) {
                tableModel.addRow(new Object[]{
                    s.getId(),
                    s.getStudentId(),
                    s.getFullName(),
                    s.getEmail(),
                    s.getPhone() != null ? s.getPhone() : "-",
                    s.getGender() != null ? s.getGender() : "-",
                    s.getDateOfBirth() != null ? s.getDateOfBirth().toString() : "-",
                    s.getDepartment(),
                    s.getYearLevel(),
                    s.getStatus()
                });
            }
            countLabel.setText("Showing: " + currentStudentList.size() + " student" + (currentStudentList.size() == 1 ? "" : "s"));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error filtering students: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetFilters() {
        searchField.setText("");
        deptFilter.setSelectedIndex(0);
        yearFilter.setSelectedIndex(0);
        statusFilter.setSelectedIndex(0);
        filterStudents();
    }

    private void onAddStudent() {
        StudentDialog dialog = new StudentDialog(parentFrame, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadStudents();
        }
    }

    private void onEditStudent() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1 || currentStudentList == null || selectedRow >= currentStudentList.size()) return;

        Student selected = currentStudentList.get(selectedRow);
        StudentDialog dialog = new StudentDialog(parentFrame, selected);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadStudents();
        }
    }

    private void onDeleteStudent() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1 || currentStudentList == null || selectedRow >= currentStudentList.size()) return;

        Student selected = currentStudentList.get(selectedRow);
        int opt = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete student: " + selected.getFullName() + " (" + selected.getStudentId() + ")?\n" +
            "This will also delete associated grade records.", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.WARNING_MESSAGE);

        if (opt == JOptionPane.YES_OPTION) {
            try {
                if (studentDAO.deleteStudent(selected.getId())) {
                    JOptionPane.showMessageDialog(this, "Student record deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadStudents();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete student.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onExportCsv() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No student data to export.", "Export Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Students to CSV");
        fileChooser.setSelectedFile(new File("students_export.csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getAbsolutePath() + ".csv");
            }
            try {
                CsvExporter.exportTableToCsv(table, file);
                JOptionPane.showMessageDialog(this, "Students exported successfully to:\n" + file.getAbsolutePath(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
