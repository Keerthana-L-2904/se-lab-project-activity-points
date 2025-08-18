package com.example.student_activity_points.repository;

import com.example.student_activity_points.domain.Guidelines;
import org.springframework.data.repository.CrudRepository;
import java.util.List;
import java.util.Optional;

public interface GuidelinesRepository extends CrudRepository<Guidelines, Integer> {
    List<Guidelines> findAll();
    Optional<Guidelines> findByGid(int gid);
}