package com.example.student_activity_points.repository;

import com.example.student_activity_points.domain.Requested;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface RequestedRepository extends CrudRepository<Requested, Long> {
    List<Requested> findByRid(Long rid);
    Optional<Requested> findByRidAndFAID(Long rid, int FAID);
    List<Requested> findByFAID(int faId);
}
