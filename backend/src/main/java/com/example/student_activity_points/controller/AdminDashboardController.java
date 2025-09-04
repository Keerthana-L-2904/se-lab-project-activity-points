package com.example.student_activity_points.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.student_activity_points.repository.ActivityRepository;
import com.example.student_activity_points.repository.FARepository;
import com.example.student_activity_points.repository.StudentRepository;

@RestController
@RequestMapping("/api/admin/")
public class AdminDashboardController {
     @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private FARepository faRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @GetMapping("/dashboard-details")
    public Map<String, Long> getDashboardStats() {
        long students_count = studentRepository.count();
        long fa_count = faRepository.count();
        long activities_count = activityRepository.count();

        return Map.of(
            "students_count", students_count,
            "fa_count", fa_count,
            "activities_count", activities_count
        );
    }
}