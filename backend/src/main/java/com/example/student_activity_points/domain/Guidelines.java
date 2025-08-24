package com.example.student_activity_points.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "guidelines")
public class Guidelines {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int gid;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String body;
    
    // Getters and Setters
    public int getGid() { return gid; }
    public void setGid(int gid) { this.gid = gid; }
    
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
}