package com.example.student_activity_points.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Service
public class CsrfTokenService {
    
    private static final Logger log = LoggerFactory.getLogger(CsrfTokenService.class);
    
    // Store CSRF tokens with 1 hour expiry
    private final Cache<String, String> csrfTokenCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(10_000)
            .build();
    
    /**
     * Generate a new CSRF token for a session/user
     */
    public String generateToken(String sessionId) {
        String token = generateSecureToken();
        csrfTokenCache.put(sessionId, token);
        log.debug("Generated CSRF token for session: {}", sessionId);
        return token;
    }
    
    /**
     * Validate CSRF token
     */
    public boolean validateToken(String sessionId, String token) {
        if (sessionId == null || token == null) {
            return false;
        }
        
        String storedToken = csrfTokenCache.getIfPresent(sessionId);
        boolean isValid = token.equals(storedToken);
        
        if (!isValid) {
            log.warn("CSRF token validation failed for session: {}", sessionId);
        }
        
        return isValid;
    }
    
    /**
     * Invalidate CSRF token (on logout)
     */
    public void invalidateToken(String sessionId) {
        csrfTokenCache.invalidate(sessionId);
        log.debug("Invalidated CSRF token for session: {}", sessionId);
    }
    
    private String generateSecureToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}