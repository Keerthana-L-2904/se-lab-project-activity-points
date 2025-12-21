package com.example.student_activity_points.controller;

import com.example.student_activity_points.domain.Departments;
import com.example.student_activity_points.domain.Fa;
import com.example.student_activity_points.domain.Student;
import com.example.student_activity_points.repository.DepartmentsRepository;
import com.example.student_activity_points.repository.FARepository;
import com.example.student_activity_points.repository.RequestsRepository;
import com.example.student_activity_points.repository.StudentRepository;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    private static final Logger log = LoggerFactory.getLogger(AdminManageUsersController.class);

    @GetMapping("/student")
    public ResponseEntity<?> getStudents() {
        try {
            List<Student> students = (List<Student>) studentRepository.findAll();
            return ResponseEntity.ok(students);

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
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please upload a valid Excel file.");
        }
        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null ||
            !originalFileName.equalsIgnoreCase("student_accounts_to_be_created.xlsx")) {
            return ResponseEntity.badRequest()
                    .body("Invalid file. Please upload 'student_accounts_to_be_created.xlsx' only.");
        }

        String[] STUDENT_HEADERS = {
            "sid", "did", "faid", "other_points",
            "dept_points", "emailid", "institute_points", "name"
        };
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (!validateHeaderRow(sheet.getRow(0), STUDENT_HEADERS)) {
                return ResponseEntity.badRequest()
                        .body("Invalid column order for student file.");
            }
    
            List<Student> students = new ArrayList<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                if (!isValidStudentRow(row)) {
                    log.warn("Skipping invalid row {}", i);
                    continue;
                }
                Student student = new Student();
                student.setSid(getCellString(row.getCell(0)));
                student.setEmailID(getCellString(row.getCell(5)));
                student.setName(getCellString(row.getCell(7)));

                student.setFaid(getIntCellValue(row, 2));
                student.setOtherPoints(getIntCellValue(row, 3));
                student.setDeptPoints(getIntCellValue(row, 4));
                student.setDid(getIntCellValue(row, 1));
                student.setInstitutePoints(getIntCellValue(row, 6));

                // Calculate activity points
                student.setActivityPoints(
                    student.getDeptPoints() + 
                    student.getInstitutePoints() + 
                    student.getOtherPoints()
                );

                // Avoid duplicates
                if (!studentRepository.existsById(student.getSid())) {
                    students.add(student);
                }
            }

            studentRepository.saveAll(students);
            log.info("Uploaded {} students successfully", students.size());
            return ResponseEntity.ok("Uploaded " + students.size() + " students successfully.");

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
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please upload a valid Excel file.");
        }

        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null ||
            !originalFileName.equalsIgnoreCase("fa_accounts_to_be_created.xlsx")) {
            return ResponseEntity.badRequest()
                    .body("Invalid file. Please upload 'fa_accounts_to_be_created.xlsx' only.");
        }

        String[] FA_HEADERS = { "name", "emailID", "DID" };

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            if (!validateHeaderRow(sheet.getRow(0), FA_HEADERS)) {
                return ResponseEntity.badRequest()
                        .body("Invalid column order for FA file.");
            }
            
            List<Fa> faList = new ArrayList<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
            
                // ✅ Validate FA row
                if (!isValidFaRow(row)) {
                    log.warn("Skipping invalid FA row {}", i + 1);
                    continue;
                }
            
                String email = getCellString(row.getCell(1));
            
                // Avoid duplicates
                if (faRepository.existsByEmailID(email)) {
                    log.warn("Skipping row {}: FA with email {} already exists", i + 1, email);
                    continue;
                }
            
                int did = getIntCellValue(row, 2);
            
                Optional<Departments> deptOpt = deptRepository.findById((long) did);
                if (deptOpt.isEmpty()) {
                    log.warn("Skipping row {}: Department ID {} not found", i + 1, did);
                    continue;
                }
            
                Fa fa = new Fa();
                fa.setName(getCellString(row.getCell(0)));
                fa.setEmailID(email);
                fa.setDepartment(deptOpt.get());
            
                faList.add(fa);
            }
            
            faRepository.saveAll(faList);
            log.info("Uploaded {} FA records successfully", faList.size());
            return ResponseEntity.ok("Uploaded " + faList.size() + " FA records successfully");

        } catch (Exception ex) {
            log.error("Error uploading FA data", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to upload FA data");
        }
    }

    @PutMapping("/student/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable String id, @RequestBody Student updatedStudent) {
        try {
            Optional<Student> existingStudentOpt = studentRepository.findById(id);
            
            if (existingStudentOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Student record not found");
            }

            Student existingStudent = existingStudentOpt.get();

            if (updatedStudent.getName() != null && !updatedStudent.getName().trim().isEmpty()) {
                existingStudent.setName(updatedStudent.getName());
            }
            
            if (updatedStudent.getEmailID() != null && !updatedStudent.getEmailID().trim().isEmpty()) {
                existingStudent.setEmailID(updatedStudent.getEmailID());
            }
            
            if (updatedStudent.getDeptPoints() != 0) {
                existingStudent.setDeptPoints(updatedStudent.getDeptPoints());
            }
            
            if (updatedStudent.getInstitutePoints() != 0) {
                existingStudent.setInstitutePoints(updatedStudent.getInstitutePoints());
            }

            if (updatedStudent.getOtherPoints() != 0) {
                existingStudent.setOtherPoints(updatedStudent.getOtherPoints());
            }
            
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
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please upload a valid Excel file.");
        }
        
        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null ||
            !originalFileName.equalsIgnoreCase("student_accounts_to_be_deleted.xlsx")) {
            return ResponseEntity.badRequest()
                    .body("Invalid file. Please upload 'student_accounts_to_be_deleted.xlsx' only.");
        }
        try {
            List<String> emails = new ArrayList<>();

            try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
                Sheet sheet = workbook.getSheetAt(0);

                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row != null && row.getCell(0) != null) {
                        String email = getCellString(row.getCell(0));
                        if (!email.isEmpty()) {
                            emails.add(email);
                        }
                    }
                }
            }

            if (emails.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("No email addresses found in the Excel file.");
            }

            int deletedCount = 0;
            for (String email : emails) {
                Optional<Student> studentOpt = studentRepository.findByEmailID(email);
                if (studentOpt.isPresent()) {
                    studentRepository.delete(studentOpt.get());
                    deletedCount++;
                }
            }

            log.info("Bulk deleted {} students successfully", deletedCount);
            return ResponseEntity.ok("Deleted " + deletedCount + " students successfully.");

        } catch (Exception ex) {
            log.error("Error during bulk delete of students", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to delete students");
        }
    }

    @GetMapping("/fas/filter")
    public ResponseEntity<?> getFasByDept(@RequestParam String deptName) {
        try {
            List<Fa> fas = faRepository.findByDepartment_Name(deptName);
            
            if (fas.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No FAs found for department " + deptName);
            }
            
            return ResponseEntity.ok(fas);

        } catch (Exception ex) {
            log.error("Error filtering FAs by department: {}", deptName, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to filter FAs");
        }
    }

    @GetMapping("/students/filter")
    public ResponseEntity<?> getStudentsByDeptAndYear(
            @RequestParam String dept,
            @RequestParam String year) {
        try {
            final String finalDept = dept.toUpperCase().trim();
            final String finalYear = year.trim();
    
            List<Student> allStudents = (List<Student>) studentRepository.findAll();
    
            List<Student> filteredStudents = allStudents.stream()
                    .filter(s -> s.getSid() != null && s.getSid().length() >= 4)
                    .filter(s -> s.getSid().substring(1, 3).equals(finalYear))
                    .filter(s -> s.getSid().substring(s.getSid().length() - 2).equalsIgnoreCase(finalDept))
                    .collect(Collectors.toList());
    
            if (filteredStudents.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No students found for department " + finalDept + " and year " + finalYear);
            }
    
            return ResponseEntity.ok(filteredStudents);
    
        } catch (Exception ex) {
            log.error("Error filtering students by dept: {} and year: {}", dept, year, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to filter students");
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

    private boolean isValidStudentRow(Row row) {
        // Must have columns 0–7
        for (int i = 0; i <= 7; i++) {
            if (row.getCell(i) == null) {
                return false;
            }
        }
    
        String sid = getCellString(row.getCell(0));
        String email = getCellString(row.getCell(5));
        String name = getCellString(row.getCell(7));
    
        // SID: exactly 9 characters
        if (sid == null || sid.length() != 9) {
            return false;
        }
    
        // Email format
        if (!isValidEmail(email)) {
            return false;
        }
    
        // Name must be alphabetic (allow spaces)
        if (name == null || !name.matches("[A-Za-z ]+")) {
            return false;
        }
    
        // Integer validations
        return isIntegerCell(row.getCell(1)) &&  // did
               isIntegerCell(row.getCell(2)) &&  // faid
               isIntegerCell(row.getCell(3)) &&  // otherPoints
               isIntegerCell(row.getCell(4)) &&  // deptPoints
               isIntegerCell(row.getCell(6));    // institutePoints
    }

    private boolean isValidEmail(String email) {
        if (email == null) return false;
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }
    
    private boolean isIntegerCell(Cell cell) {
        if (cell == null) return false;
    
        if (cell.getCellType() == CellType.NUMERIC) {
            return true;
        }
    
        if (cell.getCellType() == CellType.STRING) {
            try {
                Integer.parseInt(cell.getStringCellValue().trim());
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    
        return false;
    }

    private boolean isValidFaRow(Row row) {

        // Must have columns 0–2
        for (int i = 0; i <= 2; i++) {
            if (row.getCell(i) == null) {
                return false;
            }
        }
    
        String name = getCellString(row.getCell(0));
        String email = getCellString(row.getCell(1));
    
        // Name: alphabets and spaces only
        if (name == null || !name.matches("[A-Za-z ]+")) {
            return false;
        }
    
        // Email format
        if (!isValidEmail(email)) {
            return false;
        }
    
        // DID must be integer
        return isIntegerCell(row.getCell(2));
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