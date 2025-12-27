package com.example.student_activity_points.controller;

import com.example.student_activity_points.domain.Activity;
import com.example.student_activity_points.domain.Student;
import com.example.student_activity_points.domain.StudentActivity;
import com.example.student_activity_points.domain.StudentActivityId;
import com.example.student_activity_points.repository.ActivityRepository;
import com.example.student_activity_points.repository.StudentActivityRepository;
import com.example.student_activity_points.repository.StudentRepository;
import com.example.student_activity_points.service.ClamAvAntivirusService;
import com.example.student_activity_points.util.ExcelFileValidationUtil;
import com.example.student_activity_points.util.ExcelFileValidationUtil.ValidationResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import jakarta.transaction.Transactional;

import java.io.InputStream;
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

    @Autowired
    private ClamAvAntivirusService antivirusService;

    @Value("${antivirus.enabled:false}")
    private boolean antivirusEnabled;

    private static final Logger log = LoggerFactory.getLogger(ValidationController.class);

    @PostMapping("/check-attendance/{actid}")
    public ResponseEntity<?> checkAttendance(
            @RequestParam("file") MultipartFile file,
            @PathVariable Long actid) {

        try {
            // Validate Excel file
            ValidationResult validationResult = ExcelFileValidationUtil.validateExcelFile(
                file,
                5 * 1024 * 1024, // 5MB max
                "enrollment_list.xlsx",
                antivirusEnabled,
                antivirusService
            );
            
            if (!validationResult.isValid()) {
                log.warn("File validation failed: {}", validationResult.getErrorMessage());
                return ResponseEntity.badRequest()
                        .body(validationResult.getErrorMessage());
            }

            Optional<Activity> activityOpt = activityRepository.findById(actid);
            if (activityOpt.isEmpty()) {
                log.warn("Activity not found with ID: {}", actid);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Activity not found with ID: " + actid);
            }

            Activity activity = activityOpt.get();

            try (InputStream inputStream = file.getInputStream();
                 Workbook workbook = new XSSFWorkbook(inputStream)) {

                Sheet sheet = workbook.getSheetAt(0);
                
                int totalRows = 0;
                List<String> validSids = new ArrayList<>();
                List<String> skippedRows = new ArrayList<>();

                // Skip header row (start from row 1)
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;
                    
                    totalRows++;

                    Cell cell = row.getCell(0);
                    if (cell == null) {
                        skippedRows.add("Row " + (i + 1) + " → Empty student ID");
                        continue;
                    }

                    String sid = getCellString(cell).trim();
                    
                    if (sid.isEmpty()) {
                        skippedRows.add("Row " + (i + 1) + " → Empty student ID");
                        continue;
                    }

                    Optional<Student> studentOpt = studentRepository.findById(sid);

                    if (studentOpt.isEmpty()) {
                        skippedRows.add("Row " + (i + 1) + " → Student not found: " + sid);
                        continue;
                    }

                    // Check for duplicates
                    StudentActivityId saId = new StudentActivityId();
                    saId.setActID(actid.intValue());
                    saId.setSid(sid);
                    
                    if (studentActivityRepository.existsById(saId)) {
                        skippedRows.add("Row " + (i + 1) + " → Already enrolled: " + sid);
                        continue;
                    }

                    // Add to valid list
                    validSids.add(sid);
                }

                // Prepare result with activity details for frontend display
                Map<String, Object> result = new HashMap<>();
                result.put("totalRows", totalRows);
                result.put("validSids", validSids); // Just send student IDs
                result.put("skippedRows", skippedRows);
                result.put("activityName", activity.getName());
                result.put("points", activity.getPoints());
                result.put("category", activity.getType());

                log.info("Attendance check completed: {} valid rows, {} skipped rows for activity: {}", 
                         validSids.size(), skippedRows.size(), actid);
                return ResponseEntity.ok(result);
            }

        } catch (Exception ex) {
            String errorId = UUID.randomUUID().toString();
            log.error("Error ID {}: Error processing attendance file for activity: {}", errorId, actid, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to process attendance file. Error ID: " + errorId);
        }
    }

    // Helper method
    private String getCellString(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue().trim();
        if (cell.getCellType() == CellType.NUMERIC) return String.valueOf((long) cell.getNumericCellValue());
        return "";
    }


    @Transactional
    @PostMapping("/finalize-attendance/{actid}")
    public ResponseEntity<?> finalizeAttendance(
            @PathVariable Long actid, 
            @RequestBody List<String> validSids) { // ✅ Now just receives list of student IDs

        try {
            // Validate input
            if (actid == null || actid <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid activity ID");
            }

            if (validSids == null || validSids.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("No valid student IDs provided");
            }

            // Limit number of students to prevent memory issues
            if (validSids.size() > 10000) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Too many students. Maximum 10,000 students allowed per upload.");
            }

            // Get activity from database
            Optional<Activity> activityOpt = activityRepository.findById(actid);
            if (activityOpt.isEmpty()) {
                log.warn("Activity not found with ID: {}", actid);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Activity not found with ID: " + actid);
            }

            Activity activity = activityOpt.get();
            
            // Validate points are positive
            if (activity.getPoints() < 0) {
                log.error("Activity {} has negative points: {}", actid, activity.getPoints());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Activity has invalid points configuration");
            }

            // Mark activity as uploaded
            activity.setIsuploaded(true);
            activityRepository.save(activity);

            int points = activity.getPoints();
            String category = activity.getType();

            int successCount = 0;
            List<String> skipped = new ArrayList<>();

            for (String sid : validSids) {
                // Validate SID
                if (sid == null || sid.isBlank()) {
                    skipped.add("Invalid student ID: empty or null");
                    continue;
                }

                // Get student
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
                    skipped.add("Already enrolled: " + sid);
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
                        log.warn("Unknown category for activity {}: {}", actid, category);
                        skipped.add("Unknown category for student " + sid);
                        continue;
                }

                // Update total activity_points
                int newActivityPoints = student.getDeptPoints() + 
                                      student.getInstitutePoints() + 
                                      student.getOtherPoints();
                student.setActivityPoints(newActivityPoints);

                // Save student
                try {
                    studentRepository.save(student);
                    successCount++;
                } catch (Exception ex) {
                    log.error("Error saving student {}: {}", sid, ex.getMessage(), ex);
                    skipped.add("Failed to update points for student: " + sid);
                }
            }

            String msg = "Successfully updated " + successCount + " students. Skipped: " + skipped.size();
            Map<String, Object> resp = new HashMap<>();
            resp.put("message", msg);
            resp.put("skipped", skipped);
            resp.put("successCount", successCount);
            resp.put("totalProvided", validSids.size());

            log.info("Attendance finalized for activity {}: {} successful, {} skipped", 
                     actid, successCount, skipped.size());
            return ResponseEntity.ok(resp);

        } catch (DataIntegrityViolationException ex) {
            String errorId = UUID.randomUUID().toString();
            log.error("Error ID {}: Data integrity violation while finalizing attendance for activity: {}", 
                     errorId, actid, ex);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Database constraint violation. Error ID: " + errorId);

        } catch (Exception ex) {
            String errorId = UUID.randomUUID().toString();
            log.error("Error ID {}: Error finalizing attendance for activity: {}", errorId, actid, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to finalize attendance. Error ID: " + errorId);
        }
    }
}