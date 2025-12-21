package com.example.student_activity_points.repository;

import com.example.student_activity_points.domain.Announcements;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnnouncementsRepository extends JpaRepository<Announcements, Integer> {
    List<Announcements> findByFAID(Integer FAID);
    Announcements findByAid(Long aid);
}
