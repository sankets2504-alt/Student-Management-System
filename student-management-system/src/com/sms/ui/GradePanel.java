package com.sms.ui;

import com.sms.dao.GradeDAO;
import com.sms.model.Grade;
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

public class GradePanel extends JPanel {
    private final GradeDAO gradeDAO = new GradeDAO();
    private final Frame parentFrame;

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> semesterFilter;
    private JLabel countLabel;

    private JButton editBtn;
    private JButton deleteBtn;

    private List<Grade> currentGradeList;

    public GradePanel(Frame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        initComponents();
        loadGrades();
    }

    private void initComponents() {
        // Top Filter Bar
        JPanel filterPanel = new JPanel(new BorderLayout(10, 10));

        JPanel leftFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        leftFilters.add(new JLabel("Search:"));
        searchField = new JTextField(15);
        searchField.putClientProperty("JTextField.placeholderText", "Student, Roll No, Course...");
        leftFilters.add(searchField);

        leftFilters.add(new JLabel("Semester:"));
        semesterFilter = new JComboBox<>(new String[]{
            "All Semesters", "Semester 1", "Semester 2", "Semester 3", "Semester 4", 
            "Semester 5", "Semester 6", "Semester 7", "Semester 8"
        });
        leftFilters.add(semesterFilter);

        JButton resetFiltersBtn = new JButton("Reset");
        resetFiltersBtn.addActionListener(e -> resetFilters());
        leftFilters.add(resetFiltersBtn);

        filterPanel.add(leftFilters, BorderLayout.WEST);

        // Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        JButton addBtn = new JButton("+ Assign Grade");
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
        String[] columns = {"ID", "Roll No", "Student Name", "Course Code", "Course Title", "Semester", "Marks", "Grade", "Remarks"};
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
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(140);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(180);
        table.getColumnModel().getColumn(5).setPreferredWidth(90);
        table.getColumnModel().getColumn(6).setPreferredWidth(70);
        table.getColumnModel().getColumn(7).setPreferredWidth(60);
        table.getColumnModel().getColumn(8).setPreferredWidth(180);

        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        // Styled Grade Letter renderer
        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                c.setHorizontalAlignment(SwingConstants.CENTER);
                c.setFont(new Font("Segoe UI", Font.BOLD, 12));
                if (value != null && !isSelected) {
                    String g = value.toString();
                    if (g.startsWith("A")) c.setForeground(new Color(25, 135, 84));
                    else if (g.startsWith("B")) c.setForeground(new Color(13, 110, 253));
                    else if (g.startsWith("C")) c.setForeground(new Color(255, 193, 7));
                    else if (g.startsWith("D")) c.setForeground(new Color(253, 126, 20));
                    else if (g.equalsIgnoreCase("F")) c.setForeground(new Color(220, 53, 69));
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Bar
        JPanel bottomBar = new JPanel(new BorderLayout());
        countLabel = new JLabel("Total Grade Records: 0");
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        bottomBar.add(countLabel, BorderLayout.WEST);
        add(bottomBar, BorderLayout.SOUTH);

        // Event Listeners
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterGrades(); }
            public void removeUpdate(DocumentEvent e) { filterGrades(); }
            public void changedUpdate(DocumentEvent e) { filterGrades(); }
        });

        semesterFilter.addActionListener(e -> filterGrades());

        table.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = table.getSelectedRow() != -1;
            editBtn.setEnabled(hasSelection);
            deleteBtn.setEnabled(hasSelection);
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    onEditGrade();
                }
            }
        });

        addBtn.addActionListener(e -> onAddGrade());
        editBtn.addActionListener(e -> onEditGrade());
        deleteBtn.addActionListener(e -> onDeleteGrade());
        exportBtn.addActionListener(e -> onExportCsv());
        refreshBtn.addActionListener(e -> loadGrades());
    }

    public void loadGrades() {
        filterGrades();
    }

    private void filterGrades() {
        String query = searchField.getText().trim();
        String semester = (String) semesterFilter.getSelectedItem();

        try {
            currentGradeList = gradeDAO.searchGrades(query, semester);
            tableModel.setRowCount(0);

            for (Grade g : currentGradeList) {
                tableModel.addRow(new Object[]{
                    g.getId(),
                    g.getStudentRollNo(),
                    g.getStudentName(),
                    g.getCourseCode(),
                    g.getCourseName(),
                    g.getSemester(),
                    String.format("%.2f", g.getMarks()),
                    g.getGrade(),
                    g.getRemarks() != null ? g.getRemarks() : "-"
                });
            }
            countLabel.setText("Showing: " + currentGradeList.size() + " grade record" + (currentGradeList.size() == 1 ? "" : "s"));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error filtering grades: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetFilters() {
        searchField.setText("");
        semesterFilter.setSelectedIndex(0);
        filterGrades();
    }

    private void onAddGrade() {
        GradeDialog dialog = new GradeDialog(parentFrame, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadGrades();
        }
    }

    private void onEditGrade() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1 || currentGradeList == null || selectedRow >= currentGradeList.size()) return;

        Grade selected = currentGradeList.get(selectedRow);
        GradeDialog dialog = new GradeDialog(parentFrame, selected);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadGrades();
        }
    }

    private void onDeleteGrade() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1 || currentGradeList == null || selectedRow >= currentGradeList.size()) return;

        Grade selected = currentGradeList.get(selectedRow);
        int opt = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete grade record for " + selected.getStudentName() + " in " + selected.getCourseCode() + "?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.WARNING_MESSAGE);

        if (opt == JOptionPane.YES_OPTION) {
            try {
                if (gradeDAO.deleteGrade(selected.getId())) {
                    JOptionPane.showMessageDialog(this, "Grade record deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadGrades();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete grade record.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onExportCsv() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No grade data to export.", "Export Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Grades to CSV");
        fileChooser.setSelectedFile(new File("grades_export.csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getAbsolutePath() + ".csv");
            }
            try {
                CsvExporter.exportTableToCsv(table, file);
                JOptionPane.showMessageDialog(this, "Grades exported successfully to:\n" + file.getAbsolutePath(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
