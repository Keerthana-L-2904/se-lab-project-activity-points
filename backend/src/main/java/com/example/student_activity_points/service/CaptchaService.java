package com.example.student_activity_points.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.Map;

@Service
public class CaptchaService {
    
    private static final Logger log = LoggerFactory.getLogger(CaptchaService.class);
    
    @Value("${recaptcha.secret}")
    private String recaptchaSecret;
    
    @Value("${recaptcha.verify-url:https://www.google.com/recaptcha/api/siteverify}")
    private String recaptchaVerifyUrl;
    
    @Value("${captcha.test-mode:false}")
    private boolean testMode;
    
    /**
     * Verify CAPTCHA token with Google reCAPTCHA
     * In test mode (testMode=true), bypasses verification for testing
     */
    public boolean verifyCaptcha(String token) {
        // Test mode bypass (only enabled via application properties)
        if (testMode) {
            log.warn("CAPTCHA verification bypassed - TEST MODE ACTIVE");
            return true;
        }
        
        if (token == null || token.isEmpty()) {
            log.warn("CAPTCHA token is null or empty");
            return false;
        }
        
        try {
            RestTemplate restTemplate = new RestTemplate();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            // Build form data
            String formData = "secret=" + recaptchaSecret + "&response=" + token;
            HttpEntity<String> request = new HttpEntity<>(formData, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                recaptchaVerifyUrl, 
                request, 
                Map.class
            );
            
            Map<String, Object> responseBody = response.getBody();
            
            if (responseBody != null && Boolean.TRUE.equals(responseBody.get("success"))) {
                log.info("CAPTCHA verification successful");
                return true;
            } else {
                log.warn("CAPTCHA verification failed: {}", responseBody);
                return false;
            }
            
        } catch (Exception e) {
            log.error("Error verifying CAPTCHA: {}", e.getMessage());
            return false;
        }
    }
}