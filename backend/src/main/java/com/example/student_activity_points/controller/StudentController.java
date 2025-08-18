package com.example.student_activity_points.controller;

import com.example.student_activity_points.domain.Student;
import com.example.student_activity_points.domain.StudentActivity;
import com.example.student_activity_points.domain.Announcements;
import com.example.student_activity_points.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173") // Adjust based on frontend URL
public class StudentController {

    @Autowired
    private StudentService studentService;

    // Get student details
    @GetMapping("student/{studentID}")
    public Optional<Student> getStudent(@PathVariable String studentID) {
        return studentService.getStudentById(studentID);
    }

    // Get total department points
    @GetMapping("student/{studentID}/department-points")
    public Integer getDepartmentPoints(@PathVariable String studentID) {
        return studentService.getDepartmentPoints(studentID);
    }
    @GetMapping("student/{studentID}/name")
    public String getStudentName(@PathVariable String studentID) {
        return studentService.getStudentById(studentID)
                .map(Student::getName)
                .orElse("Name not found");
    }

    // Get total institutional points
    @GetMapping("student/{studentID}/institutional-points")
    public Integer getInstitutionalPoints(@PathVariable String studentID) {
        return studentService.getInstitutionalPoints(studentID);
    }

    // Get total activity points
    @GetMapping("student/{studentID}/activity-points")
    public Integer getActivityPoints(@PathVariable String studentID) {
        return studentService.getActivityPoints(studentID);
    }

    // Get student activity history
    @GetMapping("student/{studentID}/activities")
    public List<StudentActivity> getStudentActivities(@PathVariable String studentID) {
        return studentService.getStudentActivities(studentID);
    }

    // Get announcements/notifications
    @GetMapping("student/announcements")
    public List<Announcements> getAnnouncements() {
        return studentService.getAnnouncements();
    }
    @GetMapping("/fa/student-list/{FAID}")
    public List<Student> getStudentsByFAID(@PathVariable int FAID) {
    return studentService.getStudentsByFAID(FAID);
}

   @GetMapping("/fa/student-details/{sid}")
    public ResponseEntity<?> getStudentBySid(@PathVariable String sid) {
        Optional<Student> student = studentService.getStudentById(sid);
        
        if (student.isPresent()) {
            List<StudentActivity> activities = studentService.getStudentActivities(sid);
            
            Map<String, Object> response = new HashMap<>();
            response.put("student", student.get());
            response.put("activities", activities);
            
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(404).body(Map.of("error", "Student not found"));
        }
    }
}
