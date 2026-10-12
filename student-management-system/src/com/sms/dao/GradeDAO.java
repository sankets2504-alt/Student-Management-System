package com.sms.dao;

import com.sms.database.DatabaseManager;
import com.sms.model.DashboardStats;
import com.sms.model.Grade;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GradeDAO {

    public List<Grade> getAllGrades() throws SQLException {
        List<Grade> list = new ArrayList<>();
        String sql = """
            SELECT g.*, 
                   s.student_id AS roll_no, CONCAT(s.first_name, ' ', s.last_name) AS student_name,
                   c.course_code, c.course_name, c.credits
            FROM grades g
            JOIN students s ON g.student_id = s.id
            JOIN courses c ON g.course_id = c.id
            ORDER BY g.id DESC
        """;
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToGrade(rs));
            }
        }
        return list;
    }

    public List<Grade> getGradesByStudentId(int studentId) throws SQLException {
        List<Grade> list = new ArrayList<>();
        String sql = """
            SELECT g.*, 
                   s.student_id AS roll_no, CONCAT(s.first_name, ' ', s.last_name) AS student_name,
                   c.course_code, c.course_name, c.credits
            FROM grades g
            JOIN students s ON g.student_id = s.id
            JOIN courses c ON g.course_id = c.id
            WHERE g.student_id = ?
            ORDER BY g.semester, c.course_code
        """;
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToGrade(rs));
                }
            }
        }
        return list;
    }

    public boolean isGradeRecordExists(int studentId, int courseId, String semester, int excludeGradeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM grades WHERE student_id = ? AND course_id = ? AND semester = ? AND id != ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            ps.setString(3, semester);
            ps.setInt(4, excludeGradeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public boolean insertGrade(Grade g) throws SQLException {
        String sql = """
            INSERT INTO grades (student_id, course_id, semester, marks, grade, remarks)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, g.getStudentId());
            ps.setInt(2, g.getCourseId());
            ps.setString(3, g.getSemester());
            ps.setDouble(4, g.getMarks());
            ps.setString(5, g.getGrade());
            ps.setString(6, g.getRemarks());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        g.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean updateGrade(Grade g) throws SQLException {
        String sql = """
            UPDATE grades 
            SET student_id = ?, course_id = ?, semester = ?, marks = ?, grade = ?, remarks = ?
            WHERE id = ?
        """;
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, g.getStudentId());
            ps.setInt(2, g.getCourseId());
            ps.setString(3, g.getSemester());
            ps.setDouble(4, g.getMarks());
            ps.setString(5, g.getGrade());
            ps.setString(6, g.getRemarks());
            ps.setInt(7, g.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteGrade(int id) throws SQLException {
        String sql = "DELETE FROM grades WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Grade> searchGrades(String query, String semester) throws SQLException {
        List<Grade> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT g.*, 
                   s.student_id AS roll_no, CONCAT(s.first_name, ' ', s.last_name) AS student_name,
                   c.course_code, c.course_name, c.credits
            FROM grades g
            JOIN students s ON g.student_id = s.id
            JOIN courses c ON g.course_id = c.id
            WHERE 1=1 
        """);
        List<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            String q = "%" + query.trim() + "%";
            sql.append(" AND (s.student_id LIKE ? OR s.first_name LIKE ? OR s.last_name LIKE ? OR c.course_code LIKE ? OR c.course_name LIKE ?) ");
            params.add(q);
            params.add(q);
            params.add(q);
            params.add(q);
            params.add(q);
        }

        if (semester != null && !semester.equalsIgnoreCase("All Semesters")) {
            sql.append(" AND g.semester = ? ");
            params.add(semester);
        }

        sql.append(" ORDER BY g.id DESC");

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToGrade(rs));
                }
            }
        }
        return list;
    }

    public DashboardStats getDashboardStats() throws SQLException {
        DashboardStats stats = new DashboardStats();
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            // Total & Active Students
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*), SUM(CASE WHEN status='Active' THEN 1 ELSE 0 END) FROM students")) {
                if (rs.next()) {
                    stats.setTotalStudents(rs.getInt(1));
                    stats.setActiveStudents(rs.getInt(2));
                }
            }

            // Total courses
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM courses")) {
                if (rs.next()) {
                    stats.setTotalCourses(rs.getInt(1));
                }
            }

            // Total grades & Average Marks
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*), COALESCE(AVG(marks), 0.0) FROM grades")) {
                if (rs.next()) {
                    stats.setTotalGrades(rs.getInt(1));
                    stats.setAverageMarks(rs.getDouble(2));
                }
            }

            // Students by Department
            Map<String, Integer> deptMap = new LinkedHashMap<>();
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT department, COUNT(*) FROM students GROUP BY department ORDER BY COUNT(*) DESC")) {
                while (rs.next()) {
                    String d = rs.getString(1);
                    deptMap.put(d != null ? d : "Unassigned", rs.getInt(2));
                }
            }
            stats.setStudentsByDepartment(deptMap);

            // Students by Year
            Map<String, Integer> yearMap = new LinkedHashMap<>();
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT year_level, COUNT(*) FROM students GROUP BY year_level ORDER BY year_level")) {
                while (rs.next()) {
                    String y = rs.getString(1);
                    yearMap.put(y != null ? y : "Unassigned", rs.getInt(2));
                }
            }
            stats.setStudentsByYear(yearMap);
        }
        return stats;
    }

    private Grade mapResultSetToGrade(ResultSet rs) throws SQLException {
        Grade g = new Grade(
            rs.getInt("id"),
            rs.getInt("student_id"),
            rs.getInt("course_id"),
            rs.getString("semester"),
            rs.getDouble("marks"),
            rs.getString("grade"),
            rs.getString("remarks"),
            rs.getTimestamp("created_at")
        );
        g.setStudentRollNo(rs.getString("roll_no"));
        g.setStudentName(rs.getString("student_name"));
        g.setCourseCode(rs.getString("course_code"));
        g.setCourseName(rs.getString("course_name"));
        g.setCredits(rs.getInt("credits"));
        return g;
    }
}
