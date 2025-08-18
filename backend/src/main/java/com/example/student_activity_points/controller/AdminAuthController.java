package com.example.student_activity_points.controller;

import com.example.student_activity_points.domain.Admin;
import com.example.student_activity_points.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/admin")
@CrossOrigin
public class AdminAuthController {

    @Autowired
    private AdminRepository adminRepo;

    @PostMapping("/register")
    public Admin register(@RequestBody Admin admin) {
        return adminRepo.save(admin);
    }

    @PostMapping("/login")
    public Admin login(@RequestBody Admin admin) {
        Admin existingAdmin = adminRepo.findByEmail(admin.getEmail());
        if (existingAdmin != null && existingAdmin.getPassword().equals(admin.getPassword())) {
            return existingAdmin;
        } else {
            throw new RuntimeException("Invalid credentials");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Admin> getAdminById(@PathVariable Long id) {
        Optional<Admin> admin = adminRepo.findById(id.intValue());  
        if (admin.isPresent()) {
            return ResponseEntity.ok(admin.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
