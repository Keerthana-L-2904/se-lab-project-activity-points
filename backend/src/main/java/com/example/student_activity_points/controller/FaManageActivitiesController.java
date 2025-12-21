package com.example.student_activity_points.controller;

import com.example.student_activity_points.domain.Activity;
import com.example.student_activity_points.domain.Departments;
import com.example.student_activity_points.repository.ActivityRepository;
import com.example.student_activity_points.repository.DepartmentsRepository;
import com.example.student_activity_points.repository.ValidationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/fa")
public class FaManageActivitiesController {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private DepartmentsRepository departmentsRepository;

    @Autowired
    private ValidationRepository validationRepository;

    private static final Logger log = LoggerFactory.getLogger(FaManageActivitiesController.class);

    @GetMapping("/manage-activities")
    public ResponseEntity<?> getActivities() {
        try {
            List<Activity> activities = (List<Activity>) activityRepository.findAll();
            
            List<Map<String, Object>> response = new ArrayList<>();
            for (Activity activity : activities) {
                Map<String, Object> map = new HashMap<>();
                map.put("actID", activity.getActID());
                map.put("name", activity.getName());
                map.put("description", activity.getDescription());
                map.put("points", activity.getPoints());
                map.put("DID", activity.getDID());
                map.put("date", activity.getDate());
                map.put("end_date", activity.getEnd_date());
                map.put("type", activity.getType());
                map.put("mandatory", activity.getMandatory());
                
                // Check if attendance is uploaded for this activity
                boolean attendanceUploaded = !validationRepository.findByActivity(activity).isEmpty();
                map.put("attendanceUploaded", attendanceUploaded);
                
                response.add(map);
            }
            
            log.debug("Retrieved {} activities for FA", response.size());
            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            log.error("Error loading activities", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch activities");
        }
    }

    @GetMapping("/get-departments")
    public ResponseEntity<?> getDepartments() {
        try {
            List<Departments> departments = (List<Departments>) departmentsRepository.findAll();
            log.debug("Retrieved {} departments", departments.size());
            return ResponseEntity.ok(departments);

        } catch (Exception ex) {
            log.error("Error loading departments", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch departments");
        }
    }

    @PostMapping("/manage-activities")
    public ResponseEntity<?> addActivity(@RequestBody Activity activity) {
        try {
            if (activity.getName() == null || activity.getName().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Activity name is required");
            }

            Activity savedActivity = activityRepository.save(activity);
            log.info("Activity added successfully by FA: {}", activity.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedActivity);

        } catch (DataIntegrityViolationException ex) {
            log.warn("Data integrity violation while adding activity", ex);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Invalid activity data or duplicate entry");

        } catch (Exception ex) {
            log.error("Error adding activity", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to add activity");
        }
    }

    @PutMapping("/manage-activities/{id}")
    public ResponseEntity<?> updateActivity(@PathVariable Long id, @RequestBody Activity updatedActivity) {
        try {
            Optional<Activity> existingActivityOpt = activityRepository.findById(id);
            
            if (existingActivityOpt.isEmpty()) {
                log.warn("Activity not found for update: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Activity not found");
            }

            Activity existingActivity = existingActivityOpt.get();
            existingActivity.setName(updatedActivity.getName());
            existingActivity.setType(updatedActivity.getType());
            existingActivity.setMandatory(updatedActivity.getMandatory());
            existingActivity.setDID(updatedActivity.getDID());
            existingActivity.setDescription(updatedActivity.getDescription());
            existingActivity.setDate(updatedActivity.getDate());
            existingActivity.setEnd_date(updatedActivity.getEnd_date());
            existingActivity.setPoints(updatedActivity.getPoints());

            Activity savedActivity = activityRepository.save(existingActivity);
            log.info("Activity updated successfully: {}", id);
            return ResponseEntity.ok(savedActivity);

        } catch (DataIntegrityViolationException ex) {
            log.warn("Data integrity violation while updating activity: {}", id, ex);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Invalid activity data");

        } catch (Exception ex) {
            log.error("Error updating activity: {}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to update activity");
        }
    }

    @DeleteMapping("/manage-activities/{id}")
    public ResponseEntity<?> deleteActivity(@PathVariable Long id) {
        try {
            if (!activityRepository.existsById(id)) {
                log.warn("Activity not found for deletion: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Activity not found");
            }

            activityRepository.deleteById(id);
            log.info("Activity deleted successfully: {}", id);
            return ResponseEntity.ok("Activity deleted successfully");

        } catch (DataIntegrityViolationException ex) {
            log.warn("Cannot delete activity due to dependencies: {}", id, ex);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Cannot delete activity - it has associated records");

        } catch (Exception ex) {
            log.error("Error deleting activity: {}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to delete activity");
        }
    }
}