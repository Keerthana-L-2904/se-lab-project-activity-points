package com.example.student_activity_points.repository;

import com.example.student_activity_points.domain.Student;
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


    // Query to get total department points
    @Query("SELECT SUM(s.deptPoints) FROM Student s WHERE s.sid = :studentID")
    Integer getTotalDepartmentPoints(@Param("studentID") String studentID);

    // Query to get total institutional points
    @Query("SELECT SUM(s.institutePoints) FROM Student s WHERE s.sid = :studentID")
    Integer getTotalInstitutionalPoints(@Param("studentID") String studentID);

    // Query to get total activity points
    @Query("SELECT SUM(s.activityPoints) FROM Student s WHERE s.sid = :studentID")
    Integer getTotalActivityPoints(@Param("studentID") String studentID);
}
