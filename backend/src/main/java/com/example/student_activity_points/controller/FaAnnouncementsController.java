package com.example.student_activity_points.controller;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.student_activity_points.domain.Announcements;
import com.example.student_activity_points.domain.Fa;
import com.example.student_activity_points.domain.Student;
import com.example.student_activity_points.repository.AnnouncementsRepository;
import com.example.student_activity_points.repository.FARepository;

@RestController
@RequestMapping("/api/fa")
public class FaAnnouncementsController {

    @Autowired
    public AnnouncementsRepository announcementsRepository;
    @Autowired
    public FARepository faRepository;

    @GetMapping("/{faid}/announcements")
    public ResponseEntity<List<Announcements>> getAnnouncements(@PathVariable Long faid) {
       
        List<Announcements> announcements = announcementsRepository.findByFAID(faid.intValue()); // ✅ Method should accept int

        return ResponseEntity.ok(announcements);
    }

    @PostMapping("/announcements")
    public Announcements createAnnouncement(@RequestBody Announcements announcement) {
        System.out.println("Get FAID: " + announcement.getFaid());
        System.out.println("Get Title: " + announcement.getTitle());
        return announcementsRepository.save(announcement);
    }

    @GetMapping("/{faid}/announcements/{aid}")
public ResponseEntity<Announcements> getAnnouncement(@PathVariable Long faid, @PathVariable Long aid) {
     Announcements announcement = announcementsRepository.findByAid(aid);

    // ✅ Check if the announcement exists and belongs to the same FAID
    if (announcement == null || announcement.getFaid() != faid.intValue()) {
        return ResponseEntity.notFound().build();
    }

    

    return ResponseEntity.ok(announcement);
}
    
}
