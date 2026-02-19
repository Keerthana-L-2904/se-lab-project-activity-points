package com.example.student_activity_points.controller;

import com.example.student_activity_points.domain.Departments;
import com.example.student_activity_points.domain.Fa;
import com.example.student_activity_points.domain.Student;
import com.example.student_activity_points.dto.StudentWithFADTO;
import com.example.student_activity_points.repository.DepartmentsRepository;
import com.example.student_activity_points.repository.FARepository;
import com.example.student_activity_points.repository.RequestsRepository;
import com.example.student_activity_points.repository.StudentRepository;
import com.example.student_activity_points.util.ExcelFileValidationUtil;
import com.example.student_activity_points.util.ExcelFileValidationUtil.ValidationResult;
import com.example.student_activity_points.service.ClamAvAntivirusService;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import jakarta.transaction.Transactional;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/admin/manage-users")
public class AdminManageUsersController {
    
    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private FARepository faRepository;
    
    @Autowired
    private DepartmentsRepository deptRepository;

    @Autowired
    private RequestsRepository requestsRepository;

    @Autowired
    private ClamAvAntivirusService antivirusService;

    @Value("${antivirus.enabled:false}")
    private boolean antivirusEnabled;

    private static final Logger log = LoggerFactory.getLogger(AdminManageUsersController.class);

    @GetMapping("/student")
        public ResponseEntity<?> getStudents() {
            try {
                List<Student> students = (List<Student>) studentRepository.findAll();

                List<StudentWithFADTO> result = students.stream().map(student -> {
                    Optional<Fa> faOpt = faRepository.findById((long) student.getFaid());

                    String faName = faOpt.map(Fa::getName).orElse(null);
                    String faEmail = faOpt.map(Fa::getEmailID).orElse(null);

                    return new StudentWithFADTO(student, faName, faEmail);
                }).toList();

                return ResponseEntity.ok(result);

            } catch (Exception ex) {
                log.error("Error loading students", ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Unable to fetch students");
            }
        }

    @GetMapping("/fa")
    public ResponseEntity<?> getFA() {
        try {
            List<Fa> fas = (List<Fa>) faRepository.findAll();
            return ResponseEntity.ok(fas);

        } catch (Exception ex) {
            log.error("Error loading FAs", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch FAs");
        }
    }

    @PostMapping("/student")
    public ResponseEntity<?> addStudent(@RequestBody Student student) {
        try {
            if (student.getSid() == null || student.getSid().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Error: sid (roll number) must be provided.");
            }
    
            if (studentRepository.existsById(student.getSid())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Error: Student with sid " + student.getSid() + " already exists.");
            }
            if(student.getInstitutePoints()<0 || student.getDeptPoints()<0 || student.getOtherPoints()<0){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Points must be positive");
            }

            Student savedStudent = studentRepository.save(student);
            log.info("Student added successfully: {}", student.getSid());
            return ResponseEntity.ok(savedStudent);

        } catch (DataIntegrityViolationException ex) {
            log.warn("Data integrity violation while adding student", ex);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Invalid student data or duplicate entry");

        } catch (Exception ex) {
            log.error("Error adding student", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to add student");
        }
    }
    @PostMapping("/upload-students")
    public ResponseEntity<?> uploadStudents(@RequestParam("file") MultipartFile file) {
    
        ValidationResult validationResult = ExcelFileValidationUtil.validateExcelFile(
                file,
                10 * 1024 * 1024,
                "student_accounts_to_be_created.xlsx",
                antivirusEnabled,
                antivirusService
        );
    
        if (!validationResult.isValid()) {
            return ResponseEntity.badRequest().body(validationResult.getErrorMessage());
        }
    
        String[] STUDENT_HEADERS = {
                "sid", "name", "emailid", "did",
                "faid", "dept_points", "institute_points", "other_points"
        };
    
        int skippedDuplicate = 0;
        int skippedInvalidFa = 0;
    
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
    
            Sheet sheet = workbook.getSheetAt(0);
    
            if (!ExcelFileValidationUtil.validateHeaderRow(sheet.getRow(0), STUDENT_HEADERS)) {
                return ResponseEntity.badRequest()
                        .body("Invalid column order for student file.");
            }
    
            List<Student> students = new ArrayList<>();
    
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
    
                if (row == null) {
                    ;
                    continue;
                }
    
                if (!ExcelFileValidationUtil.isValidStudentRow(row)) {
                    log.warn("Skipping invalid student row {}", i + 1);
                    ;
                    continue;
                }
    
                String sid = getCellString(row.getCell(0));
                if (studentRepository.existsById(sid)) {
                    log.warn("Skipping duplicate student {}", sid);
                    skippedDuplicate++;
                    continue;
                }
    
                int faid = getIntCellValue(row, 4);
    
                // ✅ FA existence check
                if (!faRepository.existsById((long) faid)) {
                    log.warn("Skipping student {}: FAID {} not found", sid, faid);
                    skippedInvalidFa++;
                    continue;
                }
    
                Student student = new Student();
                student.setSid(sid);
                student.setName(getCellString(row.getCell(1)));
                student.setEmailID(getCellString(row.getCell(2)));
                student.setDid(getIntCellValue(row, 3));
                student.setFaid(faid);
                student.setDeptPoints(getIntCellValue(row, 5));
                student.setInstitutePoints(getIntCellValue(row, 6));
                student.setOtherPoints(getIntCellValue(row, 7));
    
                student.setActivityPoints(
                        student.getDeptPoints()
                                + student.getInstitutePoints()
                                + student.getOtherPoints()
                );
    
                students.add(student);
            }
    
            studentRepository.saveAll(students);
    
            return ResponseEntity.ok(
                    "Uploaded " + students.size() + " students | " +
                    ", duplicates: " + skippedDuplicate +
                    ", invalid FA: " + skippedInvalidFa
            );
    
        } catch (Exception ex) {
            log.error("Error uploading students", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to upload students");
        }
    }
    
    @PostMapping("/fa")
    public ResponseEntity<?> addFA(@RequestBody Fa fa) {
        try {
            Fa savedFa = faRepository.save(fa);
            log.info("FA added successfully: {}", fa.getEmailID());
            return ResponseEntity.ok(savedFa);

        } catch (DataIntegrityViolationException ex) {
            log.warn("Data integrity violation while adding FA", ex);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Invalid FA data or duplicate entry");

        } catch (Exception ex) {
            log.error("Error adding FA", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to add FA");
        }
    }

    @PostMapping("/upload-fas")
    public ResponseEntity<?> uploadFa(@RequestParam("file") MultipartFile file) {
    
        ValidationResult validationResult = ExcelFileValidationUtil.validateExcelFile(
                file,
                5 * 1024 * 1024,
                "fa_accounts_to_be_created.xlsx",
                antivirusEnabled,
                antivirusService
        );
    
        if (!validationResult.isValid()) {
            return ResponseEntity.badRequest().body(validationResult.getErrorMessage());
        }
    
        String[] FA_HEADERS = { "name", "emailID", "DID" };
    
        int skippedDuplicate = 0;
        int skippedMissingDept = 0;
    
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
    
            Sheet sheet = workbook.getSheetAt(0);
    
            if (!ExcelFileValidationUtil.validateHeaderRow(sheet.getRow(0), FA_HEADERS)) {
                return ResponseEntity.badRequest()
                        .body("Invalid column order for FA file.");
            }
    
            List<Fa> faList = new ArrayList<>();
    
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    ;
                    continue;
                }
    
                if (!ExcelFileValidationUtil.isValidFaRow(row)) {
                    log.warn("Skipping invalid FA row {}", i + 1);
                    ;
                    continue;
                }
    
                String email = getCellString(row.getCell(1));
    
                if (faRepository.existsByEmailID(email)) {
                    log.warn("Skipping duplicate FA {}", email);
                    skippedDuplicate++;
                    continue;
                }
    
                int did = getIntCellValue(row, 2);
                Optional<Departments> deptOpt = deptRepository.findById((long) did);
    
                if (deptOpt.isEmpty()) {
                    log.warn("Skipping FA row {}: invalid department {}", i + 1, did);
                    skippedMissingDept++;
                    continue;
                }
    
                Fa fa = new Fa();
                fa.setName(getCellString(row.getCell(0)));
                fa.setEmailID(email);
                fa.setDepartment(deptOpt.get());
    
                faList.add(fa);
            }
    
            faRepository.saveAll(faList);
    
            return ResponseEntity.ok(
                    "Uploaded " + faList.size() + " FAs | " +
                    ", duplicates: " + skippedDuplicate +
                    ", invalid department: " + skippedMissingDept
            );
    
        } catch (Exception ex) {
            log.error("Error uploading FA data", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to upload FA data");
        }
    }
    
    @PutMapping("/student/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable String id, @RequestBody Student updatedStudent) {
        int activity_points=0;
        try {
            Optional<Student> existingStudentOpt = studentRepository.findById(id);
            
            if (existingStudentOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Student record not found");
            }
            if (updatedStudent.getDeptPoints() < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Dept points must be positive");
            }
            if ( updatedStudent.getInstitutePoints() < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Institute points must be positive");
            }
            if (updatedStudent.getOtherPoints() < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Other points must be positive");
            }

            Student existingStudent = existingStudentOpt.get();

            if (updatedStudent.getName() != null && !updatedStudent.getName().trim().isEmpty()) {
                existingStudent.setName(updatedStudent.getName());
            }
            
            if (updatedStudent.getEmailID() != null && !updatedStudent.getEmailID().trim().isEmpty()) {
                existingStudent.setEmailID(updatedStudent.getEmailID());
            }
            
            if (updatedStudent.getDeptPoints() >= 0) {
                activity_points+=updatedStudent.getDeptPoints();
                existingStudent.setDeptPoints(updatedStudent.getDeptPoints());
            }
            
            if (updatedStudent.getInstitutePoints() >= 0) {
                activity_points+=updatedStudent.getInstitutePoints();
                existingStudent.setInstitutePoints(updatedStudent.getInstitutePoints());
            }

            if (updatedStudent.getOtherPoints() >= 0) {
                activity_points+=updatedStudent.getOtherPoints();
                existingStudent.setOtherPoints(updatedStudent.getOtherPoints());
            }
            
            existingStudent.setActivityPoints(activity_points);

            if (updatedStudent.getFaid() != 0) {
                existingStudent.setFaid(updatedStudent.getFaid());
            }

            Student savedStudent = studentRepository.save(existingStudent);
            log.info("Student updated successfully: {}", id);
            return ResponseEntity.ok(savedStudent);

        } catch (Exception ex) {
            log.error("Error updating student record: {}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to update student record");
        }
    }

    @DeleteMapping("/student/{id}")
    @Transactional
    public ResponseEntity<?> deleteStudent(@PathVariable String id) {
        try {
            if (!studentRepository.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Student record not found");
            }

            requestsRepository.deleteBySid(id);
            studentRepository.deleteById(id);
            log.info("Student deleted successfully: {}", id);
            return ResponseEntity.ok("Student record deleted successfully");

        } catch (Exception ex) {
            log.error("Error deleting student record: {}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to delete student record");
        }
    }

    @PostMapping("/students/bulk-delete")
    public ResponseEntity<?> deleteStudentsFromExcel(@RequestParam("file") MultipartFile file) {

        ValidationResult validationResult = ExcelFileValidationUtil.validateExcelFile(
                file,
                2 * 1024 * 1024, // 2MB
                "student_accounts_to_be_deleted.xlsx",
                antivirusEnabled,
                antivirusService
        );

        if (!validationResult.isValid()) {
            return ResponseEntity.badRequest().body(validationResult.getErrorMessage());
        }

        int deletedCount = 0;
        int skippedCount = 0;

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            /* ================= HEADER VALIDATION ================= */

            if (headerRow == null || headerRow.getCell(0) == null) {
                return ResponseEntity.badRequest()
                        .body("Header missing. Expected column: emailid");
            }

            String header = headerRow.getCell(0).getStringCellValue().trim();
            if (!"emailid".equalsIgnoreCase(header)) {
                return ResponseEntity.badRequest()
                        .body("Invalid header. Expected column name: emailid");
            }

            /* ================= PROCESS ROWS ================= */

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || row.getCell(0) == null) {
                    skippedCount++;
                    continue;
                }

                String email = row.getCell(0).getStringCellValue().trim();
                if (email.isEmpty()) {
                    skippedCount++;
                    continue;
                }

                Optional<Student> studentOpt = studentRepository.findByEmailID(email);
                if (studentOpt.isPresent()) {
                    studentRepository.delete(studentOpt.get());
                    deletedCount++;
                } else {
                    skippedCount++;
                }
            }

            return ResponseEntity.ok(
                    "Deleted " + deletedCount +
                    " students | Skipped " + skippedCount
            );

        } catch (Exception ex) {
            log.error("Bulk delete failed", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Bulk delete failed");
        }
    }

    // Helper methods
    private String getCellString(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue().trim();
        if (cell.getCellType() == CellType.NUMERIC) return String.valueOf((int) cell.getNumericCellValue());
        return "";
    }

    private int getIntCellValue(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) return 0;
        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return Integer.parseInt(cell.getStringCellValue().trim());
            } catch (NumberFormatException ex) {
                return 0;
            }
        }
        return 0;
    }

    
    
}