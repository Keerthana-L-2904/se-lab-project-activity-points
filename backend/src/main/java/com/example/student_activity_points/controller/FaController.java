package com.example.student_activity_points.controller;

import com.example.student_activity_points.domain.Departments;
import com.example.student_activity_points.domain.Fa;
import com.example.student_activity_points.service.DepartmentService;
import com.example.student_activity_points.service.FaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/fa")
@CrossOrigin(origins = "http://localhost:5173")
public class FaController {

    @Autowired
    private FaService faService;

    @Autowired
    private DepartmentService departmentService;

    private static final Logger log = LoggerFactory.getLogger(FaController.class);

    @GetMapping("/{faid}")
    public ResponseEntity<?> getFaById(@PathVariable Long faid) {
        try {
            Optional<Fa> fa = faService.getFaById(faid);
            
            if (fa.isEmpty()) {
                log.warn("FA not found with id: {}", faid);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("FA not found");
            }

            return ResponseEntity.ok(fa.get());

        } catch (Exception ex) {
            log.error("Error fetching FA by id: {}", faid, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch FA");
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getAllFacultyAdvisors() {
        try {
            List<Fa> facultyAdvisors = faService.getAllFacultyAdvisors();
            log.debug("Retrieved {} faculty advisors", facultyAdvisors.size());
            return ResponseEntity.ok(facultyAdvisors);

        } catch (Exception ex) {
            log.error("Error fetching all faculty advisors", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch faculty advisors");
        }
    }
    
    @GetMapping("/departments/{did}")
    public ResponseEntity<?> getDepartmentById(@PathVariable Integer did) {
        try {
            Optional<Departments> department = departmentService.getDepartmentById((long) did);
            
            if (department.isEmpty()) {
                log.warn("Department not found with id: {}", did);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Department not found");
            }

            return ResponseEntity.ok(department.get());

        } catch (Exception ex) {
            log.error("Error fetching department by id: {}", did, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch department");
        }
    }
}