package com.example.student_activity_points.controller;

import com.example.student_activity_points.domain.Validation;
import com.example.student_activity_points.domain.Validation.Validated;
import com.example.student_activity_points.domain.Activity;
import com.example.student_activity_points.repository.ValidationRepository;
import com.example.student_activity_points.repository.ActivityRepository;
import com.example.student_activity_points.repository.StudentRepository;
import com.example.student_activity_points.repository.StudentActivityRepository;
import com.example.student_activity_points.domain.StudentActivity;
import com.example.student_activity_points.domain.StudentActivityId;
import com.example.student_activity_points.domain.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/api/admin")

public class ValidationController {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentActivityRepository studentActivityRepository;

@PostMapping("/check-attendance/{actid}")
public ResponseEntity<?> checkAttendance(
        @RequestParam("file") MultipartFile file,
        @PathVariable Long actid) {

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {

        Activity activity = activityRepository.findById(actid).orElse(null);
        if (activity == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("❌ Activity not found with ID: " + actid);
        }

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

        return ResponseEntity.ok(result);

    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("❌ Error processing file: " + e.getMessage());
    }
}

@Transactional
@PostMapping("/finalize-attendance/{actid}")
public ResponseEntity<?> finalizeAttendance(@PathVariable Long actid, @RequestBody List<Map<String, String>> validRows) {
    try {
        // Fetch managed activity once
        Activity activity = activityRepository.findById(actid).orElse(null);
        if (activity == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("❌ Activity not found with ID: " + actid);
        }
        
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
                skipped.add("Points adding for " + sid + "unsuccessfull ");
                continue;
            }

            String category = activity.getType();

            Student student = studentRepository.findById(sid).orElse(null);
            if (student == null) {
                skipped.add("Student not found: " + sid);
                continue;
            }

            // Avoid inserting duplicate StudentActivity if already exists
            // Build a composite id object if you have one (StudentActivityId), or check repository
            // Example using findById with an IdClass key (actID + sid):
            StudentActivityId saId = new StudentActivityId();
            saId.setActID(actid.intValue());
            saId.setSid(sid);
            if (studentActivityRepository.existsById(saId)) {
                skipped.add("Already exists: actID=" + actid + ", sid=" + sid);
                continue;
            }

            // Create and populate StudentActivity. IMPORTANT: set the entity references
            StudentActivity studentActivity = new StudentActivity();

            // set both entity references (required because of @MapsId) and primitive id fields
            studentActivity.setActivity(activity);         // managed Activity entity
            studentActivity.setStudent(student);          // managed Student entity
            studentActivity.setActID(actid.intValue());   // set primitive field too
            studentActivity.setSid(sid);                  // set primitive field too

            studentActivity.setPoints(points);
            studentActivity.setValidated(StudentActivity.Validated.Yes);
            studentActivity.setDate(new Date());

            // set title/activityType/link/proof as needed (optional)
            studentActivity.setTitle(activity.getName());
            studentActivity.setActivityType(activity.getType());
            studentActivity.setLink(""); // if no link
            // studentActivity.setProof(...); // if available

            // Save StudentActivity first (it references managed entities)
            studentActivityRepository.save(studentActivity);

            

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
                    // unknown category — treat as skipped or default action
                    skipped.add("Unknown category for SID " + sid + ": " + category);
                    // optionally delete the studentActivity you just created to keep DB consistent
                    // studentActivityRepository.delete(studentActivity);
                    continue;
            }

            // Keep activity_points in sync if you maintain a composite field:
            int newActivityPoints = student.getDeptPoints() + student.getInstitutePoints();
            student.setActivityPoints(newActivityPoints);

            // Save student
            studentRepository.save(student);

            successCount++;
        }

        String msg = "✅ Successfully updated " + successCount + " students. Skipped: " + skipped.size();
        Map<String, Object> resp = new HashMap<>();
        resp.put("message", msg);
        resp.put("skipped", skipped);
        resp.put("successCount", successCount);

        return ResponseEntity.ok(resp);

    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("❌ Error finalizing attendance: " + e.getMessage());
    }
}
}