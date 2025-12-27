package com.example.student_activity_points.service;

import com.example.student_activity_points.domain.RefreshToken;
import com.example.student_activity_points.repository.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);
    
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private static final long REFRESH_TOKEN_VALIDITY_DAYS = 7; // 7 days

    /**
     * Create a new refresh token for a user
     */
    @Transactional
    public RefreshToken createRefreshToken(String email, String role, String ipAddress, String userAgent) {
        // Invalidate any existing refresh token for this user
        refreshTokenRepository.deleteByUserEmailAndUserRole(email, role);

        // Generate secure random token
        String token = generateSecureToken();
        
        // Create new refresh token
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setUserEmail(email);
        refreshToken.setUserRole(role);
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS));
        refreshToken.setIpAddress(ipAddress);
        refreshToken.setUserAgent(userAgent);
        
        refreshToken = refreshTokenRepository.save(refreshToken);
        
        log.info("Created refresh token for user: {} ({})", email, role);
        return refreshToken;
    }

    /**
     * Verify and retrieve a refresh token
     */
    @Transactional(readOnly = true)
    public Optional<RefreshToken> verifyRefreshToken(String token) {
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(token);
        
        if (refreshTokenOpt.isEmpty()) {
            log.warn("Refresh token not found");
            return Optional.empty();
        }
        
        RefreshToken refreshToken = refreshTokenOpt.get();
        
        // Check if token is expired
        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            log.warn("Refresh token expired for user: {}", refreshToken.getUserEmail());
            refreshTokenRepository.delete(refreshToken);
            return Optional.empty();
        }
        
        return Optional.of(refreshToken);
    }

    /**
     * Invalidate a refresh token (on logout)
     */
    @Transactional
    public void invalidateToken(String token) {
        try {
            refreshTokenRepository.deleteByToken(token);
            log.info("Invalidated refresh token");
        } catch (Exception e) {
            log.error("Error invalidating refresh token", e);
        }
    }

    /**
     * Clean up expired tokens (should be called periodically)
     */
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("Cleaned up expired refresh tokens");
    }

    /**
     * Generate a secure random token
     */
    private String generateSecureToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[64];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}