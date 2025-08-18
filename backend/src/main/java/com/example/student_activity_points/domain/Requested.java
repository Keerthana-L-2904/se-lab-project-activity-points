package com.example.student_activity_points.domain;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "Requested")
public class Requested {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rid")
    private Long rid;

    @Column(name = "FAID", nullable = false)
    private int FAID;

    @Column(name = "Approved", nullable = false)
    private Boolean approved;

    // Getters and Setters
    public Long getRid() { return rid; }
    public void setRid( Long rid) { this.rid = rid; }

    public int getFaid() { return FAID; }
    public void setFaid(int faid) { this.FAID = faid; }

    public Boolean getApproved() { return approved; }
    public void setApproved(Boolean approved) { this.approved= approved; }


}
