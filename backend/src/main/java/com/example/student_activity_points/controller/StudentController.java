package com.example.student_activity_points.controller;

import com.example.student_activity_points.domain.Announcements;
import com.example.student_activity_points.domain.Student;
import com.example.student_activity_points.domain.StudentActivity;
import com.example.student_activity_points.dto.StudentWithMandatoryDTO;
import com.example.student_activity_points.service.StudentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class StudentController {

    @Autowired
    private StudentService studentService;

    private static final Logger log = LoggerFactory.getLogger(StudentController.class);

    @GetMapping("student/{studentID}")
    public ResponseEntity<?> getStudent(@PathVariable String studentID) {
        try {
            return studentService.getStudentWithFA(studentID)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        log.warn("Student not found: {}", studentID);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Student not found");
                    });

        } catch (Exception ex) {
            log.error("Error fetching student: {}", studentID, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch student");
        }
    }

    @GetMapping("student/{studentID}/department-points")
    public ResponseEntity<?> getDepartmentPoints(@PathVariable String studentID) {
        try {
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

    @GetMapping("student/{studentID}/name")
    public ResponseEntity<?> getStudentName(@PathVariable String studentID) {
        try {
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

    @GetMapping("student/{studentID}/institutional-points")
    public ResponseEntity<?> getInstitutionalPoints(@PathVariable String studentID) {
        try {
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

    @GetMapping("student/{studentID}/activity-points")
    public ResponseEntity<?> getActivityPoints(@PathVariable String studentID) {
        try {
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

    @GetMapping("student/{studentID}/activities")
    public ResponseEntity<?> getStudentActivities(@PathVariable String studentID) {
        try {
            List<StudentActivity> activities = studentService.getStudentActivities(studentID);
            log.debug("Retrieved {} activities for student: {}", activities.size(), studentID);
            return ResponseEntity.ok(activities);

        } catch (Exception ex) {
            log.error("Error fetching activities for student: {}", studentID, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch student activities");
        }
    }

    @GetMapping("student/{studentID}/latest-activity")
    public ResponseEntity<?> getLatestActivity(@PathVariable String studentID) {
        try {
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

    @GetMapping("student/announcements")
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

    @GetMapping("/fa/student-list/{FAID}")
    public ResponseEntity<?> getStudentsByFAID(@PathVariable int FAID) {
        try {
            List<Student> students = studentService.getStudentsByFAID(FAID);
            log.debug("Retrieved {} students for FA: {}", students.size(), FAID);
            return ResponseEntity.ok(students);

        } catch (Exception ex) {
            log.error("Error fetching students for FA: {}", FAID, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch students");
        }
    }

    @GetMapping("/fa/student-list/{FAID}/with-mandatory")
    public ResponseEntity<?> getStudentsByFAIDWithMandatory(@PathVariable int FAID) {
        try {
            List<StudentWithMandatoryDTO> students = studentService.getStudentsByFAIDWithMandatoryCount(FAID);
            log.debug("Retrieved {} students with mandatory count for FA: {}", students.size(), FAID);
            return ResponseEntity.ok(students);

        } catch (Exception ex) {
            log.error("Error fetching students with mandatory count for FA: {}", FAID, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch students");
        }
    }

    @GetMapping("fa/student-list/{FAID}/search")
    public ResponseEntity<?> searchStudents(
            @PathVariable int FAID,
            @RequestParam String name) {
        try {
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Search name is required");
            }

            List<StudentWithMandatoryDTO> students = studentService.searchStudentsByFAIDAndName(FAID, name);
            log.debug("Search found {} students for FA: {} with name: {}", students.size(), FAID, name);
            return ResponseEntity.ok(students);

        } catch (Exception ex) {
            log.error("Error searching students for FA: {} with name: {}", FAID, name, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to search students");
        }
    }

    @GetMapping("fa/student-list/{FAID}/search-by-mandatory")
    public ResponseEntity<?> searchByMandatoryCount(
            @PathVariable int FAID,
            @RequestParam Long mandatoryCount) {
        try {
            if (mandatoryCount == null || mandatoryCount < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Valid mandatory count is required");
            }

            List<StudentWithMandatoryDTO> students = studentService.searchStudentsByMandatoryCount(FAID, mandatoryCount);
            log.debug("Search found {} students for FA: {} with mandatory count: {}", 
                     students.size(), FAID, mandatoryCount);
            return ResponseEntity.ok(students);

        } catch (Exception ex) {
            log.error("Error searching students by mandatory count for FA: {}", FAID, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to search students");
        }
    }

    @GetMapping("fa/student-list/{FAID}/sort-by-asc")
    public ResponseEntity<?> sortByAsc(@PathVariable int FAID) {
        try {
            List<StudentWithMandatoryDTO> students = studentService.getStudentsByFAIDWithMandatoryCountAsc(FAID);
            log.debug("Retrieved {} students sorted ascending for FA: {}", students.size(), FAID);
            return ResponseEntity.ok(students);

        } catch (Exception ex) {
            log.error("Error sorting students ascending for FA: {}", FAID, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to sort students");
        }
    }

    @GetMapping("fa/student-list/{FAID}/sort-by-desc")
    public ResponseEntity<?> sortByDesc(@PathVariable int FAID) {
        try {
            List<StudentWithMandatoryDTO> students = studentService.getStudentsByFAIDWithMandatoryCountDesc(FAID);
            log.debug("Retrieved {} students sorted descending for FA: {}", students.size(), FAID);
            return ResponseEntity.ok(students);

        } catch (Exception ex) {
            log.error("Error sorting students descending for FA: {}", FAID, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to sort students");
        }
    }

    @GetMapping("fa/student-list/{FAID}/filter-dept-points-above")
    public ResponseEntity<?> filterDeptPointsAbove(
            @PathVariable int FAID,
            @RequestParam Long points) {
        try {
            if (points == null || points < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Valid points value is required");
            }

            List<StudentWithMandatoryDTO> students = studentService.filterDeptPointsAbove(FAID, points);
            log.debug("Filtered {} students with dept points above {} for FA: {}", 
                     students.size(), points, FAID);
            return ResponseEntity.ok(students);

        } catch (Exception ex) {
            log.error("Error filtering dept points above for FA: {}", FAID, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to filter students");
        }
    }

    @GetMapping("fa/student-list/{FAID}/filter-dept-points-below")
    public ResponseEntity<?> filterDeptPointsBelow(
            @PathVariable int FAID,
            @RequestParam Long points) {
        try {
            if (points == null || points < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Valid points value is required");
            }

            List<StudentWithMandatoryDTO> students = studentService.filterDeptPointsBelow(FAID, points);
            log.debug("Filtered {} students with dept points below {} for FA: {}", 
                     students.size(), points, FAID);
            return ResponseEntity.ok(students);

        } catch (Exception ex) {
            log.error("Error filtering dept points below for FA: {}", FAID, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to filter students");
        }
    }

    @GetMapping("fa/student-list/{FAID}/filter-inst-points-above")
    public ResponseEntity<?> filterInstPointsAbove(
            @PathVariable int FAID,
            @RequestParam Long points) {
        try {
            if (points == null || points < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Valid points value is required");
            }

            List<StudentWithMandatoryDTO> students = studentService.filterInstPointsAbove(FAID, points);
            log.debug("Filtered {} students with inst points above {} for FA: {}", 
                     students.size(), points, FAID);
            return ResponseEntity.ok(students);

        } catch (Exception ex) {
            log.error("Error filtering inst points above for FA: {}", FAID, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to filter students");
        }
    }

    @GetMapping("fa/student-list/{FAID}/filter-inst-points-below")
    public ResponseEntity<?> filterInstPointsBelow(
            @PathVariable int FAID,
            @RequestParam Long points) {
        try {
            if (points == null || points < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Valid points value is required");
            }

            List<StudentWithMandatoryDTO> students = studentService.filterInstPointsBelow(FAID, points);
            log.debug("Filtered {} students with inst points below {} for FA: {}", 
                     students.size(), points, FAID);
            return ResponseEntity.ok(students);

        } catch (Exception ex) {
            log.error("Error filtering inst points below for FA: {}", FAID, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to filter students");
        }
    }

    @GetMapping("fa/student-list/{FAID}/filter-activity-points-above")
    public ResponseEntity<?> filterActivityPointsAbove(
            @PathVariable int FAID,
            @RequestParam Long points) {
        try {
            if (points == null || points < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Valid points value is required");
            }

            List<StudentWithMandatoryDTO> students = studentService.filterActivityPointsAbove(FAID, points);
            log.debug("Filtered {} students with activity points above {} for FA: {}", 
                     students.size(), points, FAID);
            return ResponseEntity.ok(students);

        } catch (Exception ex) {
            log.error("Error filtering activity points above for FA: {}", FAID, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to filter students");
        }
    }

    @GetMapping("fa/student-list/{FAID}/filter-activity-points-below")
    public ResponseEntity<?> filterActivityPointsBelow(
            @PathVariable int FAID,
            @RequestParam Long points) {
        try {
            if (points == null || points < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Valid points value is required");
            }

            List<StudentWithMandatoryDTO> students = studentService.filterActivityPointsBelow(FAID, points);
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
    public ResponseEntity<?> getStudentBySid(@PathVariable String sid) {
        try {
            Optional<Student> student = studentService.getStudentById(sid);
            
            if (student.isEmpty()) {
                log.warn("Student not found: {}", sid);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Student not found"));
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