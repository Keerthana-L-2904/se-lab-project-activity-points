package com.example.student_activity_points.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Optional;

import com.example.student_activity_points.domain.Fa;
import com.example.student_activity_points.domain.Student;
import com.example.student_activity_points.repository.FARepository;
import com.example.student_activity_points.repository.StudentRepository;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private FARepository faRepository;

 @PostMapping("/login-student")
public ResponseEntity<?> loginstudent(@RequestBody Map<String, String> request) {
    String email = request.get("email");
    System.out.println("Student Login - Email: " + email);

    Optional<Student> studentOptional = studentRepository.findByEmailID(email);

    if (studentOptional.isPresent()) {
        Student student = studentOptional.get();
        Map<String, Object> response = Map.of(
            "sid", student.getSid(),
            "name", student.getName(),
            "email", student.getEmailID(),
            "role", "student" // ✅ Added role for student
        );
        return ResponseEntity.ok(response);
    } else {
        return ResponseEntity.status(401).body("Invalid email");
    }
}

    @PostMapping("/login-fa")
public ResponseEntity<?> loginFA(@RequestBody Map<String, String> request) {
    String email = request.get("email");
    System.out.println("FA Login - Email: " + email);

    Optional<Fa> faOptional = faRepository.findByEmailID(email);

    if (faOptional.isPresent()) {
        Fa fa = faOptional.get();
        Map<String, Object> response = Map.of(
            "faid", fa.getFAID(),
            "name", fa.getName(),
            "email", fa.getEmailID(),
            "role", "fa"
        );
        return ResponseEntity.ok(response);
    } else {
        return ResponseEntity.status(401).body("Invalid email");
    }
}

    }
