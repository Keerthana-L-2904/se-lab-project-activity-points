package com.example.student_activity_points.controller;

import com.example.student_activity_points.domain.Activity;
import com.example.student_activity_points.domain.Departments;
import com.example.student_activity_points.repository.ActivityRepository;
import com.example.student_activity_points.repository.DepartmentsRepository;
import com.example.student_activity_points.service.ClamAvAntivirusService;
import com.example.student_activity_points.util.ExcelFileValidationUtil;
import com.example.student_activity_points.util.ExcelFileValidationUtil.ValidationResult;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/admin")
public class AdminManageActivitiesController {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private DepartmentsRepository departmentsRepository;

    @Autowired
    private ClamAvAntivirusService antivirusService;

    @Value("${antivirus.enabled:false}")
    private boolean antivirusEnabled;

    private static final Logger log =
            LoggerFactory.getLogger(AdminManageActivitiesController.class);

    /* ===================== FETCH ===================== */

    @GetMapping("/manage-activities")
    public ResponseEntity<?> getActivities(
            @RequestParam(required = false) String mandatory) {
        try {
            if (mandatory != null) {
                int m = "yes".equalsIgnoreCase(mandatory) ? 1 : 0;
                return ResponseEntity.ok(activityRepository.findByMandatory(m));
            }
            return ResponseEntity.ok(activityRepository.findAll());

        } catch (Exception ex) {
            log.error("Error loading activities", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch activities");
        }
    }

    @GetMapping("/get-departments")
    public ResponseEntity<?> getDepartments() {
        return ResponseEntity.ok(departmentsRepository.findAll());
    }

    /* ===================== ADD SINGLE ===================== */

    @PostMapping("/manage-activities")
    public ResponseEntity<?> addActivity(@RequestBody Activity activity) {
        try {
            if (activity.getPoints() < 0) {
                return ResponseEntity.badRequest()
                        .body("Points must be positive");
            }
            return ResponseEntity.ok(activityRepository.save(activity));

        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Invalid activity data");

        } catch (Exception ex) {
            log.error("Add activity failed", ex);
            return ResponseEntity.badRequest()
                    .body("Unable to add activity");
        }
    }

    /* ===================== BULK UPLOAD ===================== */

    @PostMapping("/bulk-upload-activities")
    public ResponseEntity<?> bulkUpload(@RequestParam("file") MultipartFile file) {
    
        ValidationResult vr = ExcelFileValidationUtil.validateExcelFile(
                file,
                5 * 1024 * 1024,
                "activities_to_be_created.xlsx",
                antivirusEnabled,
                antivirusService
        );
    
        if (!vr.isValid()) {
            return ResponseEntity.badRequest().body(vr.getErrorMessage());
        }
    
        String[] HEADERS = {
                "name", "description", "points", "DID",
                "date", "end_date", "type", "mandatory"
        };
    
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
    
            Sheet sheet = workbook.getSheetAt(0);
    
            if (!ExcelFileValidationUtil.validateHeaderRow(sheet.getRow(0), HEADERS)) {
                return ResponseEntity.badRequest()
                        .body("Invalid column order for activity file");
            }
    
            List<Activity> activities = new ArrayList<>();
            int skippedCount = 0;
    
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
    
                if (row == null) {
                    skippedCount++;
                    continue;
                }
    
                if (!ExcelFileValidationUtil.isValidActivityRow(row)) {
                    log.warn("Skipping invalid activity row {}", i + 1);
                    skippedCount++;
                    continue;
                }
    
                Activity a = new Activity();
                a.setName(ExcelFileValidationUtil.getCellString(row.getCell(0)));
                a.setDescription(ExcelFileValidationUtil.getCellString(row.getCell(1)));
                a.setPoints(ExcelFileValidationUtil.getCellInt(row.getCell(2)));
                a.setDID(ExcelFileValidationUtil.getCellInt(row.getCell(3)));
                a.setDate(ExcelFileValidationUtil.parseDateSafe(row.getCell(4)));
                a.setEnd_date(ExcelFileValidationUtil.parseDateSafe(row.getCell(5)));
                a.setType(ExcelFileValidationUtil.getCellString(row.getCell(6)));
                a.setMandatory(ExcelFileValidationUtil.getCellInt(row.getCell(7)));
    
                activities.add(a);
            }
    
            activityRepository.saveAll(activities);
    
            String message = String.format(
                    "Uploaded %d activities. Skipped %d invalid rows.",
                    activities.size(),
                    skippedCount
            );
    
            return ResponseEntity.ok(message);
    
        } catch (Exception ex) {
            String errorId = UUID.randomUUID().toString();
            log.error("Bulk upload error ID {}", errorId, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed. Error ID: " + errorId);
        }
    }
    
    /* ===================== BULK DELETE ===================== */

    @PostMapping("/bulk-delete-activities")
    public ResponseEntity<?> bulkDelete(@RequestParam("file") MultipartFile file) {
    
        ValidationResult vr = ExcelFileValidationUtil.validateExcelFile(
                file,
                2 * 1024 * 1024,
                "activities_to_be_deleted.xlsx",
                antivirusEnabled,
                antivirusService
        );
    
        if (!vr.isValid()) {
            return ResponseEntity.badRequest().body(vr.getErrorMessage());
        }
    
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
    
            Sheet sheet = workbook.getSheetAt(0);
    
            // ✅ HEADER VALIDATION (only header name)
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return ResponseEntity.badRequest()
                        .body("Missing header row in Excel file");
            }
    
            String header = ExcelFileValidationUtil.getCellString(headerRow.getCell(0));
            if (!"name".equalsIgnoreCase(header)) {
                return ResponseEntity.badRequest()
                        .body("Invalid header. Expected column: name");
            }
    
            List<String> names = new ArrayList<>();
    
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
    
                String name = ExcelFileValidationUtil.getCellString(row.getCell(0));
                if (!name.isBlank()) {
                    names.add(name);
                }
            }
    
            if (names.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("No valid activity names found in file");
            }
    
            List<Activity> toDelete = activityRepository.findByNameIn(names);
    
            if (toDelete.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("No matching activities found");
            }
    
            activityRepository.deleteAll(toDelete);
    
            return ResponseEntity.ok(
                    "Deleted " + toDelete.size() + " activities successfully"
            );
    
        } catch (Exception ex) {
            log.error("Bulk delete failed", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Bulk delete failed");
        }
    }
    
    /* ===================== UPDATE / DELETE ===================== */

    @PutMapping("/manage-activities/{id}")
    public ResponseEntity<?> updateActivity(
            @PathVariable Long id,
            @RequestBody Activity upd) {

        Optional<Activity> opt = activityRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }

        if (upd.getPoints() < 0) {
            return ResponseEntity.badRequest().body("Points must be positive");
        }

        Activity a = opt.get();
        a.setName(upd.getName());
        a.setDescription(upd.getDescription());
        a.setType(upd.getType());
        a.setMandatory(upd.getMandatory());
        a.setDID(upd.getDID());
        a.setDate(upd.getDate());
        a.setEnd_date(upd.getEnd_date());
        a.setPoints(upd.getPoints());

        return ResponseEntity.ok(activityRepository.save(a));
    }

    @DeleteMapping("/manage-activities/{id}")
    public ResponseEntity<?> deleteActivity(@PathVariable Long id) {
        if (!activityRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
        activityRepository.deleteById(id);
        return ResponseEntity.ok("Deleted successfully");
    }
}
