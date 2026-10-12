package com.sms.ui;

import com.sms.dao.CourseDAO;
import com.sms.model.Course;
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

public class CoursePanel extends JPanel {
    private final CourseDAO courseDAO = new CourseDAO();
    private final Frame parentFrame;

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> deptFilter;
    private JLabel countLabel;

    private JButton editBtn;
    private JButton deleteBtn;

    private List<Course> currentCourseList;

    public CoursePanel(Frame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        initComponents();
        loadCourses();
    }

    private void initComponents() {
        // Top Filter Bar
        JPanel filterPanel = new JPanel(new BorderLayout(10, 10));

        JPanel leftFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        leftFilters.add(new JLabel("Search:"));
        searchField = new JTextField(15);
        searchField.putClientProperty("JTextField.placeholderText", "Code, Name, Instructor...");
        leftFilters.add(searchField);

        leftFilters.add(new JLabel("Department:"));
        deptFilter = new JComboBox<>(new String[]{"All Departments"});
        leftFilters.add(deptFilter);

        JButton resetFiltersBtn = new JButton("Reset");
        resetFiltersBtn.addActionListener(e -> resetFilters());
        leftFilters.add(resetFiltersBtn);

        filterPanel.add(leftFilters, BorderLayout.WEST);

        // Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        JButton addBtn = new JButton("+ Add Course");
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
        String[] columns = {"ID", "Course Code", "Course Title", "Department", "Credits", "Instructor", "Description"};
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

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        table.getColumnModel().getColumn(3).setPreferredWidth(140);
        table.getColumnModel().getColumn(4).setPreferredWidth(60);
        table.getColumnModel().getColumn(5).setPreferredWidth(140);
        table.getColumnModel().getColumn(6).setPreferredWidth(220);

        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Bar
        JPanel bottomBar = new JPanel(new BorderLayout());
        countLabel = new JLabel("Total Courses: 0");
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        bottomBar.add(countLabel, BorderLayout.WEST);
        add(bottomBar, BorderLayout.SOUTH);

        // Event Listeners
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterCourses(); }
            public void removeUpdate(DocumentEvent e) { filterCourses(); }
            public void changedUpdate(DocumentEvent e) { filterCourses(); }
        });

        deptFilter.addActionListener(e -> filterCourses());

        table.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = table.getSelectedRow() != -1;
            editBtn.setEnabled(hasSelection);
            deleteBtn.setEnabled(hasSelection);
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    onEditCourse();
                }
            }
        });

        addBtn.addActionListener(e -> onAddCourse());
        editBtn.addActionListener(e -> onEditCourse());
        deleteBtn.addActionListener(e -> onDeleteCourse());
        exportBtn.addActionListener(e -> onExportCsv());
        refreshBtn.addActionListener(e -> loadCourses());
    }

    public void loadCourses() {
        try {
            deptFilter.removeAllItems();
            deptFilter.addItem("All Departments");
            deptFilter.addItem("Computer Science");
            deptFilter.addItem("Electrical Engineering");
            deptFilter.addItem("Mechanical Engineering");
            deptFilter.addItem("Civil Engineering");
            deptFilter.addItem("Mathematics");
            deptFilter.addItem("Business Administration");

            filterCourses();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load courses: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterCourses() {
        String query = searchField.getText().trim();
        String dept = (String) deptFilter.getSelectedItem();

        try {
            currentCourseList = courseDAO.searchCourses(query, dept);
            tableModel.setRowCount(0);

            for (Course c : currentCourseList) {
                tableModel.addRow(new Object[]{
                    c.getId(),
                    c.getCourseCode(),
                    c.getCourseName(),
                    c.getDepartment(),
                    c.getCredits(),
                    c.getInstructor() != null ? c.getInstructor() : "-",
                    c.getDescription() != null ? c.getDescription() : "-"
                });
            }
            countLabel.setText("Showing: " + currentCourseList.size() + " course" + (currentCourseList.size() == 1 ? "" : "s"));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error filtering courses: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetFilters() {
        searchField.setText("");
        deptFilter.setSelectedIndex(0);
        filterCourses();
    }

    private void onAddCourse() {
        CourseDialog dialog = new CourseDialog(parentFrame, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadCourses();
        }
    }

    private void onEditCourse() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1 || currentCourseList == null || selectedRow >= currentCourseList.size()) return;

        Course selected = currentCourseList.get(selectedRow);
        CourseDialog dialog = new CourseDialog(parentFrame, selected);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadCourses();
        }
    }

    private void onDeleteCourse() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1 || currentCourseList == null || selectedRow >= currentCourseList.size()) return;

        Course selected = currentCourseList.get(selectedRow);
        int opt = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete course: " + selected.getCourseCode() + " (" + selected.getCourseName() + ")?\n" +
            "This will also delete associated student grade records.", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.WARNING_MESSAGE);

        if (opt == JOptionPane.YES_OPTION) {
            try {
                if (courseDAO.deleteCourse(selected.getId())) {
                    JOptionPane.showMessageDialog(this, "Course deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadCourses();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete course.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onExportCsv() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No course data to export.", "Export Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Courses to CSV");
        fileChooser.setSelectedFile(new File("courses_export.csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getAbsolutePath() + ".csv");
            }
            try {
                CsvExporter.exportTableToCsv(table, file);
                JOptionPane.showMessageDialog(this, "Courses exported successfully to:\n" + file.getAbsolutePath(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
