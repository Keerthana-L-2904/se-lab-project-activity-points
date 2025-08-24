package com.example.student_activity_points.controller;

import com.example.student_activity_points.domain.Activity;
import com.example.student_activity_points.domain.Departments;
import com.example.student_activity_points.repository.ActivityRepository;
import com.example.student_activity_points.repository.DepartmentsRepository;
import com.example.student_activity_points.repository.ValidationRepository;
import com.example.student_activity_points.repository.StudentActivityRepository;
import com.example.student_activity_points.domain.StudentActivity;
import com.example.student_activity_points.domain.Student;
import com.example.student_activity_points.repository.StudentRepository;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminManageActivitiesController {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private DepartmentsRepository departmentsRepository;


    @Autowired
    private ValidationRepository validationRepository;

    @Autowired
    private StudentActivityRepository studentActivityRepository;

    @Autowired
    private StudentRepository studentRepository;
    // Upload attendance and update StudentActivity table
    @PostMapping("/upload-attendance/{actID}")
    public ResponseEntity<?> uploadAttendance(@RequestParam("file") MultipartFile file, @PathVariable int actID) {
        System.out.println("[DEBUG] uploadAttendance called with actID: " + actID);
        if (file.isEmpty()) {
            System.out.println("[DEBUG] Uploaded file is empty.");
            return ResponseEntity.badRequest().body("Please upload a valid CSV file!");
        }
        try {
            System.out.println("[DEBUG] Attempting to find activity with ID: " + actID);
            Optional<Activity> activityOptional = activityRepository.findById(Long.valueOf(actID));
            if (!activityOptional.isPresent()) {
                System.out.println("[DEBUG] Activity not found for ID: " + actID);
                return ResponseEntity.badRequest().body("Invalid Activity ID!");
            }
            Activity activity = activityOptional.get();
            System.out.println("[DEBUG] Activity found: " + activity.getName() + ", Type: " + activity.getType() + ", Points: " + activity.getPoints());

            // 1️⃣ Subtract points for previous attendance records for this activity
            System.out.println("[DEBUG] Fetching previous validation records for activity ID: " + actID);
            List<com.example.student_activity_points.domain.Validation> oldValidations = validationRepository.findByActivity(activity);
            for (com.example.student_activity_points.domain.Validation oldVal : oldValidations) {
                String oldSid = oldVal.getSid();
                Optional<Student> oldStudentOpt = studentRepository.findById(oldSid);
                if (oldStudentOpt.isPresent()) {
                    Student oldStudent = oldStudentOpt.get();
                    if ("Institute".equalsIgnoreCase(activity.getType())) {
                        int currentPoints = oldStudent.getInstitutePoints();
                        oldStudent.setInstitutePoints(Math.max(0, currentPoints - activity.getPoints()));
                        System.out.println("[DEBUG] Subtracted " + activity.getPoints() + " institute points from SID: " + oldSid);
                    } else if ("Department".equalsIgnoreCase(activity.getType())) {
                        int currentPoints = oldStudent.getDeptPoints();
                        oldStudent.setDeptPoints(Math.max(0, currentPoints - activity.getPoints()));
                        System.out.println("[DEBUG] Subtracted " + activity.getPoints() + " department points from SID: " + oldSid);
                    } else {
                        int currentPoints = oldStudent.getActivityPoints();
                        oldStudent.setActivityPoints(Math.max(0, currentPoints - activity.getPoints()));
                        System.out.println("[DEBUG] Subtracted " + activity.getPoints() + " activity points from SID: " + oldSid);
                    }
                    // Always update activity_points as sum of dept_points and institute_points
                    int newDeptPoints = oldStudent.getDeptPoints();
                    int newInstitutePoints = oldStudent.getInstitutePoints();
                    int newActivityPoints = newDeptPoints + newInstitutePoints;
                    oldStudent.setActivityPoints(newActivityPoints);
                    studentRepository.save(oldStudent);
                }
            }
            System.out.println("[DEBUG] Deleting previous validation records for activity ID: " + actID);
            validationRepository.deleteByActivity(activity);

            List<com.example.student_activity_points.domain.Validation> validationList = new java.util.ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
            String line;
            boolean firstLine = true;
            int pointsToAdd = activity.getPoints() != null ? activity.getPoints() : 0;
            String activityType = activity.getType() != null ? activity.getType() : "Other";
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    System.out.println("[DEBUG] Skipping header row: " + line);
                    firstLine = false;
                    continue;
                }
                String sid = line.trim();
                if (!sid.isEmpty()) {
                    System.out.println("[DEBUG] Processing student ID: " + sid);
                    com.example.student_activity_points.domain.Validation validation = new com.example.student_activity_points.domain.Validation();
                    validation.setSid(sid);
                    validation.setUploadDate(new Date());
                    validation.setValidated(com.example.student_activity_points.domain.Validation.Validated.Yes);
                    validation.setActivity(activity);
                    validationList.add(validation);

                    // Update student points based on activity type
                    Optional<Student> studentOpt = studentRepository.findById(sid);
                    if (studentOpt.isPresent()) {
                        Student student = studentOpt.get();
                        System.out.println("[DEBUG] Found student: " + student.getName() + " (SID: " + sid + ")");
                        if ("Institute".equalsIgnoreCase(activityType)) {
                            int currentPoints = 0;
                            try {
                                currentPoints = student.getInstitutePoints();
                                System.out.println("[DEBUG] Current institute points: " + currentPoints);
                            } catch (Exception e) {
                                System.out.println("[DEBUG] Error getting institute points: " + e.getMessage());
                                currentPoints = 0;
                            }
                            student.setInstitutePoints(currentPoints + pointsToAdd);
                            System.out.println("[DEBUG] Updated institute points: " + (currentPoints + pointsToAdd));
                        } else if ("Department".equalsIgnoreCase(activityType)) {
                            int currentPoints = 0;
                            try {
                                currentPoints = student.getDeptPoints();
                                System.out.println("[DEBUG] Current department points: " + currentPoints);
                            } catch (Exception e) {
                                System.out.println("[DEBUG] Error getting department points: " + e.getMessage());
                                currentPoints = 0;
                            }
                            student.setDeptPoints(currentPoints + pointsToAdd);
                            System.out.println("[DEBUG] Updated department points: " + (currentPoints + pointsToAdd));
                        } else {
                            int currentPoints = 0;
                            try {
                                currentPoints = student.getActivityPoints();
                                System.out.println("[DEBUG] Current activity points: " + currentPoints);
                            } catch (Exception e) {
                                System.out.println("[DEBUG] Error getting activity points: " + e.getMessage());
                                currentPoints = 0;
                            }
                            student.setActivityPoints(currentPoints + pointsToAdd);
                            System.out.println("[DEBUG] Updated activity points: " + (currentPoints + pointsToAdd));
                        }
                        // Always update activity_points as sum of dept_points and institute_points
                        int newDeptPoints = student.getDeptPoints();
                        int newInstitutePoints = student.getInstitutePoints();
                        int newActivityPoints = newDeptPoints + newInstitutePoints;
                        student.setActivityPoints(newActivityPoints);
                        System.out.println("[DEBUG] Set activity_points (dept_points + institute_points): " + newActivityPoints + " (" + newDeptPoints + "+" + newInstitutePoints + ")");
                        studentRepository.save(student);
                        System.out.println("[DEBUG] Saved updated student record for SID: " + sid);

                        // Add StudentActivity record
                        StudentActivity studentActivity = new StudentActivity();
                        studentActivity.setSid(sid);
                        studentActivity.setActID(actID);
                        studentActivity.setDate(new Date());
                        studentActivity.setTitle(activity.getName());
                        studentActivity.setPoints(pointsToAdd);
                        studentActivity.setActivityType(activityType);
                        studentActivity.setLink(""); // No link from CSV, set as empty
                        studentActivity.setActivity(activity);
                        studentActivity.setStudent(student);
                        studentActivity.setValidated(StudentActivity.Validated.Yes);
                        studentActivityRepository.save(studentActivity);
                        System.out.println("[DEBUG] Saved StudentActivity for SID: " + sid + ", actID: " + actID);
                    } else {
                        System.out.println("[DEBUG] Student not found for SID: " + sid);
                    }
                } else {
                    System.out.println("[DEBUG] Encountered empty SID line, skipping.");
                }
            }

            // 2️⃣ Insert the new data
            System.out.println("[DEBUG] Saving all validation records. Total: " + validationList.size());
            validationRepository.saveAll(validationList);
            System.out.println("[DEBUG] Upload and update process completed successfully.");
            return ResponseEntity.ok("File uploaded successfully! Attendance list, student points, and StudentActivity updated.");
        } catch (Exception e) {
            System.out.println("[DEBUG] Exception occurred: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error processing the file!");
        }
    }

    // Fetch all activities with attendanceUploaded property
    @GetMapping("/manage-activities")
    public ResponseEntity<?> getActivities() {
        try {
            List<Activity> activities = (List<Activity>) activityRepository.findAll();
            // Build a response with attendanceUploaded property
            List<java.util.Map<String, Object>> response = new java.util.ArrayList<>();
            for (Activity activity : activities) {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("actID", activity.getActID());
                map.put("name", activity.getName());
                map.put("description", activity.getDescription());
                map.put("points", activity.getPoints());
                map.put("DID", activity.getDID());
                map.put("date", activity.getDate());
                map.put("end_date", activity.getEnd_date());
                map.put("type", activity.getType());
                map.put("outside_inside", activity.getOutside_inside());
                map.put("no_of_people", activity.getNo_of_people());
                map.put("mandatory", activity.getMandatory());
                // Check if attendance is uploaded for this activity
                boolean attendanceUploaded = !validationRepository.findByActivity(activity).isEmpty();
                map.put("attendanceUploaded", attendanceUploaded);
                response.add(map);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
        }
    }

    @GetMapping("/get-departments")
    public ResponseEntity<?> getDepartments() {
        try {
            List<Departments> departments = (List<Departments>) departmentsRepository.findAll();
            return ResponseEntity.ok(departments);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
        }
    }

    // Add a new activity
    @PostMapping("/manage-activities")
    public ResponseEntity<?> addActivity(@RequestBody Activity activity) {
        try {
            Activity savedActivity = activityRepository.save(activity);
            return ResponseEntity.ok(savedActivity);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error adding activity: " + e.getMessage());
        }
    }

    // Edit an existing activity
    @PutMapping("/manage-activities/{id}")
    public ResponseEntity<?> updateActivity(@PathVariable Long id, @RequestBody Activity updatedActivity) {
        try {
            Optional<Activity> existingActivityOpt = activityRepository.findById(id);
            if (existingActivityOpt.isPresent()) {
                Activity existingActivity = existingActivityOpt.get();
                existingActivity.setName(updatedActivity.getName());
                existingActivity.setType(updatedActivity.getType());
                existingActivity.setMandatory(updatedActivity.getMandatory());
                existingActivity.setDID(updatedActivity.getDID());
                existingActivity.setDescription(updatedActivity.getDescription());
                existingActivity.setOutside_inside(updatedActivity.getOutside_inside());
                existingActivity.setDate(updatedActivity.getDate());
                existingActivity.setEnd_date(updatedActivity.getEnd_date());
                existingActivity.setPoints(updatedActivity.getPoints());
                existingActivity.setNo_of_people(updatedActivity.getNo_of_people());

                Activity savedActivity = activityRepository.save(existingActivity);
                return ResponseEntity.ok(savedActivity);
            } else {
                return ResponseEntity.status(404).body("Activity not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error updating activity: " + e.getMessage());
        }
    }

    // Delete an activity
    @DeleteMapping("/manage-activities/{id}")
    public ResponseEntity<?> deleteActivity(@PathVariable Long id) {
        try {

            if (activityRepository.existsById(id)) {
                activityRepository.deleteById(id);
                return ResponseEntity.ok("Activity deleted successfully");
            } else {
                return ResponseEntity.status(404).body("Activity not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error deleting activity: " + e.getMessage());
        }
    }
}
