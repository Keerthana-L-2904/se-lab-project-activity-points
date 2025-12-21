package com.example.student_activity_points.service;

import com.example.student_activity_points.domain.StudentActivity;
import com.example.student_activity_points.repository.StudentActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentActivityService {

    @Autowired
    private StudentActivityRepository studentActivityRepository;

    // Fetch all activities for a student using SID
    public List<StudentActivity> getStudentActivities(String sid) {
        return studentActivityRepository.findBySid(sid);
    }
}
