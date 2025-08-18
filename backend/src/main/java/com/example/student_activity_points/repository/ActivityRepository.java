package com.example.student_activity_points.repository;

import  com.example.student_activity_points.domain.Activity;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface ActivityRepository extends CrudRepository<Activity, Long> {
    Optional<Activity> findByName(String name);
    
}

