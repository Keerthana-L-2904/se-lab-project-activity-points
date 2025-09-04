package com.example.student_activity_points.controller;

import com.example.student_activity_points.domain.Announcements;
import com.example.student_activity_points.domain.Student;
import com.example.student_activity_points.repository.AnnouncementsRepository;
import com.example.student_activity_points.repository.StudentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/student")
public class AnnouncementsController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AnnouncementsRepository announcementsRepository;

    @GetMapping("/{sid}/announcements")
    public ResponseEntity<List<Announcements>> getAnnouncements(@PathVariable String sid) {
        Optional<Student> student = studentRepository.findById(sid);

        if (student.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        int faid = student.get().getFaid(); // ✅ Now using int
        List<Announcements> announcements = announcementsRepository.findByFAID(faid); // ✅ Method should accept int

        return ResponseEntity.ok(announcements);
    }

    @GetMapping("/{sid}/announcements/{aid}")
public ResponseEntity<Announcements> getAnnouncement(@PathVariable String sid, @PathVariable Long aid) {
    Optional<Student> student = studentRepository.findById(sid);

    if (student.isEmpty()) {
        return ResponseEntity.notFound().build();
    }

    int faid = student.get().getFaid(); // ✅ Get the student's FAID

    Announcements announcement = announcementsRepository.findByAid(aid);

    // ✅ Check if the announcement exists and belongs to the same FAID
    if (announcement == null || announcement.getFaid() != faid) {
        return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(announcement);
}


}
