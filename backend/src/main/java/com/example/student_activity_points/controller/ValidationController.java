package com.example.student_activity_points.controller;

import com.example.student_activity_points.domain.Validation;
import com.example.student_activity_points.domain.Validation.Validated;
import com.example.student_activity_points.domain.Activity;
import com.example.student_activity_points.repository.ValidationRepository;
import com.example.student_activity_points.repository.ActivityRepository;
import com.example.student_activity_points.repository.StudentRepository;
import com.example.student_activity_points.repository.StudentActivityRepository;
import com.example.student_activity_points.domain.StudentActivity;
import com.example.student_activity_points.domain.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/fa")

public class ValidationController {


    @Autowired
    private ValidationRepository validationRepository;

    @Autowired
    private ActivityRepository activityRepository;


    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentActivityRepository studentActivityRepository;


    @PostMapping("/upload-attendance/{actID}")
    public String uploadFile(@RequestParam("file") MultipartFile file, @PathVariable int actID) {
        System.out.println("[DEBUG] uploadFile called with actID: " + actID);
        if (file.isEmpty()) {
            System.out.println("[DEBUG] Uploaded file is empty.");
            return "Please upload a valid CSV file!";
        }

        try {
            System.out.println("[DEBUG] Attempting to find activity with ID: " + actID);
            Optional<Activity> activityOptional = activityRepository.findById(Long.valueOf(actID));

            if (!activityOptional.isPresent()) {
                System.out.println("[DEBUG] Activity not found for ID: " + actID);
                return "Invalid Activity ID!";
            }

            Activity activity = activityOptional.get();
            System.out.println("[DEBUG] Activity found: " + activity.getName() + ", Type: " + activity.getType() + ", Points: " + activity.getPoints());


            // 1️⃣ Subtract points for previous attendance records for this activity
            System.out.println("[DEBUG] Fetching previous validation records for activity ID: " + actID);
            List<Validation> oldValidations = validationRepository.findByActivity(activity);
            for (Validation oldVal : oldValidations) {
                String oldSid = oldVal.getSid();
                Optional<Student> oldStudentOpt = studentRepository.findById(oldSid);
                if (oldStudentOpt.isPresent()) {
                    Student oldStudent = oldStudentOpt.get();
                    if ("Institute".equalsIgnoreCase(activity.getType())) {
                        int currentPoints = oldStudent.getInstitutePoints();
                        oldStudent.setInstitutePoints(Math.max(0, currentPoints - activity.getPoints()));
                        System.out.println("[DEBUG] Subtracted " + activity.getPoints() + " institute points from SID: " + oldSid);
                    } else if ("Department".equalsIgnoreCase(activity.getType())) {
                        int currentPoints = oldStudent.getDeptPoints();
                        oldStudent.setDeptPoints(Math.max(0, currentPoints - activity.getPoints()));
                        System.out.println("[DEBUG] Subtracted " + activity.getPoints() + " department points from SID: " + oldSid);
                    } else {
                        int currentPoints = oldStudent.getActivityPoints();
                        oldStudent.setActivityPoints(Math.max(0, currentPoints - activity.getPoints()));
                        System.out.println("[DEBUG] Subtracted " + activity.getPoints() + " activity points from SID: " + oldSid);
                    }
                    // Always update activity_points as sum of dept_points and institute_points
                    int newDeptPoints = oldStudent.getDeptPoints();
                    int newInstitutePoints = oldStudent.getInstitutePoints();
                    int newActivityPoints = newDeptPoints + newInstitutePoints;
                    oldStudent.setActivityPoints(newActivityPoints);
                    studentRepository.save(oldStudent);
                }
            }
            System.out.println("[DEBUG] Deleting previous validation records for activity ID: " + actID);
            validationRepository.deleteByActivity(activity);

            List<Validation> validationList = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
            String line;
            boolean firstLine = true;

            int pointsToAdd = 0;
            String activityType = "Other";
            try {
                pointsToAdd = Integer.parseInt(String.valueOf(activity.getPoints()));
                activityType = activity.getType();
                System.out.println("[DEBUG] Points to add: " + pointsToAdd + ", Activity type: " + activityType);
            } catch (Exception e) {
                System.out.println("[DEBUG] Error parsing points or type: " + e.getMessage());
                pointsToAdd = 0;
            }


            while ((line = br.readLine()) != null) {
                if (firstLine) { // Skip header row
                    System.out.println("[DEBUG] Skipping header row: " + line);
                    firstLine = false;
                    continue;
                }

                String sid = line.trim();
                if (!sid.isEmpty()) { // Ensure non-empty student ID
                    System.out.println("[DEBUG] Processing student ID: " + sid);
                    Validation validation = new Validation();
                    validation.setSid(sid);
                    validation.setUploadDate(new Date()); // Store the current date
                    validation.setValidated(Validated.NA); // Default status
                    validation.setActivity(activity);
                    validationList.add(validation);

                    // Update student points based on activity type
                    Optional<Student> studentOpt = studentRepository.findById(sid);
                    if (studentOpt.isPresent()) {
                        Student student = studentOpt.get();
                        System.out.println("[DEBUG] Found student: " + student.getName() + " (SID: " + sid + ")");
                        boolean updated = false;
                        if ("Institute".equalsIgnoreCase(activityType)) {
                            int currentPoints = 0;
                            try {
                                currentPoints = student.getInstitutePoints();
                                System.out.println("[DEBUG] Current institute points: " + currentPoints);
                            } catch (Exception e) {
                                System.out.println("[DEBUG] Error getting institute points: " + e.getMessage());
                                currentPoints = 0;
                            }
                            student.setInstitutePoints(currentPoints + pointsToAdd);
                            System.out.println("[DEBUG] Updated institute points: " + (currentPoints + pointsToAdd));
                            updated = true;
                        } else if ("Department".equalsIgnoreCase(activityType)) {
                            int currentPoints = 0;
                            try {
                                currentPoints = student.getDeptPoints();
                                System.out.println("[DEBUG] Current department points: " + currentPoints);
                            } catch (Exception e) {
                                System.out.println("[DEBUG] Error getting department points: " + e.getMessage());
                                currentPoints = 0;
                            }
                            student.setDeptPoints(currentPoints + pointsToAdd);
                            System.out.println("[DEBUG] Updated department points: " + (currentPoints + pointsToAdd));
                            updated = true;
                        } else {
                            int currentPoints = 0;
                            try {
                                currentPoints = student.getActivityPoints();
                                System.out.println("[DEBUG] Current activity points: " + currentPoints);
                            } catch (Exception e) {
                                System.out.println("[DEBUG] Error getting activity points: " + e.getMessage());
                                currentPoints = 0;
                            }
                            student.setActivityPoints(currentPoints + pointsToAdd);
                            System.out.println("[DEBUG] Updated activity points: " + (currentPoints + pointsToAdd));
                        }
                        // Always update activity_points as sum of dept_points and institute_points
                        int newDeptPoints = student.getDeptPoints();
                        int newInstitutePoints = student.getInstitutePoints();
                        int newActivityPoints = newDeptPoints + newInstitutePoints;
                        student.setActivityPoints(newActivityPoints);
                        System.out.println("[DEBUG] Set activity_points (dept_points + institute_points): " + newActivityPoints + " (" + newDeptPoints + "+" + newInstitutePoints + ")");
                        studentRepository.save(student);
                        System.out.println("[DEBUG] Saved updated student record for SID: " + sid);

                        // Add StudentActivity record
                        StudentActivity studentActivity = new StudentActivity();
                        studentActivity.setSid(sid);
                        studentActivity.setActID(actID);
                        studentActivity.setDate(new Date());
                        studentActivity.setTitle(activity.getName());
                        studentActivity.setPoints(pointsToAdd);
                        studentActivity.setActivityType(activityType);
                        studentActivity.setValidated(StudentActivity.Validated.Yes);  //WHEN TO CHANGE IT AS NO 
                        studentActivity.setLink(""); // No link from CSV, set as empty
                        studentActivityRepository.save(studentActivity);
                        System.out.println("[DEBUG] Saved StudentActivity for SID: " + sid + ", actID: " + actID);
                    } else {
                        System.out.println("[DEBUG] Student not found for SID: " + sid);
                    }
                } else {
                    System.out.println("[DEBUG] Encountered empty SID line, skipping.");
                }
            }

            // 2️⃣ Insert the new data
            System.out.println("[DEBUG] Saving all validation records. Total: " + validationList.size());
            validationRepository.saveAll(validationList);
            System.out.println("[DEBUG] Upload and update process completed successfully.");
            return "File uploaded successfully! Attendance list and student points updated.";
        } catch (Exception e) {
            System.out.println("[DEBUG] Exception occurred: " + e.getMessage());
            e.printStackTrace();
            return "Error processing the file!";
        }
    }
    
}
