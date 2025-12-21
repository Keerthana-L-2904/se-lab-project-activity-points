package com.example.student_activity_points.dto;

import com.example.student_activity_points.domain.Student;

public class StudentWithFADTO {
    private String sid;
    private String name;
    private String email;
    private String faName;
    private String faEmail;
    private int deptPoints;
    private int institutePoints;
    private int otherPoints;
    private int activityPoints;

    public StudentWithFADTO(Student student, String faName, String faEmail) {
        this.sid = student.getSid();
        this.name = student.getName();
        this.email = student.getEmailID();
        this.faName = faName;
        this.faEmail = faEmail;
        this.deptPoints = student.getDeptPoints();
        this.institutePoints = student.getInstitutePoints();
        this.otherPoints = student.getOtherPoints();
        this.activityPoints = student.getActivityPoints();
    }

    // Getters
    public String getSid() {
        return sid;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getFaName() {
        return faName;
    }

    public String getFaEmail() {
        return faEmail;
    }

    // Setters
    public void setSid(String sid) {
        this.sid = sid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFaName(String faName) {
        this.faName = faName;
    }

    public void setFaEmail(String faEmail) {
        this.faEmail = faEmail;
    }

    public int getDeptPoints() {
        return deptPoints;
    }

    public void setDeptPoints(int deptPoints) {
        this.deptPoints = deptPoints;
    }

    public int getInstitutePoints() {
        return institutePoints;
    }

    public void setInstitutePoints(int institutePoints) {
        this.institutePoints = institutePoints;
    }
    
    public int getOtherPoints() {
        return otherPoints;
    }

    public void setOtherPoints(int otherPoints) {
        this.otherPoints = otherPoints;
    }
    public int getActivityPoints() {
        return activityPoints;
    }

    public void setActivityPoints(int activityPoints) {
        this.activityPoints = activityPoints;
    }
}
