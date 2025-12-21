package com.example.student_activity_points.controller;

import com.example.student_activity_points.domain.Activity;
import com.example.student_activity_points.domain.Student;
import com.example.student_activity_points.domain.StudentActivity;
import com.example.student_activity_points.domain.StudentActivityId;
import com.example.student_activity_points.repository.ActivityRepository;
import com.example.student_activity_points.repository.StudentActivityRepository;
import com.example.student_activity_points.repository.StudentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/admin")
public class ValidationController {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentActivityRepository studentActivityRepository;

    private static final Logger log = LoggerFactory.getLogger(ValidationController.class);

    @PostMapping("/check-attendance/{actid}")
    public ResponseEntity<?> checkAttendance(
            @RequestParam("file") MultipartFile file,
            @PathVariable Long actid) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("File is required");
            }

            Optional<Activity> activityOpt = activityRepository.findById(actid);
            if (activityOpt.isEmpty()) {
                log.warn("Activity not found with ID: {}", actid);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Activity not found with ID: " + actid);
            }

            Activity activity = activityOpt.get();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

                // Skip header row
                reader.readLine();

                int totalRows = 0;
                List<Map<String, String>> validRows = new ArrayList<>();
                List<String> skippedRows = new ArrayList<>();

                String line;
                while ((line = reader.readLine()) != null) {
                    totalRows++;
                    String[] data = line.split(",");

                    if (data.length < 1) {
                        skippedRows.add("Row " + totalRows + " → Invalid format");
                        continue;
                    }

                    String sid = data[0].trim();
                    Optional<Student> studentOpt = studentRepository.findById(sid);

                    if (studentOpt.isEmpty()) {
                        skippedRows.add("Row " + totalRows + " → Student not found: " + sid);
                        continue;
                    }

                    // Store for preview
                    Map<String, String> row = new HashMap<>();
                    row.put("sid", sid);
                    row.put("points", String.valueOf(activity.getPoints()));
                    row.put("category", activity.getType());

                    validRows.add(row);
                }

                // Prepare result
                Map<String, Object> result = new HashMap<>();
                result.put("totalRows", totalRows);
                result.put("validRows", validRows);
                result.put("skippedRows", skippedRows);

                log.info("Attendance check completed: {} valid rows, {} skipped rows for activity: {}", 
                         validRows.size(), skippedRows.size(), actid);
                return ResponseEntity.ok(result);
            }

        } catch (Exception ex) {
            log.error("Error processing attendance file for activity: {}", actid, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to process attendance file");
        }
    }

    @Transactional
    @PostMapping("/finalize-attendance/{actid}")
    public ResponseEntity<?> finalizeAttendance(
            @PathVariable Long actid, 
            @RequestBody List<Map<String, String>> validRows) {

        try {
            if (validRows == null || validRows.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("No valid rows provided");
            }

            Optional<Activity> activityOpt = activityRepository.findById(actid);
            if (activityOpt.isEmpty()) {
                log.warn("Activity not found with ID: {}", actid);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Activity not found with ID: " + actid);
            }

            Activity activity = activityOpt.get();
            activity.setIsuploaded(true);
            activityRepository.save(activity);

            int successCount = 0;
            List<String> skipped = new ArrayList<>();

            for (Map<String, String> row : validRows) {
                String sid = row.get("sid");
                if (sid == null || sid.isBlank()) {
                    skipped.add("Missing SID in row: " + row);
                    continue;
                }

                int points;
                try {
                    points = activity.getPoints();
                } catch (Exception ex) {
                    log.warn("Error getting points for activity: {}", actid, ex);
                    skipped.add("Points calculation failed for " + sid);
                    continue;
                }

                String category = activity.getType();

                Optional<Student> studentOpt = studentRepository.findById(sid);
                if (studentOpt.isEmpty()) {
                    skipped.add("Student not found: " + sid);
                    continue;
                }

                Student student = studentOpt.get();

                // Check for duplicate StudentActivity
                StudentActivityId saId = new StudentActivityId();
                saId.setActID(actid.intValue());
                saId.setSid(sid);
                
                if (studentActivityRepository.existsById(saId)) {
                    skipped.add("Already exists: actID=" + actid + ", sid=" + sid);
                    continue;
                }

                // Create and populate StudentActivity
                StudentActivity studentActivity = new StudentActivity();
                studentActivity.setActivity(activity);
                studentActivity.setStudent(student);
                studentActivity.setActID(actid.intValue());
                studentActivity.setSid(sid);
                studentActivity.setPoints(points);
                studentActivity.setValidated(StudentActivity.Validated.Yes);
                studentActivity.setDate(new Date());
                studentActivity.setTitle(activity.getName());
                studentActivity.setActivityType(activity.getType());

                try {
                    studentActivityRepository.save(studentActivity);
                } catch (DataIntegrityViolationException ex) {
                    log.warn("Data integrity violation for student activity: actID={}, sid={}", actid, sid, ex);
                    skipped.add("Database error for student: " + sid);
                    continue;
                }

                // Update student's point totals based on category
                switch (category) {
                    case "Institute":
                        student.setInstitutePoints(student.getInstitutePoints() + points);
                        break;
                    case "Department":
                        student.setDeptPoints(student.getDeptPoints() + points);
                        break;
                    case "Other":
                        student.setOtherPoints(student.getOtherPoints() + points);
                        break;
                    default:
                        log.warn("Unknown category for student: sid={}, category={}", sid, category);
                        skipped.add("Unknown category for SID " + sid + ": " + category);
                        continue;
                }

                // Update activity_points
                int newActivityPoints = student.getDeptPoints() + student.getInstitutePoints();
                student.setActivityPoints(newActivityPoints);

                // Save student
                studentRepository.save(student);
                successCount++;
            }

            String msg = "Successfully updated " + successCount + " students. Skipped: " + skipped.size();
            Map<String, Object> resp = new HashMap<>();
            resp.put("message", msg);
            resp.put("skipped", skipped);
            resp.put("successCount", successCount);

            log.info("Attendance finalized for activity {}: {} successful, {} skipped", 
                     actid, successCount, skipped.size());
            return ResponseEntity.ok(resp);

        } catch (DataIntegrityViolationException ex) {
            log.error("Data integrity violation while finalizing attendance for activity: {}", actid, ex);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Database constraint violation while finalizing attendance");

        } catch (Exception ex) {
            log.error("Error finalizing attendance for activity: {}", actid, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to finalize attendance");
        }
    }
}