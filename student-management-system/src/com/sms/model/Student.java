package com.sms.model;

import java.sql.Date;
import java.sql.Timestamp;

public class Student {
    private int id;
    private String studentId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String gender;
    private Date dateOfBirth;
    private String department;
    private String yearLevel;
    private String status;
    private Timestamp createdAt;

    public Student() {}

    public Student(int id, String studentId, String firstName, String lastName, String email, 
                   String phone, String gender, Date dateOfBirth, String department, 
                   String yearLevel, String status, Timestamp createdAt) {
        this.id = id;
        this.studentId = studentId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.department = department;
        this.yearLevel = yearLevel;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Date getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(Date dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getYearLevel() { return yearLevel; }
    public void setYearLevel(String yearLevel) { this.yearLevel = yearLevel; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return studentId + " - " + getFullName();
    }
}
