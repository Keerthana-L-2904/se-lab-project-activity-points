package com.example.student_activity_points.domain;

import jakarta.persistence.*;

import java.util.Date;


@Entity
@Table(name = "Departments")
public class Departments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long DID;

    @Column(name = "name")
    private String name;
    
    public Long getDID() { return DID; }
    public void setDID(Long DID) { this.DID = DID; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
