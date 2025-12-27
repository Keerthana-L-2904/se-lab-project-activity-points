package com.example.student_activity_points.repository;

import com.example.student_activity_points.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    
    Optional<RefreshToken> findByToken(String token);
    
    Optional<RefreshToken> findByUserEmailAndUserRole(String email, String role);
    
    @Transactional
    @Modifying
    void deleteByUserEmailAndUserRole(String email, String role);
    
    @Transactional
    @Modifying
    void deleteByToken(String token);
    
    // Cleanup expired tokens (run periodically)
    @Transactional
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
    void deleteExpiredTokens(LocalDateTime now);
}