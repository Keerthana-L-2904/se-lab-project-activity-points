package com.example.student_activity_points.repository;

import com.example.student_activity_points.domain.Student;
import com.example.student_activity_points.dto.StudentWithMandatoryDTO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;


public interface StudentRepository extends CrudRepository<Student, String> {
    
    // Fetch student by email
    Optional<Student> findByEmailID(String emailID);

    // Fetch student by student ID
    Optional<Student> findBySid(String studentID);


    // List<Student> findByFAID(int FAID);

    @Query("SELECT s FROM Student s WHERE s.FAID = :FAID")
    List<Student> findByFAID(@Param("FAID") int FAID);
    
 // Query to get department points for a student
    @Query("SELECT s.deptPoints FROM Student s WHERE s.sid = :studentID")
    Integer getTotalDepartmentPoints(@Param("studentID") String studentID);

    // Query to get institutional points for a student
    @Query("SELECT s.institutePoints FROM Student s WHERE s.sid = :studentID")
    Integer getTotalInstitutionalPoints(@Param("studentID") String studentID);

    // Query to get activity points for a student
    @Query("SELECT s.activityPoints FROM Student s WHERE s.sid = :studentID")
    Integer getTotalActivityPoints(@Param("studentID") String studentID);

    @Query("SELECT new com.example.student_activity_points.dto.StudentWithMandatoryDTO(" +
    "s, COUNT(CASE WHEN a.mandatory = 1 THEN sa.actID END)) " +
    "FROM Student s " +
    "LEFT JOIN StudentActivity sa ON s.sid = sa.sid " +
    "LEFT JOIN Activity a ON sa.actID = a.actID " +
    "WHERE s.FAID = :faid " +
    "GROUP BY s")
    List<StudentWithMandatoryDTO> findStudentsWithMandatoryCount(@Param("faid") int faid);
        
    @Query("SELECT new com.example.student_activity_points.dto.StudentWithMandatoryDTO(" +
           "s, COUNT(CASE WHEN a.mandatory = 1 THEN sa.actID END)) " +
           "FROM Student s " +
           "LEFT JOIN StudentActivity sa ON s.sid = sa.sid " +
           "LEFT JOIN Activity a ON sa.actID = a.actID " +
           "WHERE s.FAID = :faid AND LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "GROUP BY s")
    List<StudentWithMandatoryDTO> searchStudentsByFAIDAndName(
            @Param("faid") int faid,
            @Param("name") String name
    );

    @Query("SELECT new com.example.student_activity_points.dto.StudentWithMandatoryDTO(" +
    "s, COUNT(CASE WHEN a.mandatory = 1 THEN sa.actID END)) " +
    "FROM Student s " +
    "LEFT JOIN StudentActivity sa ON s.sid = sa.sid " +
    "LEFT JOIN Activity a ON sa.actID = a.actID " +
    "WHERE s.FAID = :faid " +
    "GROUP BY s " +
    "HAVING COUNT(CASE WHEN a.mandatory = 1 THEN sa.actID END) = :mandatoryCount")
    List<StudentWithMandatoryDTO> searchStudentsByFAIDAndMandatoryCount(
            @Param("faid") int faid,
            @Param("mandatoryCount") Long mandatoryCount
    );

}
