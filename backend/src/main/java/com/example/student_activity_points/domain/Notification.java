package com.example.student_activity_points.domain;
import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "Notification")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long NID;
    private String receiver_id;
    private String receiver_type;
    private String title;
    private String body;
    private String category;
    private int related_id;
    private Date date;
    private String time;
    private String status;

    public Long getNID() { return NID; }
    public void setNID(Long NID) { this.NID = NID; }

    public String getReceiver_id() { return receiver_id; }
    public void setReceiver_id(String receiver_id) { this.receiver_id = receiver_id; }

    public String getReceiver_type() { return receiver_type; }
    public void setReceiver_type(String receiver_type) { this.receiver_type = receiver_type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getRelated_id() { return related_id; }
    public void setRelated_id(int related_id) { this.related_id = related_id; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }


}
