package com.example.student_activity_points.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class FailedAttemptService {
    
    private static final Logger log = LoggerFactory.getLogger(FailedAttemptService.class);
    
    // Track failed attempts per IP address
    private final Cache<String, Integer> attemptCache = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES) // Reset after 30 minutes
            .maximumSize(10_000)
            .build();
    
    /**
     * Check if CAPTCHA is required (after 3 failed attempts)
     */
    public boolean requiresCaptcha(String identifier) {
        Integer attempts = attemptCache.getIfPresent(identifier);
        boolean required = attempts != null && attempts >= 3;
        
        if (required) {
            log.info("CAPTCHA required for identifier: {} (attempts: {})", identifier, attempts);
        }
        
        return required;
    }
    
    /**
     * Record a failed attempt
     */
    public void recordFailure(String identifier) {
        Integer currentAttempts = attemptCache.getIfPresent(identifier);
        int newAttempts = (currentAttempts == null ? 0 : currentAttempts) + 1;
        attemptCache.put(identifier, newAttempts);
        
        log.warn("Failed attempt recorded for {}: {} total attempts", identifier, newAttempts);
    }
    
    /**
     * Reset attempts on successful action
     */
    public void resetAttempts(String identifier) {
        attemptCache.invalidate(identifier);
        log.debug("Attempts reset for identifier: {}", identifier);
    }
    
    /**
     * Get current attempt count
     */
    public int getAttemptCount(String identifier) {
        return attemptCache.getIfPresent(identifier) != null 
            ? attemptCache.getIfPresent(identifier) 
            : 0;
    }
}