package com.example.student_activity_points.controller;

import com.example.student_activity_points.domain.Requests;
import com.example.student_activity_points.repository.RequestsRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/requests")
@CrossOrigin(origins = "http://localhost:5173") // adjust if needed
public class RequestsController {

    @Autowired
    private RequestsRepository requestsRepository;

    // Get all requests
    @GetMapping
    public List<Requests> getAllRequests() {
        return (List<Requests>) requestsRepository.findAll();
    }

    // Get requests by student ID
    @GetMapping("/student/{sid}")
    public ResponseEntity<List<Requests>> getRequestsBySid(@PathVariable String sid) {
        List<Requests> requests = requestsRepository.findBySid(sid);
        if (requests.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(requests);
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
    @RequestParam("points") Integer points   // <-- new field
) {
    try {
        // Log incoming data for debugging
        System.out.println("Received request:");
        System.out.println("sid: " + sid);
        System.out.println("points: " + points);  // log points

        // Parse dates and enums
        Date parsedDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(date);
        Date parsedDecisionDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(decisionDate);
        Date parsedActivityDate = new SimpleDateFormat("yyyy-MM-dd").parse(activityDate);
        Requests.Status parsedStatus = Requests.Status.valueOf(status);
        Requests.Type parsedType = Requests.Type.valueOf(type);

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
        request.setPoints(points);  // <-- set points

        // Save the file content
        request.setProof(proof.getBytes());

        // Save to database
        Requests savedRequest = requestsRepository.save(request);
        return ResponseEntity.ok(savedRequest);

    } catch (Exception e) {
        e.printStackTrace();
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", "An error occurred while processing the request.");
        errorResponse.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}

    // Update request (approve/reject)
    @PutMapping("/{rid}")
    public ResponseEntity<Requests> updateRequest(@PathVariable Long rid, @RequestBody Requests updated) {
        return requestsRepository.findById(rid).map(existing -> {
            existing.setStatus(updated.getStatus());
            existing.setDecisionDate(new Date());
            existing.setLink(updated.getLink());
            existing.setDescription(updated.getDescription());
            existing.setActivityDate(updated.getActivityDate());
            existing.setActivityName(updated.getActivityName());
            existing.setType(updated.getType());
            return ResponseEntity.ok(requestsRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Delete a request
    @DeleteMapping("/{rid}")
    public ResponseEntity<Void> deleteRequest(@PathVariable Long rid) {
        if (requestsRepository.existsById(rid)) {
            requestsRepository.deleteById(rid);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
