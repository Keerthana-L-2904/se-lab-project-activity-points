package com.example.student_activity_points.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.example.student_activity_points.domain.Requests;

public interface RequestsRepository extends CrudRepository<Requests, Long> {
    List<Requests> findBySidIn(List<String> sid);  // For multiple SIDs
    List<Requests> findBySid(String sid);  // For a single SID
    void deleteBySid(String sid);
}
