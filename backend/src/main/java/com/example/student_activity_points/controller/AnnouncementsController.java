package com.example.student_activity_points.controller;

import com.example.student_activity_points.domain.Announcements;
import com.example.student_activity_points.domain.Student;
import com.example.student_activity_points.repository.AnnouncementsRepository;
import com.example.student_activity_points.repository.StudentRepository;
import com.example.student_activity_points.security.AuthUser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/student")
public class AnnouncementsController {

    private AuthUser currentUser() {
        return (AuthUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AnnouncementsRepository announcementsRepository;

    private static final Logger log = LoggerFactory.getLogger(AnnouncementsController.class);

    @GetMapping("/announcements")
    public ResponseEntity<?> getAnnouncements() {
        String sid = null;
        try {
            sid = currentUser().getSid();
            Optional<Student> student = studentRepository.findById(sid);

            if (student.isEmpty()) {
                log.warn("Student not found: {}", sid);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Student not found");
            }

            int faid = student.get().getFaid();
            List<Announcements> announcements = announcementsRepository.findByFAID(faid);

            log.debug("Retrieved {} announcements for student: {}", announcements.size(), sid);
            return ResponseEntity.ok(announcements);

        } catch (Exception ex) {
            log.error("Error fetching announcements for student: {}", sid, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch announcements");
        }
    }

    @GetMapping("/announcements/{aid}")
    public ResponseEntity<?> getAnnouncement(@PathVariable Long aid) {
        String sid = null;
        try {
            sid = currentUser().getSid();
            Optional<Student> student = studentRepository.findById(sid);

            if (student.isEmpty()) {
                log.warn("Student not found: {}", sid);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Student not found");
            }

            int faid = student.get().getFaid();
            Announcements announcement = announcementsRepository.findByAid(aid);

            if (announcement == null) {
                log.warn("Announcement not found: {}", aid);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Announcement not found");
            }

            if (announcement.getFaid() != faid) {
                log.warn("Unauthorized access attempt: student {} tried to access announcement {} from different FA", 
                         sid, aid);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You do not have access to this announcement");
            }

            return ResponseEntity.ok(announcement);

        } catch (Exception ex) {
            log.error("Error fetching announcement {} for student: {}", aid, sid, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch announcement");
        }
    }
}