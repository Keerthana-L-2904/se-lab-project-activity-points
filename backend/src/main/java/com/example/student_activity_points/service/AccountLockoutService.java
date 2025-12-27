package com.example.student_activity_points.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class AccountLockoutService {
    
    private static final Logger log = LoggerFactory.getLogger(AccountLockoutService.class);
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MINUTES = 15;
    
    private final Cache<String, LockoutInfo> lockoutCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(10_000)
            .build();
    
    /**
     * Record a failed login attempt for an email
     */
    public void recordFailedLogin(String email) {
        LockoutInfo info = lockoutCache.get(email, k -> new LockoutInfo());
        info.increment();
        
        if (info.getAttempts() >= MAX_ATTEMPTS) {
            long lockUntil = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(LOCKOUT_DURATION_MINUTES);
            info.lockUntil(lockUntil);
            log.warn("Account locked for email: {} until {}", email, lockUntil);
        }
        
        lockoutCache.put(email, info);
    }
    
    /**
     * Check if an account is locked
     */
    public boolean isLocked(String email) {
        LockoutInfo info = lockoutCache.getIfPresent(email);
        if (info != null && info.isLocked()) {
            long remainingMs = info.getLockUntil() - System.currentTimeMillis();
            log.info("Account is locked for email: {} (remaining: {} minutes)", 
                    email, TimeUnit.MILLISECONDS.toMinutes(remainingMs));
            return true;
        }
        return false;
    }
    
    /**
     * Get remaining lockout time in minutes
     */
    public long getRemainingLockoutMinutes(String email) {
        LockoutInfo info = lockoutCache.getIfPresent(email);
        if (info != null && info.isLocked()) {
            long remainingMs = info.getLockUntil() - System.currentTimeMillis();
            return TimeUnit.MILLISECONDS.toMinutes(remainingMs);
        }
        return 0;
    }
    
    /**
     * Reset lockout on successful login
     */
    public void resetLockout(String email) {
        lockoutCache.invalidate(email);
        log.debug("Lockout reset for email: {}", email);
    }
    
    // Inner class to track lockout info
    private static class LockoutInfo {
        private int attempts = 0;
        private long lockUntil = 0;
        
        public void increment() {
            attempts++;
        }
        
        public int getAttempts() {
            return attempts;
        }
        
        public void lockUntil(long timestamp) {
            lockUntil = timestamp;
        }
        
        public boolean isLocked() {
            return System.currentTimeMillis() < lockUntil;
        }
        
        public long getLockUntil() {
            return lockUntil;
        }
    }
}