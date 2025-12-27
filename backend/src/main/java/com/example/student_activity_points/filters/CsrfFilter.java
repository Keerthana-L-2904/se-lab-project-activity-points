package com.example.student_activity_points.filters;

import com.example.student_activity_points.service.CsrfTokenService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
public class CsrfFilter extends OncePerRequestFilter {
    
    private static final Logger log = LoggerFactory.getLogger(CsrfFilter.class);
    
    @Autowired
    private CsrfTokenService csrfTokenService;
    
    // Methods that require CSRF protection
    private static final String[] PROTECTED_METHODS = {"POST", "PUT", "DELETE", "PATCH"};
    
    // Endpoints that don't need CSRF (initial login, public endpoints)
    private static final String[] EXCLUDED_PATHS = {
        "/api/auth/login-student",
        "/api/auth/login-fa",
        "/admin/login",
        "/admin/register",
        "/admin/forgot-password",
        "/admin/reset-password",
        "/api/auth/refresh" // Refresh token endpoint
    };
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        
        String method = request.getMethod();
        String path = request.getRequestURI();
        
        // Check if this is a state-changing request
        boolean isProtectedMethod = Arrays.asList(PROTECTED_METHODS).contains(method);
        boolean isExcludedPath = Arrays.stream(EXCLUDED_PATHS).anyMatch(path::startsWith);
        
        if (isProtectedMethod && !isExcludedPath) {
            // Get session ID from refresh token cookie
            String sessionId = getRefreshTokenFromCookie(request);
            
            if (sessionId == null) {
                log.warn("CSRF check failed: No session found for path: {}", path);
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.getWriter().write("{\"message\":\"CSRF validation failed: No session\"}");
                return;
            }
            
            // Get CSRF token from header
            String csrfToken = request.getHeader("X-CSRF-Token");
            
            if (csrfToken == null || !csrfTokenService.validateToken(sessionId, csrfToken)) {
                log.warn("CSRF validation failed for path: {} from IP: {}", path, getClientIp(request));
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"message\":\"CSRF validation failed\"}");
                return;
            }
            
            log.debug("CSRF validation passed for path: {}", path);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
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
}