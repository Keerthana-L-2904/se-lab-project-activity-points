package com.example.student_activity_points.repository;

import com.example.student_activity_points.domain.Requests;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface RequestsRepository extends CrudRepository<Requests, Long> {
    List<Requests> findBySidIn(List<String> sid);  // For multiple SIDs
    List<Requests> findBySid(String sid);  // For a single SID
}
