package com.example.student_activity_points.controller;

import com.example.student_activity_points.domain.StudentActivity;
import com.example.student_activity_points.security.AuthUser;
import com.example.student_activity_points.service.StudentActivityService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/student/activity")
public class StudentActivityController {

    @Autowired
    private StudentActivityService studentActivityService;

    private static final Logger log = LoggerFactory.getLogger(StudentActivityController.class);

    @GetMapping()
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getMyActivities() {
        AuthUser user = (AuthUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return ResponseEntity.ok(
            studentActivityService.getStudentActivities(user.getSid())
        );
    }
}