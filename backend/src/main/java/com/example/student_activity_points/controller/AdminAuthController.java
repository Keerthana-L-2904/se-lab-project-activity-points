package com.example.student_activity_points.controller;

import com.example.student_activity_points.domain.Admin;
import com.example.student_activity_points.dto.AdminDTO;
import com.example.student_activity_points.dto.ResetPasswordRequest;
import com.example.student_activity_points.repository.AdminRepository;
import org.springframework.security.core.Authentication;
import com.example.student_activity_points.security.JwtUtil;
import com.example.student_activity_points.service.EmailService;
import com.example.student_activity_points.service.RefreshTokenService;
import com.example.student_activity_points.service.CsrfTokenService;
import com.example.student_activity_points.domain.RefreshToken;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

import com.example.student_activity_points.service.FailedAttemptService;
import com.example.student_activity_points.service.AccountLockoutService;
import com.example.student_activity_points.service.CaptchaService;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminAuthController {

    @Autowired
    private AdminRepository adminRepo;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private CsrfTokenService csrfTokenService;
    
    @Autowired
    private FailedAttemptService failedAttemptService;
    
    @Autowired
    private AccountLockoutService accountLockoutService;
    
    @Autowired
    private CaptchaService captchaService;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    private static final Logger log = LoggerFactory.getLogger(AdminAuthController.class);
    private final Map<String, LocalDateTime> recentResetRequests = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupScheduler = Executors.newScheduledThreadPool(1);
    private static final String DUMMY_PASSWORD_HASH = "$2a$10$abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01234";

    private boolean validatePassword(String password) {
        if (password.length() <= 12 ||
            !password.matches(".*[A-Z].*") ||
            !password.matches(".*[a-z].*") ||
            !password.matches(".*[0-9].*") ||
            !password.matches(".*[@#$%!].*")) {
    
            return false;
        }
        return true;
    }
@PostMapping("/register")
public ResponseEntity<?> register(
        @RequestBody Admin admin,
        @RequestParam(required = false) String captchaToken,
        HttpServletRequest httpRequest,
        Authentication authentication  // ✅ ADD THIS
) {
    long startTime = System.nanoTime();
    
    try {
        // ✅ Log the authenticated admin (for debugging)
        if (authentication != null && authentication.isAuthenticated()) {
            log.info("Admin registration initiated by: {}", authentication.getName());
        } else {
            log.warn("Unauthenticated registration attempt blocked");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authentication required"));
        }
        
        String ip = getClientIp(httpRequest);
        
        log.info("Registration attempt for email: {} from IP: {} by admin: {}", 
                 admin.getEmail(), ip, authentication.getName());
        
            
            // ✅ Require CAPTCHA after 3 failed registration attempts
            if (failedAttemptService.requiresCaptcha(ip + ":register")) {
                log.info("CAPTCHA required for registration from IP: {}", ip);
                
                if (captchaToken == null || captchaToken.isEmpty()) {
                    log.warn("CAPTCHA required but not provided");
                    return ResponseEntity.badRequest()
                            .body(Map.of(
                                    "message", "CAPTCHA verification required",
                                    "requiresCaptcha", true
                            ));
                }
                
                boolean captchaValid = captchaService.verifyCaptcha(captchaToken);
                if (!captchaValid) {
                    log.warn("CAPTCHA verification failed for registration");
                    return ResponseEntity.badRequest()
                            .body(Map.of(
                                    "message", "CAPTCHA verification failed",
                                    "requiresCaptcha", true
                            ));
                }
                
                log.info("✅ CAPTCHA verified successfully for registration");
            }
            
            // ✅ ALWAYS validate password FIRST (before any DB operations)
            if (!validatePassword(admin.getPassword())) {
                failedAttemptService.recordFailure(ip + ":register");
                boolean requiresCaptcha = failedAttemptService.requiresCaptcha(ip + ":register");
                
                // Normalize timing before returning
                normalizeResponseTime(startTime, 150);
                
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Password does not meet requirements. Password must contain upper, lower, number and special character and must have 12 characters.");
                response.put("requiresCaptcha", requiresCaptcha);
                
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // ✅ Check if admin already exists
            Admin existingAdmin = adminRepo.findByEmail(admin.getEmail());
            
            // ✅ ALWAYS encode password (even if email exists) to normalize timing
            String encodedPassword = passwordEncoder.encode(admin.getPassword());
            
            if (existingAdmin != null) {
                // ✅ Record failed attempt
                failedAttemptService.recordFailure(ip + ":register");
                
                boolean requiresCaptcha = failedAttemptService.requiresCaptcha(ip + ":register");
                
                log.warn("Registration failed - email already exists: {}", admin.getEmail());
                
                // ✅ Normalize response time
                normalizeResponseTime(startTime, 150);
                
                // ✅ Return GENERIC message (don't reveal email exists)
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Registration failed. Please check your details and try again.");
                response.put("requiresCaptcha", requiresCaptcha);
                
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Save new admin with already-encoded password
            admin.setPassword(encodedPassword);
            adminRepo.save(admin);
            
            // ✅ Reset on successful registration
            failedAttemptService.resetAttempts(ip + ":register");
            
            log.info("✅ Admin registered successfully: {}", admin.getEmail());
            
            // ✅ Normalize response time for success case too
            normalizeResponseTime(startTime, 150);
            
            return ResponseEntity.ok(Map.of("message", "Admin registered successfully"));

        } catch (Exception ex) {
            log.error("Error during admin registration", ex);
            
            // ✅ Normalize response time even on error
            normalizeResponseTime(startTime, 150);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Unable to register admin"));
        }
    }
    private void normalizeResponseTime(long startTime, long targetMillis) {
        long elapsedNanos = System.nanoTime() - startTime;
        long elapsedMillis = elapsedNanos / 1_000_000;
        
        // Add random jitter (20-80ms) to make timing analysis harder
        long jitter = 20 + new java.util.Random().nextInt(60);
        long targetTime = targetMillis + jitter;
        
        try {
            if (elapsedMillis < targetTime) {
                Thread.sleep(targetTime - elapsedMillis);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

        @PostMapping("/login")
        public ResponseEntity<?> login(
                @RequestBody Admin admin,
                @RequestParam(required = false) String captchaToken,
                HttpServletRequest httpRequest,
                HttpServletResponse httpResponse
        ) {
            long startTime = System.nanoTime();
            
            try {
                String email = admin.getEmail();
                String password = admin.getPassword();
                String ip = getClientIp(httpRequest);
                
                log.info("Login attempt for email: {} from IP: {}", email, ip);
                
                // ✅ Check if account is locked
                if (accountLockoutService.isLocked(email)) {
                    long remainingMinutes = accountLockoutService.getRemainingLockoutMinutes(email);
                    log.warn("Account locked for email: {} (remaining: {} minutes)", email, remainingMinutes);
                    
                    normalizeResponseTime(startTime, 150);
                    
                    return ResponseEntity.status(423)
                            .body(Map.of(
                                    "message", "Account temporarily locked due to too many failed attempts",
                                    "remainingMinutes", remainingMinutes
                            ));
                }
                
                // ✅ Check if CAPTCHA is required
                boolean captchaRequired = failedAttemptService.requiresCaptcha(ip);
                boolean captchaValid = false;
                
                if (captchaRequired) {
                    log.info("CAPTCHA required for IP: {}", ip);
                    
                    if (captchaToken != null && !captchaToken.isEmpty()) {
                        log.info("Verifying CAPTCHA for IP: {}", ip);
                        captchaValid = captchaService.verifyCaptcha(captchaToken);
                        
                        if (!captchaValid) {
                            log.warn("❌ CAPTCHA verification FAILED for IP: {}", ip);
                        } else {
                            log.info("✅ CAPTCHA verification SUCCESSFUL for IP: {}", ip);
                        }
                    }
                }
                
                Admin existingAdmin = adminRepo.findByEmail(email);
                String hashToCheck = (existingAdmin != null) 
                    ? existingAdmin.getPassword() 
                    : DUMMY_PASSWORD_HASH;
                boolean passwordMatches = passwordEncoder.matches(password, hashToCheck);
                
                if (captchaRequired && !captchaValid) {
                    failedAttemptService.recordFailure(ip);
                    if (existingAdmin != null) {
                        accountLockoutService.recordFailedLogin(email);
                    }
                    
                    log.warn("CAPTCHA required but not provided or invalid for IP: {}", ip);
                    normalizeResponseTime(startTime, 150);
                    
                    return ResponseEntity.badRequest()
                            .body(Map.of(
                                    "message", "CAPTCHA verification required",
                                    "requiresCaptcha", true
                            ));
                }
                
                // ✅ Check if credentials are valid
                boolean isValid = (existingAdmin != null) && passwordMatches;
                
                if (!isValid) {
                    failedAttemptService.recordFailure(ip);
                    accountLockoutService.recordFailedLogin(email);
                    
                    boolean requiresCaptcha = failedAttemptService.requiresCaptcha(ip);
                    
                    log.warn("Failed login attempt for email: {}", email);
                    normalizeResponseTime(startTime, 150);
                    
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of(
                                    "message", "Invalid credentials",
                                    "requiresCaptcha", requiresCaptcha
                            ));
                }
                
                // ✅ Success - reset attempts
                failedAttemptService.resetAttempts(ip);
                accountLockoutService.resetLockout(email);

                String accessToken = jwtUtil.generateToken(email, "ADMIN");
                
                // Generate refresh token (long-lived, httpOnly)
                String userAgent = httpRequest.getHeader("User-Agent");
                RefreshToken refreshToken = refreshTokenService.createRefreshToken(email, "ADMIN", ip, userAgent);
                
                // ✅ Store ACCESS token in httpOnly cookie (SHORT-LIVED: 10 minutes)
                Cookie accessCookie = new Cookie("accessToken", accessToken);
                accessCookie.setHttpOnly(true);
                accessCookie.setSecure(false); // Set to TRUE in production (HTTPS)
                accessCookie.setPath("/");
                accessCookie.setMaxAge(10 * 60);  // ✅ 10 MINUTES (not 10 hours)
                accessCookie.setAttribute("SameSite", "Strict");
                httpResponse.addCookie(accessCookie);
                
                // ✅ Store REFRESH token in httpOnly cookie (LONG-LIVED: 7 days)
                Cookie refreshCookie = new Cookie("refreshToken", refreshToken.getToken());
                refreshCookie.setHttpOnly(true);
                refreshCookie.setSecure(false); // Set to TRUE in production
                refreshCookie.setPath("/");
                refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
                refreshCookie.setAttribute("SameSite", "Strict");
                httpResponse.addCookie(refreshCookie);
                
                // Generate and send CSRF token in HEADER (not body)
                String csrfToken = csrfTokenService.generateToken(refreshToken.getToken());
                httpResponse.setHeader("X-CSRF-Token", csrfToken);
                
                // Create DTO (only include safe fields)
                AdminDTO adminDTO = new AdminDTO(
                    existingAdmin.getId(),
                    existingAdmin.getName(),
                    existingAdmin.getEmail()
                );

                // ✅ ONLY send admin data - CSRF token is in header
                Map<String, Object> response = Map.of(
                    "admin", adminDTO
                    // NO csrfToken in body anymore
                );

                log.info("✅ Admin logged in successfully: {}", email);
                normalizeResponseTime(startTime, 150);
                
                return ResponseEntity.ok(response);
            }
            
            catch (Exception ex) {
                log.error("Error during admin login", ex);
                normalizeResponseTime(startTime, 150);
                
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("message", "Unable to process login"));
            }
        }

@PostMapping("/logout")
public ResponseEntity<?> logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
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
                
                // ✅ Clear BOTH cookies
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
        
        log.info("Admin logged out successfully");
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
        
    } catch (Exception ex) {
        log.error("Error during logout", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Error during logout"));
    }
}

@PostMapping("/refresh")
public ResponseEntity<?> refreshToken(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
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
        
        // Verify refresh token
        Optional<RefreshToken> refreshTokenOpt = refreshTokenService.verifyRefreshToken(refreshTokenValue);
        
        if (refreshTokenOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or expired refresh token"));
        }
        
        RefreshToken refreshToken = refreshTokenOpt.get();
        
        // Generate new access token
        String newAccessToken = jwtUtil.generateToken(refreshToken.getUserEmail(), refreshToken.getUserRole());
        
        // ✅ Store new access token in httpOnly cookie
        Cookie accessCookie = new Cookie("accessToken", newAccessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(false);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(10 * 60 ); // 10 minutes
        accessCookie.setAttribute("SameSite", "Strict");
        httpResponse.addCookie(accessCookie);
        
        // Generate new CSRF token
        String newCsrfToken = csrfTokenService.generateToken(refreshTokenValue);
        httpResponse.setHeader("X-CSRF-Token", newCsrfToken);
        
        log.info("Access token refreshed for user: {}", refreshToken.getUserEmail());
        
        return ResponseEntity.ok(Map.of()); 
        
    } catch (Exception ex) {
        log.error("Error refreshing token", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Error refreshing token"));
    }
}

    @GetMapping("/{id}")
    public ResponseEntity<?> getAdminById(@PathVariable Long id) {
        try {
            Optional<Admin> admin = adminRepo.findById(id.intValue());
            
            if (admin.isEmpty()) {
                log.warn("Admin not found with id: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Admin not found");
            }

            return ResponseEntity.ok(admin.get());

        } catch (Exception ex) {
            log.error("Error fetching admin by id: {}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch admin");
        }
    }
        @PostConstruct
        public void initCleanup() {
            // Clean up old entries every 10 minutes
            cleanupScheduler.scheduleAtFixedRate(() -> {
                LocalDateTime cutoff = LocalDateTime.now().minusMinutes(30);
                recentResetRequests.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));
            }, 10, 10, TimeUnit.MINUTES);
        }
        @PostMapping("/forgot-password")
        public ResponseEntity<?> forgotPassword(
                @RequestParam String email,
                HttpServletRequest httpRequest
        ) {
            long startTime = System.nanoTime();
            
            try {
                String ip = getClientIp(httpRequest);
                String emailLower = email.toLowerCase().trim();
                String requestKey = ip + ":" + emailLower;
                
                // ✅ Check if identical request was made in last 2 minutes
                LocalDateTime lastRequest = recentResetRequests.get(requestKey);
                if (lastRequest != null && lastRequest.isAfter(LocalDateTime.now().minusMinutes(2))) {
                    log.warn("⚠️ Duplicate forgot-password request blocked for {} from {} (within 2min)", emailLower, ip);
                    
                    // Still normalize timing
                    normalizeResponseTime(startTime, 150);
                    
                    // Return same generic message
                    return ResponseEntity.ok(
                        "If an account exists with that email, a password reset link has been sent."
                    );
                }
                
                // Record this request
                recentResetRequests.put(requestKey, LocalDateTime.now());
                
                // ALWAYS perform lookup
                Admin admin = adminRepo.findByEmail(emailLower);
                
                // ALWAYS generate secure token (even for non-existent emails)
                SecureRandom secureRandom = new SecureRandom();
                byte[] tokenBytes = new byte[32];
                secureRandom.nextBytes(tokenBytes);
                String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
                
                // ALWAYS perform expensive operation
                LocalDateTime expiry = LocalDateTime.now().plusMinutes(30);
                passwordEncoder.encode(token); // Normalize timing
                
                if (admin != null) {
                    // Check if user already has a recent valid token (prevents token spam)
                    if (admin.getResetToken() != null && 
                        admin.getResetTokenExpiry() != null &&
                        admin.getResetTokenExpiry().isAfter(LocalDateTime.now().minusMinutes(25))) {
                        
                        log.info("✅ Reset token still valid for {}, reusing existing token", emailLower);
                        
                        // Don't generate new token or send email
                        normalizeResponseTime(startTime, 150);
                        return ResponseEntity.ok(
                            "If an account exists with that email, a password reset link has been sent."
                        );
                    }
                    
                    admin.setResetToken(token);
                    admin.setResetTokenExpiry(expiry);
                    adminRepo.save(admin);
                    
                    // Send email ASYNCHRONOUSLY (non-blocking)
                    final String adminEmail = admin.getEmail();
                    final String tokenToSend = token;
                    CompletableFuture.runAsync(() -> {
                        try {
                            
                            String resetLink = frontendBaseUrl + "/reset-password?token=" +
        URLEncoder.encode(tokenToSend, StandardCharsets.UTF_8);
                            emailService.sendEmail(adminEmail, "Password Reset Request",
                                    "You requested a password reset. Click here to reset your password: " + 
                                    resetLink + "\n\nThis link expires in 30 minutes.\n\n" +
                                    "If you didn't request this, please ignore this email.");
                            log.info("✅ Password reset email sent to: {}", adminEmail);
                        } catch (Exception e) {
                            log.error("❌ Failed to send password reset email to {}", adminEmail, e);
                        }
                    });
                } else {
                    // For non-existent emails, do equivalent database work
                    adminRepo.findByEmail(emailLower + ".dummy");
                    adminRepo.count(); // Match transaction overhead
                    log.warn("⚠️ Password reset attempt for non-existent email: {}", emailLower);
                }
                
            } catch (Exception ex) {
                log.error("❌ Error processing password reset", ex);
            }
            
            // ALWAYS enforce fixed response time with large jitter
            long elapsedNanos = System.nanoTime() - startTime;
            long elapsedMillis = elapsedNanos / 1_000_000;
            
            long baseTime = 150; // Base response time
            long jitter = 50 + new java.util.Random().nextInt(100); // 50-149ms jitter
            long targetTime = baseTime + jitter;
            
            try {
                if (elapsedMillis < targetTime) {
                    Thread.sleep(targetTime - elapsedMillis);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // ALWAYS return the same generic message
            return ResponseEntity.ok(
                "If an account exists with that email, a password reset link has been sent."
            );
        }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            Admin admin = adminRepo.findByResetToken(request.getToken());
            
            if (admin == null) {
                log.warn("Invalid reset token provided");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid or expired token");
            }

            if (admin.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
                log.warn("Expired reset token used for email: {}", admin.getEmail());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid or expired token");
            }

            if(validatePassword(request.getNewPassword()) == false){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Weak Password entered. Password must contain upper, lower, number and special character"));
            }

            admin.setPassword(passwordEncoder.encode(request.getNewPassword()));
            admin.setResetToken(null);
            admin.setResetTokenExpiry(null);
            adminRepo.save(admin);

            log.info("Password successfully reset for: {}", admin.getEmail());
            return ResponseEntity.ok("Password successfully reset");

        } catch (Exception ex) {
            log.error("Error resetting password", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to reset password");
        }
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