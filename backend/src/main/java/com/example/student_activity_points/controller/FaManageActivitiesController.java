package com.example.student_activity_points.controller;

import com.example.student_activity_points.domain.Activity;
import com.example.student_activity_points.domain.Departments;
import com.example.student_activity_points.repository.ActivityRepository;
import com.example.student_activity_points.repository.DepartmentsRepository;
import com.example.student_activity_points.repository.ValidationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/fa")
public class FaManageActivitiesController {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private DepartmentsRepository departmentsRepository;

    @Autowired
    private ValidationRepository validationRepository;

    // Fetch all activities with attendanceUploaded property
    @GetMapping("/manage-activities")
    public ResponseEntity<?> getActivities() {
        try {
            List<Activity> activities = (List<Activity>) activityRepository.findAll();
            // Build a response with attendanceUploaded property
            List<java.util.Map<String, Object>> response = new java.util.ArrayList<>();
            for (Activity activity : activities) {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("actID", activity.getActID());
                map.put("name", activity.getName());
                map.put("description", activity.getDescription());
                map.put("points", activity.getPoints());
                map.put("DID", activity.getDID());
                map.put("date", activity.getDate());
                map.put("end_date", activity.getEnd_date());
                map.put("type", activity.getType());
                map.put("outside_inside", activity.getOutside_inside());
                map.put("no_of_people", activity.getNo_of_people());
                map.put("mandatory", activity.getMandatory());
                // Check if attendance is uploaded for this activity
                boolean attendanceUploaded = !validationRepository.findByActivity(activity).isEmpty();
                map.put("attendanceUploaded", attendanceUploaded);
                response.add(map);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
        }
    }

    @GetMapping("/get-departments")
    public ResponseEntity<?> getDepartments() {
        try {
            List<Departments> departments = (List<Departments>) departmentsRepository.findAll();
            return ResponseEntity.ok(departments);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
        }
    }

    // Add a new activity
    @PostMapping("/manage-activities")
    public ResponseEntity<?> addActivity(@RequestBody Activity activity) {
        try {
            Activity savedActivity = activityRepository.save(activity);
            return ResponseEntity.ok(savedActivity);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error adding activity: " + e.getMessage());
        }
    }

    // Edit an existing activity
    @PutMapping("/manage-activities/{id}")
    public ResponseEntity<?> updateActivity(@PathVariable Long id, @RequestBody Activity updatedActivity) {
        try {
            Optional<Activity> existingActivityOpt = activityRepository.findById(id);
            if (existingActivityOpt.isPresent()) {
                Activity existingActivity = existingActivityOpt.get();
                existingActivity.setName(updatedActivity.getName());
                existingActivity.setType(updatedActivity.getType());
                existingActivity.setMandatory(updatedActivity.getMandatory());
                existingActivity.setDID(updatedActivity.getDID());
                existingActivity.setDescription(updatedActivity.getDescription());
                existingActivity.setOutside_inside(updatedActivity.getOutside_inside());
                existingActivity.setDate(updatedActivity.getDate());
                existingActivity.setEnd_date(updatedActivity.getEnd_date());
                existingActivity.setPoints(updatedActivity.getPoints());
                existingActivity.setNo_of_people(updatedActivity.getNo_of_people());

                Activity savedActivity = activityRepository.save(existingActivity);
                return ResponseEntity.ok(savedActivity);
            } else {
                return ResponseEntity.status(404).body("Activity not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error updating activity: " + e.getMessage());
        }
    }

    // Delete an activity
    @DeleteMapping("/manage-activities/{id}")
    public ResponseEntity<?> deleteActivity(@PathVariable Long id) {
        try {

            if (activityRepository.existsById(id)) {
                activityRepository.deleteById(id);
                return ResponseEntity.ok("Activity deleted successfully");
            } else {
                return ResponseEntity.status(404).body("Activity not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error deleting activity: " + e.getMessage());
        }
    }
}
