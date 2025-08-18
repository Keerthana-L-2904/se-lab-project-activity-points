package com.example.student_activity_points.service;

import com.example.student_activity_points.domain.Departments;
import com.example.student_activity_points.repository.DepartmentsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;


@Service
public class DepartmentService {
    @Autowired
    private DepartmentsRepository departmentRepository;

    public Optional<Departments> getDepartmentById(Long did) {
        return departmentRepository.findById(did);
    }
}
