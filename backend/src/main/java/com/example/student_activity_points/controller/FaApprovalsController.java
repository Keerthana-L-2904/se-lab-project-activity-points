package com.example.student_activity_points.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.student_activity_points.repository.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.example.student_activity_points.domain.Activity;
import com.example.student_activity_points.domain.Fa;
import com.example.student_activity_points.domain.Requested;
import com.example.student_activity_points.domain.Student;
import com.example.student_activity_points.domain.StudentActivity;
import com.example.student_activity_points.domain.Validation;
import com.example.student_activity_points.domain.Requests.Status;
import com.example.student_activity_points.domain.Validation.Validated;
import com.example.student_activity_points.domain.Requests;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



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
    private RequestedRepository requestedRepository;

    @Autowired
    private ValidationRepository validationRepository;

    @Autowired
    private ActivityRepository activityRepository;


    FaApprovalsController(StudentActivityRepository studentActivityRepository) {
        this.studentActivityRepository = studentActivityRepository;
    }

    @GetMapping("/details")
public ResponseEntity<?> getFaDetails(@RequestParam String email) {
    Optional<Fa> faOptional = faRepository.findByEmailID(email);
    if (!faOptional.isPresent()) {
        return ResponseEntity.status(404).body("FA not found");
    }

    Fa fa = faOptional.get();
    Long faId = fa.getFAID();

    // Step 1: Get all requests assigned directly to this FA (where approval is still pending)
    List<Long> assignedRequestIds = requestedRepository.findByFAID(faId.intValue()).stream()
            .filter(req -> !req.getApproved()) // Pending approvals
            .map(Requested::getRid)
            .collect(Collectors.toList());

    // Step 2: Get requests from students under this FA's FA-ship
    List<Student> myStudents = studentRepository.findByFAID(faId.intValue());
    List<String> myStudentsIds = myStudents.stream().map(Student::getSid).collect(Collectors.toList());

    // Step 3: Get requests from my students (only include if ALL other assigned FAs have approved)
    List<Long> myStudentRequestIds = requestRepository.findBySidIn(myStudentsIds).stream()
            .map(Requests::getRid)
            .filter(rid -> {
                List<Requested> faApprovals = requestedRepository.findByRid(rid);
                return faApprovals.stream()
                        .filter(req -> req.getFaid() != faId) // Check all other FAs
                        .allMatch(Requested::getApproved); // Ensure all other FAs have approved
            })
            .collect(Collectors.toList());

    // Step 4: Combine both request lists (requests assigned to FA + requests from their students after all approvals)
    Set<Long> finalRequestIds = new HashSet<>();
    finalRequestIds.addAll(assignedRequestIds);
    finalRequestIds.addAll(myStudentRequestIds);

    // Step 5: Get full request details
    List<Requests> finalRequests = StreamSupport.stream(
            requestRepository.findAllById(finalRequestIds).spliterator(), false)
        .collect(Collectors.toList());

    // Step 6: Structure response (Include "validated" status from the validation table)
    List<Map<String, Object>> responseList = new ArrayList<>();
    for (Requests request : finalRequests) {
        Map<String, Object> response = new HashMap<>();
        response.put("rid", request.getRid());
        response.put("sid", request.getSid());
        response.put("status", request.getStatus());
        response.put("activity_name", request.getActivityName());
        response.put("activity_date", request.getActivityDate());
        response.put("link", request.getLink());
        response.put("type", request.getType());

        // Fetch validation status
        if(!activityRepository.findByName(request.getActivityName()).isEmpty()){
            // Check if the activity exists in validation table
            List<Validation> validationOpt = validationRepository.findByActID(activityRepository.findByName(request.getActivityName()).get().getActID());
            String validationStatus = "Not uploaded"; // Default value
            System.out.println("Outside inside: " + activityRepository.findByName(request.getActivityName()).get().getOutside_inside());
            if(activityRepository.findByName(request.getActivityName()).get().getOutside_inside().equals("Outside")){
                validationStatus="N/A";
            }
        if(validationOpt!=null && !validationOpt.isEmpty()){
            // System.out.println(validationOpt);
            Validation validationOpt_ = validationRepository.findByActIDAndSID(activityRepository.findByName(request.getActivityName()).get().getActID(),request.getSid());
            if(validationOpt_==null){
                validationStatus="Pending";
            }
            else validationStatus=validationOpt_.getValidated().toString();
        }
        System.out.println("Validation status: " + validationStatus);
        response.put("validated", validationStatus);
        }
        else response.put("validated", "N/A");

        responseList.add(response);
    }

    return ResponseEntity.ok(responseList);
}

    

    @PostMapping("/approve-request/{rid}")
    public ResponseEntity<?> approveRequest(@PathVariable Long rid, @RequestParam String email,@RequestParam Integer points) {
        Optional<Fa> faOptional = faRepository.findByEmailID(email);
        if (!faOptional.isPresent()) {
            return ResponseEntity.status(404).body("FA not found");
        }
        Fa fa = faOptional.get();
        Optional<Requests> requestOpt = requestRepository.findById(rid);
        if (requestOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Request not found");
        }
        Requests req = requestOpt.get();
        Student student = studentRepository.findById(req.getSid()).get();
        if(student.getFaid()!=fa.getFAID()){
            Optional<Requested> requestedOpt = requestedRepository.findByRidAndFAID(rid,fa.getFAID().intValue());
            Requested requested=requestedOpt.get();
            requested.setApproved(true);
            requestedRepository.save(requested);
            return ResponseEntity.status(200).body("Successfully approved!");
        }

        Optional<Activity> activityOpt = activityRepository.findByName(req.getActivityName());

    if (activityOpt.isEmpty()) {
        Activity new_activity=new Activity();
        new_activity.setName(req.getActivityName());
        new_activity.setDescription(req.getDescription());
        new_activity.setDate(req.getActivityDate());
        new_activity.setOutside_inside("Outside");
        new_activity.setMandatory(0);
        new_activity.setPoints(points);
        new_activity.setType("Institute");
        activityRepository.save(new_activity);
    }
    StudentActivity studentActivity=new StudentActivity();
    studentActivity.setActID(activityRepository.findByName(req.getActivityName()).get().getActID().intValue());
    studentActivity.setSid(req.getSid());
    studentActivity.setDate(new Date());
    studentActivity.setLink(req.getLink());
    
    studentActivity.setTitle(req.getActivityName());
    
    
    if(activityOpt.isEmpty()){
        studentActivity.setPoints(points);
        studentActivity.setActivityType("Institute");
        student.setInstitutePoints(points+student.getInstitutePoints());
    }
    else if(activityOpt.get().getType().equals("Institute")){
        studentActivity.setPoints(activityOpt.get().getPoints());
        studentActivity.setActivityType("Institute");
        student.setInstitutePoints(activityOpt.get().getPoints()+student.getInstitutePoints());
    }
    else{
        studentActivity.setPoints(activityOpt.get().getPoints());
        studentActivity.setActivityType("Department");
        student.setDeptPoints(activityOpt.get().getPoints()+student.getDeptPoints());
    }
    studentActivityRepository.save(studentActivity);
    studentRepository.save(student);
    req.setStatus(Status.Approved);
    requestRepository.save(req);
        
    return ResponseEntity.ok("Successfully approved");
    

    }

    @GetMapping("/get-Fa")
    public ResponseEntity<String> getFa(@RequestParam String sid) {
        Optional<Student> student=studentRepository.findBySid(sid);
        if(student==null){
           return ResponseEntity.status(404).body("Can't find FA for this student");

        }
        return ResponseEntity.ok(String.valueOf(student.get().getFaid()));

    }
    

    @PostMapping("/validate-request/{rid}")
public ResponseEntity<?> validateRequest(@PathVariable Long rid) {
    // Find the request by ID
    Optional<Requests> requestOpt = requestRepository.findById(rid);
    
    if (requestOpt.isEmpty()) {
        return ResponseEntity.status(404).body("Request not found");
    }

    Requests req = requestOpt.get();

    // Find activity by name
    Optional<Activity> activityOpt = activityRepository.findByName(req.getActivityName());

    if (activityOpt.isEmpty()) {
        return ResponseEntity.status(404).body("Activity not found");
    }

    Activity activity = activityOpt.get();

    // Check if validation already exists
    List<Validation> validationOpt = validationRepository.findByActID(activity.getActID());
    System.out.println("ActID: " + activity.getActID() + ", SID: " + req.getSid());
    if (validationOpt == null) {
        return ResponseEntity.status(400).body("Cannot validate:Attendance not uploaded");
    }
    Validation validation = validationRepository.findByActIDAndSID(activity.getActID(), req.getSid());
    if (validation != null) {
        validation.setValidated(Validated.Yes);
        validationRepository.save(validation);
    }else{
        Validation newValidation = new Validation();
        newValidation.setSid(req.getSid());
        newValidation.setActivity(activity);
        newValidation.setUploadDate(new Date());
        newValidation.setValidated(Validated.No);
        validationRepository.save(newValidation);
    }
   
    return ResponseEntity.ok("Successfully validated");
}

    @PostMapping("/reject-request/{rid}")
    public ResponseEntity<?>  RejectRequest(@PathVariable Long rid) {
        Optional<Requests> request=requestRepository.findById(rid);
        Requests req=request.get();
        req.setStatus(Status.Rejected);
        requestRepository.save(req);
        
        return ResponseEntity.ok("Successfully rejected");
    }    
}
