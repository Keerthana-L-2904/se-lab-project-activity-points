package com.example.student_activity_points.dto;

import com.example.student_activity_points.domain.Student;

public class StudentWithMandatoryDTO {
    private Student student;
    private Long mandatoryCount;

    public StudentWithMandatoryDTO(Student student, Long mandatoryCount) {
        this.student = student;
        this.mandatoryCount = mandatoryCount;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Long getMandatoryCount() {
        return mandatoryCount;
    }

    public void setMandatoryCount(Long mandatoryCount) {
        this.mandatoryCount = mandatoryCount;
    }
}