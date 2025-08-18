package com.example.student_activity_points.service;
import com.example.student_activity_points.domain.Student;
import com.example.student_activity_points.domain.StudentActivity;
import com.example.student_activity_points.domain.Announcements;
import com.example.student_activity_points.repository.StudentRepository;
import com.example.student_activity_points.repository.StudentActivityRepository;
import com.example.student_activity_points.repository.AnnouncementsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentActivityRepository studentActivityRepository;

    @Autowired
    private AnnouncementsRepository announcementsRepository;

    // Get student by ID
    public Optional<Student> getStudentById(String studentID) {
        return studentRepository.findBySid(studentID);
    }

    // Get total department points
    public Integer getDepartmentPoints(String studentID) {
        return studentRepository.getTotalDepartmentPoints(studentID);
    }

    // Get total institutional points
    public Integer getInstitutionalPoints(String studentID) {
        return studentRepository.getTotalInstitutionalPoints(studentID);
    }

    // Get total activity points
    public Integer getActivityPoints(String studentID) {
        return studentRepository.getTotalActivityPoints(studentID);
    }

    // Get student activity history
    public List<StudentActivity> getStudentActivities(String studentID) {
        return studentActivityRepository.findBySid(studentID);
    }

    // Get announcements/notifications
    public List<Announcements> getAnnouncements() {
    List<Announcements> announcementsList = new ArrayList<>();
    announcementsRepository.findAll().forEach(announcementsList::add);
    return announcementsList;
    }
    public List<Student> getStudentsByFAID(int FAID) {
    return studentRepository.findByFAID(FAID);
    }
}
