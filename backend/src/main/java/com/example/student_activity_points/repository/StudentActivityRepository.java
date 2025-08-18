package com.example.student_activity_points.repository;

import com.example.student_activity_points.domain.StudentActivity;
import com.example.student_activity_points.domain.StudentActivityId;

import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface StudentActivityRepository extends CrudRepository<StudentActivity, StudentActivityId> {

    List<StudentActivity> findBySid(String sid);
}
