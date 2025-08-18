package com.example.student_activity_points.controller;

import com.example.student_activity_points.domain.Fa;
import com.example.student_activity_points.domain.Departments;
import com.example.student_activity_points.service.FaService;
import com.example.student_activity_points.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/fa")
@CrossOrigin(origins = "http://localhost:5173") // Ensure frontend can access API
public class FaController {

    @Autowired
    private FaService faService;

    @Autowired
    private DepartmentService departmentService; // Add department service

    @GetMapping("/{faid}")
    public Optional<Fa> getFaById(@PathVariable Long faid) {
        return faService.getFaById(faid);
    }

    @GetMapping("/dashboard") // Matches frontend request
    public List<Fa> getAllFacultyAdvisors(){
        return faService.getAllFacultyAdvisors(); // Should return a List
    }

    // 🔥 **NEW: Get Department by ID**
    @GetMapping("/departments/{did}")
    public Optional<Departments> getDepartmentById(@PathVariable Integer did) {
        return departmentService.getDepartmentById((long)did);
    }
}
