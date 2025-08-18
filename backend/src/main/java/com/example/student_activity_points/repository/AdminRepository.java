package com.example.student_activity_points.repository;

import com.example.student_activity_points.domain.Admin;
import org.springframework.data.repository.CrudRepository;

public interface AdminRepository extends CrudRepository<Admin, Integer> {
    Admin findByEmail(String email);
}
