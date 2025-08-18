package com.example.student_activity_points.service;

import com.example.student_activity_points.domain.Fa;
import com.example.student_activity_points.repository.FARepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
public class FaService {

    @Autowired
    private FARepository faRepository;

    public Optional<Fa> getFaById(Long faid) {
        return faRepository.findById(faid);
    }

    public List<Fa> getAllFacultyAdvisors() {
        List<Fa> faList = new ArrayList<>();
        faRepository.findAll().forEach(faList::add); // Convert Iterable to List
        return faList;
    }
}
