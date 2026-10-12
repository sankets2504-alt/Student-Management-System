package com.sms.database;

import java.sql.*;

public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {}

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            DatabaseConfig config = DatabaseConfig.getInstance();
            connection = DriverManager.getConnection(
                config.getJdbcUrl(),
                config.getUser(),
                config.getPassword()
            );
        }
        return connection;
    }

    public synchronized void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {}
            connection = null;
        }
    }

    public boolean testConnection(String host, int port, String user, String password) throws SQLException {
        String testUrl = "jdbc:mysql://" + host + ":" + port + "/?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
        try (Connection conn = DriverManager.getConnection(testUrl, user, password)) {
            return conn.isValid(3);
        }
    }

    public void initializeDatabase() throws SQLException {
        DatabaseConfig config = DatabaseConfig.getInstance();
        String serverUrl = config.getServerJdbcUrl();

        // 1. Create database if it does not exist
        try (Connection serverConn = DriverManager.getConnection(serverUrl, config.getUser(), config.getPassword());
             Statement stmt = serverConn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + config.getDbName() + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;");
        }

        // Close old connection to reset if any
        closeConnection();

        // 2. Connect to the specific database and create tables
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Students table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS students (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    student_id VARCHAR(30) UNIQUE NOT NULL,
                    first_name VARCHAR(50) NOT NULL,
                    last_name VARCHAR(50) NOT NULL,
                    email VARCHAR(100) UNIQUE NOT NULL,
                    phone VARCHAR(20),
                    gender VARCHAR(15),
                    date_of_birth DATE,
                    department VARCHAR(80),
                    year_level VARCHAR(20),
                    status VARCHAR(20) DEFAULT 'Active',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB;
            """);

            // Courses table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS courses (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    course_code VARCHAR(20) UNIQUE NOT NULL,
                    course_name VARCHAR(100) NOT NULL,
                    department VARCHAR(80) NOT NULL,
                    credits INT NOT NULL,
                    instructor VARCHAR(100),
                    description TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB;
            """);

            // Grades / Enrollments table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS grades (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    student_id INT NOT NULL,
                    course_id INT NOT NULL,
                    semester VARCHAR(30) NOT NULL,
                    marks DECIMAL(5,2),
                    grade VARCHAR(5),
                    remarks VARCHAR(255),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    CONSTRAINT fk_grade_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
                    CONSTRAINT fk_grade_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
                    CONSTRAINT uq_student_course_sem UNIQUE (student_id, course_id, semester)
                ) ENGINE=InnoDB;
            """);

            // Users table (Role-based access: ADMIN / STUDENT)
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(50) UNIQUE NOT NULL,
                    password VARCHAR(100) NOT NULL,
                    role VARCHAR(20) NOT NULL,
                    student_id INT NULL,
                    full_name VARCHAR(100) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    CONSTRAINT fk_user_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE SET NULL
                ) ENGINE=InnoDB;
            """);

            // Seed sample data if empty
            seedInitialDataIfEmpty(conn);
            seedUsersIfEmpty(conn);
        }
    }

    private void seedInitialDataIfEmpty(Connection conn) throws SQLException {
        try (Statement checkStmt = conn.createStatement();
             ResultSet rs = checkStmt.executeQuery("SELECT COUNT(*) FROM students")) {
            if (rs.next() && rs.getInt(1) == 0) {
                // Insert Sample Students
                try (PreparedStatement ps = conn.prepareStatement("""
                    INSERT INTO students (student_id, first_name, last_name, email, phone, gender, date_of_birth, department, year_level, status)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """)) {
                    Object[][] studentsData = {
                        {"STU-1001", "Aarav", "Sharma", "aarav.sharma@example.com", "9876543210", "Male", "2003-05-15", "Computer Science", "3rd Year", "Active"},
                        {"STU-1002", "Priya", "Patel", "priya.patel@example.com", "9876543211", "Female", "2004-08-22", "Computer Science", "2nd Year", "Active"},
                        {"STU-1003", "Rohan", "Verma", "rohan.verma@example.com", "9876543212", "Male", "2002-12-10", "Mechanical Engineering", "4th Year", "Active"},
                        {"STU-1004", "Ananya", "Iyer", "ananya.iyer@example.com", "9876543213", "Female", "2003-03-30", "Electrical Engineering", "3rd Year", "Active"},
                        {"STU-1005", "Kabir", "Mehta", "kabir.mehta@example.com", "9876543214", "Male", "2005-01-18", "Civil Engineering", "1st Year", "Active"}
                    };
                    for (Object[] row : studentsData) {
                        ps.setString(1, (String) row[0]);
                        ps.setString(2, (String) row[1]);
                        ps.setString(3, (String) row[2]);
                        ps.setString(4, (String) row[3]);
                        ps.setString(5, (String) row[4]);
                        ps.setString(6, (String) row[5]);
                        ps.setDate(7, Date.valueOf((String) row[6]));
                        ps.setString(8, (String) row[7]);
                        ps.setString(9, (String) row[8]);
                        ps.setString(10, (String) row[9]);
                        ps.executeUpdate();
                    }
                }

                // Insert Sample Courses
                try (PreparedStatement psCourse = conn.prepareStatement("""
                    INSERT INTO courses (course_code, course_name, department, credits, instructor, description)
                    VALUES (?, ?, ?, ?, ?, ?)
                """)) {
                    Object[][] coursesData = {
                        {"CS101", "Introduction to Programming", "Computer Science", 4, "Dr. Ramesh Gupta", "Fundamental concepts of programming in Java"},
                        {"CS201", "Data Structures & Algorithms", "Computer Science", 4, "Prof. Sunita Rao", "Linear and non-linear data structures, searching, sorting"},
                        {"EE102", "Circuit Theory & Analysis", "Electrical Engineering", 3, "Dr. Alok Verma", "Basic electrical circuits, Kirchhoff laws, AC/DC analysis"},
                        {"ME203", "Engineering Thermodynamics", "Mechanical Engineering", 3, "Dr. Vikram Joshi", "First and second laws of thermodynamics, energy conversion"},
                        {"MA101", "Linear Algebra & Calculus", "Mathematics", 4, "Prof. Neha Kapoor", "Matrix theory, eigenvalues, multivariable calculus"}
                    };
                    for (Object[] row : coursesData) {
                        psCourse.setString(1, (String) row[0]);
                        psCourse.setString(2, (String) row[1]);
                        psCourse.setString(3, (String) row[2]);
                        psCourse.setInt(4, (Integer) row[3]);
                        psCourse.setString(5, (String) row[4]);
                        psCourse.setString(6, (String) row[5]);
                        psCourse.executeUpdate();
                    }
                }

                // Insert Sample Grades
                try (PreparedStatement psGrade = conn.prepareStatement("""
                    INSERT INTO grades (student_id, course_id, semester, marks, grade, remarks)
                    VALUES (?, ?, ?, ?, ?, ?)
                """)) {
                    Object[][] gradesData = {
                        {1, 1, "Semester 1", 92.5, "A+", "Excellent performance"},
                        {1, 2, "Semester 2", 88.0, "A", "Very good understanding"},
                        {2, 1, "Semester 1", 85.5, "A", "Great coursework"},
                        {3, 4, "Semester 1", 76.0, "B+", "Good problem solving"},
                        {4, 3, "Semester 1", 91.0, "A+", "Outstanding laboratory work"}
                    };
                    for (Object[] row : gradesData) {
                        psGrade.setInt(1, (Integer) row[0]);
                        psGrade.setInt(2, (Integer) row[1]);
                        psGrade.setString(3, (String) row[2]);
                        psGrade.setDouble(4, (Double) row[3]);
                        psGrade.setString(5, (String) row[4]);
                        psGrade.setString(6, (String) row[5]);
                        psGrade.executeUpdate();
                    }
                }
            }
        }
    }

    private void seedUsersIfEmpty(Connection conn) throws SQLException {
        try (Statement userCheck = conn.createStatement();
             ResultSet rsUser = userCheck.executeQuery("SELECT COUNT(*) FROM users")) {
            if (rsUser.next() && rsUser.getInt(1) == 0) {
                try (PreparedStatement psUser = conn.prepareStatement("""
                    INSERT INTO users (username, password, role, student_id, full_name)
                    VALUES (?, ?, ?, ?, ?)
                """)) {
                    // 1. Admin Account
                    psUser.setString(1, "admin");
                    psUser.setString(2, "admin123");
                    psUser.setString(3, "ADMIN");
                    psUser.setNull(4, Types.INTEGER);
                    psUser.setString(5, "System Administrator");
                    psUser.executeUpdate();

                    // 2. Student Account (Aarav Sharma - Student ID 1)
                    psUser.setString(1, "student");
                    psUser.setString(2, "student123");
                    psUser.setString(3, "STUDENT");
                    psUser.setInt(4, 1);
                    psUser.setString(5, "Aarav Sharma");
                    psUser.executeUpdate();

                    // 3. Student Account (Priya Patel - Student ID 2)
                    psUser.setString(1, "priya");
                    psUser.setString(2, "priya123");
                    psUser.setString(3, "STUDENT");
                    psUser.setInt(4, 2);
                    psUser.setString(5, "Priya Patel");
                    psUser.executeUpdate();
                }
            }
        }
    }
}
