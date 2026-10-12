package com.sms.model;

import java.sql.Timestamp;

public class Grade {
    private int id;
    private int studentId;
    private int courseId;
    private String semester;
    private double marks;
    private String grade;
    private String remarks;
    private Timestamp createdAt;

    // Joined fields for display
    private String studentRollNo;
    private String studentName;
    private String courseCode;
    private String courseName;
    private int credits;

    public Grade() {}

    public Grade(int id, int studentId, int courseId, String semester, double marks, 
                 String grade, String remarks, Timestamp createdAt) {
        this.id = id;
        this.studentId = studentId;
        this.courseId = courseId;
        this.semester = semester;
        this.marks = marks;
        this.grade = grade;
        this.remarks = remarks;
        this.createdAt = createdAt;
    }

    public static String computeLetterGrade(double marks) {
        if (marks >= 90.0) return "A+";
        if (marks >= 80.0) return "A";
        if (marks >= 70.0) return "B+";
        if (marks >= 60.0) return "B";
        if (marks >= 50.0) return "C";
        if (marks >= 40.0) return "D";
        return "F";
    }

    public static double computeGradePoint(String letterGrade) {
        if (letterGrade == null) return 0.0;
        return switch (letterGrade.toUpperCase()) {
            case "A+" -> 4.0;
            case "A" -> 3.7;
            case "B+" -> 3.3;
            case "B" -> 3.0;
            case "C" -> 2.0;
            case "D" -> 1.0;
            default -> 0.0;
        };
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public double getMarks() { return marks; }
    public void setMarks(double marks) { this.marks = marks; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getStudentRollNo() { return studentRollNo; }
    public void setStudentRollNo(String studentRollNo) { this.studentRollNo = studentRollNo; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }
}
