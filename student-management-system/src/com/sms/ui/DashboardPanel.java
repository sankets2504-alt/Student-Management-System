package com.sms.ui;

import com.sms.dao.GradeDAO;
import com.sms.model.DashboardStats;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class DashboardPanel extends JPanel {
    private final GradeDAO gradeDAO = new GradeDAO();
    private final MainFrame mainFrame;

    private JLabel totalStudentsVal;
    private JLabel activeStudentsVal;
    private JLabel totalCoursesVal;
    private JLabel totalGradesVal;
    private JLabel avgMarksVal;
    private JLabel lastUpdatedLabel;

    private JPanel deptDistributionPanel;
    private JPanel yearDistributionPanel;

    public DashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        initComponents();
        loadDashboardData();
    }

    private void initComponents() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JPanel titleBox = new JPanel(new GridLayout(2, 1));
        JLabel title = new JLabel("Overview & Analytics Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(33, 37, 41));

        lastUpdatedLabel = new JLabel("Last updated: just now");
        lastUpdatedLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lastUpdatedLabel.setForeground(new Color(108, 117, 125));

        titleBox.add(title);
        titleBox.add(lastUpdatedLabel);
        headerPanel.add(titleBox, BorderLayout.WEST);

        JButton refreshBtn = new JButton("Refresh Data");
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshBtn.addActionListener(e -> loadDashboardData());
        headerPanel.add(refreshBtn, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Center Content inside a ScrollPane
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // 1. Stat Cards Row
        JPanel statsRow = new JPanel(new GridLayout(1, 5, 12, 12));
        statsRow.setMaximumSize(new Dimension(2000, 110));
        statsRow.setPreferredSize(new Dimension(800, 100));

        totalStudentsVal = new JLabel("0");
        activeStudentsVal = new JLabel("0");
        totalCoursesVal = new JLabel("0");
        totalGradesVal = new JLabel("0");
        avgMarksVal = new JLabel("0.0%");

        statsRow.add(createCard("Total Students", totalStudentsVal, new Color(13, 110, 253)));
        statsRow.add(createCard("Active Students", activeStudentsVal, new Color(25, 135, 84)));
        statsRow.add(createCard("Available Courses", totalCoursesVal, new Color(111, 66, 193)));
        statsRow.add(createCard("Grades Logged", totalGradesVal, new Color(253, 126, 20)));
        statsRow.add(createCard("Average Score", avgMarksVal, new Color(32, 201, 151)));

        contentPanel.add(statsRow);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // 2. Middle Grid: Department & Year Distribution
        JPanel chartsRow = new JPanel(new GridLayout(1, 2, 16, 16));
        chartsRow.setPreferredSize(new Dimension(800, 260));

        // Dept Panel
        JPanel deptBox = new JPanel(new BorderLayout(10, 10));
        deptBox.setBorder(new CompoundBorder(
            new LineBorder(new Color(222, 226, 230), 1, true),
            new EmptyBorder(12, 15, 12, 15)
        ));
        JLabel deptTitle = new JLabel("Students by Department");
        deptTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        deptBox.add(deptTitle, BorderLayout.NORTH);

        deptDistributionPanel = new JPanel();
        deptDistributionPanel.setLayout(new BoxLayout(deptDistributionPanel, BoxLayout.Y_AXIS));
        deptBox.add(new JScrollPane(deptDistributionPanel), BorderLayout.CENTER);

        // Year Panel
        JPanel yearBox = new JPanel(new BorderLayout(10, 10));
        yearBox.setBorder(new CompoundBorder(
            new LineBorder(new Color(222, 226, 230), 1, true),
            new EmptyBorder(12, 15, 12, 15)
        ));
        JLabel yearTitle = new JLabel("Students by Academic Year");
        yearTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        yearBox.add(yearTitle, BorderLayout.NORTH);

        yearDistributionPanel = new JPanel();
        yearDistributionPanel.setLayout(new BoxLayout(yearDistributionPanel, BoxLayout.Y_AXIS));
        yearBox.add(new JScrollPane(yearDistributionPanel), BorderLayout.CENTER);

        chartsRow.add(deptBox);
        chartsRow.add(yearBox);
        contentPanel.add(chartsRow);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // 3. Quick Actions Bar
        JPanel quickActionsBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        quickActionsBox.setBorder(new CompoundBorder(
            new LineBorder(new Color(222, 226, 230), 1, true),
            new EmptyBorder(10, 15, 10, 15)
        ));

        JLabel qaLabel = new JLabel("Quick Actions:");
        qaLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        quickActionsBox.add(qaLabel);

        JButton addStudentBtn = new JButton("+ Add Student");
        JButton addCourseBtn = new JButton("+ Add Course");
        JButton assignGradeBtn = new JButton("+ Assign Grade");

        addStudentBtn.addActionListener(e -> {
            StudentDialog dlg = new StudentDialog(mainFrame, null);
            dlg.setVisible(true);
            if (dlg.isSaved()) {
                mainFrame.reloadAllPanels();
            }
        });

        addCourseBtn.addActionListener(e -> {
            CourseDialog dlg = new CourseDialog(mainFrame, null);
            dlg.setVisible(true);
            if (dlg.isSaved()) {
                mainFrame.reloadAllPanels();
            }
        });

        assignGradeBtn.addActionListener(e -> {
            GradeDialog dlg = new GradeDialog(mainFrame, null);
            dlg.setVisible(true);
            if (dlg.isSaved()) {
                mainFrame.reloadAllPanels();
            }
        });

        quickActionsBox.add(addStudentBtn);
        quickActionsBox.add(addCourseBtn);
        quickActionsBox.add(assignGradeBtn);

        contentPanel.add(quickActionsBox);

        add(new JScrollPane(contentPanel), BorderLayout.CENTER);
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

        // Accent top stripe
        JPanel stripe = new JPanel();
        stripe.setPreferredSize(new Dimension(10, 4));
        stripe.setBackground(accentColor);

        card.add(stripe, BorderLayout.NORTH);
        card.add(t, BorderLayout.CENTER);
        card.add(valueLabel, BorderLayout.SOUTH);
        return card;
    }

    public void loadDashboardData() {
        try {
            DashboardStats stats = gradeDAO.getDashboardStats();
            totalStudentsVal.setText(String.valueOf(stats.getTotalStudents()));
            activeStudentsVal.setText(String.valueOf(stats.getActiveStudents()));
            totalCoursesVal.setText(String.valueOf(stats.getTotalCourses()));
            totalGradesVal.setText(String.valueOf(stats.getTotalGrades()));
            avgMarksVal.setText(String.format("%.1f%%", stats.getAverageMarks()));

            // Update Department Breakdown
            deptDistributionPanel.removeAll();
            int total = stats.getTotalStudents();
            for (Map.Entry<String, Integer> entry : stats.getStudentsByDepartment().entrySet()) {
                deptDistributionPanel.add(createProgressRow(entry.getKey(), entry.getValue(), total, new Color(13, 110, 253)));
                deptDistributionPanel.add(Box.createRigidArea(new Dimension(0, 6)));
            }

            // Update Year Breakdown
            yearDistributionPanel.removeAll();
            for (Map.Entry<String, Integer> entry : stats.getStudentsByYear().entrySet()) {
                yearDistributionPanel.add(createProgressRow(entry.getKey(), entry.getValue(), total, new Color(25, 135, 84)));
                yearDistributionPanel.add(Box.createRigidArea(new Dimension(0, 6)));
            }

            lastUpdatedLabel.setText("Last updated: " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            revalidate();
            repaint();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load dashboard metrics: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createProgressRow(String labelText, int count, int total, Color barColor) {
        JPanel row = new JPanel(new BorderLayout(8, 2));
        row.setMaximumSize(new Dimension(1000, 36));
        row.setOpaque(false);

        int pct = total > 0 ? (int) Math.round(((double) count / total) * 100.0) : 0;

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JLabel countLbl = new JLabel(count + " (" + pct + "%)");
        countLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(pct);
        bar.setForeground(barColor);
        bar.setPreferredSize(new Dimension(100, 8));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(lbl, BorderLayout.WEST);
        top.add(countLbl, BorderLayout.EAST);

        row.add(top, BorderLayout.NORTH);
        row.add(bar, BorderLayout.SOUTH);
        return row;
    }
}
