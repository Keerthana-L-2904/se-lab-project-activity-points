package com.example.student_activity_points.controller;

import com.example.student_activity_points.domain.Announcements;
import com.example.student_activity_points.repository.AnnouncementsRepository;
import com.example.student_activity_points.repository.FARepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/fa")
public class FaAnnouncementsController {

    @Autowired
    private AnnouncementsRepository announcementsRepository;
    
    @Autowired
    private FARepository faRepository;

    private static final Logger log = LoggerFactory.getLogger(FaAnnouncementsController.class);

    @GetMapping("/{faid}/announcements")
    public ResponseEntity<?> getAnnouncements(@PathVariable Long faid) {
        try {
            List<Announcements> announcements = announcementsRepository.findByFAID(faid.intValue());
            log.debug("Retrieved {} announcements for FA: {}", announcements.size(), faid);
            return ResponseEntity.ok(announcements);

        } catch (Exception ex) {
            log.error("Error fetching announcements for FA: {}", faid, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch announcements");
        }
    }

    @PostMapping("/announcements")
    public ResponseEntity<?> createAnnouncement(@RequestBody Announcements announcement) {
        try {
            if (announcement.getFaid() == 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("FAID is required");
            }

            if (announcement.getTitle() == null || announcement.getTitle().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Title is required");
            }

            Announcements savedAnnouncement = announcementsRepository.save(announcement);
            log.info("Announcement created successfully by FA: {}", announcement.getFaid());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedAnnouncement);

        } catch (Exception ex) {
            log.error("Error creating announcement", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to create announcement");
        }
    }

    @GetMapping("/{faid}/announcements/{aid}")
    public ResponseEntity<?> getAnnouncement(@PathVariable Long faid, @PathVariable Long aid) {
        try {
            Announcements announcement = announcementsRepository.findByAid(aid);

            if (announcement == null) {
                log.warn("Announcement not found: {}", aid);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Announcement not found");
            }

            if (announcement.getFaid() != faid.intValue()) {
                log.warn("Unauthorized access attempt: FA {} tried to access announcement {} from different FA", 
                         faid, aid);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You do not have access to this announcement");
            }

            return ResponseEntity.ok(announcement);

        } catch (Exception ex) {
            log.error("Error fetching announcement {} for FA: {}", aid, faid, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch announcement");
        }
    }
}