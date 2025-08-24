// GuidelinesService.java
package com.example.student_activity_points.service;

import com.example.student_activity_points.domain.Guidelines;
import com.example.student_activity_points.repository.GuidelinesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GuidelinesService {
    
    @Autowired
    private GuidelinesRepository guidelinesRepository;
    
    public List<Guidelines> getAllGuidelines() {
        return guidelinesRepository.findAll();
    }
    
    public Optional<Guidelines> getGuidelineByGid(int gid) {
        return guidelinesRepository.findByGid(gid);
    }
}