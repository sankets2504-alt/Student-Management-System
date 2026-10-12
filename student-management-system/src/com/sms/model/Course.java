package com.sms.model;

import java.sql.Timestamp;

public class Course {
    private int id;
    private String courseCode;
    private String courseName;
    private String department;
    private int credits;
    private String instructor;
    private String description;
    private Timestamp createdAt;

    public Course() {}

    public Course(int id, String courseCode, String courseName, String department, 
                  int credits, String instructor, String description, Timestamp createdAt) {
        this.id = id;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.department = department;
        this.credits = credits;
        this.instructor = instructor;
        this.description = description;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public String getInstructor() { return instructor; }
    public void setInstructor(String instructor) { this.instructor = instructor; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return courseCode + " - " + courseName;
    }
}
