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
     @JsonIgnore
    @JoinColumn(name = "actID", insertable = false, updatable = false)
    private Activity activity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "sid", insertable = false, updatable = false)
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

    @Lob
    @Column(name = "proof", columnDefinition = "LONGBLOB")
        private byte[] proof;

    public byte[] getProof() {
         return proof == null ? null : proof.clone();
    }

    public void setProof(byte[] proof) {
        this.proof = proof == null ? null : proof.clone();
        }

    public String getSid() { return sid; }
    public void setSid(String sid) { this.sid = sid; }

    public int getActID() { return actID; }
    public void setActID(int actID) { this.actID = actID; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Date getDate() { return date == null ? null : new Date(date.getTime());}
    public void setDate(Date date) { this.date = date == null ? null : new Date(date.getTime()); }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }

    public Activity getActivity() { return activity ;}
    public void setActivity(Activity activity) { this.activity = activity; }

    public Student getStudent() {return student;}
    public void setStudent(Student student) { this.student = student; }
}
