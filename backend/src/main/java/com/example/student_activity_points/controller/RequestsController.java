package com.example.student_activity_points.controller;

import com.example.student_activity_points.domain.Requests;
import com.example.student_activity_points.repository.RequestsRepository;
import com.example.student_activity_points.security.AuthUser;
import com.example.student_activity_points.service.ClamAvAntivirusService;
import com.example.student_activity_points.dto.CreateRequestDTO;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

import jakarta.validation.Valid;
import org.apache.commons.text.StringEscapeUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/requests")
@CrossOrigin(origins = "http://localhost:5173")
public class RequestsController {

    private AuthUser currentUser() {
        return (AuthUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }


    @Autowired
    private RequestsRepository requestsRepository;

    @Autowired
    private ClamAvAntivirusService antivirusService;

    private static final Logger log = LoggerFactory.getLogger(RequestsController.class);
    private static final long MAX_FILE_SIZE = 1024 * 1024; // 1MB
    
    
    
   
    @GetMapping
    public ResponseEntity<?> getAllRequests() {
        try {
            List<Requests> requests = (List<Requests>) requestsRepository.findAll();
            log.debug("Retrieved {} requests", requests.size());
            return ResponseEntity.ok(requests);

        } catch (Exception ex) {
            log.error("Error fetching all requests", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch requests");
        }
    }

    @GetMapping("/student")
    public ResponseEntity<?> getRequestsBySid() {
        String sid = null;
        try {
            sid = currentUser().getSid();
            List<Requests> requests = requestsRepository.findBySid(sid);
            log.debug("Retrieved {} requests for student: {}", requests.size(), sid);
            return ResponseEntity.ok(requests);

        } catch (Exception ex) {
            log.error("Error fetching requests for student: {}", sid, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch requests");
        }
    }

    @PostMapping
    public ResponseEntity<?> createRequest(
            @RequestPart("proof") MultipartFile proof,
            @Valid @ModelAttribute CreateRequestDTO dto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors()
                    .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }

        AuthUser user = currentUser();
        String sid = user.getSid();

        try {
            // -------- File validation --------
            ResponseEntity<?> fileValidation = validateFile(proof);
            if (fileValidation != null) return fileValidation;

            if (!"application/pdf".equalsIgnoreCase(proof.getContentType())) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Only PDF files are allowed"));
            }

            if (!antivirusService.isFileSafe(proof)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("File failed security scan"));
            }

            // -------- Date parsing --------
            Date activityDate;
            try {
                LocalDate localDate = LocalDate.parse(dto.getActivityDate());
                activityDate = Date.from(
                        localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
                );

                if (activityDate.after(new Date())) {
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("Activity date cannot be in the future"));
                }
            } catch (DateTimeParseException e) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Invalid date format (YYYY-MM-DD expected)"));
            }

            // -------- Enum parsing --------
            Requests.Type type;
            try {
                type = Requests.Type.valueOf(dto.getType());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Invalid activity type"));
            }

            // -------- Build entity --------
            Requests request = new Requests();
            request.setSid(sid);
            request.setStatus(Requests.Status.Pending);
            request.setDate(new Date());
            request.setDecisionDate(new Date());
            request.setActivityName(sanitizeInput(dto.getActivityName()));
            request.setDescription(sanitizeInput(dto.getDescription()));
            request.setActivityDate(activityDate);
            request.setType(type);
            request.setPoints(dto.getPoints());
            request.setProof(proof.getBytes());

            Requests saved = requestsRepository.save(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (DataIntegrityViolationException ex) {
            log.warn("Duplicate/invalid request for student {}", sid, ex);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createErrorResponse("Duplicate or invalid request"));

        } catch (IOException ex) {
            log.error("File processing error for student {}", sid, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("File upload failed"));

        } catch (Exception ex) {
            log.error("Unexpected error while creating request for student {}", sid, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Unable to create request"));
        }
    }

    @PutMapping("/{rid}")
    public ResponseEntity<?> updateRequest(@PathVariable Long rid, @RequestBody Requests updated) {
        try {
            Optional<Requests> existingOpt = requestsRepository.findById(rid);
            
            if (existingOpt.isEmpty()) {
                log.warn("Request not found for update: {}", rid);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Request not found");
            }

            Requests existing = existingOpt.get();
            
            // ✅ Only update allowed fields with sanitization
            if (updated.getDescription() != null) {
                existing.setDescription(sanitizeInput(updated.getDescription()));
            }
            if (updated.getActivityDate() != null) {
                existing.setActivityDate(updated.getActivityDate());
            }
            if (updated.getActivityName() != null) {
                existing.setActivityName(sanitizeInput(updated.getActivityName()));
            }
            if (updated.getType() != null) {
                existing.setType(updated.getType());
            }
            
            existing.setDecisionDate(new Date());

            Requests savedRequest = requestsRepository.save(existing);
            log.info("Request updated successfully: {}", rid);
            return ResponseEntity.ok(savedRequest);

        } catch (DataIntegrityViolationException ex) {
            log.warn("Data integrity violation while updating request: {}", rid, ex);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Invalid request data");

        } catch (Exception ex) {
            log.error("Error updating request: {}", rid, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to update request");
        }
    }

    @DeleteMapping("/{rid}")
    public ResponseEntity<?> deleteRequest(@PathVariable Long rid) {
        try {
            if (!requestsRepository.existsById(rid)) {
                log.warn("Request not found for deletion: {}", rid);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Request not found");
            }

            requestsRepository.deleteById(rid);
            log.info("Request deleted successfully: {}", rid);
            return ResponseEntity.noContent().build();

        } catch (DataIntegrityViolationException ex) {
            log.warn("Cannot delete request due to dependencies: {}", rid, ex);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Cannot delete request - it has associated records");

        } catch (Exception ex) {
            log.error("Error deleting request: {}", rid, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to delete request");
        }
    }

    // ✅ Helper method to validate file uploads
    private ResponseEntity<?> validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Proof document is required"));
        }

        // Validate content type
        String contentType = file.getContentType();
        if (!"application/pdf".equals(contentType)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Only PDF files are allowed"));
        }

        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("File size must be less than 1MB"));
        }

        // Validate file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Invalid file extension"));
        }

        // Validate PDF magic bytes
        try {
            byte[] fileBytes = file.getBytes();
            if (fileBytes.length < 5 || 
                fileBytes[0] != 0x25 || fileBytes[1] != 0x50 || 
                fileBytes[2] != 0x44 || fileBytes[3] != 0x46) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("File is not a valid PDF"));
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Error reading file"));
        }

        return null; // No validation errors
    }

    // ✅ Helper method to sanitize text input (XSS prevention)
    private String sanitizeInput(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        // Escape HTML to prevent XSS
        String sanitized = StringEscapeUtils.escapeHtml4(input);
        
        // Remove excessive whitespace
        sanitized = sanitized.replaceAll("\\s+", " ").trim();
        if(sanitized.length()>2000){
            sanitized=sanitized.substring(0,2000);
        }
        
        return sanitized;
    }

    // Helper method to create consistent error responses
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", message);
        return errorResponse;
    }
}
