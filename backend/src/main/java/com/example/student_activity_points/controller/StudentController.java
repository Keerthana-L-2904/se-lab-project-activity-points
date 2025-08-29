package com.example.student_activity_points.controller;

import com.example.student_activity_points.domain.Student;
import com.example.student_activity_points.domain.StudentActivity;
import com.example.student_activity_points.dto.StudentWithMandatoryDTO;
import com.example.student_activity_points.domain.Announcements;
import com.example.student_activity_points.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;



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
    public List<StudentWithMandatoryDTO> getStudentsByFAID(@PathVariable int FAID) {
        return studentService.getStudentsByFAIDWithMandatoryCount(FAID);
    }

    @GetMapping("fa/student-list/{FAID}/search")
    public List<StudentWithMandatoryDTO> searchStudents(
            @PathVariable int FAID,
            @RequestParam String name
    ) {
        return studentService.searchStudentsByFAIDAndName(FAID, name);
    }

    @GetMapping("fa/student-list/{FAID}/search-by-mandatory")
    public List<StudentWithMandatoryDTO> searchByMandatoryCount(
            @PathVariable int FAID,
            @RequestParam Long mandatoryCount
    ) {
        return studentService.searchStudentsByMandatoryCount(FAID, mandatoryCount);
    }

    @GetMapping("fa/student-list/{FAID}/sort-by-asc")
    public List<StudentWithMandatoryDTO> sortByAsc(@PathVariable int FAID) {
        return studentService.getStudentsByFAIDWithMandatoryCountAsc(FAID);
    }
    
    @GetMapping("fa/student-list/{FAID}/sort-by-desc")
    public List<StudentWithMandatoryDTO> sortByDesc(@PathVariable int FAID) {
        return studentService.getStudentsByFAIDWithMandatoryCountDesc(FAID);
    }

    @GetMapping("fa/student-list/{FAID}/filter-dept-points-above")
    public List<StudentWithMandatoryDTO> filterDeptPointsAbove(
            @PathVariable int FAID,
            @RequestParam Long points
    ) {
        return studentService.filterDeptPointsAbove(FAID, points);
    }

    @GetMapping("fa/student-list/{FAID}/filter-dept-points-below")
    public List<StudentWithMandatoryDTO> filterDeptPointsBelow(
            @PathVariable int FAID,
            @RequestParam Long points
    ) {
        return studentService.filterDeptPointsBelow(FAID, points);
    }

    @GetMapping("fa/student-list/{FAID}/filter-inst-points-above")
    public List<StudentWithMandatoryDTO> filterInstPointsAbove(
            @PathVariable int FAID,
            @RequestParam Long points
    ) {
        return studentService.filterInstPointsAbove(FAID, points);
    }

    @GetMapping("fa/student-list/{FAID}/filter-inst-points-below")
    public List<StudentWithMandatoryDTO> filterInstPointsBelow(
            @PathVariable int FAID,
            @RequestParam Long points
    ) {
        return studentService.filterInstPointsBelow(FAID, points);
    }

    // Activity Points - Above
    @GetMapping("fa/student-list/{FAID}/filter-activity-points-above")
    public List<StudentWithMandatoryDTO> filterActivityPointsAbove(
            @PathVariable int FAID,
            @RequestParam Long points
    ) {
        return studentService.filterActivityPointsAbove(FAID, points);
    }

    // Activity Points - Below
    @GetMapping("fa/student-list/{FAID}/filter-activity-points-below")
    public List<StudentWithMandatoryDTO> filterActivityPointsBelow(
            @PathVariable int FAID,
            @RequestParam Long points
    ) {
        return studentService.filterActivityPointsBelow(FAID, points);
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
