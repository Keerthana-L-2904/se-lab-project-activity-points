package com.example.student_activity_points.controller;

import com.example.student_activity_points.domain.Activity;
import com.example.student_activity_points.repository.ActivityRepository;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/activities")
@CrossOrigin(origins = "http://localhost:5173")
public class ActivityController {

    private final ActivityRepository activityRepository;
    
    private static final Logger log = LoggerFactory.getLogger(ActivityController.class);

    public ActivityController(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @GetMapping
    public ResponseEntity<?> getAllActivities() {
        try {
            List<Activity> activities = (List<Activity>) activityRepository.findAll();
            log.debug("Retrieved {} activities", activities.size());
            return ResponseEntity.ok(activities);

        } catch (Exception ex) {
            log.error("Error loading activities", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch activities");
        }
    }
}