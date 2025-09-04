package com.example.student_activity_points.controller;

import com.example.student_activity_points.domain.Admin;
import com.example.student_activity_points.dto.ResetPasswordRequest;
import com.example.student_activity_points.repository.AdminRepository;
import com.example.student_activity_points.service.EmailService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
@CrossOrigin
public class AdminAuthController {

    @Autowired
    private AdminRepository adminRepo;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;


@PostMapping("/register")
    public Admin register(@RequestBody Admin admin) {
        // hash the password before saving
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        return adminRepo.save(admin);
    }

   @PostMapping("/login")
    public Admin login(@RequestBody Admin admin) {
        Admin existingAdmin = adminRepo.findByEmail(admin.getEmail());
        if (existingAdmin != null && passwordEncoder.matches(admin.getPassword(), existingAdmin.getPassword())) {
            return existingAdmin;
        } else {
            throw new RuntimeException("Invalid credentials");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Admin> getAdminById(@PathVariable Long id) {
        Optional<Admin> admin = adminRepo.findById(id.intValue());  
        return admin.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        Admin admin = adminRepo.findByEmail(email);
        if (admin == null) {
            return ResponseEntity.badRequest().body("Email not found");
        }

        // generate token + expiry
        String token = UUID.randomUUID().toString();
        admin.setResetToken(token);
        admin.setResetTokenExpiry(LocalDateTime.now().plusMinutes(30)); // valid for 30 mins
        adminRepo.save(admin);

        // build reset link
        String resetLink = "http://localhost:5173/reset-password?token=" + token;

        // TODO: send this link via email
        emailService.sendEmail(admin.getEmail(), "Password Reset Link",
                "Click here to reset your password: " + resetLink);

        return ResponseEntity.ok("Password reset link has been sent to your email");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        Admin admin = adminRepo.findByResetToken(request.getToken());
        if (admin == null || admin.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Invalid or expired token");
        }
    
        admin.setPassword(passwordEncoder.encode(request.getNewPassword()));
        admin.setResetToken(null);
        admin.setResetTokenExpiry(null);
        adminRepo.save(admin);
    
        return ResponseEntity.ok("Password successfully reset");
    }
    

}