package com.example.student_activity_points.domain;

import jakarta.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList; 
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "Activity")
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long actID;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "points")
    private Integer points;

    @Column(name = "DID")
    private Integer DID;

    @Column(name = "Date")
    private Date date;

    @Column(name = "end_date")
    private Date end_date;

    @Column(name = "type")
    private String type;

    @Column(name = "outside_inside")
    private String outside_inside;

    @Column(name = "no_of_people")
    private Integer no_of_people;

    @Column(name = "mandatory")
    private Integer mandatory;

    @OneToMany(mappedBy = "activity", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
     private List<StudentActivity> studentActivities;

    public Long getActID() { return actID; }
    public void setActID(Long actID) { this.actID = actID; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getDID() { return DID; }
    public void setDID(Integer DID) { this.DID = DID; }

    public Date getDate() {return new Date(date.getTime());}
    public void setDate(Date date) { this.date = new Date(date.getTime()); }

    public Date getEnd_date() { return new Date(end_date.getTime()); }
    public void setEnd_date(Date end_date) { this.end_date = new Date(end_date.getTime());}

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getOutside_inside() { return outside_inside; }
    public void setOutside_inside(String outside_inside) { this.outside_inside = outside_inside; }

    public Integer getNo_of_people() { return no_of_people; }
    public void setNo_of_people(Integer no_of_people) { this.no_of_people = no_of_people; }

    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }

    public Integer getMandatory() { return mandatory; }
    public void setMandatory(Integer mandatory) { this.mandatory = mandatory; }

    public List<StudentActivity> getStudentActivities() { return new ArrayList<>(studentActivities); }
    public void setStudentActivities(List<StudentActivity> studentActivities) { this.studentActivities = new ArrayList<>(studentActivities); }

    @Column(name = "isuploaded", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isuploaded=false;
    public boolean getIsuploaded() {
        return isuploaded;
    }
    public void setIsuploaded(boolean isuploaded) {
        this.isuploaded = isuploaded;
    }
    
}
