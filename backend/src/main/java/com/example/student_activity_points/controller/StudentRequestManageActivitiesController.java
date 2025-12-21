package com.example.student_activity_points.controller;

import com.example.student_activity_points.domain.Activity;
import com.example.student_activity_points.repository.ActivityRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/student")
public class StudentRequestManageActivitiesController {
    
    @Autowired
    private ActivityRepository activityRepository;
    
    private static final Logger log = LoggerFactory.getLogger(StudentRequestManageActivitiesController.class);
    
    @GetMapping("/manage-activities")
    public ResponseEntity<?> getActivities(
            @RequestParam(required = false) String mandatory,
            @RequestParam(required = false) String type) {
        try {
            List<Activity> activities;

            if (mandatory != null) {
                Integer mandatoryValue = "yes".equalsIgnoreCase(mandatory) ? 1 : 0;
                activities = activityRepository.findByMandatory(mandatoryValue);
            } else {
                activities = (List<Activity>) activityRepository.findAll();
            }

            List<Map<String, Object>> response = new ArrayList<>();
            for (Activity activity : activities) {
                Map<String, Object> map = new HashMap<>();
                map.put("actID", activity.getActID());
                map.put("name", activity.getName());
                map.put("description", activity.getDescription());
                map.put("points", activity.getPoints());
                map.put("DID", activity.getDID());
                map.put("date", activity.getDate());
                map.put("end_date", activity.getEnd_date());
                map.put("type", activity.getType());
                map.put("isuploaded", activity.getIsuploaded());
                map.put("mandatory", activity.getMandatory());
                
                response.add(map);
            }
            
            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            log.error("Error loading activities", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch activities");
        }
    }
}