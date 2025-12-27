package com.example.student_activity_points.filters;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class RegistrationRateLimitFilter extends OncePerRequestFilter {
    
    private static final Logger log = LoggerFactory.getLogger(RegistrationRateLimitFilter.class);
    
    private final Cache<String, Bucket> cache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(10_000)
            .build();
    
    private Bucket newBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(
                        3, // Only 3 registration attempts
                        Refill.intervally(3, Duration.ofMinutes(5)) // per 5 minutes
                ))
                .build();
    }
    
    private String getClientIp(HttpServletRequest request) {
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isEmpty()) {
            return realIp;
        }
        
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        return request.getRemoteAddr();
    }
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        // Apply to registration endpoints
        if (path.contains("/register")) {
            String ip = getClientIp(request);
            Bucket bucket = cache.get(ip, k -> newBucket());
            
            if (!bucket.tryConsume(1)) {
                log.warn("Rate limit exceeded for IP: {} on registration endpoint", ip);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"message\":\"Too many registration attempts. Please try again later.\"}"
                );
                return;
            }
            
            log.debug("Rate limit check passed for IP: {} on registration", ip);
        }
        
        filterChain.doFilter(request, response);
    }
}