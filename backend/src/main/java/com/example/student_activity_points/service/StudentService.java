package com.example.student_activity_points.service;
import com.example.student_activity_points.domain.Student;
import com.example.student_activity_points.domain.StudentActivity;
import com.example.student_activity_points.dto.StudentWithFADTO;
import com.example.student_activity_points.dto.StudentWithMandatoryDTO;
import com.example.student_activity_points.domain.Announcements;
import com.example.student_activity_points.domain.Fa;
import com.example.student_activity_points.repository.StudentRepository;
import com.example.student_activity_points.repository.StudentActivityRepository;
import com.example.student_activity_points.repository.AnnouncementsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Comparator;

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

    // Get all activities (sorted)
    public List<StudentActivity> getStudentActivities(String studentID) {
        return studentActivityRepository.findBySidOrderByDateDesc(studentID);
    }

    // Get latest activity only
    public StudentActivity getLatestActivity(String studentID) {
        return studentActivityRepository.findTopBySidOrderByDateDesc(studentID);
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
    public List<StudentWithMandatoryDTO> getStudentsByFAIDWithMandatoryCount(int FAID) {
        return studentRepository.findStudentsWithMandatoryCount(FAID);
    }

    public List<StudentWithMandatoryDTO> searchStudentsByFAIDAndName(int FAID, String name) {
        return studentRepository.searchStudentsByFAIDAndName(FAID, name);
    }
    
    public List<StudentWithMandatoryDTO> searchStudentsByMandatoryCount(int FAID, Long mandatoryCount) {
        return studentRepository.searchStudentsByFAIDAndMandatoryCount(FAID, mandatoryCount);
    }
    
    // Ascending order
   // Ascending order (by name)
    public List<StudentWithMandatoryDTO> getStudentsByFAIDWithMandatoryCountAsc(int FAID) {
        List<StudentWithMandatoryDTO> students = studentRepository.findStudentsWithMandatoryCount(FAID);
        return students.stream()
                .sorted(Comparator.comparing(s -> s.getStudent().getName(), String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    // Descending order (by name)
    public List<StudentWithMandatoryDTO> getStudentsByFAIDWithMandatoryCountDesc(int FAID) {
        List<StudentWithMandatoryDTO> students = studentRepository.findStudentsWithMandatoryCount(FAID);
        return students.stream()
                .sorted(Comparator.comparing(
                        (StudentWithMandatoryDTO s) -> s.getStudent().getName(),
                        String.CASE_INSENSITIVE_ORDER
                ).reversed())
                .collect(Collectors.toList());
    }

     public List<StudentWithMandatoryDTO> filterDeptPointsAbove(int FAID, Long points) {
        return studentRepository.findStudentsWithMandatoryCount(FAID).stream()
                .filter(s -> s.getStudent().getDeptPoints() > points)
                .collect(Collectors.toList());
    }
    
    public List<StudentWithMandatoryDTO> filterDeptPointsBelow(int FAID, Long points) {
        return studentRepository.findStudentsWithMandatoryCount(FAID).stream()
                .filter(s -> s.getStudent().getDeptPoints() < points)
                .collect(Collectors.toList());
    }
    
    public List<StudentWithMandatoryDTO> filterInstPointsAbove(int FAID, Long points) {
        return studentRepository.findStudentsWithMandatoryCount(FAID).stream()
                .filter(s ->  s.getStudent().getInstitutePoints() > points)
                .collect(Collectors.toList());
    }
    
    public List<StudentWithMandatoryDTO> filterInstPointsBelow(int FAID, Long points) {
        return studentRepository.findStudentsWithMandatoryCount(FAID).stream()
                .filter(s -> s.getStudent().getInstitutePoints() < points)
                .collect(Collectors.toList());
    }
    
    // Activity Points - Above
    public List<StudentWithMandatoryDTO> filterActivityPointsAbove(int FAID, Long points) {
        return studentRepository.findStudentsWithMandatoryCount(FAID).stream()
                .filter(s -> s.getStudent().getActivityPoints() > points)
                .collect(Collectors.toList());
    }

    // Activity Points - Below
    public List<StudentWithMandatoryDTO> filterActivityPointsBelow(int FAID, Long points) {
        return studentRepository.findStudentsWithMandatoryCount(FAID).stream()
                .filter(s -> s.getStudent().getActivityPoints() < points)
                .collect(Collectors.toList());
    }

    //dto
     @Autowired
    private FaService faService;
    public Optional<StudentWithFADTO> getStudentWithFA(String studentID) {
    Optional<Student> studentOpt = studentRepository.findBySid(studentID);
    if (studentOpt.isPresent()) {
        Student student = studentOpt.get();
        Optional<Fa> faOpt = faService.getFaById((long) student.getFaid());
        if (faOpt.isPresent()) {
            Fa fa = faOpt.get();
            return Optional.of(new StudentWithFADTO(
                student,
                fa.getName(),
                fa.getEmailID()
            ));
        }
        return Optional.of(new StudentWithFADTO(student, null, null));
    }
    return Optional.empty();
    }
}
