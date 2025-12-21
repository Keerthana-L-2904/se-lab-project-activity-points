package com.example.student_activity_points.controller;

import com.example.student_activity_points.domain.Admin;
import com.example.student_activity_points.domain.Fa;
import com.example.student_activity_points.domain.Student;
import com.example.student_activity_points.repository.AdminRepository;
import com.example.student_activity_points.repository.FARepository;
import com.example.student_activity_points.repository.StudentRepository;
import com.example.student_activity_points.security.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private FARepository faRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/login-student")
    public ResponseEntity<?> loginStudent(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Email is required"));
            }

            Optional<Student> studentOptional = studentRepository.findByEmailID(email);
            
            if (studentOptional.isEmpty()) {
                log.warn("Student login attempt with non-existent email: {}", email);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Invalid credentials"));
            }

            Student student = studentOptional.get();
            String token = jwtUtil.generateToken(student.getEmailID(), "STUDENT");
            
            Map<String, Object> response = Map.of(
                "sid", student.getSid(),
                "name", student.getName(),
                "email", student.getEmailID(),
                "role", "student",
                "token", token
            );

            log.info("Student logged in successfully: {}", email);
            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            log.error("Error during student login", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Unable to process login"));
        }
    }

    @PostMapping("/login-fa")
    public ResponseEntity<?> loginFA(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Email is required"));
            }

            Optional<Fa> faOptional = faRepository.findByEmailID(email);

            if (faOptional.isEmpty()) {
                log.warn("FA login attempt with non-existent email: {}", email);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Invalid credentials"));
            }

            Fa fa = faOptional.get();
            String token = jwtUtil.generateToken(fa.getEmailID(), "FA");
            
            Map<String, Object> response = Map.of(
                "faid", fa.getFAID(),
                "name", fa.getName(),
                "email", fa.getEmailID(),
                "role", "fa",
                "token", token
            );

            log.info("FA logged in successfully: {}", email);
            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            log.error("Error during FA login", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Unable to process login"));
        }
    }
}