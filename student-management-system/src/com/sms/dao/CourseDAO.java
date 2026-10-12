package com.sms.dao;

import com.sms.database.DatabaseManager;
import com.sms.model.Course;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {

    public List<Course> getAllCourses() throws SQLException {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT * FROM courses ORDER BY id DESC";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToCourse(rs));
            }
        }
        return list;
    }

    public Course getCourseById(int id) throws SQLException {
        String sql = "SELECT * FROM courses WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCourse(rs);
                }
            }
        }
        return null;
    }

    public boolean isCourseCodeExists(String code, int excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM courses WHERE course_code = ? AND id != ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public boolean insertCourse(Course c) throws SQLException {
        String sql = """
            INSERT INTO courses (course_code, course_name, department, credits, instructor, description)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getCourseCode());
            ps.setString(2, c.getCourseName());
            ps.setString(3, c.getDepartment());
            ps.setInt(4, c.getCredits());
            ps.setString(5, c.getInstructor());
            ps.setString(6, c.getDescription());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        c.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean updateCourse(Course c) throws SQLException {
        String sql = """
            UPDATE courses 
            SET course_code = ?, course_name = ?, department = ?, credits = ?, instructor = ?, description = ?
            WHERE id = ?
        """;
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getCourseCode());
            ps.setString(2, c.getCourseName());
            ps.setString(3, c.getDepartment());
            ps.setInt(4, c.getCredits());
            ps.setString(5, c.getInstructor());
            ps.setString(6, c.getDescription());
            ps.setInt(7, c.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteCourse(int id) throws SQLException {
        String sql = "DELETE FROM courses WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Course> searchCourses(String query, String department) throws SQLException {
        List<Course> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM courses WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            String q = "%" + query.trim() + "%";
            sql.append(" AND (course_code LIKE ? OR course_name LIKE ? OR instructor LIKE ?) ");
            params.add(q);
            params.add(q);
            params.add(q);
        }

        if (department != null && !department.equalsIgnoreCase("All Departments")) {
            sql.append(" AND department = ? ");
            params.add(department);
        }

        sql.append(" ORDER BY id DESC");

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToCourse(rs));
                }
            }
        }
        return list;
    }

    private Course mapResultSetToCourse(ResultSet rs) throws SQLException {
        return new Course(
            rs.getInt("id"),
            rs.getString("course_code"),
            rs.getString("course_name"),
            rs.getString("department"),
            rs.getInt("credits"),
            rs.getString("instructor"),
            rs.getString("description"),
            rs.getTimestamp("created_at")
        );
    }
}
