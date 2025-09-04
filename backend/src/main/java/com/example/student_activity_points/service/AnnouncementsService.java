package com.example.student_activity_points.service;

import com.example.student_activity_points.domain.Announcements;
import com.example.student_activity_points.repository.AnnouncementsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnnouncementsService {

    @Autowired
    private AnnouncementsRepository announcementsRepository;

    // Fetch announcements for a specific FAID
    public List<Announcements> getAnnouncementsByFAID(Integer faid) {
        return announcementsRepository.findByFAID(faid);
    }
}
