package com.example.student_activity_points.controller;
import com.example.student_activity_points.domain.Student;
import com.example.student_activity_points.domain.StudentActivity;
import com.example.student_activity_points.repository.RequestsRepository;
import com.example.student_activity_points.repository.StudentActivityRepository;
import com.example.student_activity_points.dto.TrackingDTO;
import com.example.student_activity_points.domain.Requests;
import com.example.student_activity_points.service.TrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController
@RequestMapping("/api/{sid}/tracking")
public class TrackingController {

    private final TrackingService trackingService;

    public TrackingController(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @GetMapping
    public ResponseEntity<TrackingDTO> getTracking(@PathVariable String sid) {
        return ResponseEntity.ok(trackingService.getTrackingBySid(sid));
    }
}
