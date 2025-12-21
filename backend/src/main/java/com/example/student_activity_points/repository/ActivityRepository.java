package com.example.student_activity_points.repository;

import  java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.example.student_activity_points.domain.Activity;

public interface ActivityRepository extends CrudRepository<Activity, Long> {
    Optional<Activity> findByName(String name);
    List<Activity> findByMandatory(Integer mandatory);
    List<Activity> findByTypeIgnoreCase(String type);
    List<Activity> findByNameIn(List<String> names);
}