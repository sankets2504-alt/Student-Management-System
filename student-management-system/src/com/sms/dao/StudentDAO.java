package com.sms.dao;

import com.sms.database.DatabaseManager;
import com.sms.model.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    public List<Student> getAllStudents() throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY id DESC";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToStudent(rs));
            }
        }
        return list;
    }

    public Student getStudentById(int id) throws SQLException {
        String sql = "SELECT * FROM students WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStudent(rs);
                }
            }
        }
        return null;
    }

    public boolean isStudentIdExists(String studentId, int excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM students WHERE student_id = ? AND id != ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public boolean isEmailExists(String email, int excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM students WHERE email = ? AND id != ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public boolean insertStudent(Student s) throws SQLException {
        String sql = """
            INSERT INTO students (student_id, first_name, last_name, email, phone, gender, date_of_birth, department, year_level, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getStudentId());
            ps.setString(2, s.getFirstName());
            ps.setString(3, s.getLastName());
            ps.setString(4, s.getEmail());
            ps.setString(5, s.getPhone());
            ps.setString(6, s.getGender());
            ps.setDate(7, s.getDateOfBirth());
            ps.setString(8, s.getDepartment());
            ps.setString(9, s.getYearLevel());
            ps.setString(10, s.getStatus() != null ? s.getStatus() : "Active");
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        s.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean updateStudent(Student s) throws SQLException {
        String sql = """
            UPDATE students 
            SET student_id = ?, first_name = ?, last_name = ?, email = ?, phone = ?, 
                gender = ?, date_of_birth = ?, department = ?, year_level = ?, status = ?
            WHERE id = ?
        """;
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getStudentId());
            ps.setString(2, s.getFirstName());
            ps.setString(3, s.getLastName());
            ps.setString(4, s.getEmail());
            ps.setString(5, s.getPhone());
            ps.setString(6, s.getGender());
            ps.setDate(7, s.getDateOfBirth());
            ps.setString(8, s.getDepartment());
            ps.setString(9, s.getYearLevel());
            ps.setString(10, s.getStatus());
            ps.setInt(11, s.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteStudent(int id) throws SQLException {
        String sql = "DELETE FROM students WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Student> searchStudents(String query, String department, String yearLevel, String status) throws SQLException {
        List<Student> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM students WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            String q = "%" + query.trim() + "%";
            sql.append(" AND (student_id LIKE ? OR first_name LIKE ? OR last_name LIKE ? OR email LIKE ? OR phone LIKE ?) ");
            params.add(q);
            params.add(q);
            params.add(q);
            params.add(q);
            params.add(q);
        }

        if (department != null && !department.equalsIgnoreCase("All Departments")) {
            sql.append(" AND department = ? ");
            params.add(department);
        }

        if (yearLevel != null && !yearLevel.equalsIgnoreCase("All Years")) {
            sql.append(" AND year_level = ? ");
            params.add(yearLevel);
        }

        if (status != null && !status.equalsIgnoreCase("All Status")) {
            sql.append(" AND status = ? ");
            params.add(status);
        }

        sql.append(" ORDER BY id DESC");

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToStudent(rs));
                }
            }
        }
        return list;
    }

    public List<String> getDistinctDepartments() throws SQLException {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT department FROM students WHERE department IS NOT NULL AND department != '' ORDER BY department";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString("department"));
            }
        }
        return list;
    }

    private Student mapResultSetToStudent(ResultSet rs) throws SQLException {
        return new Student(
            rs.getInt("id"),
            rs.getString("student_id"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getString("gender"),
            rs.getDate("date_of_birth"),
            rs.getString("department"),
            rs.getString("year_level"),
            rs.getString("status"),
            rs.getTimestamp("created_at")
        );
    }
}
