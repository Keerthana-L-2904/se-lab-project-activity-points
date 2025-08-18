package com.example.student_activity_points.domain;

import jakarta.persistence.*;

import java.util.Date;
@Entity
@Table(name="Fa")
public class Fa {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long FAID;

    @Column(name="name", nullable=false,length=255)
    private String name;

    @Column(name="emailID", nullable=false,unique=true)
    private String emailID; 


    @Column(name="DID", nullable=false,length=255)
    private int DID;

    // Getters and setters
    public Long getFAID() { return FAID; }
    public void setFAID(Long faid) { this.FAID = faid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmailID() { return emailID; }
    public void setEmailID(String emailID) { this.emailID = emailID; }

    public int getDID() { return DID; }
    public void setDID(int did) { this.DID = did; }

}
