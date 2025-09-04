package com.example.student_activity_points.dto;

import java.util.Date;
import java.util.List;

public class TrackingDTO {
    private List<RequestDTO> requests;
    private List<ActivityDTO> activities;

    public List<RequestDTO> getRequests() {
        return requests;
    }

    public void setRequests(List<RequestDTO> requests) {
        this.requests = requests;
    }

    public List<ActivityDTO> getActivities() {
        return activities;
    }

    public void setActivities(List<ActivityDTO> activities) {
        this.activities = activities;
    }

    // ================== Request DTO ==================
    public static class RequestDTO {
        private Long rid;
        private String activityName;
        private String description;
        private Date activityDate;
        private String status;   // Requests.Status (enum as String)
        private String type;     // Requests.Type (enum as String)

        public Long getRid() {
            return rid;
        }

        public void setRid(Long rid) {
            this.rid = rid;
        }

        public String getActivityName() {
            return activityName;
        }

        public void setActivityName(String activityName) {
            this.activityName = activityName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Date getActivityDate() {
            return activityDate;
        }

        public void setActivityDate(Date activityDate) {
            this.activityDate = activityDate;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    // ================== Activity DTO ==================
    public static class ActivityDTO {
        private int actID;
        private String title;
        private Date date;
        private String validated; // StudentActivity.Validated enum as String
        private int points;
        private String activityType;

        public int getActID() {
            return actID;
        }

        public void setActID(int actID) {
            this.actID = actID;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public String getValidated() {
            return validated;
        }

        public void setValidated(String validated) {
            this.validated = validated;
        }

        public int getPoints() {
            return points;
        }

        public void setPoints(int points) {
            this.points = points;
        }

        public String getActivityType() {
            return activityType;
        }

        public void setActivityType(String activityType) {
            this.activityType = activityType;
        }
    }
}
