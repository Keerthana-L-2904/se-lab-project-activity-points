package com.example.student_activity_points.controller;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.student_activity_points.repository.*;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.example.student_activity_points.domain.Activity;
import com.example.student_activity_points.domain.Fa;
import com.example.student_activity_points.domain.Requests;
import com.example.student_activity_points.domain.Requests.Status;
import com.example.student_activity_points.domain.Requests.Type;
import com.example.student_activity_points.domain.StudentActivity.Validated;
import com.example.student_activity_points.domain.Student;
import com.example.student_activity_points.domain.StudentActivity;



@RestController
@RequestMapping("/api/fa")
public class FaApprovalsController {

    private final StudentActivityRepository studentActivityRepository;

    @Autowired
    private FARepository faRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private RequestsRepository requestRepository;

    @Autowired
    private ActivityRepository activityRepository;



    FaApprovalsController(StudentActivityRepository studentActivityRepository) {
        this.studentActivityRepository = studentActivityRepository;
    }

    
    @GetMapping("/get-Fa")
    public ResponseEntity<String> getFa(@RequestParam String sid) {
        Optional<Student> student=studentRepository.findBySid(sid);
        if(student==null){
           return ResponseEntity.status(404).body("Can't find FA for this student");

        }
        return ResponseEntity.ok(String.valueOf(student.get().getFaid()));

    }

    @GetMapping("/details")
    public ResponseEntity<?> getFaDetails(@RequestParam String email) {
        Optional<Fa> faOptional = faRepository.findByEmailID(email);
        if (!faOptional.isPresent()) {
            return ResponseEntity.status(404).body("FA not found");
        }
    
        Fa fa = faOptional.get();
        Long faId = fa.getFAID();
    
        // Step 2: Get requests from students under this FA's FA-ship
        List<Student> myStudents = studentRepository.findByFAID(faId.intValue());
        List<String> myStudentsIds = myStudents.stream().map(Student::getSid).collect(Collectors.toList());
    
        // Step 3: Get requests from my students
        List<Long> myStudentRequestIds = requestRepository.findBySidIn(myStudentsIds).stream()
                .map(Requests::getRid)
                .collect(Collectors.toList());
    
        
        
        // Step 4: Get full request details
        List<Requests> finalRequests = StreamSupport.stream(
                requestRepository.findAllById(myStudentRequestIds).spliterator(), false)
            .collect(Collectors.toList());
    
        // Step 5: Structure response
        List<Map<String, Object>> responseList = new ArrayList<>();
        for (Requests request : finalRequests) {
            Map<String, Object> response = new HashMap<>();
            response.put("rid", request.getRid());
            response.put("name", studentRepository.findById(request.getSid()).get().getName());
            response.put("sid", request.getSid());
            response.put("status", request.getStatus());
            response.put("activity_name", request.getActivityName());
            response.put("activity_date", request.getActivityDate());
            response.put("points", request.getPoints());  
            
            // Instead of sending raw byte[], send a URL to fetch the proof
            if (request.getProof() != null) {
                response.put("proof", "/requests/" + request.getRid() + "/proof");
            } else {
                response.put("proof", null);
            }
    
            response.put("type", request.getType());
            responseList.add(response);
        }
    
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/requests/{rid}/proof")
public ResponseEntity<byte[]> getProof(@PathVariable Long rid) {
    Optional<Requests> requestOpt = requestRepository.findById(rid);
    if (!requestOpt.isPresent()) {
        return ResponseEntity.status(404).body(null);
    }

    Requests request = requestOpt.get();
    byte[] proof = request.getProof();

    if (proof == null) {
        return ResponseEntity.status(404).body(null);
    }

    // assume proof is a PDF
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.setContentLength(proof.length);
    headers.setContentDisposition(ContentDisposition.inline()
            .filename("proof_" + rid + ".pdf")
            .build());

    return new ResponseEntity<>(proof, headers, HttpStatus.OK);
}
@GetMapping("/requests/{sid}/{aid}/proof")
public ResponseEntity<byte[]> get_Proof(@PathVariable String sid,@PathVariable Integer aid) {
    Optional<StudentActivity> studentActivityOpt = studentActivityRepository.findBySidAndActID(sid,aid);
    if (!studentActivityOpt.isPresent()) {
        return ResponseEntity.status(404).body(null);
    }

    StudentActivity studentActivity = studentActivityOpt.get();
    byte[] proof = studentActivity.getProof();

    if (proof == null) {
        return ResponseEntity.status(404).body(null);
    }

    // assume proof is a PDF
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.setContentLength(proof.length);
    headers.setContentDisposition(ContentDisposition.inline()
            .filename("proof_" + ".pdf")
            .build());

    return new ResponseEntity<>(proof, headers, HttpStatus.OK);
}

@PostMapping("/reject-request/{rid}")
public ResponseEntity<?> rejectRequest(@PathVariable Long rid, @RequestBody Map<String, String> body) {
    String comment = body.get("comment"); // clean value: "not clear"

    Optional<Requests> request = requestRepository.findById(rid);
    if (request.isPresent()) {
        Requests req = request.get();
        req.setStatus(Status.Rejected);
        req.setDecisionDate(new Date());
        req.setComments(comment);
        requestRepository.save(req);
    }
    return ResponseEntity.ok("Successfully rejected");
}
@PostMapping("/approve-request/{rid}")
public ResponseEntity<?> approveRequest(
        @PathVariable Long rid,
        @RequestParam String email,
        @RequestParam Integer points) {

    Optional<Fa> faOptional = faRepository.findByEmailID(email);
    if (!faOptional.isPresent()) {
        return ResponseEntity.status(404).body("FA not found");
    }

    Optional<Requests> requestOpt = requestRepository.findById(rid);
    if (requestOpt.isEmpty()) {
        return ResponseEntity.status(404).body("Request not found");
    }

    Requests req = requestOpt.get();
    Student student = studentRepository.findById(req.getSid()).get();

    // 🔍 Check if activity already exists
    Activity savedActivity = activityRepository.findByName(req.getActivityName())
        .orElseGet(() -> {
            Activity newActivity = new Activity();
            newActivity.setName(req.getActivityName());
            newActivity.setDescription(req.getDescription());
            newActivity.setDate(req.getActivityDate());
            newActivity.setEnd_date(req.getActivityDate());
            newActivity.setOutside_inside("Outside");
            newActivity.setMandatory(0);
            newActivity.setPoints(points);
            newActivity.setType(req.getType().toString());
            return activityRepository.save(newActivity);  // only saves if not found
        });

    // ✅ Add record to StudentActivity
    StudentActivity studentActivity = new StudentActivity();
    studentActivity.setActID(savedActivity.getActID().intValue());
    studentActivity.setSid(req.getSid());
    studentActivity.setDate(new Date());
    studentActivity.setProof(req.getProof());
    studentActivity.setValidated(Validated.Yes);
    studentActivity.setTitle(req.getActivityName());
    studentActivity.setPoints(points);
    studentActivity.setActivityType(req.getType().toString());

    if (req.getType() == Type.Institute) {
        student.setInstitutePoints(student.getInstitutePoints() + points);
    } else if (req.getType() == Type.Department) {
        student.setDeptPoints(student.getDeptPoints() + points);
    } else if (req.getType() == Type.other) {
        student.setOtherPoints(student.getOtherPoints() + points);
    }

    studentActivityRepository.save(studentActivity);
    studentRepository.save(student);

    req.setStatus(Status.Approved);
    req.setDecisionDate(new Date());
    requestRepository.save(req);

    return ResponseEntity.ok("Successfully approved");
}

}