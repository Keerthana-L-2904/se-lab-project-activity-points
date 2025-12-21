package com.example.student_activity_points.domain;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "Requests")
public class Requests {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rid")
    private Long rid;
    
    @Column(unique = false, nullable = false)
    private String sid;  

    @Column(name = "date", nullable = false)
    private Date date;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status = Status.Pending;

    @Column(name = "decison_date") // note: column name "decison_date" as per your existing schema
    private Date decisionDate;

    @Column(name = "activity_name")
    private String activityName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "activity_date")
    private Date activityDate;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private Type type;

    @Column(name = "points")
    private Integer points;

    @Lob
    @Column(name = "proof", columnDefinition = "LONGBLOB")
    private byte[] proof;

    public byte[] getProof() {
        return proof == null ? null : proof.clone();
    }

    public void setProof(byte[] proof) {
        this.proof = (proof == null) ? null : proof.clone();
    }

    // Enum types
    public enum Status {
        Pending, Approved, Rejected
    }

    public enum Type {
        Institute, Department, Other
    }
    @Column(name="Comments")
    private String comments;

    public String getComments() {
        return comments;
    }
    public void setComments(String comments) {
        this.comments = comments;
    }
    // Getters and Setters
    public Long getRid() { return rid; }
    public void setRid(Long rid) { this.rid = rid; }

    public String getSid() { return sid; }
    public void setSid(String sid) { this.sid = sid; }

    public Date getDate() { return date == null ? null : new Date(date.getTime());}
    public void setDate(Date date) { this.date = (date == null) ? null : new Date(date.getTime());}

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Date getDecisionDate() { return decisionDate == null ? null : new Date(decisionDate.getTime());}
    public void setDecisionDate(Date decisionDate) { this.decisionDate = (decisionDate == null) ? null : new Date(decisionDate.getTime()); }

    public String getActivityName() { return activityName; }
    public void setActivityName(String activityName) { this.activityName = activityName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getActivityDate() {return activityDate == null ? null : new Date(activityDate.getTime()); }
    public void setActivityDate(Date activityDate) { this.activityDate = (activityDate == null) ? null : new Date(activityDate.getTime());}

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
}
