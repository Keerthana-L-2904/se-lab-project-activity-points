package com.example.student_activity_points.filters;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(LoginRateLimitFilter.class);

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    private Bucket createNewBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(
                        5, // max attempts
                        Refill.intervally(5, Duration.ofMinutes(1))
                ))
                .build();
    }

    private Bucket resolveBucket(String ip) {
        return cache.computeIfAbsent(ip, k -> createNewBucket());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // Apply rate limiting to login endpoints
            if (request.getRequestURI().contains("/login") && "POST".equals(request.getMethod())) {
                String ip = request.getRemoteAddr();
                Bucket bucket = resolveBucket(ip);

                if (!bucket.tryConsume(1)) {
                    log.warn("Rate limit exceeded for IP: {} on login attempt", ip);
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 429
                    response.setContentType("application/json");
                    response.getWriter().write("{\"message\": \"Too many login attempts. Please try again later.\"}");
                    return;
                }
                
                log.debug("Rate limit check passed for IP: {}", ip);
            }

            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            log.error("Error in rate limit filter for IP: {}", request.getRemoteAddr(), ex);
            // Continue the request even if rate limiting fails to avoid blocking legitimate users
            filterChain.doFilter(request, response);
        }
    }
}