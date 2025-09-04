package com.example.student_activity_points.controller;

import com.example.student_activity_points.domain.StudentActivity;
import com.example.student_activity_points.service.StudentActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/student/activity")
public class StudentActivityController {

    @Autowired
    private StudentActivityService studentActivityService;

    // Get all activities for a specific student
    @GetMapping("/{sid}")
    public List<StudentActivity> getStudentActivities(@PathVariable String sid) {
        return studentActivityService.getStudentActivities(sid);
    }
}
