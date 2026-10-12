package com.sms.ui;

import com.sms.dao.UserDAO;
import com.sms.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class UserAccountsPanel extends JPanel {
    private final UserDAO userDAO = new UserDAO();
    private final Frame parentFrame;

    private JTable table;
    private DefaultTableModel model;
    private JLabel countLabel;

    public UserAccountsPanel(Frame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        initComponents();
        loadUsers();
    }

    private void initComponents() {
        // Top Action Bar
        JPanel topBar = new JPanel(new BorderLayout());

        JPanel titleBox = new JPanel(new GridLayout(2, 1));
        JLabel title = new JLabel("System User Accounts & Role Permissions");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        JLabel sub = new JLabel("Manage administrative and student authentication credentials");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(108, 117, 125));
        titleBox.add(title);
        titleBox.add(sub);
        topBar.add(titleBox, BorderLayout.WEST);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton addBtn = new JButton("+ Add User");
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addBtn.setBackground(new Color(13, 110, 253));
        addBtn.setForeground(Color.WHITE);
        addBtn.addActionListener(e -> onAddUser());

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadUsers());

        rightActions.add(addBtn);
        rightActions.add(refreshBtn);
        topBar.add(rightActions, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // Table
        String[] cols = {"User ID", "Username", "Full Name", "Role", "Linked Student ID", "Created At"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(center);
        table.getColumnModel().getColumn(1).setCellRenderer(center);
        table.getColumnModel().getColumn(3).setCellRenderer(center);
        table.getColumnModel().getColumn(4).setCellRenderer(center);

        // Styled Role renderer
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSel, boolean hasFocus, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tbl, val, isSel, hasFocus, r, c);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                if (val != null && !isSel) {
                    if ("ADMIN".equalsIgnoreCase(val.toString())) {
                        l.setForeground(new Color(220, 53, 69));
                    } else {
                        l.setForeground(new Color(13, 110, 253));
                    }
                }
                return l;
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Bottom Bar
        countLabel = new JLabel("Total Accounts: 0");
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        add(countLabel, BorderLayout.SOUTH);
    }

    public void loadUsers() {
        try {
            List<User> list = userDAO.getAllUsers();
            model.setRowCount(0);
            for (User u : list) {
                model.addRow(new Object[]{
                    u.getId(),
                    u.getUsername(),
                    u.getFullName(),
                    u.getRole(),
                    u.getStudentId() != null ? "Student #" + u.getStudentId() : "N/A (Admin)",
                    u.getCreatedAt() != null ? u.getCreatedAt().toString() : "System"
                });
            }
            countLabel.setText("Total User Accounts: " + list.size());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load user accounts: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onAddUser() {
        JTextField uField = new JTextField();
        JPasswordField pField = new JPasswordField();
        JTextField nameField = new JTextField();
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"STUDENT", "ADMIN"});
        JTextField studentIdField = new JTextField();

        JPanel p = new JPanel(new GridLayout(5, 2, 8, 8));
        p.add(new JLabel("Username:"));
        p.add(uField);
        p.add(new JLabel("Password:"));
        p.add(pField);
        p.add(new JLabel("Full Name:"));
        p.add(nameField);
        p.add(new JLabel("Role:"));
        p.add(roleCombo);
        p.add(new JLabel("Student DB ID (optional):"));
        p.add(studentIdField);

        int res = JOptionPane.showConfirmDialog(parentFrame, p, "Create System User", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            String u = uField.getText().trim();
            String pwd = new String(pField.getPassword()).trim();
            String name = nameField.getText().trim();
            String role = (String) roleCombo.getSelectedItem();
            String sIdStr = studentIdField.getText().trim();

            if (u.isEmpty() || pwd.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(parentFrame, "Username, Password, and Full Name are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Integer sId = null;
            if (!sIdStr.isEmpty()) {
                try {
                    sId = Integer.parseInt(sIdStr);
                } catch (NumberFormatException ignored) {}
            }

            User nu = new User(0, u, pwd, role, sId, name, null);
            try {
                if (userDAO.createUser(nu)) {
                    JOptionPane.showMessageDialog(parentFrame, "User account created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadUsers();
                } else {
                    JOptionPane.showMessageDialog(parentFrame, "Failed to create user.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(parentFrame, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
