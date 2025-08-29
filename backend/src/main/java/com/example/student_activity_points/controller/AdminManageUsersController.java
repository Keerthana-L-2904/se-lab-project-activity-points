package com.example.student_activity_points.controller;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import com.example.student_activity_points.repository.DepartmentsRepository;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.student_activity_points.domain.Activity;
import com.example.student_activity_points.domain.Departments;
import com.example.student_activity_points.domain.Fa;
import com.example.student_activity_points.domain.Student;
import com.example.student_activity_points.repository.FARepository;
import com.example.student_activity_points.repository.StudentRepository;


@RestController
@RequestMapping("/api/admin/manage-users")
public class AdminManageUsersController {
    
    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private FARepository faRepository;
    
    @Autowired
    private DepartmentsRepository deptRepository;


    @GetMapping("/student")
     public ResponseEntity<?> getStudents() {
        try {
            List<Student> students = (List<Student>) studentRepository.findAll();
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
        }
    }

    @GetMapping("/fa")
     public ResponseEntity<?> getFA() {
        try {
            List<Fa> fas = (List<Fa>) faRepository.findAll();
            return ResponseEntity.ok(fas);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
        }
    }

    @PostMapping ("/student")
    public ResponseEntity<?> addStudent(@RequestBody Student student) {
        try {

            System.out.println("Received Student: " + student); // Debugging
            if (student.getSid() == null || student.getSid().trim().isEmpty()) {
                return ResponseEntity.status(400).body("Error: sid (roll number) must be provided.");
            }
    
            if (studentRepository.existsById(student.getSid())) {
                return ResponseEntity.status(400).body("Error: Student with sid " + student.getSid() + " already exists.");
            }
            System.out.println("FAID: " + student.getFaid());
            System.out.println("SID: " + student.getSid());
            System.out.println("DID: " + student.getDid());
            System.out.println("Dept points: " + student.getDeptPoints());
            System.out.println("Inst points: " + student.getInstitutePoints());
            System.out.println("Other points: " + student.getOtherPoints());
            System.out.println("EmailID: " + student.getEmailID());



            
            Student savedStudent = studentRepository.save(student);
            return ResponseEntity.ok(savedStudent);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error adding student: " + e.getMessage());
        }
    }

    @PostMapping("/upload-students")
    public ResponseEntity<?> uploadStudents(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please upload a valid Excel file.");
        }
        
        try (InputStream inputStream = file.getInputStream();
            Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0); // First sheet
            List<Student> students = new ArrayList<>();

            // Skip the header (row 0), start from row 1
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Student student = new Student();
                student.setSid(row.getCell(0).getStringCellValue().trim());
                student.setFaid((int) row.getCell(2).getNumericCellValue());
                student.setDid((int) row.getCell(1).getNumericCellValue());
                student.setDeptPoints((int) row.getCell(4).getNumericCellValue());
                student.setInstitutePoints((int) row.getCell(6).getNumericCellValue());
                student.setEmailID(row.getCell(5).getStringCellValue().trim());
                student.setActivityPoints((int) row.getCell(3).getNumericCellValue());
                student.setName(row.getCell(7).getStringCellValue());

                // avoid duplicates
                if (!studentRepository.existsById(student.getSid())) {
                    students.add(student);
                }
            }

            studentRepository.saveAll(students); // bulk save
            return ResponseEntity.ok("Uploaded " + students.size() + " students successfully.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error uploading students: " + e.getMessage());
        }
    }
    @PostMapping ("/fa")
    public ResponseEntity<?> addFA(@RequestBody Fa fa) {
        try {
            Fa savedFa = faRepository.save(fa);
            return ResponseEntity.ok(savedFa);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error adding fa: " + e.getMessage());
        }
    }

    @PostMapping("/upload-fas")
    public ResponseEntity<?> uploadFa(@RequestParam("file") MultipartFile file) {
        try (InputStream is = file.getInputStream();
            Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            List<Fa> faList = new ArrayList<>();

            // skip first row (headers)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Fa fa = new Fa();

                // assuming Excel columns are: name | emailID | DID
                fa.setName(row.getCell(0).getStringCellValue().trim());
                fa.setEmailID(row.getCell(1).getStringCellValue().trim());
                fa.setDID((int) row.getCell(2).getNumericCellValue());

                faList.add(fa);
            }

            faRepository.saveAll(faList);
            return ResponseEntity.ok("Uploaded " + faList.size() + " FA records successfully");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error uploading FA data: " + e.getMessage());
        }
    }


    @PutMapping("/student/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable String id, @RequestBody Student updatedStudent) {
        try {
            System.out.println(id);
            Optional<Student> existingStudentOpt = studentRepository.findById(id);
            if (existingStudentOpt .isPresent()) {
                Student existingStudent = existingStudentOpt.get();

                if (updatedStudent.getName() != null && !updatedStudent.getName().trim().isEmpty()) {
                    existingStudent.setName(updatedStudent.getName());
                }
                
                if (updatedStudent.getDid() != 0) { // assuming 0 means "not set"
                    existingStudent.setDid(updatedStudent.getDid());
                }
                
                if (updatedStudent.getEmailID() != null && !updatedStudent.getEmailID().trim().isEmpty()) {
                    existingStudent.setEmailID(updatedStudent.getEmailID());
                }
                
                if (updatedStudent.getFaid() != 0) {
                    existingStudent.setFaid(updatedStudent.getFaid());
                }
                
                if (updatedStudent.getInstitutePoints() != 0) {
                    existingStudent.setInstitutePoints(updatedStudent.getInstitutePoints());
                }
                
                if (updatedStudent.getDeptPoints() != 0) {
                    existingStudent.setDeptPoints(updatedStudent.getDeptPoints());
                }
                

                Student savedStudent = studentRepository.save(existingStudent);
                return ResponseEntity.ok(savedStudent);
            } else {
                return ResponseEntity.status(404).body("Student record not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error updating Student record: " + e.getMessage());
        }
    
}

@PutMapping("/fa/{id}")
public ResponseEntity<?> updateFA(@PathVariable Long id, @RequestBody Fa updatedFa) {
    try {
        Optional<Fa> existingFaOpt = faRepository.findById(id);
        if (existingFaOpt.isPresent()) {
            Fa existingFa = existingFaOpt.get();
            existingFa.setName(updatedFa.getName());
            existingFa.setEmailID(updatedFa.getEmailID());
            existingFa.setDID(updatedFa.getDID());


            Fa savedFa = faRepository.save(existingFa);
            return ResponseEntity.ok(savedFa);
        } else {
            return ResponseEntity.status(404).body("Fa record not found");
        }
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(500).body("Error updating Fa record: " + e.getMessage());
    }
}

    @DeleteMapping("/student/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable String id) {
        try {

            if (studentRepository.existsById(id)) {
                studentRepository.deleteById(id);
                return ResponseEntity.ok("Student record deleted successfully");
            } else {
                return ResponseEntity.status(404).body("Student record not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error deleting student record: " + e.getMessage());
        }
    }
    @DeleteMapping("/fa/{id}")
    public ResponseEntity<?> deleteActivity(@PathVariable Long id) {
        try {

            if (faRepository.existsById(id)) {
                faRepository.deleteById(id);
                return ResponseEntity.ok("Fa record deleted successfully");
            } else {
                return ResponseEntity.status(404).body("Fa record not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error deleting Fa record: " + e.getMessage());
        }
    }

    @PostMapping("/students/bulk-delete")
    public ResponseEntity<?> deleteStudentsFromExcel(@RequestParam("file") MultipartFile file) {
        try {
            List<String> emails = new ArrayList<>();

            // Read Excel
            try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
                Sheet sheet = workbook.getSheetAt(0);

                // Skip header row (start from row 1)
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row != null && row.getCell(0) != null) { // Assuming first column has emails
                        String email = row.getCell(0).getStringCellValue().trim();
                        if (!email.isEmpty()) {
                            emails.add(email);
                        }
                    }
                }
            }

            if (emails.isEmpty()) {
                return ResponseEntity.badRequest().body("No email addresses found in the Excel file.");
            }

            // Delete by email
            int deletedCount = 0;
            for (String email : emails) {
                Optional<Student> studentOpt = studentRepository.findByEmailID(email);
                if (studentOpt.isPresent()) {
                    studentRepository.delete(studentOpt.get());
                    deletedCount++;
                }
            }

            return ResponseEntity.ok("Deleted " + deletedCount + " students successfully.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error while deleting students: " + e.getMessage());
        }
    }

        @GetMapping("/fas/filter")
        public ResponseEntity<?> getFasByDept(@RequestParam String deptName) {
            List<Fa> fas = faRepository.findByDepartment_Name(deptName);
            if (fas.isEmpty()) {
                return ResponseEntity.status(404).body("No FAs found for dept " + deptName);
            }
            return ResponseEntity.ok(fas);
        }

    @GetMapping("/students/filter")
    public ResponseEntity<?> getStudentsByDeptAndYear(
            @RequestParam String dept,
            @RequestParam String year) {
        try {
            // Normalize input into new variables
            final String finalDept = dept.toUpperCase().trim();
            final String finalYear = year.trim();
    
            // Fetch all students
            List<Student> allStudents = (List<Student>) studentRepository.findAll();
    
            // Filter based on sid
            List<Student> filteredStudents = allStudents.stream()
                    .filter(s -> s.getSid() != null && s.getSid().length() >= 4)
                    .filter(s -> s.getSid().substring(1, 3).equals(finalYear)) // year check
                    .filter(s -> s.getSid().substring(s.getSid().length() - 2).equalsIgnoreCase(finalDept)) // dept check
                    .toList();
    
            if (filteredStudents.isEmpty()) {
                return ResponseEntity.status(404)
                        .body("No students found for department " + finalDept + " and year " + finalYear);
            }
    
            return ResponseEntity.ok(filteredStudents);
    
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body("Error while filtering students: " + e.getMessage());
        }
    }
    
}