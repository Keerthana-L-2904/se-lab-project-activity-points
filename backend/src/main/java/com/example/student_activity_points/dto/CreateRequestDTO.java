package com.example.student_activity_points.dto;

import com.example.student_activity_points.domain.Requests.Status;
import com.example.student_activity_points.domain.Requests.Type;
import jakarta.validation.constraints.*;

public class CreateRequestDTO {

    private Long rid;

    private String date;

    private Status status = Status.Pending;

    private String decisionDate;

    @NotBlank(message = "Activity name is required")
    @Size(min = 3, max = 200, message = "Activity name must be between 3 and 200 characters")
    private String activityName;

    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @NotBlank(message = "Activity date is required")
    // ✅ Change to String with pattern validation
   // @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Date must be in format YYYY-MM-DD")
    private String activityDate;

    @NotBlank(message = "Type is required")
    private String type;  // ✅ Keep as String, convert in controller

    @NotNull(message = "Points are required")
    @Min(value = 1, message = "Points must be at least 1")
    @Max(value = 100, message = "Points cannot exceed 100")
    private Integer points;


    private String comments;

    private String organization;
    
    private String location;

    // Simple Getters and Setters (no defensive copying needed for strings)
    
    public Long getRid() { return rid; }
    public void setRid(Long rid) { this.rid = rid; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getDecisionDate() { return decisionDate; }
    public void setDecisionDate(String decisionDate) { this.decisionDate = decisionDate; }

    public String getActivityName() { return activityName; }
    public void setActivityName(String activityName) { this.activityName = activityName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getActivityDate() { return activityDate; }
    public void setActivityDate(String activityDate) { this.activityDate = activityDate; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }


    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}