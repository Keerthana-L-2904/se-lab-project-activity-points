package com.example.student_activity_points.controller;

import com.example.student_activity_points.domain.Fa;
import com.example.student_activity_points.domain.Student;
import com.example.student_activity_points.domain.RefreshToken;
import com.example.student_activity_points.repository.FARepository;
import com.example.student_activity_points.repository.StudentRepository;
import com.example.student_activity_points.security.JwtUtil;
import com.example.student_activity_points.service.RefreshTokenService;
import com.example.student_activity_points.service.CsrfTokenService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private FARepository faRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private CsrfTokenService csrfTokenService;

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private static final String GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    /* ================= RATE LIMITING ================= */
    
    private static class LoginAttemptInfo {
        int failedAttempts = 0;
        long lastFailedAttempt = 0;
        long lockoutUntil = 0;
    }

    private final ConcurrentHashMap<String, LoginAttemptInfo> loginAttempts = new ConcurrentHashMap<>();
    
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final long LOCKOUT_DURATION_MS = 5 * 60 * 1000; // 5 minutes
    private static final long ATTEMPT_WINDOW_MS = 10 * 60 * 1000; // 10 minutes

    private boolean isEmailLocked(String email) {
        LoginAttemptInfo info = loginAttempts.get(email);
        
        if (info == null) {
            return false;
        }

        long now = System.currentTimeMillis();

        if (info.lockoutUntil > now) {
            long remainingSeconds = (info.lockoutUntil - now) / 1000;
            log.warn("Email {} is locked. Remaining time: {} seconds", email, remainingSeconds);
            return true;
        }

        if (now - info.lastFailedAttempt > ATTEMPT_WINDOW_MS) {
            loginAttempts.remove(email);
            return false;
        }

        return false;
    }

    private void recordFailedAttempt(String email) {
        long now = System.currentTimeMillis();
        
        LoginAttemptInfo info = loginAttempts.computeIfAbsent(email, k -> new LoginAttemptInfo());
        
        if (now - info.lastFailedAttempt > ATTEMPT_WINDOW_MS) {
            info.failedAttempts = 0;
        }

        info.failedAttempts++;
        info.lastFailedAttempt = now;

        log.info("Failed login attempt {} for email: {}", info.failedAttempts, email);

        if (info.failedAttempts >= MAX_FAILED_ATTEMPTS) {
            info.lockoutUntil = now + LOCKOUT_DURATION_MS;
            log.warn("Email {} locked due to {} failed attempts. Lockout until: {}", 
                     email, info.failedAttempts, new java.util.Date(info.lockoutUntil));
        }
    }

    private void recordSuccessfulLogin(String email) {
        loginAttempts.remove(email);
    }

    private long getRemainingLockoutTime(String email) {
        LoginAttemptInfo info = loginAttempts.get(email);
        if (info == null || info.lockoutUntil <= System.currentTimeMillis()) {
            return 0;
        }
        return (info.lockoutUntil - System.currentTimeMillis()) / 1000;
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

    /* ================= GOOGLE TOKEN VERIFICATION ================= */

    private Map<String, Object> verifyGoogleToken(String accessToken) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                GOOGLE_USERINFO_URL,
                HttpMethod.GET,
                entity,
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
            
            return null;
        } catch (HttpClientErrorException e) {
            log.error("Google token verification failed: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Error verifying Google token", e);
            return null;
        }
    }

    /* ================= STUDENT LOGIN ================= */

    @PostMapping("/login-student")
    public ResponseEntity<?> loginStudent(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        String accessToken = request.get("accessToken");

        if (accessToken == null || accessToken.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Access token is required"));
        }

        Map<String, Object> googleUserInfo = verifyGoogleToken(accessToken);

        if (googleUserInfo == null) {
            log.warn("Invalid Google access token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid credentials"));
        }

        String email = (String) googleUserInfo.get("email");
        Boolean emailVerified = (Boolean) googleUserInfo.get("email_verified");

        if (email == null || !Boolean.TRUE.equals(emailVerified)) {
            log.warn("Email not verified or missing");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid credentials"));
        }

        if (isEmailLocked(email)) {
            long remainingSeconds = getRemainingLockoutTime(email);
            long remainingMinutes = remainingSeconds / 60;
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of(
                        "message", "Too many failed login attempts. Please try again in " + remainingMinutes + " minutes.",
                        "remainingSeconds", remainingSeconds
                    ));
        }

        Optional<Student> studentOpt = studentRepository.findByEmailID(email);

        if (studentOpt.isEmpty()) {
            log.warn("Student login with valid Google account but not in DB: {}", email);
            recordFailedAttempt(email);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid credentials"));
        }

        Student student = studentOpt.get();

        recordSuccessfulLogin(email);

        // Generate access token (short-lived)
        String jwtToken = jwtUtil.generateStudentToken(
                student.getEmailID(),
                student.getSid()
        );

        // Generate refresh token (long-lived)
        String ip = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(email, "STUDENT", ip, userAgent);

        // ✅ Store ACCESS token in httpOnly cookie (10 minutes)
        Cookie accessCookie = new Cookie("accessToken", jwtToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(false); // Set to TRUE in production (HTTPS)
        accessCookie.setPath("/");
        accessCookie.setMaxAge(10 * 60); // 10 minutes
        accessCookie.setAttribute("SameSite", "Strict");
        httpResponse.addCookie(accessCookie);

        // ✅ Store REFRESH token in httpOnly cookie (7 days)
        Cookie refreshCookie = new Cookie("refreshToken", refreshToken.getToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false); // Set to TRUE in production
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        refreshCookie.setAttribute("SameSite", "Strict");
        httpResponse.addCookie(refreshCookie);

        // ✅ Generate and send CSRF token in HEADER
        String csrfToken = csrfTokenService.generateToken(refreshToken.getToken());
        httpResponse.setHeader("X-CSRF-Token", csrfToken);

        // Return student data (NO tokens in response body)
        Map<String, Object> response = Map.of(
                "sid", student.getSid(),
                "name", student.getName(),
                "email", student.getEmailID(),
                "role", "STUDENT"
        );

        log.info("Student logged in successfully: {}", email);
        return ResponseEntity.ok(response);
    }

    /* ================= FA LOGIN ================= */

    @PostMapping("/login-fa")
    public ResponseEntity<?> loginFA(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        String accessToken = request.get("accessToken");

        if (accessToken == null || accessToken.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Access token is required"));
        }

        Map<String, Object> googleUserInfo = verifyGoogleToken(accessToken);

        if (googleUserInfo == null) {
            log.warn("Invalid Google access token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid credentials"));
        }

        String email = (String) googleUserInfo.get("email");
        Boolean emailVerified = (Boolean) googleUserInfo.get("email_verified");

        if (email == null || !Boolean.TRUE.equals(emailVerified)) {
            log.warn("Email not verified or missing");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid credentials"));
        }

        if (isEmailLocked(email)) {
            long remainingSeconds = getRemainingLockoutTime(email);
            long remainingMinutes = remainingSeconds / 60;
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of(
                        "message", "Too many failed login attempts. Please try again in " + remainingMinutes + " minutes.",
                        "remainingSeconds", remainingSeconds
                    ));
        }

        Optional<Fa> faOpt = faRepository.findByEmailID(email);

        if (faOpt.isEmpty()) {
            log.warn("FA login with valid Google account but not in DB: {}", email);
            recordFailedAttempt(email);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid credentials"));
        }

        Fa fa = faOpt.get();

        recordSuccessfulLogin(email);

        // Generate access token (short-lived)
        String jwtToken = jwtUtil.generateFaToken(
                fa.getEmailID(),
                fa.getFAID()
        );

        // Generate refresh token (long-lived)
        String ip = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(email, "FA", ip, userAgent);

        // ✅ Store ACCESS token in httpOnly cookie (10 minutes)
        Cookie accessCookie = new Cookie("accessToken", jwtToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(false); // Set to TRUE in production (HTTPS)
        accessCookie.setPath("/");
        accessCookie.setMaxAge(10 * 60); // 10 minutes
        accessCookie.setAttribute("SameSite", "Strict");
        httpResponse.addCookie(accessCookie);

        // ✅ Store REFRESH token in httpOnly cookie (7 days)
        Cookie refreshCookie = new Cookie("refreshToken", refreshToken.getToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false); // Set to TRUE in production
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        refreshCookie.setAttribute("SameSite", "Strict");
        httpResponse.addCookie(refreshCookie);

        // ✅ Generate and send CSRF token in HEADER
        String csrfToken = csrfTokenService.generateToken(refreshToken.getToken());
        httpResponse.setHeader("X-CSRF-Token", csrfToken);

        // Return FA data (NO tokens in response body)
        Map<String, Object> response = Map.of(
                "faid", fa.getFAID(),
                "name", fa.getName(),
                "email", fa.getEmailID(),
                "role", "FA"
        );

        log.info("FA logged in successfully: {}", email);
        return ResponseEntity.ok(response);
    }

    /* ================= LOGOUT (STUDENT & FA) ================= */

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        try {
            Cookie[] cookies = httpRequest.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("refreshToken".equals(cookie.getName())) {
                        String refreshToken = cookie.getValue();
                        
                        // Invalidate tokens in database
                        refreshTokenService.invalidateToken(refreshToken);
                        csrfTokenService.invalidateToken(refreshToken);
                    }
                    
                    // Clear BOTH cookies
                    if ("refreshToken".equals(cookie.getName()) || "accessToken".equals(cookie.getName())) {
                        Cookie clearCookie = new Cookie(cookie.getName(), null);
                        clearCookie.setHttpOnly(true);
                        clearCookie.setSecure(false);
                        clearCookie.setPath("/");
                        clearCookie.setMaxAge(0);
                        httpResponse.addCookie(clearCookie);
                    }
                }
            }
            
            log.info("User logged out successfully");
            return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
            
        } catch (Exception ex) {
            log.error("Error during logout", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error during logout"));
        }
    }

    /* ================= REFRESH TOKEN (STUDENT & FA) ================= */

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        try {
            Cookie[] cookies = httpRequest.getCookies();
            String refreshTokenValue = null;
            
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("refreshToken".equals(cookie.getName())) {
                        refreshTokenValue = cookie.getValue();
                        break;
                    }
                }
            }
            
            if (refreshTokenValue == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "No refresh token found"));
            }
            
            Optional<RefreshToken> refreshTokenOpt = refreshTokenService.verifyRefreshToken(refreshTokenValue);
            
            if (refreshTokenOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Invalid or expired refresh token"));
            }
            
            RefreshToken refreshToken = refreshTokenOpt.get();
            String email = refreshToken.getUserEmail();
            String role = refreshToken.getUserRole();
            
            // Generate new access token based on role
            String newAccessToken;
            if ("STUDENT".equals(role)) {
                Optional<Student> studentOpt = studentRepository.findByEmailID(email);
                if (studentOpt.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("message", "User not found"));
                }
                Student student = studentOpt.get();
                newAccessToken = jwtUtil.generateStudentToken(email, student.getSid());
            } 
            else if ("FA".equals(role)) 
                {
                    Optional<Fa> faOpt = faRepository.findByEmailID(email);
                    if (faOpt.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(Map.of("message", "User not found"));
                    }
                    Fa fa = faOpt.get();
                    newAccessToken = jwtUtil.generateFaToken(email, fa.getFAID());
                }
            else if ("ADMIN".equals(role)) 
                {
                newAccessToken = jwtUtil.generateToken(email, "ADMIN");
                }
            else 
                {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("message", "Invalid role"));
                }
            
            // Store new access token in httpOnly cookie
            Cookie accessCookie = new Cookie("accessToken", newAccessToken);
            accessCookie.setHttpOnly(true);
            accessCookie.setSecure(false);
            accessCookie.setPath("/");
            accessCookie.setMaxAge(10 * 60); // 10 minutes
            accessCookie.setAttribute("SameSite", "Strict");
            httpResponse.addCookie(accessCookie);
            
            // Generate new CSRF token
            String newCsrfToken = csrfTokenService.generateToken(refreshTokenValue);
            httpResponse.setHeader("X-CSRF-Token", newCsrfToken);
            
            log.info("Access token refreshed for user: {} ({})", email, role);
            
            return ResponseEntity.ok(Map.of("message", "Token refreshed successfully"));
            
        } catch (Exception ex) {
            log.error("Error refreshing token", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error refreshing token"));
        }
    }
}