package com.example.student_activity_points.repository;

import com.example.student_activity_points.domain.Activity;
import com.example.student_activity_points.domain.Validation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;

public interface ValidationRepository extends CrudRepository<Validation, Long> {
    
    // Deletes all validations for a specific activity
    @Transactional
    @Modifying
    @Query("DELETE FROM Validation v WHERE v.activity = :activity")
    void deleteByActivity(@Param("activity") Activity activity);

    // Corrected method to find Validation by actID and sid
    @Query(value = "SELECT * FROM Validation WHERE actID = ?1 AND SID = ?2", nativeQuery = true)
    Validation findByActIDAndSID(Long actID, String SID);

    @Query(value = "SELECT * FROM Validation WHERE actID = ?1", nativeQuery = true)
    List<Validation> findByActID(Long actID);
}

