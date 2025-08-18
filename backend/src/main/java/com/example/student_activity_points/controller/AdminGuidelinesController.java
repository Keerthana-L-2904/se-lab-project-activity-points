package com.example.student_activity_points.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.student_activity_points.repository.GuidelinesRepository;
import com.example.student_activity_points.domain.Guidelines;
import com.example.student_activity_points.domain.Student;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/admin/guidelines")
public class AdminGuidelinesController {

    @Autowired
    private GuidelinesRepository guidelinesRepository;

    @GetMapping
    public ResponseEntity<?> getGuidelines() {
        try {
            List<Guidelines> guidelines = (List<Guidelines>) guidelinesRepository.findAll();
            return ResponseEntity.ok(guidelines);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
        }
    }

    @PostMapping
    public Guidelines createGuideline(@RequestBody Guidelines guideline) {
        return guidelinesRepository.save(guideline);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Guidelines> updateGuideline(@PathVariable int id, @RequestBody Guidelines updatedGuideline) {
        return guidelinesRepository.findById(id).map(existingGuideline -> {
            existingGuideline.setBody(updatedGuideline.getBody());
            return ResponseEntity.ok(guidelinesRepository.save(existingGuideline));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGuideline(@PathVariable int id) {
        if (guidelinesRepository.existsById(id)) {
            guidelinesRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
    
}
