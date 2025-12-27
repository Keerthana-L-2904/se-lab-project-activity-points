package com.example.student_activity_points.controller;

import com.example.student_activity_points.domain.Announcements;
import com.example.student_activity_points.domain.Student;
import com.example.student_activity_points.domain.StudentActivity;
import com.example.student_activity_points.dto.StudentWithMandatoryDTO;
import com.example.student_activity_points.security.AuthUser;
import com.example.student_activity_points.service.StudentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class StudentController {

    private AuthUser currentUser() {
        return (AuthUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    @Autowired
    private StudentService studentService;

    private static final Logger log = LoggerFactory.getLogger(StudentController.class);

    @GetMapping("student")
    public ResponseEntity<?> getStudent() {
        String studentID=null;
        try {
            studentID = currentUser().getSid();
            return studentService.getStudentWithFA(studentID)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Student not found");
                    });

        } catch (Exception ex) {
            log.error("Error fetching student: {}", studentID, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch student");
        }
    }

    @GetMapping("student/department-points")
    public ResponseEntity<?> getDepartmentPoints() {
        String studentID=null;
        try {
            studentID = currentUser().getSid();
            Integer points = studentService.getDepartmentPoints(studentID);
            
            if (points == null) {
                log.warn("Department points not found for student: {}", studentID);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Student not found");
            }

            return ResponseEntity.ok(points);

        } catch (Exception ex) {
            log.error("Error fetching department points for student: {}", studentID, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch department points");
        }
    }

    @GetMapping("student/name")
    public ResponseEntity<?> getStudentName() {
        String studentID=null;
        try {
            studentID = currentUser().getSid();
            Optional<Student> student = studentService.getStudentById(studentID);
            
            if (student.isEmpty()) {
                log.warn("Student not found: {}", studentID);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Student not found");
            }

            return ResponseEntity.ok(student.get().getName());

        } catch (Exception ex) {
            log.error("Error fetching student name: {}", studentID, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch student name");
        }
    }

    @GetMapping("student/institutional-points")
    public ResponseEntity<?> getInstitutionalPoints() {
        String studentID=null;
        try {
            studentID = currentUser().getSid();
            Integer points = studentService.getInstitutionalPoints(studentID);
            
            if (points == null) {
                log.warn("Institutional points not found for student: {}", studentID);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Student not found");
            }

            return ResponseEntity.ok(points);

        } catch (Exception ex) {
            log.error("Error fetching institutional points for student: {}", studentID, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch institutional points");
        }
    }

    @GetMapping("student/activity-points")
    public ResponseEntity<?> getActivityPoints() {
        String studentID = null;
        try {
            studentID = currentUser().getSid();
            Integer points = studentService.getActivityPoints(studentID);
            
            if (points == null) {
                log.warn("Activity points not found for student: {}", studentID);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Student not found");
            }

            return ResponseEntity.ok(points);

        } catch (Exception ex) {
            log.error("Error fetching activity points for student: {}", studentID, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch activity points");
        }
    }

    @GetMapping("student/activities")
    public ResponseEntity<?> getStudentActivities() {
        String studentID = null;
        try {
            studentID = currentUser().getSid();
            List<StudentActivity> activities = studentService.getStudentActivities(studentID);
            log.debug("Retrieved {} activities for student: {}", activities.size(), studentID);
            return ResponseEntity.ok(activities);

        } catch (Exception ex) {
            log.error("Error fetching activities for student: {}", studentID, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch student activities");
        }
    }

    @GetMapping("student/latest-activity")
    public ResponseEntity<?> getLatestActivity() {
        String studentID = null;
        try {
            studentID = currentUser().getSid();
            StudentActivity activity = studentService.getLatestActivity(studentID);
            
            if (activity == null) {
                log.warn("No activities found for student: {}", studentID);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No activities found");
            }

            return ResponseEntity.ok(activity);

        } catch (Exception ex) {
            log.error("Error fetching latest activity for student: {}", studentID, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch latest activity");
        }
    }

    @GetMapping("student/announcements/HELLO")
    public ResponseEntity<?> getAnnouncements() {
        try {
            List<Announcements> announcements = studentService.getAnnouncements();
            log.debug("Retrieved {} announcements", announcements.size());
            return ResponseEntity.ok(announcements);

        } catch (Exception ex) {
            log.error("Error fetching announcements", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch announcements");
        }
    }

    @GetMapping("/fa/student-list")
    public ResponseEntity<?> getStudentsByFAID() {
        Long FAID = null;
        try {
            FAID= currentUser().getFaid();
            List<Student> students = studentService.getStudentsByFAID(FAID.intValue());
            log.debug("Retrieved {} students for FA: {}", students.size(), FAID);
            return ResponseEntity.ok(students);

        } catch (Exception ex) {
            log.error("Error fetching students for FA: {}", FAID, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch students");
        }
    }

    @GetMapping("/fa/student-list/with-mandatory")
    public ResponseEntity<?> getStudentsByFAIDWithMandatory() {
        Long FAID = null;
        try {
            FAID = currentUser().getFaid();
            List<StudentWithMandatoryDTO> students = studentService.getStudentsByFAIDWithMandatoryCount(FAID.intValue());
            log.debug("Retrieved {} students with mandatory count for FA: {}", students.size(), FAID);
            return ResponseEntity.ok(students);

        } catch (Exception ex) {
            log.error("Error fetching students with mandatory count for FA: {}", FAID, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch students");
        }
    }

    @GetMapping("fa/student-list/search")
    public ResponseEntity<?> searchStudents(
            @RequestParam String name) {
                Long FAID = null;
        try {
            FAID =currentUser().getFaid();
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Search name is required");
            }

            List<StudentWithMandatoryDTO> students = studentService.searchStudentsByFAIDAndName(FAID.intValue(), name);
            log.debug("Search found {} students for FA: {} with name: {}", students.size(), FAID, name);
            return ResponseEntity.ok(students);

        } catch (Exception ex) {
            log.error("Error searching students for FA: {} with name: {}", FAID, name, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to search students");
        }
    }

    @GetMapping("fa/student-list/search-by-mandatory")
    public ResponseEntity<?> searchByMandatoryCount(
            @RequestParam Long mandatoryCount) {
                Long FAID = null;
        try {
            FAID = currentUser().getFaid();
            if (mandatoryCount == null || mandatoryCount < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Valid mandatory count is required");
            }

            List<StudentWithMandatoryDTO> students = studentService.searchStudentsByMandatoryCount(FAID.intValue(), mandatoryCount);
            log.debug("Search found {} students for FA: {} with mandatory count: {}", 
                     students.size(), FAID, mandatoryCount);
            return ResponseEntity.ok(students);

        } catch (Exception ex) {
            log.error("Error searching students by mandatory count for FA: {}", FAID, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to search students");
        }
    }

    @GetMapping("fa/student-list/sort-by-asc")
    public ResponseEntity<?> sortByAsc() {
        Long FAID = null;
        try {
            FAID = currentUser().getFaid();
            List<StudentWithMandatoryDTO> students = studentService.getStudentsByFAIDWithMandatoryCountAsc(FAID.intValue());
            log.debug("Retrieved {} students sorted ascending for FA: {}", students.size(), FAID);
            return ResponseEntity.ok(students);

        } catch (Exception ex) {
            log.error("Error sorting students ascending for FA: {}", FAID, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to sort students");
        }
    }

    @GetMapping("fa/student-list/sort-by-desc")
    public ResponseEntity<?> sortByDesc() {
        Long FAID = null;
        try {
            FAID = currentUser().getFaid();
            List<StudentWithMandatoryDTO> students = studentService.getStudentsByFAIDWithMandatoryCountDesc(FAID.intValue());
            log.debug("Retrieved {} students sorted descending for FA: {}", students.size(), FAID);
            return ResponseEntity.ok(students);

        } catch (Exception ex) {
            log.error("Error sorting students descending for FA: {}", FAID, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to sort students");
        }
    }

    @GetMapping("fa/student-list/filter-dept-points-above")
    public ResponseEntity<?> filterDeptPointsAbove(
            @RequestParam Long points) {
                Long FAID = null;
        try {
            FAID = currentUser().getFaid();
            if (points == null || points < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Valid points value is required");
            }

            List<StudentWithMandatoryDTO> students = studentService.filterDeptPointsAbove(FAID.intValue(), points);
            log.debug("Filtered {} students with dept points above {} for FA: {}", 
                     students.size(), points, FAID);
            return ResponseEntity.ok(students);

        } catch (Exception ex) {
            log.error("Error filtering dept points above for FA: {}", FAID, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to filter students");
        }
    }

    @GetMapping("fa/student-list/filter-dept-points-below")
    public ResponseEntity<?> filterDeptPointsBelow(
            @RequestParam Long points) {
                Long FAID = null;
        try {
            FAID = currentUser().getFaid();
            if (points == null || points < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Valid points value is required");
            }

            List<StudentWithMandatoryDTO> students = studentService.filterDeptPointsBelow(FAID.intValue(), points);
            log.debug("Filtered {} students with dept points below {} for FA: {}", 
                     students.size(), points, FAID);
            return ResponseEntity.ok(students);

        } catch (Exception ex) {
            log.error("Error filtering dept points below for FA: {}", FAID, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to filter students");
        }
    }

    @GetMapping("fa/student-list/filter-inst-points-above")
    public ResponseEntity<?> filterInstPointsAbove(
            @RequestParam Long points) {
                Long FAID = null;
        try {
            FAID = currentUser().getFaid();
            if (points == null || points < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Valid points value is required");
            }

            List<StudentWithMandatoryDTO> students = studentService.filterInstPointsAbove(FAID.intValue(), points);
            log.debug("Filtered {} students with inst points above {} for FA: {}", 
                     students.size(), points, FAID);
            return ResponseEntity.ok(students);

        } catch (Exception ex) {
            log.error("Error filtering inst points above for FA: {}", FAID, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to filter students");
        }
    }

    @GetMapping("fa/student-list/filter-inst-points-below")
    public ResponseEntity<?> filterInstPointsBelow(

            @RequestParam Long points) {
                Long FAID = null;
        try {
            FAID = currentUser().getFaid();
            if (points == null || points < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Valid points value is required");
            }

            List<StudentWithMandatoryDTO> students = studentService.filterInstPointsBelow(FAID.intValue(), points);
            log.debug("Filtered {} students with inst points below {} for FA: {}", 
                     students.size(), points, FAID);
            return ResponseEntity.ok(students);

        } catch (Exception ex) {
            log.error("Error filtering inst points below for FA: {}", FAID, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to filter students");
        }
    }

    @GetMapping("fa/student-list/filter-activity-points-above")
    public ResponseEntity<?> filterActivityPointsAbove(
            @RequestParam Long points) {
                Long FAID = null;

        try {
            FAID = currentUser().getFaid();
            if (points == null || points < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Valid points value is required");
            }

            List<StudentWithMandatoryDTO> students = studentService.filterActivityPointsAbove(FAID.intValue(), points);
            log.debug("Filtered {} students with activity points above {} for FA: {}", 
                     students.size(), points, FAID);
            return ResponseEntity.ok(students);

        } catch (Exception ex) {
            log.error("Error filtering activity points above for FA: {}", FAID, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to filter students");
        }
    }

    @GetMapping("fa/student-list/filter-activity-points-below")
    public ResponseEntity<?> filterActivityPointsBelow(
            @RequestParam Long points) {
                Long FAID = null;
        try {
            FAID = currentUser().getFaid();
            if (points == null || points < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Valid points value is required");
            }

            List<StudentWithMandatoryDTO> students = studentService.filterActivityPointsBelow(FAID.intValue(), points);
            log.debug("Filtered {} students with activity points below {} for FA: {}", 
                     students.size(), points, FAID);
            return ResponseEntity.ok(students);

        } catch (Exception ex) {
            log.error("Error filtering activity points below for FA: {}", FAID, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to filter students");
        }
    }

    @GetMapping("/fa/student-details/{sid}")
    public ResponseEntity<?> getStudentBySid(@PathVariable String sid) 
    {
        Long faid = currentUser().getFaid();
      
        try {
            Optional<Student> student = studentService.getStudentById(sid);
            
            if (student.isEmpty()) {return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Student not found"));}
            if (student.get().getFaid() != faid.intValue()) 
                {log.warn("FA {} attempted to access student {} without authorization", faid, sid);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied"));
    }
            List<StudentActivity> activities = studentService.getStudentActivities(sid);
            
            Map<String, Object> response = new HashMap<>();
            response.put("student", student.get());
            response.put("activities", activities);
            
            log.debug("Retrieved student details with {} activities: {}", activities.size(), sid);
            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            log.error("Error fetching student details: {}", sid, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unable to fetch student details"));
        }
    }
}