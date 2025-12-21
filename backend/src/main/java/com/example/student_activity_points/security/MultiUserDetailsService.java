package com.example.student_activity_points.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.student_activity_points.domain.Fa;
import com.example.student_activity_points.domain.Student;

import com.example.student_activity_points.repository.AdminRepository;
import com.example.student_activity_points.repository.StudentRepository;

import com.example.student_activity_points.repository.FARepository;
import java.util.Optional;

@Service  // 👈 makes it a Spring Bean so Security can find it automatically
public class MultiUserDetailsService implements UserDetailsService {

    @Autowired
    private StudentRepository studentRepo;

    @Autowired
    private FARepository faRepo;

    @Autowired
    private AdminRepository adminRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 1️⃣ Try student table
        Optional<Student> student = studentRepo.findByEmailID(username);
        if (student.get() != null) {
            return User.withUsername(student.get().getEmailID())
                    
                    .roles("STUDENT") // sets ROLE_STUDENT internally
                    .build();
        }

        // 2️⃣ Try FA table
        Optional<Fa> fa = faRepo.findByEmailID(username);
        if (fa.get() != null) {
            return User.withUsername(fa.get().getEmailID())
                    
                    .roles("FA") // sets ROLE_FA
                    .build();
        }

        // 3️⃣ Try Admin table
        com.example.student_activity_points.domain.Admin admin = adminRepo.findByEmail(username);
        if (admin != null) {
            return User.withUsername(admin.getEmail())
                    .password(admin.getPassword())
                    .roles("ADMIN") // sets ROLE_ADMIN
                    .build();
        }

        throw new UsernameNotFoundException("User not found in any table: " + username);
    }
}
