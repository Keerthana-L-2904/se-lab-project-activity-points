package com.example.student_activity_points.controller;

import com.example.student_activity_points.domain.Requests;
import com.example.student_activity_points.repository.RequestsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    // Submit a new request
    @PostMapping
    public ResponseEntity<Requests> createRequest(@RequestBody Requests req) {
        req.setDate(new Date());
        req.setStatus(Requests.Status.Pending);
        req.setDecisionDate(null);
        Requests saved = requestsRepository.save(req);
        return ResponseEntity.ok(saved);
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
