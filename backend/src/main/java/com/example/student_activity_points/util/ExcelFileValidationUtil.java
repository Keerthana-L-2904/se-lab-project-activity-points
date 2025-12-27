package com.example.student_activity_points.util;

import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExcelFileValidationUtil {

    private static final Logger log = LoggerFactory.getLogger(ExcelFileValidationUtil.class);

    /* ===================== FILE SECURITY ===================== */

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-excel"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("xlsx", "xls");

    private static final List<String> DANGEROUS_PATTERNS = List.of(
            "..", "./", "\\", ":", "|", "<", ">", "\"", "*", "?"
    );

    /* ===================== PUBLIC VALIDATION ===================== */

    public static ValidationResult validateExcelFile(
            MultipartFile file,
            long maxSizeBytes,
            String expectedFilename,
            boolean enableVirusScanning,
            AntivirusService antivirusService) {

        if (file == null || file.isEmpty()) {
            return ValidationResult.failure("File is empty or missing");
        }

        if (file.getSize() > maxSizeBytes) {
            return ValidationResult.failure("File size exceeds allowed limit");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            return ValidationResult.failure("Filename missing");
        }

        if (expectedFilename != null &&
                !filename.equalsIgnoreCase(expectedFilename)) {
            return ValidationResult.failure(
                    "Invalid filename. Expected: " + expectedFilename
            );
        }

        for (String pattern : DANGEROUS_PATTERNS) {
            if (filename.contains(pattern)) {
                log.warn("Dangerous filename detected: {}", filename);
                return ValidationResult.failure("Invalid filename detected");
            }
        }

        String extension = getFileExtension(filename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            return ValidationResult.failure("Only .xlsx or .xls files are allowed");
        }

        if (!ALLOWED_MIME_TYPES.contains(file.getContentType())) {
            return ValidationResult.failure("Invalid file type");
        }

        try {
            if (!isValidExcelFile(file.getInputStream())) {
                return ValidationResult.failure("File content is not Excel format");
            }
        } catch (IOException e) {
            return ValidationResult.failure("Unable to read file content");
        }

        if (enableVirusScanning) {
            if (antivirusService == null) {
                return ValidationResult.failure("Antivirus unavailable");
            }
            try {
                if (!antivirusService.isFileSafe(file)) {
                    return ValidationResult.failure("File failed security scan");
                }
            } catch (Exception e) {
                return ValidationResult.failure("Security scan failed");
            }
        }

        return ValidationResult.success();
    }


    public static boolean isValidStudentRow(Row row) {
        // Must have columns 0–7
        for (int i = 0; i <= 7; i++) {
            if (row.getCell(i) == null) {
                return false;
            }
        }
    
        String sid = getCellString(row.getCell(0));
        String email = getCellString(row.getCell(2));
        String name = getCellString(row.getCell(1));
    
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
                return isIntegerCell(row.getCell(3)) &&  // did
                       isIntegerCell(row.getCell(4)) &&  // faid
                       isIntegerCell(row.getCell(5)) &&  // deptPoints
                       isIntegerCell(row.getCell(6)) &&  // institutePoints
                       isIntegerCell(row.getCell(7));    // otherPoints
            }
        
    private static boolean isValidEmail(String email) {
        if (email == null) return false;
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }


    public static boolean isValidFaRow(Row row) {

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
    

    /* ===================== EXCEL STRUCTURE ===================== */

    public static boolean validateHeaderRow(Row headerRow, String[] expectedHeaders) {
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

    /* ===================== CELL HELPERS ===================== */

    public static String getCellString(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING)
            return cell.getStringCellValue().trim();
        if (cell.getCellType() == CellType.NUMERIC)
            return String.valueOf((int) cell.getNumericCellValue());
        return "";
    }

    public static Integer getCellInt(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC)
                return (int) cell.getNumericCellValue();
            if (cell.getCellType() == CellType.STRING)
                return Integer.parseInt(cell.getStringCellValue().trim());
        } catch (Exception ignored) {}
        return null;
    }

    public static boolean isIntegerCell(Cell cell) {
        if (cell == null) return false;
        if (cell.getCellType() == CellType.NUMERIC) return true;
        if (cell.getCellType() == CellType.STRING)
            return cell.getStringCellValue().trim().matches("\\d+");
        return false;
    }

    /* ===================== DATE HELPERS ===================== */

    public static Date parseDateSafe(Cell cell) {
        try {
            if (cell == null) return null;

            if (cell.getCellType() == CellType.NUMERIC &&
                    DateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue();
            }

            if (cell.getCellType() == CellType.STRING) {
                String s = cell.getStringCellValue().trim();
                if (!s.isEmpty()) {
                    return new SimpleDateFormat("yyyy-MM-dd").parse(s);
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    public static LocalDate parseLocalDate(Cell cell) {
        try {
            if (cell == null) return null;

            if (cell.getCellType() == CellType.NUMERIC &&
                    DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            }

            if (cell.getCellType() == CellType.STRING) {
                return LocalDate.parse(cell.getStringCellValue().trim());
            }
        } catch (Exception ignored) {}
        return null;
    }

    /* ===================== ACTIVITY ROW VALIDATION ===================== */

    public static boolean isValidActivityRow(Row row) {

        for (int i = 0; i <= 7; i++) {
            if (row.getCell(i) == null) return false;
        }

        String name = getCellString(row.getCell(0));
        String desc = getCellString(row.getCell(1));
        String type = getCellString(row.getCell(6));

        if (name.isBlank() || desc.isBlank() || type.isBlank()) return false;

        Integer points = getCellInt(row.getCell(2));
        Integer did = getCellInt(row.getCell(3));
        Integer mandatory = getCellInt(row.getCell(7));

        if (points == null || points <= 0) return false;
        if (did == null || did <= 0) return false;
        if (mandatory == null || (mandatory != 0 && mandatory != 1)) return false;

        LocalDate start = parseLocalDate(row.getCell(4));
        LocalDate end = parseLocalDate(row.getCell(5));

        return start != null && end != null && !end.isBefore(start);
    }

    /* ===================== INTERNAL ===================== */

    private static boolean isValidExcelFile(InputStream is) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(is)) {
            FileMagic magic = FileMagic.valueOf(bis);
            return magic == FileMagic.OOXML || magic == FileMagic.OLE2;
        }
    }

    private static String getFileExtension(String name) {
        int dot = name.lastIndexOf('.');
        return (dot == -1) ? "" : name.substring(dot + 1).toLowerCase();
    }

    /* ===================== RESULT + AV ===================== */

    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult failure(String msg) {
            return new ValidationResult(false, msg);
        }

        public boolean isValid() { return valid; }
        public String getErrorMessage() { return errorMessage; }
    }

    public interface AntivirusService {
        boolean isFileSafe(MultipartFile file) throws IOException;
    }
}
