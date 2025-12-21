package com.example.student_activity_points.repository;

import com.example.student_activity_points.domain.StudentActivity;
import com.example.student_activity_points.domain.StudentActivityId;

import org.springframework.data.repository.CrudRepository;
import java.util.List;
import java.util.Optional;

public interface StudentActivityRepository extends CrudRepository<StudentActivity, StudentActivityId> {

    List<StudentActivity> findBySid(String sid);

    List<StudentActivity> findByActID(int intValue);

    Optional<StudentActivity> findBySidAndActID(String sid, int aid);
    // Get all activities sorted by date (latest first)
    List<StudentActivity> findBySidOrderByDateDesc(String sid);

    // Get only the latest activity
    StudentActivity findTopBySidOrderByDateDesc(String sid);

}