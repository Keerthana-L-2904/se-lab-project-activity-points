package com.example.student_activity_points.controller;

import com.example.student_activity_points.domain.Requests;
import com.example.student_activity_points.repository.RequestsRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/requests")
@CrossOrigin(origins = "http://localhost:5173")
public class RequestsController {

    @Autowired
    private RequestsRepository requestsRepository;

    private static final Logger log = LoggerFactory.getLogger(RequestsController.class);
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

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

    @GetMapping("/student/{sid}")
    public ResponseEntity<?> getRequestsBySid(@PathVariable String sid) {
        try {
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
            @RequestParam("sid") String sid,
            @RequestParam("date") String date,
            @RequestParam("status") String status,
            @RequestParam("decisionDate") String decisionDate,
            @RequestParam("activityName") String activityName,
            @RequestParam("description") String description,
            @RequestParam("activityDate") String activityDate,
            @RequestParam("type") String type,
            @RequestParam("points") Integer points) {

        try {
            // Validate inputs
            if (sid == null || sid.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Student ID is required"));
            }

            if (activityName == null || activityName.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Activity name is required"));
            }

            if (points == null || points <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Valid points value is required"));
            }

            if (proof.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Proof document is required"));
            }

            // Parse dates and enums
            Date parsedDate;
            Date parsedDecisionDate;
            Date parsedActivityDate;
            Requests.Status parsedStatus;
            Requests.Type parsedType;

            try {
                parsedDate = DATE_TIME_FORMAT.parse(date);
                parsedDecisionDate = DATE_TIME_FORMAT.parse(decisionDate);
                parsedActivityDate = DATE_FORMAT.parse(activityDate);
                parsedStatus = Requests.Status.valueOf(status);
                parsedType = Requests.Type.valueOf(type);
            } catch (ParseException ex) {
                log.warn("Date parsing error for request from student: {}", sid, ex);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Invalid date format"));
            } catch (IllegalArgumentException ex) {
                log.warn("Invalid enum value for request from student: {}", sid, ex);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Invalid status or type value"));
            }

            // Create and populate Requests object
            Requests request = new Requests();
            request.setSid(sid);
            request.setDate(parsedDate);
            request.setStatus(parsedStatus);
            request.setDecisionDate(parsedDecisionDate);
            request.setActivityName(activityName);
            request.setDescription(description);
            request.setActivityDate(parsedActivityDate);
            request.setType(parsedType);
            request.setPoints(points);
            request.setProof(proof.getBytes());

            // Save to database
            Requests savedRequest = requestsRepository.save(request);
            log.info("Request created successfully: rid={}, sid={}, activity={}", 
                     savedRequest.getRid(), sid, activityName);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedRequest);

        } catch (DataIntegrityViolationException ex) {
            log.warn("Data integrity violation while creating request for student: {}", sid, ex);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createErrorResponse("Invalid request data or duplicate entry"));

        } catch (Exception ex) {
            log.error("Error creating request for student: {}", sid, ex);
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
            existing.setStatus(updated.getStatus());
            existing.setDecisionDate(new Date());
            existing.setDescription(updated.getDescription());
            existing.setActivityDate(updated.getActivityDate());
            existing.setActivityName(updated.getActivityName());
            existing.setType(updated.getType());

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

    // Helper method to create consistent error responses
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", message);
        return errorResponse;
    }
}