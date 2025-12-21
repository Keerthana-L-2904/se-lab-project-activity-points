package com.example.student_activity_points.controller;

import com.example.student_activity_points.domain.Departments;
import com.example.student_activity_points.repository.DepartmentsRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    @Autowired
    private DepartmentsRepository departmentRepository;

    private static final Logger log = LoggerFactory.getLogger(DepartmentController.class);

    @GetMapping
    public ResponseEntity<?> getAllDepartments() {
        try {
            List<Departments> departments = (List<Departments>) departmentRepository.findAll();
            log.debug("Retrieved {} departments", departments.size());
            return ResponseEntity.ok(departments);

        } catch (Exception ex) {
            log.error("Error fetching departments", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch departments");
        }
    }

    @GetMapping("/{did}")
    public ResponseEntity<?> getDepartmentById(@PathVariable Long did) {
        try {
            Optional<Departments> department = departmentRepository.findById(did);
            
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