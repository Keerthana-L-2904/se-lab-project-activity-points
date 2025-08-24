package com.example.student_activity_points.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.student_activity_points.domain.Activity;
import com.example.student_activity_points.domain.Fa;
import com.example.student_activity_points.domain.Student;
import com.example.student_activity_points.repository.FARepository;
import com.example.student_activity_points.repository.StudentRepository;

@RestController
@RequestMapping("/api/admin/manage-users")
public class AdminManageUsersController {
    
    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private FARepository faRepository;

    @GetMapping("/student")
     public ResponseEntity<?> getStudents() {
        try {
            List<Student> students = (List<Student>) studentRepository.findAll();
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
        }
    }

    @GetMapping("/fa")
     public ResponseEntity<?> getFA() {
        try {
            List<Fa> fas = (List<Fa>) faRepository.findAll();
            return ResponseEntity.ok(fas);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
        }
    }

    @PostMapping ("/student")
    public ResponseEntity<?> addStudent(@RequestBody Student student) {
        try {

            System.out.println("Received Student: " + student); // Debugging
            if (student.getSid() == null || student.getSid().trim().isEmpty()) {
                return ResponseEntity.status(400).body("Error: sid (roll number) must be provided.");
            }
    
            if (studentRepository.existsById(student.getSid())) {
                return ResponseEntity.status(400).body("Error: Student with sid " + student.getSid() + " already exists.");
            }
            System.out.println("FAID: " + student.getFaid());
            System.out.println("SID: " + student.getSid());
            System.out.println("DID: " + student.getDid());
            System.out.println("Dept points: " + student.getDeptPoints());
            System.out.println("Inst points: " + student.getInstitutePoints());
            System.out.println("EmailID: " + student.getEmailID());



            
            Student savedStudent = studentRepository.save(student);
            return ResponseEntity.ok(savedStudent);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error adding student: " + e.getMessage());
        }
    }

    @PostMapping ("/fa")
    public ResponseEntity<?> addFA(@RequestBody Fa fa) {
        try {
            Fa savedFa = faRepository.save(fa);
            return ResponseEntity.ok(savedFa);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error adding fa: " + e.getMessage());
        }
    }

    @PutMapping("/student/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable String id, @RequestBody Student updatedStudent) {
        try {
            System.out.println(id);
            Optional<Student> existingStudentOpt = studentRepository.findById(id);
            if (existingStudentOpt .isPresent()) {
                Student existingStudent = existingStudentOpt.get();
                existingStudent .setName(updatedStudent.getName());
                existingStudent.setDid(updatedStudent.getDid());
                existingStudent.setEmailID(updatedStudent.getEmailID());
                existingStudent.setFaid(updatedStudent.getFaid());
                existingStudent.setInstitutePoints(updatedStudent.getInstitutePoints());
                existingStudent.setDeptPoints(updatedStudent.getDeptPoints());

                Student savedStudent = studentRepository.save(existingStudent);
                return ResponseEntity.ok(savedStudent);
            } else {
                return ResponseEntity.status(404).body("Student record not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error updating Student record: " + e.getMessage());
        }
    
}

@PutMapping("/fa/{id}")
public ResponseEntity<?> updateFA(@PathVariable Long id, @RequestBody Fa updatedFa) {
    try {
        Optional<Fa> existingFaOpt = faRepository.findById(id);
        if (existingFaOpt.isPresent()) {
            Fa existingFa = existingFaOpt.get();
            existingFa.setName(updatedFa.getName());
            existingFa.setEmailID(updatedFa.getEmailID());
            existingFa.setDID(updatedFa.getDID());


            Fa savedFa = faRepository.save(existingFa);
            return ResponseEntity.ok(savedFa);
        } else {
            return ResponseEntity.status(404).body("Fa record not found");
        }
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(500).body("Error updating Fa record: " + e.getMessage());
    }
}

    @DeleteMapping("/student/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable String id) {
        try {

            if (studentRepository.existsById(id)) {
                studentRepository.deleteById(id);
                return ResponseEntity.ok("Student record deleted successfully");
            } else {
                return ResponseEntity.status(404).body("Student record not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error deleting student record: " + e.getMessage());
        }
    }
    @DeleteMapping("/fa/{id}")
    public ResponseEntity<?> deleteActivity(@PathVariable Long id) {
        try {

            if (faRepository.existsById(id)) {
                faRepository.deleteById(id);
                return ResponseEntity.ok("Fa record deleted successfully");
            } else {
                return ResponseEntity.status(404).body("Fa record not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error deleting Fa record: " + e.getMessage());
        }
    }
}


