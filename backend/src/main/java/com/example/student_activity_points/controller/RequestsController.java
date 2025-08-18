package com.example.student_activity_points.controller;

import com.example.student_activity_points.domain.Activity;
import com.example.student_activity_points.domain.Requested;
import com.example.student_activity_points.domain.Requests;
import com.example.student_activity_points.domain.Requests.Status;
import com.example.student_activity_points.repository.ActivityRepository;
import com.example.student_activity_points.repository.RequestedRepository;
import com.example.student_activity_points.repository.RequestsRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.student_activity_points.domain.Requests.Type;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/requests")
public class RequestsController {

    private final RequestsRepository requestsRepository;

    @Autowired
    private RequestedRepository requestedRepository;

    @Autowired
    private ActivityRepository activityRepository;

    public RequestsController(RequestsRepository requestsRepository) {
        this.requestsRepository = requestsRepository;
    }

    // Get all requests
    @GetMapping
    public List<Requests> getAllRequests() {
        return (List<Requests>) requestsRepository.findAll();
    }

    @GetMapping("/student/{sid}")
    public ResponseEntity<List<Requests>> getRequestsBySid(@PathVariable String sid) {
        List<Requests> requests = requestsRepository.findBySid(sid);
        if (requests.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Requests> getRequestById(@PathVariable Long id) {
        Optional<Requests> request = requestsRepository.findById(id);
        return request.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Create a new request
    @PostMapping
    public Requests createRequest(@RequestBody Map<String, Object> requestBody) {
        System.out.println(requestBody.get("sid"));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); 
        Requests request = new Requests();
        request.setSid((String) requestBody.get("sid"));
        request.setStatus(Status.Pending);
        request.setLink((String) requestBody.get("link"));
        Optional<Activity> activity=activityRepository.findByName((String) requestBody.get("activityName"));
        Date activity_date=null;
        if(activity.isPresent()){
            activity_date=activity.get().getDate();
        }
        System.out.println(requestBody.get("activityDate"));

        try {
            // Validate and parse decisionDate
            String decisionDateStr = (String) requestBody.get("decisionDate");
            if (decisionDateStr != null && !decisionDateStr.isEmpty()) {
                request.setDecisionDate(dateFormat.parse(decisionDateStr));
            } else {
                System.err.println("Invalid or missing decisionDate");
                request.setDecisionDate(null); // Set to null or handle as needed
            }

            // Validate and parse activityDate
            // String activityDateStr = (String) requestBody.get("activityDate");
            // if(activityDateStr==null){
            //     activityDateStr=activity_date.toString();
            // } 
            SimpleDateFormat dateFormat_ = new SimpleDateFormat("yyyy-MM-dd");
            
            // if (activityDateStr != null && !activityDateStr.isEmpty()) {
               
            //   // request.setActivityDate(dateFormat.parse((String) requestBody.get("activityDate")));
            //   if (activityDateStr.contains("T")) {
            //     System.out.println("Received activityDate: " + activityDateStr);
            //     request.setActivityDate(dateFormat.parse(activityDateStr));
            //   }else{
            //     System.out.println("Received activityDate: " + activityDateStr);
            //     request.setActivityDate(dateFormat_.parse(activityDateStr));
            //   }
                
            // } else {
            //     System.err.println("Invalid or missing activityDate");
            //     request.setActivityDate(null); // Set to null or handle as needed
            // }

            // Validate and parse activityDate
            String activityDateStr = (String) requestBody.get("activityDate");
            if (activityDateStr != null && !activityDateStr.isEmpty()) {
                if(activityDateStr .isEmpty() || activityDateStr.contains("T")){
                    System.out.println("Received activityDate: " + activityDateStr);
                    request.setActivityDate(dateFormat.parse(activityDateStr));
                }else{
                    request.setActivityDate(dateFormat_.parse(activityDateStr));
                }
                
            } else {
                System.err.println("Invalid or missing activityDate");
                request.setActivityDate(null); // Set to null or handle as needed
            }


            request.setDate(new Date()); // Current date
        } catch (ParseException e) {
            e.printStackTrace();
            // Optionally, return an error response or handle the exception
        }

        request.setActivityName((String) requestBody.get("activityName"));
        request.setDescription((String) requestBody.get("description"));
        
        String type = (String) requestBody.get("type");
        if (type.equals("other")) {
            request.setType(Type.other);
        } else if (type.equals("Institute")) {
            request.setType(Type.Institute);
        } else if (type.equals("Department")) {
            request.setType(Type.Department);
        }
        request.setLink((String) requestBody.get("pastUrl"));

        // Save request and get generated rid
        Requests savedRequest = requestsRepository.save(request);
        Long rid = savedRequest.getRid(); // Ensure Requests entity has @Id @GeneratedValue

        // Extract faIds from requestBody
        List<Integer> faIds = (List<Integer>) requestBody.get("faIds");

        // Save FA approvals in 'requested' table
        for (Integer faId : faIds) {
            Requested requested = new Requested(); // 0 = Pending
            requested.setFaid(faId);
            requested.setApproved(false);  
            requested.setRid(rid); // Set the rid from the saved request
            requestedRepository.save(requested);
        }

        return savedRequest;
    }


    // Update an existing request
    @PutMapping("/{id}")
    public ResponseEntity<Requests> updateRequest(@PathVariable Long id, @RequestBody Requests updatedRequest) {
        return requestsRepository.findById(id).map(existingRequest -> {
            existingRequest.setSid(updatedRequest.getSid());
            existingRequest.setDate(updatedRequest.getDate());
            existingRequest.setStatus(updatedRequest.getStatus());
            existingRequest.setLink(updatedRequest.getLink());
            existingRequest.setDecisionDate(updatedRequest.getDecisionDate());
            existingRequest.setActivityName(updatedRequest.getActivityName());
            existingRequest.setDescription(updatedRequest.getDescription());
            existingRequest.setActivityDate(updatedRequest.getActivityDate());
            existingRequest.setType(updatedRequest.getType());
            Requests savedRequest = requestsRepository.save(existingRequest);
            return ResponseEntity.ok(savedRequest);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Delete a request
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRequest(@PathVariable Long id) {
        if (requestsRepository.existsById(id)) {
            requestsRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}