// GuidelinesController.java
package com.example.student_activity_points.controller;

import com.example.student_activity_points.domain.Guidelines;
import com.example.student_activity_points.service.GuidelinesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/guidelines")
public class GuidelinesController {
    
    @Autowired
    private GuidelinesService guidelinesService;
    
    @GetMapping
    public List<Guidelines> getAllGuidelines() {
        return guidelinesService.getAllGuidelines();
    }
    
    @GetMapping("/{gid}")
    public ResponseEntity<Guidelines> getGuidelineByGid(@PathVariable int gid) {
        return guidelinesService.getGuidelineByGid(gid)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}