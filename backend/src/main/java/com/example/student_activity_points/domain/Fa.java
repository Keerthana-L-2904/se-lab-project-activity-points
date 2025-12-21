package com.example.student_activity_points.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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

    @ManyToOne
    @JoinColumn(name = "DID", referencedColumnName = "DID")
    private Departments department;


    // Getters and setters
    public Long getFAID() { return FAID; }
    public void setFAID(Long faid) { this.FAID = faid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmailID() { return emailID; }
    public void setEmailID(String emailID) { this.emailID = emailID; }

    
    public Departments getDepartment() { return department ; }
    public void setDepartment(Departments department) {  this.department = department; }

}
