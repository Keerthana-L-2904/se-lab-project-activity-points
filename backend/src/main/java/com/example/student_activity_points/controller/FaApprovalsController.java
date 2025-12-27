package com.example.student_activity_points.controller;

import com.example.student_activity_points.domain.Activity;
import com.example.student_activity_points.domain.Fa;
import com.example.student_activity_points.domain.Requests;
import com.example.student_activity_points.domain.Requests.Status;
import com.example.student_activity_points.domain.Requests.Type;
import com.example.student_activity_points.domain.Student;
import com.example.student_activity_points.domain.StudentActivity;
import com.example.student_activity_points.domain.StudentActivity.Validated;
import com.example.student_activity_points.repository.ActivityRepository;
import com.example.student_activity_points.repository.FARepository;
import com.example.student_activity_points.repository.RequestsRepository;
import com.example.student_activity_points.repository.StudentActivityRepository;
import com.example.student_activity_points.repository.StudentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/fa")
public class FaApprovalsController {

    @Autowired
    private StudentActivityRepository studentActivityRepository;

    @Autowired
    private FARepository faRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private RequestsRepository requestRepository;

    @Autowired
    private ActivityRepository activityRepository;

    private static final Logger log = LoggerFactory.getLogger(FaApprovalsController.class);

    @GetMapping("/get-Fa")
    public ResponseEntity<?> getFa(@RequestParam String sid) {
        try {
            Optional<Student> student = studentRepository.findBySid(sid);
            
            if (student.isEmpty()) {
                log.warn("Student not found: {}", sid);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Can't find FA for this student");
            }

            return ResponseEntity.ok(String.valueOf(student.get().getFaid()));

        } catch (Exception ex) {
            log.error("Error fetching FA for student: {}", sid, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch FA details");
        }
    }

    @GetMapping("/details")
    public ResponseEntity<?> getFaDetails(@RequestParam String email) {
        try {
            Optional<Fa> faOptional = faRepository.findByEmailID(email);
            
            if (faOptional.isEmpty()) {
                log.warn("FA not found: {}", email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("FA not found");
            }

            Fa fa = faOptional.get();
            Long faId = fa.getFAID();

            // Get students under this FA
            List<Student> myStudents = studentRepository.findByFAID(faId.intValue());
            List<String> myStudentsIds = myStudents.stream()
                    .map(Student::getSid)
                    .collect(Collectors.toList());

            // Get requests from FA's students
            List<Long> myStudentRequestIds = requestRepository.findBySidIn(myStudentsIds).stream()
                    .map(Requests::getRid)
                    .collect(Collectors.toList());

            // Get full request details
            List<Requests> finalRequests = StreamSupport.stream(
                    requestRepository.findAllById(myStudentRequestIds).spliterator(), false)
                    .collect(Collectors.toList());

            // Structure response
            List<Map<String, Object>> responseList = new ArrayList<>();
            for (Requests request : finalRequests) {
                Optional<Student> studentOpt = studentRepository.findById(request.getSid());
                
                if (studentOpt.isEmpty()) {
                    log.warn("Student not found for request: {}", request.getRid());
                    continue;
                }

                Map<String, Object> response = new HashMap<>();
                response.put("rid", request.getRid());
                response.put("name", studentOpt.get().getName());
                response.put("sid", request.getSid());
                response.put("status", request.getStatus());
                response.put("activity_name", request.getActivityName());
                response.put("activity_date", request.getActivityDate());
                response.put("points", request.getPoints());

                // Send proof URL instead of raw bytes
                if (request.getProof() != null) {
                    response.put("proof", "/api/fa/requests/" + request.getRid() + "/proof");
                } else {
                    response.put("proof", null);
                }

                response.put("type", request.getType());
                responseList.add(response);
            }

            log.debug("Retrieved {} requests for FA: {}", responseList.size(), email);
         

                return ResponseEntity.ok(responseList);

        } catch (Exception ex) {
            log.error("Error fetching FA details for: {}", email, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch FA details");
        }
    }

    @GetMapping("/requests/{rid}/proof")
    public ResponseEntity<?> getProof(@PathVariable Long rid) {
        try {
            Optional<Requests> requestOpt = requestRepository.findById(rid);
            
            if (requestOpt.isEmpty()) {
                log.warn("Request not found: {}", rid);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Request not found");
            }

            Requests request = requestOpt.get();
            byte[] proof = request.getProof();

            if (proof == null) {
                log.warn("No proof available for request: {}", rid);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Proof not found");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentLength(proof.length);
            headers.setContentDisposition(ContentDisposition.inline()
                    .filename("proof_" + rid + ".pdf")
                    .build());

            return new ResponseEntity<>(proof, headers, HttpStatus.OK);

        } catch (Exception ex) {
            log.error("Error fetching proof for request: {}", rid, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch proof");
        }
    }

    @GetMapping("/requests/{sid}/{aid}/proof")
    public ResponseEntity<?> getStudentActivityProof(@PathVariable String sid, @PathVariable Integer aid) {
        try {
            Optional<StudentActivity> studentActivityOpt = studentActivityRepository.findBySidAndActID(sid, aid);
            
            if (studentActivityOpt.isEmpty()) {
                log.warn("Student activity not found: sid={}, aid={}", sid, aid);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Student activity not found");
            }

            StudentActivity studentActivity = studentActivityOpt.get();
            byte[] proof = studentActivity.getProof();

            if (proof == null) {
                log.warn("No proof available for student activity: sid={}, aid={}", sid, aid);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Proof not found");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentLength(proof.length);
            headers.setContentDisposition(ContentDisposition.inline()
                    .filename("proof_" + sid + "_" + aid + ".pdf")
                    .build());

            return new ResponseEntity<>(proof, headers, HttpStatus.OK);

        } catch (Exception ex) {
            log.error("Error fetching proof for student activity: sid={}, aid={}", sid, aid, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch proof");
        }
    }

    @PostMapping("/reject-request/{rid}")
    public ResponseEntity<?> rejectRequest(@PathVariable Long rid, @RequestBody Map<String, String> body) {
        try {
            String comment = body.get("comment");
            
            Optional<Requests> requestOpt = requestRepository.findById(rid);
            
            if (requestOpt.isEmpty()) {
                log.warn("Request not found for rejection: {}", rid);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Request not found");
            }

            Requests req = requestOpt.get();
            req.setStatus(Status.Rejected);
            req.setDecisionDate(new Date());
            req.setComments(comment);
            requestRepository.save(req);

            log.info("Request rejected: rid={}", rid);
            return ResponseEntity.ok("Successfully rejected");

        } catch (Exception ex) {
            log.error("Error rejecting request: {}", rid, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to reject request");
        }
    }

    @PostMapping("/approve-request/{rid}")
    public ResponseEntity<?> approveRequest(
            @PathVariable Long rid,
            @RequestParam String email,
            @RequestParam Integer points) {

        try {
            if (points == null || points <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid points value");
            }

            Optional<Fa> faOptional = faRepository.findByEmailID(email);
            if (faOptional.isEmpty()) {
                log.warn("FA not found: {}", email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("FA not found");
            }

            Optional<Requests> requestOpt = requestRepository.findById(rid);
            if (requestOpt.isEmpty()) {
                log.warn("Request not found for approval: {}", rid);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Request not found");
            }

            Requests req = requestOpt.get();
            Optional<Student> studentOpt = studentRepository.findById(req.getSid());
            
            if (studentOpt.isEmpty()) {
                log.error("Student not found for request: {}", rid);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Student not found");
            }

            Student student = studentOpt.get();
            Activity activityToLink;

            // Check if activity already exists
            Optional<Activity> activityOpt = activityRepository.findByName(req.getActivityName());

            if (activityOpt.isPresent()) {
                // Activity exists, link to it
                activityToLink = activityOpt.get();

                Optional<StudentActivity> existingRecordOpt = studentActivityRepository
                        .findBySidAndActID(req.getSid(), activityToLink.getActID().intValue());

                if (existingRecordOpt.isPresent()) {
                    log.warn("Activity already approved for student: sid={}, actID={}", 
                             req.getSid(), activityToLink.getActID());
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body("Activity already approved for this student");
                }
            } else {
                // Create new activity
                activityToLink = new Activity();
                activityToLink.setName(req.getActivityName());
                activityToLink.setDescription(req.getDescription());
                activityToLink.setDate(req.getActivityDate());
                activityToLink.setEnd_date(req.getActivityDate());
                activityToLink.setMandatory(0);
                activityToLink.setPoints(points);
                activityToLink.setType(req.getType().toString());

                activityToLink = activityRepository.save(activityToLink);
                log.info("New activity created: {}", activityToLink.getName());
            }

            // Add record to StudentActivity
            StudentActivity studentActivity = new StudentActivity();
            studentActivity.setActID(activityToLink.getActID().intValue());
            studentActivity.setSid(req.getSid());
            studentActivity.setDate(new Date());
            studentActivity.setProof(req.getProof());
            studentActivity.setValidated(Validated.Yes);
            studentActivity.setTitle(req.getActivityName());
            studentActivity.setPoints(points);
            studentActivity.setActivityType(req.getType().toString());

            // Update student points based on activity type
            if (req.getType() == Type.Institute) {
                student.setInstitutePoints(student.getInstitutePoints() + points);
            } else if (req.getType() == Type.Department) {
                student.setDeptPoints(student.getDeptPoints() + points);
            } else if (req.getType() == Type.Other) {
                student.setOtherPoints(student.getOtherPoints() + points);
            }

            studentActivityRepository.save(studentActivity);
            studentRepository.save(student);

            req.setStatus(Status.Approved);
            req.setDecisionDate(new Date());
            requestRepository.save(req);

            log.info("Request approved: rid={}, sid={}, points={}", rid, req.getSid(), points);
            return ResponseEntity.ok("Successfully approved");

        } catch (Exception ex) {
            log.error("Error approving request: {}", rid, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to approve request");
        }
    }
}