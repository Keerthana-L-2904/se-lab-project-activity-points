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

public class ForgotPasswordRateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ForgotPasswordRateLimitFilter.class);

    // IP-based rate limiting
    private final Cache<String, Bucket> ipCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(10_000)
            .build();

    // Email-based rate limiting (secondary protection)
    private final Cache<String, Bucket> emailCache = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(50_000)
            .build();

    private Bucket newIpBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(
                    3, // Only 3 requests per IP
                    Refill.intervally(3, Duration.ofMinutes(15))
                ))
                .build();
    }

    private Bucket newEmailBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(
                    2, // Only 2 requests per email
                    Refill.intervally(2, Duration.ofMinutes(30))
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

    private String extractEmailFromRequest(HttpServletRequest request) {
        String email = request.getParameter("email");
        if (email != null && !email.isEmpty()) {
            return email.toLowerCase().trim();
        }
        return null;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String ip = getClientIp(request);

        // Check IP-based rate limit FIRST
        Bucket ipBucket = ipCache.get(ip, k -> newIpBucket());
        if (!ipBucket.tryConsume(1)) {
            log.warn("⚠️ IP rate limit exceeded for forgot-password: {}", ip);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"message\":\"Too many password reset requests. Please try again later.\"}"
            );
            return;
        }

        // Check email-based rate limit SECOND
        String email = extractEmailFromRequest(request);
        if (email != null) {
            Bucket emailBucket = emailCache.get(email, k -> newEmailBucket());
            if (!emailBucket.tryConsume(1)) {
                log.warn("⚠️ Email rate limit exceeded for forgot-password: {}", email);
                
                // IMPORTANT: Still return generic message (don't reveal email exists)
                // Add artificial delay to match normal response
                try {
                    Thread.sleep(150 + (long)(Math.random() * 100));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                response.setStatus(HttpStatus.OK.value());
                response.setContentType("text/plain");
                response.getWriter().write(
                    "If an account exists with that email, a password reset link has been sent."
                );
                return;
            }
        }

        log.debug("✅ Rate limit check passed for forgot-password from IP: {}", ip);
        
        // Continue to controller
        filterChain.doFilter(request, response);
    }
}