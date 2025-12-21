package com.example.student_activity_points.controller;

import com.example.student_activity_points.domain.StudentActivity;
import com.example.student_activity_points.service.StudentActivityService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/student/activity")
public class StudentActivityController {

    @Autowired
    private StudentActivityService studentActivityService;

    private static final Logger log = LoggerFactory.getLogger(StudentActivityController.class);

    @GetMapping("/{sid}")
    public ResponseEntity<?> getStudentActivities(@PathVariable String sid) {
        try {
            if (sid == null || sid.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Student ID is required");
            }

            List<StudentActivity> activities = studentActivityService.getStudentActivities(sid);
            log.debug("Retrieved {} activities for student: {}", activities.size(), sid);
            return ResponseEntity.ok(activities);

        } catch (Exception ex) {
            log.error("Error fetching activities for student: {}", sid, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch student activities");
        }
    }
}