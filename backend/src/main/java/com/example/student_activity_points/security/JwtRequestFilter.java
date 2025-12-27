package com.example.student_activity_points.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtRequestFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        String jwt = null;
        String email = null;
        String role = null;
        String sid = null;
        Long faid = null;

        // ✅ PRIORITY 1: Read JWT from httpOnly cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    break;
                }
            }
        }

        // ✅ FALLBACK: Check Authorization header (for backward compatibility during migration)
        if (jwt == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7);
                log.debug("JWT found in Authorization header (legacy)");
            }
        }

        // ✅ Process JWT if found
        if (jwt != null && !jwt.isEmpty()) {
            try {
                email = jwtUtil.extractEmail(jwt);
                role = jwtUtil.extractRole(jwt);

                if ("STUDENT".equals(role)) {
                    sid = jwtUtil.extractStudentSid(jwt);
                } else if ("FA".equals(role)) {
                    faid = jwtUtil.extractFaId(jwt);
                }

                // ✅ Set authentication if not already set
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    if (jwtUtil.validateToken(jwt, email)) {
                        AuthUser authUser = new AuthUser(email, role, sid, faid);
                        
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        authUser,
                                        null,
                                        authUser.getAuthorities()
                                );
                        
                        authToken.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );
                        
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        log.debug("Authentication set for user: {} with role: {}", email, role);
                    } else {
                        log.warn("JWT validation failed for email: {}", email);
                    }
                }
            } catch (Exception e) {
                log.error("Cannot set user authentication: {}", e.getMessage());
                // Don't throw - just continue without authentication
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // ✅ Skip JWT filter for public endpoints
        return path.equals("/api/auth/login-student") ||
               path.equals("/api/auth/login-fa") ||
               path.equals("/api/auth/refresh") ||
               path.equals("/api/auth/logout") ||
               path.equals("/admin/login") ||
               path.equals("/admin/logout") ||
               path.equals("/admin/refresh") ||
               path.equals("/admin/forgot-password") ||
               path.equals("/admin/reset-password");
    }
}
