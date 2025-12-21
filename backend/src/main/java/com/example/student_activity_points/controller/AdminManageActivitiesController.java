package com.example.student_activity_points.controller;

import com.example.student_activity_points.domain.Activity;
import com.example.student_activity_points.domain.Departments;
import com.example.student_activity_points.repository.ActivityRepository;
import com.example.student_activity_points.repository.DepartmentsRepository;

import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;

@RestController
@RequestMapping("/api/admin")
public class AdminManageActivitiesController {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private DepartmentsRepository departmentsRepository;

    private static final Logger log = LoggerFactory.getLogger(AdminManageActivitiesController.class);

    @GetMapping("/manage-activities")
    public ResponseEntity<?> getActivities(
            @RequestParam(required = false) String mandatory,
            @RequestParam(required = false) String type) {
        try {
            List<Activity> activities;

            if (mandatory != null) {
                Integer mandatoryValue = "yes".equalsIgnoreCase(mandatory) ? 1 : 0;
                activities = activityRepository.findByMandatory(mandatoryValue);
            } else {
                activities = (List<Activity>) activityRepository.findAll();
            }

            return ResponseEntity.ok(activities);

        } catch (Exception ex) {
            log.error("Error loading activities", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch activities");
        }
    }

    @GetMapping("/get-departments")
    public ResponseEntity<?> getDepartments() {
        try {
            List<Departments> departments = (List<Departments>) departmentsRepository.findAll();
            return ResponseEntity.ok(departments);

        } catch (Exception ex) {
            log.error("Error loading departments", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch departments");
        }
    }

    @PostMapping("/manage-activities")
    public ResponseEntity<?> addActivity(@RequestBody Activity activity) {
        try {
            Activity saved = activityRepository.save(activity);
            return ResponseEntity.ok(saved);

        } catch (DataIntegrityViolationException ex) {
            log.warn("Invalid activity data", ex);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Invalid activity data");

        } catch (Exception ex) {
            log.error("Error adding activity", ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Unable to process request");
        }
    }

    @PostMapping("/bulk-upload-activities")
    public ResponseEntity<?> bulkUpload(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid file");
        }

        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null ||
            !originalFileName.equalsIgnoreCase("activities_to_be_posted.xlsx")) {
            return ResponseEntity.badRequest()
                    .body("Invalid file. Please upload 'activities_to_be_posted.xlsx' only.");
        }

        String[] ACTIVITY_HEADERS = {
            "name", "description", "points", "DID",
            "date", "end_date", "type", "mandatory"
        };
        
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            if (!validateHeaderRow(sheet.getRow(0), ACTIVITY_HEADERS)) {
                return ResponseEntity.badRequest()
                        .body("Invalid column order for activity file.");
            }
            
            List<Activity> activities = new ArrayList<>();

            boolean firstRow = true;

            for (Row row : sheet) {
                if (firstRow) {
                    firstRow = false;
                    continue;
                }
            
                if (!isValidActivityRow(row)) {
                    log.warn("Skipping invalid activity row {}", row.getRowNum() + 1);
                    continue;
                }
            
                Activity activity = new Activity();
            
                activity.setName(getCellString(row.getCell(0)));
                activity.setDescription(getCellString(row.getCell(1)));
                activity.setPoints(getCellInt(row.getCell(2)));
                activity.setDID(getCellInt(row.getCell(3)));
            
                // Dates
                activity.setDate(parseDateSafe(row.getCell(4)));
                activity.setEnd_date(parseDateSafe(row.getCell(5)));
            
                activity.setType(getCellString(row.getCell(6)));
                activity.setMandatory(getCellInt(row.getCell(7)));
            
                activities.add(activity);
            }
            
            activityRepository.saveAll(activities);

            return ResponseEntity.ok("Uploaded successfully");

        } catch (Exception ex) {
            log.error("Error during bulk upload", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to process file");
        }
    }

    @PostMapping("/bulk-delete-activities")
    public ResponseEntity<?> bulkDelete(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid file");
        }

        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null ||
            !originalFileName.equalsIgnoreCase("activities_to_be_deleted.xlsx")) {
            return ResponseEntity.badRequest()
                    .body("Invalid file. Please upload 'activities_to_be_deleted.xlsx' only.");
        }

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            List<String> names = new ArrayList<>();

            boolean firstRow = true;

            for (Row row : sheet) {
                if (firstRow) { firstRow = false; continue; }

                String name = getCellString(row.getCell(0));
                if (!name.isEmpty()) names.add(name);
            }

            if (names.isEmpty()) {
                return ResponseEntity.badRequest().body("No valid entries found");
            }

            List<Activity> toDelete = activityRepository.findByNameIn(names);

            if (toDelete.isEmpty()) {
                return ResponseEntity.badRequest().body("No matching entries");
            }

            activityRepository.deleteAll(toDelete);

            return ResponseEntity.ok("Deleted successfully");

        } catch (Exception ex) {
            log.error("Bulk delete failed", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to process request");
        }
    }

    @PutMapping("/manage-activities/{id}")
    public ResponseEntity<?> updateActivity(@PathVariable Long id,
                                            @RequestBody Activity upd) {
        try {
            Optional<Activity> opt = activityRepository.findById(id);
            if (opt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
            }

            Activity act = opt.get();
            act.setName(upd.getName());
            act.setType(upd.getType());
            act.setMandatory(upd.getMandatory());
            act.setDID(upd.getDID());
            act.setDescription(upd.getDescription());
            act.setDate(upd.getDate());
            act.setEnd_date(upd.getEnd_date());
            act.setPoints(upd.getPoints());

            return ResponseEntity.ok(activityRepository.save(act));

        } catch (Exception ex) {
            log.error("Update failed", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to update activity");
        }
    }

    @DeleteMapping("/manage-activities/{id}")
    public ResponseEntity<?> deleteActivity(@PathVariable Long id) {
        try {
            if (!activityRepository.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
            }

            activityRepository.deleteById(id);
            return ResponseEntity.ok("Deleted successfully");

        } catch (Exception ex) {
            log.error("Delete failed", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to delete");
        }
    }

    private String getCellString(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue().trim();
        if (cell.getCellType() == CellType.NUMERIC) return String.valueOf((int) cell.getNumericCellValue());
        return "";
    }

    private Integer getCellInt(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) return (int) cell.getNumericCellValue();
        if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().isEmpty())
            return Integer.parseInt(cell.getStringCellValue().trim());
        return null;
    }

    private Date parseDateSafe(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return cell.getDateCellValue();
            }
            if (cell.getCellType() == CellType.STRING) {
                String s = cell.getStringCellValue().trim();
                if (!s.isEmpty()) {
                    return new SimpleDateFormat("yyyy-MM-dd").parse(s);
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private LocalDate parseDate(Cell cell) {
        if (cell == null) return null;
    
        try {
            if (cell.getCellType() == CellType.NUMERIC &&
                DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            }
    
            if (cell.getCellType() == CellType.STRING) {
                return LocalDate.parse(cell.getStringCellValue().trim());
            }
        } catch (Exception e) {
            return null;
        }
    
        return null;
    }

    private boolean isIntegerCell(Cell cell) {
        if (cell == null) return false;
    
        if (cell.getCellType() == CellType.NUMERIC) {
            return true;
        }
    
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim().matches("\\d+");
        }
    
        return false;
    }
    
    

    private boolean isValidActivityRow(Row row) {

        // Must have columns 0–7
        for (int i = 0; i <= 7; i++) {
            if (row.getCell(i) == null) {
                return false;
            }
        }

        String name = getCellString(row.getCell(0));
        String description = getCellString(row.getCell(1));
        String type = getCellString(row.getCell(6));

        // Name & description must exist
        if (name == null || name.isBlank()) return false;
        if (description == null || description.isBlank()) return false;

        // Points must be positive integer
        if (!isIntegerCell(row.getCell(2)) || getCellInt(row.getCell(2)) <= 0) {
            return false;
        }

        // DID must be valid integer
        if (!isIntegerCell(row.getCell(3)) || getCellInt(row.getCell(3)) <= 0) {
            return false;
        }

        // Dates must be valid
        LocalDate start = parseDate(row.getCell(4));
        LocalDate end = parseDate(row.getCell(5));

        if (start == null || end == null || end.isBefore(start)) {
            return false;
        }

        // Type must be non-empty
        if (type == null || type.isBlank()) {
            return false;
        }

        // Mandatory must be 0 or 1
        int mandatory = getCellInt(row.getCell(7));
        return mandatory == 0 || mandatory == 1;
    }


    private boolean validateHeaderRow(Row headerRow, String[] expectedHeaders) {
        if (headerRow == null) return false;
    
        for (int i = 0; i < expectedHeaders.length; i++) {
            Cell cell = headerRow.getCell(i);
            if (cell == null) return false;
    
            String actual = cell.getStringCellValue().trim();
            if (!expectedHeaders[i].equalsIgnoreCase(actual)) {
                return false;
            }
        }
        return true;
    }
    
}
