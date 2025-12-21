package com.example.student_activity_points.controller;

import com.example.student_activity_points.dto.TrackingDTO;
import com.example.student_activity_points.service.TrackingService;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/{sid}/tracking")
public class TrackingController {

    private final TrackingService trackingService;

    private static final Logger log = LoggerFactory.getLogger(TrackingController.class);

    public TrackingController(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @GetMapping
    public ResponseEntity<?> getTracking(@PathVariable String sid) {
        try {
            if (sid == null || sid.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Student ID is required");
            }

            TrackingDTO tracking = trackingService.getTrackingBySid(sid);
            
            if (tracking == null) {
                log.warn("Tracking data not found for student: {}", sid);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Tracking data not found");
            }

            log.debug("Retrieved tracking data for student: {}", sid);
            return ResponseEntity.ok(tracking);

        } catch (Exception ex) {
            log.error("Error fetching tracking data for student: {}", sid, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch tracking data");
        }
    }
}