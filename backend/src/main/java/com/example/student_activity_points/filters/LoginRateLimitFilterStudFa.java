
// ==================== FILE 2: LoginRateLimitFilterStudFa.java ====================

package com.example.student_activity_points.filters;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class LoginRateLimitFilterStudFa extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(LoginRateLimitFilterStudFa.class);

    // ✅ Auto-expiring cache with Caffeine
    private final Cache<String, Bucket> ipBuckets = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(10_000)
            .build();

    private Bucket newBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(
                        20, // 20 attempts per IP (allows multiple account logins)
                        Refill.intervally(20, Duration.ofMinutes(1))
                ))
                .build();
    }

    // ✅ FIXED: No more recursion
    private String getClientIp(HttpServletRequest request) {
        // Priority 1: X-Real-IP (set by Nginx, AWS ALB)
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isEmpty()) {
            return realIp;
        }

        // Priority 2: X-Forwarded-For (proxy chain, take first/original client)
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        // Priority 3: Direct connection IP
        return request.getRemoteAddr();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        // ✅ Rate limit only login endpoints
        if (path.startsWith("/api/auth/login")) {
            String ip = getClientIp(request);

            // ✅ Use Caffeine's get() method with cache loader
            Bucket bucket = ipBuckets.get(ip, k -> newBucket());

            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
            
            if (!probe.isConsumed()) {
                long waitSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;
                log.warn("IP rate limit exceeded for: {} on login endpoint: {}", ip, path);
                
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write(String.format(
                        "{\"message\":\"Too many requests from your network. Please wait %d seconds and try again.\",\"remainingSeconds\":%d}",
                        waitSeconds, waitSeconds
                ));
                return;
            }

            log.debug("IP rate limit check passed for: {} on {} (remaining: {})", 
                      ip, path, probe.getRemainingTokens());
        }

        filterChain.doFilter(request, response);
    }
}