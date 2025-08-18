package com.example.student_activity_points.domain;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "Requests")
public class Requests {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rid")
    private Long rid;
    
    @Column(unique = true, nullable = false)
    private String sid;  

    @Column(name = "date", nullable = false)
    private Date date;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status = Status.Pending;

    @Column(name = "link")
    private String link;

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

    public enum Status {
        Pending, Approved, Rejected
    }

    public enum Type {
        Institute, Department, other
    }

    // Getters and Setters
    public Long getRid() {
        return rid;
    }

    public void setRid(Long rid) {
        this.rid = rid;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Date getDecisionDate() {
        return decisionDate;
    }

    public void setDecisionDate(Date decisionDate) {
        this.decisionDate = decisionDate;
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

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
