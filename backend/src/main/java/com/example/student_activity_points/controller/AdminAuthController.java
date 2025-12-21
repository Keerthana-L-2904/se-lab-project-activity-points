package com.example.student_activity_points.controller;

import com.example.student_activity_points.domain.Admin;
import com.example.student_activity_points.dto.AdminDTO;
import com.example.student_activity_points.dto.ResetPasswordRequest;
import com.example.student_activity_points.repository.AdminRepository;
import com.example.student_activity_points.security.JwtUtil;
import com.example.student_activity_points.service.EmailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    @Autowired
    private JwtUtil jwtUtil;

    private static final Logger log = LoggerFactory.getLogger(AdminAuthController.class);

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Admin admin) {
        try {
            // Check if admin already exists
            Admin existingAdmin = adminRepo.findByEmail(admin.getEmail());
            if (existingAdmin != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("message", "Email already registered"));
            }

            admin.setPassword(passwordEncoder.encode(admin.getPassword()));
            adminRepo.save(admin);
            
            log.info("Admin registered successfully: {}", admin.getEmail());
            return ResponseEntity.ok(Map.of("message", "Admin registered successfully"));

        } catch (Exception ex) {
            log.error("Error during admin registration", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Unable to register admin"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Admin admin) {
        try {
            Admin existingAdmin = adminRepo.findByEmail(admin.getEmail());
            
            if (existingAdmin == null) {
                log.warn("Login attempt with non-existent email: {}", admin.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Invalid credentials"));
            }

            if (!passwordEncoder.matches(admin.getPassword(), existingAdmin.getPassword())) {
                log.warn("Failed login attempt for email: {}", admin.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Invalid credentials"));
            }

            String token = jwtUtil.generateToken(admin.getEmail(), "ADMIN");

            // Create DTO (only include safe fields)
            AdminDTO adminDTO = new AdminDTO(
                existingAdmin.getId(),
                existingAdmin.getName(),
                existingAdmin.getEmail()
            );

            Map<String, Object> response = Map.of(
                "admin", adminDTO,
                "token", token
            );

            log.info("Admin logged in successfully: {}", admin.getEmail());
            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            log.error("Error during admin login", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Unable to process login"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAdminById(@PathVariable Long id) {
        try {
            Optional<Admin> admin = adminRepo.findById(id.intValue());
            
            if (admin.isEmpty()) {
                log.warn("Admin not found with id: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Admin not found");
            }

            return ResponseEntity.ok(admin.get());

        } catch (Exception ex) {
            log.error("Error fetching admin by id: {}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch admin");
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        try {
            Admin admin = adminRepo.findByEmail(email);
            
            if (admin == null) {
                log.warn("Password reset requested for non-existent email: {}", email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Email not found");
            }

            // Generate token + expiry
            String token = UUID.randomUUID().toString();
            admin.setResetToken(token);
            admin.setResetTokenExpiry(LocalDateTime.now().plusMinutes(30)); // valid for 30 mins
            adminRepo.save(admin);

            // Build reset link
            String resetLink = "http://localhost:5173/reset-password?token=" + token;

            // Send reset link via email
            emailService.sendEmail(admin.getEmail(), "Password Reset Link",
                    "Click here to reset your password: " + resetLink);

            log.info("Password reset link sent to: {}", email);
            return ResponseEntity.ok("Password reset link has been sent to your email");

        } catch (Exception ex) {
            log.error("Error processing forgot password request", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to process password reset request");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            Admin admin = adminRepo.findByResetToken(request.getToken());
            
            if (admin == null) {
                log.warn("Invalid reset token provided");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid or expired token");
            }

            if (admin.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
                log.warn("Expired reset token used for email: {}", admin.getEmail());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid or expired token");
            }

            admin.setPassword(passwordEncoder.encode(request.getNewPassword()));
            admin.setResetToken(null);
            admin.setResetTokenExpiry(null);
            adminRepo.save(admin);

            log.info("Password successfully reset for: {}", admin.getEmail());
            return ResponseEntity.ok("Password successfully reset");

        } catch (Exception ex) {
            log.error("Error resetting password", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to reset password");
        }
    }
}