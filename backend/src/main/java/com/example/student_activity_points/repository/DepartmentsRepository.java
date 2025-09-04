package com.example.student_activity_points.repository;

import com.example.student_activity_points.domain.Departments;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface DepartmentsRepository extends CrudRepository<Departments, Long> {
    Optional<Departments> findByName(String name);
}
