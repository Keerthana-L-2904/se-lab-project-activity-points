package com.example.student_activity_points.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.student_activity_points.repository.*;

import java.util.*;
import java.util.stream.Collectors;
import com.example.student_activity_points.domain.Fa;

import com.example.student_activity_points.domain.Student;
import com.example.student_activity_points.domain.StudentActivity;

@RestController
@RequestMapping("/api/fa")
public class FaApprovalsController {

    private final StudentActivityRepository studentActivityRepository;

    @Autowired
    private FARepository faRepository;

    @Autowired
    private StudentRepository studentRepository;

    FaApprovalsController(StudentActivityRepository studentActivityRepository) {
        this.studentActivityRepository = studentActivityRepository;
    }

    @GetMapping("/details")
    public ResponseEntity<?> getFaDetails(@RequestParam String email) {
        Optional<Fa> faOptional = faRepository.findByEmailID(email);
        if (!faOptional.isPresent()) {
            return ResponseEntity.status(404).body("FA not found");
        }
        Fa fa = faOptional.get();
        Long faId = fa.getFAID();

        // Get all students under this FA
        List<Student> myStudents = studentRepository.findByFAID(faId.intValue());
        List<String> myStudentsIds = myStudents.stream().map(Student::getSid).collect(Collectors.toList());

        // Get all StudentActivity records for these students
        List<StudentActivity> activities = new ArrayList<>();
        for (String sid : myStudentsIds) {
            activities.addAll(studentActivityRepository.findBySid(sid));
        }

        // Structure response
        List<Map<String, Object>> responseList = new ArrayList<>();
        for (StudentActivity sa : activities) {
            Map<String, Object> response = new HashMap<>();
            response.put("sid", sa.getSid());
            response.put("activity_name", sa.getTitle());
            response.put("activity_date", sa.getDate());
            response.put("link", sa.getLink());
            response.put("type", sa.getActivityType());
            response.put("validated", sa.getValidated() != null ? sa.getValidated().toString() : "Pending");
            responseList.add(response);
        }
        return ResponseEntity.ok(responseList);
    }
    // Approval logic would be implemented here if needed, using StudentActivity only

    @GetMapping("/get-Fa")
    public ResponseEntity<String> getFa(@RequestParam String sid) {
        Optional<Student> student=studentRepository.findBySid(sid);
        if(student==null){
           return ResponseEntity.status(404).body("Can't find FA for this student");

        }
        return ResponseEntity.ok(String.valueOf(student.get().getFaid()));

    }
}
