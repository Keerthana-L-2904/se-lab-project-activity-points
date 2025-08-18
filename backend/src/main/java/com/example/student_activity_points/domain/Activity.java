package com.example.student_activity_points.domain;

import jakarta.persistence.*;
import java.util.Date;
import java.util.List;

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

     // Add OneToMany relationship with StudentActivity
    @OneToMany(mappedBy = "activity", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
     private List<StudentActivity> studentActivities;

//     @OneToMany(mappedBy = "activity", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
// private List<Validation> validations;
     
    // Getters and setters
    public Long getActID() { return actID; }
    public void setActID(Long actID) { this.actID = actID; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getDID() { return DID; }
    public void setDID(Integer DID) { this.DID = DID; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public Date getEnd_date() { return end_date; }
    public void setEnd_date(Date end_date) { this.end_date = end_date; }

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

    public List<StudentActivity> getStudentActivities() { return studentActivities; }
    public void setStudentActivities(List<StudentActivity> studentActivities) { this.studentActivities = studentActivities; }

    // public List<Validation> getValidations() {
    //     return validations;
    // }
    
    // public void setValidations(List<Validation> validations) {
    //     this.validations = validations;
    //}
    
}
