package com.sms.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardStats {
    private int totalStudents;
    private int activeStudents;
    private int totalCourses;
    private int totalGrades;
    private double averageMarks;
    private Map<String, Integer> studentsByDepartment = new LinkedHashMap<>();
    private Map<String, Integer> studentsByYear = new LinkedHashMap<>();

    public DashboardStats() {}

    public int getTotalStudents() { return totalStudents; }
    public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }

    public int getActiveStudents() { return activeStudents; }
    public void setActiveStudents(int activeStudents) { this.activeStudents = activeStudents; }

    public int getTotalCourses() { return totalCourses; }
    public void setTotalCourses(int totalCourses) { this.totalCourses = totalCourses; }

    public int getTotalGrades() { return totalGrades; }
    public void setTotalGrades(int totalGrades) { this.totalGrades = totalGrades; }

    public double getAverageMarks() { return averageMarks; }
    public void setAverageMarks(double averageMarks) { this.averageMarks = averageMarks; }

    public Map<String, Integer> getStudentsByDepartment() { return studentsByDepartment; }
    public void setStudentsByDepartment(Map<String, Integer> map) { this.studentsByDepartment = map; }

    public Map<String, Integer> getStudentsByYear() { return studentsByYear; }
    public void setStudentsByYear(Map<String, Integer> map) { this.studentsByYear = map; }
}
