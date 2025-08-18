package com.example.student_activity_points.controller;

import com.example.student_activity_points.domain.Validation;
import com.example.student_activity_points.domain.Validation.Validated;
import com.example.student_activity_points.domain.Activity;
import com.example.student_activity_points.repository.ValidationRepository;
import com.example.student_activity_points.repository.ActivityRepository;
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
@RequestMapping("/api/admin")
public class ValidationController {

    @Autowired
    private ValidationRepository validationRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @PostMapping("/upload-attendance/{actID}")
    public String uploadFile(@RequestParam("file") MultipartFile file, @PathVariable int actID) {
        if (file.isEmpty()) {
            return "Please upload a valid CSV file!";
        }
    
        try {
            Optional<Activity> activityOptional = activityRepository.findById(Long.valueOf(actID));
    
            if (!activityOptional.isPresent()) {
                return "Invalid Activity ID!";
            }
    
            Activity activity = activityOptional.get();
    
            // 1️⃣ Delete previous records for the given activity
            validationRepository.deleteByActivity(activity);
    
            List<Validation> validationList = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
            String line;
            boolean firstLine = true;
    
            while ((line = br.readLine()) != null) {
                if (firstLine) { // Skip header row
                    firstLine = false;
                    continue;
                }
    
                String sid = line.trim();
                if (!sid.isEmpty()) { // Ensure non-empty student ID
                    Validation validation = new Validation();
                    validation.setSid(sid);
                    validation.setUploadDate(new Date()); // Store the current date
                    validation.setValidated(Validated.Pending); // Default status
                    validation.setActivity(activity);
                    validationList.add(validation);
                }
            }
    
            // 2️⃣ Insert the new data
            validationRepository.saveAll(validationList);
            return "File uploaded successfully! Attendance list updated.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error processing the file!";
        }
    }
    
}
