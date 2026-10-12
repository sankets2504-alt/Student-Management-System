-- ========================================================
-- Student Management System (SMS) - MySQL Database Schema
-- ========================================================

-- 1. Create database
CREATE DATABASE IF NOT EXISTS `student_management_db`
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `student_management_db`;

-- 2. Students table
CREATE TABLE IF NOT EXISTS `students` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `student_id` VARCHAR(30) UNIQUE NOT NULL,
    `first_name` VARCHAR(50) NOT NULL,
    `last_name` VARCHAR(50) NOT NULL,
    `email` VARCHAR(100) UNIQUE NOT NULL,
    `phone` VARCHAR(20),
    `gender` VARCHAR(15),
    `date_of_birth` DATE,
    `department` VARCHAR(80),
    `year_level` VARCHAR(20),
    `status` VARCHAR(20) DEFAULT 'Active',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 3. Courses table
CREATE TABLE IF NOT EXISTS `courses` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `course_code` VARCHAR(20) UNIQUE NOT NULL,
    `course_name` VARCHAR(100) NOT NULL,
    `department` VARCHAR(80) NOT NULL,
    `credits` INT NOT NULL,
    `instructor` VARCHAR(100),
    `description` TEXT,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 4. Grades / Enrollments table
CREATE TABLE IF NOT EXISTS `grades` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `student_id` INT NOT NULL,
    `course_id` INT NOT NULL,
    `semester` VARCHAR(30) NOT NULL,
    `marks` DECIMAL(5,2),
    `grade` VARCHAR(5),
    `remarks` VARCHAR(255),
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_grade_student` FOREIGN KEY (`student_id`) REFERENCES `students`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_grade_course` FOREIGN KEY (`course_id`) REFERENCES `courses`(`id`) ON DELETE CASCADE,
    CONSTRAINT `uq_student_course_sem` UNIQUE (`student_id`, `course_id`, `semester`)
) ENGINE=InnoDB;

-- 5. Sample seed data
INSERT INTO `students` (`student_id`, `first_name`, `last_name`, `email`, `phone`, `gender`, `date_of_birth`, `department`, `year_level`, `status`)
VALUES 
    ('STU-1001', 'Aarav', 'Sharma', 'aarav.sharma@example.com', '9876543210', 'Male', '2003-05-15', 'Computer Science', '3rd Year', 'Active'),
    ('STU-1002', 'Priya', 'Patel', 'priya.patel@example.com', '9876543211', 'Female', '2004-08-22', 'Computer Science', '2nd Year', 'Active'),
    ('STU-1003', 'Rohan', 'Verma', 'rohan.verma@example.com', '9876543212', 'Male', '2002-12-10', 'Mechanical Engineering', '4th Year', 'Active'),
    ('STU-1004', 'Ananya', 'Iyer', 'ananya.iyer@example.com', '9876543213', 'Female', '2003-03-30', 'Electrical Engineering', '3rd Year', 'Active'),
    ('STU-1005', 'Kabir', 'Mehta', 'kabir.mehta@example.com', '9876543214', 'Male', '2005-01-18', 'Civil Engineering', '1st Year', 'Active')
ON DUPLICATE KEY UPDATE `id`=`id`;

INSERT INTO `courses` (`course_code`, `course_name`, `department`, `credits`, `instructor`, `description`)
VALUES
    ('CS101', 'Introduction to Programming', 'Computer Science', 4, 'Dr. Ramesh Gupta', 'Fundamental concepts of programming in Java'),
    ('CS201', 'Data Structures & Algorithms', 'Computer Science', 4, 'Prof. Sunita Rao', 'Linear and non-linear data structures, searching, sorting'),
    ('EE102', 'Circuit Theory & Analysis', 'Electrical Engineering', 3, 'Dr. Alok Verma', 'Basic electrical circuits, Kirchhoff laws, AC/DC analysis'),
    ('ME203', 'Engineering Thermodynamics', 'Mechanical Engineering', 3, 'Dr. Vikram Joshi', 'First and second laws of thermodynamics, energy conversion'),
    ('MA101', 'Linear Algebra & Calculus', 'Mathematics', 4, 'Prof. Neha Kapoor', 'Matrix theory, eigenvalues, multivariable calculus')
ON DUPLICATE KEY UPDATE `id`=`id`;

INSERT INTO `grades` (`student_id`, `course_id`, `semester`, `marks`, `grade`, `remarks`)
VALUES
    (1, 1, 'Semester 1', 92.5, 'A+', 'Excellent performance'),
    (1, 2, 'Semester 2', 88.0, 'A', 'Very good understanding'),
    (2, 1, 'Semester 1', 85.5, 'A', 'Great coursework'),
    (3, 4, 'Semester 1', 76.0, 'B+', 'Good problem solving'),
    (4, 3, 'Semester 1', 91.0, 'A+', 'Outstanding laboratory work')
ON DUPLICATE KEY UPDATE `id`=`id`;

-- 5. Users table (Admin & Student roles)
CREATE TABLE IF NOT EXISTS `users` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) UNIQUE NOT NULL,
    `password` VARCHAR(100) NOT NULL,
    `role` VARCHAR(20) NOT NULL,
    `student_id` INT NULL,
    `full_name` VARCHAR(100) NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_user_student` FOREIGN KEY (`student_id`) REFERENCES `students`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB;

INSERT INTO `users` (`username`, `password`, `role`, `student_id`, `full_name`)
VALUES
    ('admin', 'admin123', 'ADMIN', NULL, 'System Administrator'),
    ('student', 'student123', 'STUDENT', 1, 'Aarav Sharma'),
    ('priya', 'priya123', 'STUDENT', 2, 'Priya Patel')
ON DUPLICATE KEY UPDATE `id`=`id`;

