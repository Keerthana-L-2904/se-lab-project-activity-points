package com.example.student_activity_points.controller;

import com.example.student_activity_points.domain.Departments;
import com.example.student_activity_points.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.student_activity_points.repository.DepartmentsRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    @Autowired
    private DepartmentsRepository departmentRepository;

    // ✅ Get all departments
    @GetMapping
    public ResponseEntity<List<Departments>> getAllDepartments() {
        List<Departments> departments = (List<Departments>) departmentRepository.findAll();
        return ResponseEntity.ok(departments);
    }

    // ✅ Get a department by ID (ONLY IF NEEDED FOR OTHER PURPOSES)
    @GetMapping("/{did}")
    public ResponseEntity<?> getDepartmentById(@PathVariable Long did) {
        Optional<Departments> department = departmentRepository.findById(did);
        if (department.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Department not found");
        }
        return ResponseEntity.ok(department.get());
    }
}