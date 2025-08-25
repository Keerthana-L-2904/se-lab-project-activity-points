package com.example.student_activity_points.domain;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;

@Entity
@Table(name = "StudentActivity")
@IdClass(StudentActivityId.class)  // Composite Key
public class StudentActivity {

    public enum Validated {
        No, Yes, Pending
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "validated", nullable = false, columnDefinition = "ENUM('No','Yes','Pending') DEFAULT 'Pending'")
    private Validated validated = Validated.Pending;

    public StudentActivity() {
        this.validated = Validated.Pending;
    }

    public Validated getValidated() { return validated; }
    public void setValidated(Validated validated) { this.validated = validated; }

    @Id
    private String sid;

    @Id
    private int actID;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("actID")  // Maps composite key field
    @JoinColumn(name = "actID", referencedColumnName = "actID")
    @JsonIgnore
    private Activity activity;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("sid")  // Maps composite key field
    @JoinColumn(name = "sid", referencedColumnName = "sid")
    @JsonIgnore
    private Student student;

    @Column(name = "date", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date date;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "points", columnDefinition = "INT DEFAULT 0")
    private int points;

    @Column(name = "activity_type", nullable = false)
    private String activityType;

    @Column(name="link", nullable = false)
    private String link;

    // Getters and Setters
    public String getSid() { return sid; }
    public void setSid(String sid) { this.sid = sid; }

    public int getActID() { return actID; }
    public void setActID(int actID) { this.actID = actID; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public Activity getActivity() { return activity; }
    public void setActivity(Activity activity) { this.activity = activity; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
}
