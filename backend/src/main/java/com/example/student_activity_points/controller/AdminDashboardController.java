package com.example.student_activity_points.controller;

import com.example.student_activity_points.repository.ActivityRepository;
import com.example.student_activity_points.repository.FARepository;
import com.example.student_activity_points.repository.StudentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/admin/")
public class AdminDashboardController {
    
    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private FARepository faRepository;

    @Autowired
    private ActivityRepository activityRepository;

    private static final Logger log = LoggerFactory.getLogger(AdminDashboardController.class);

    @GetMapping("/dashboard-details")
    public ResponseEntity<?> getDashboardStats() {
        try {
            long studentsCount = studentRepository.count();
            long faCount = faRepository.count();
            long activitiesCount = activityRepository.count();

            Map<String, Long> stats = Map.of(
                "students_count", studentsCount,
                "fa_count", faCount,
                "activities_count", activitiesCount
            );

            log.debug("Dashboard stats retrieved: {} students, {} FAs, {} activities", 
                     studentsCount, faCount, activitiesCount);
            
            return ResponseEntity.ok(stats);

        } catch (Exception ex) {
            log.error("Error fetching dashboard statistics", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch dashboard statistics");
        }
    }
}