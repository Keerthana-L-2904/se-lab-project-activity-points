package com.example.student_activity_points.security;

import com.example.student_activity_points.filters.LoginRateLimitFilter;
import com.example.student_activity_points.filters.LoginRateLimitFilterStudFa;
import com.example.student_activity_points.filters.CsrfFilter;
 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Autowired
    private LoginRateLimitFilter loginRateLimitFilter; 

    @Autowired
    private CsrfFilter csrfFilter;

    @Autowired
    private LoginRateLimitFilterStudFa loginRateLimitFilterStudFa;

    private static final String CSP_POLICY_DEV =
        "default-src 'self'; " +
        "script-src 'self' 'unsafe-inline' https://accounts.google.com https://apis.google.com https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; " +
        "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; " +
        "font-src 'self' https://fonts.gstatic.com https://cdn.jsdelivr.net; " +
        "img-src 'self' data: https://lh3.googleusercontent.com https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; " +
        "connect-src 'self' http://localhost:8080 http://localtest.me:8080 http://127.0.0.1:8080 http://localhost:5173 http://localtest.me:5173 http://127.0.0.1:5173 https://accounts.google.com https://oauth2.googleapis.com; " +
        "frame-ancestors 'none'; " +
        "object-src 'none'; " +
        "base-uri 'self'; " +
        "form-action 'self';";

    private static final String CSP_POLICY = CSP_POLICY_DEV;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives(CSP_POLICY))
                .frameOptions(frame -> frame.deny())
                .xssProtection(xss ->
                    xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                )
                .addHeaderWriter((request, response) -> {
                    response.setHeader("X-Content-Type-Options", "nosniff");
                    response.setHeader("Referrer-Policy", "no-referrer");
                    response.setHeader("Permissions-Policy", "geolocation=(), microphone=()");
                    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                    response.setHeader("Pragma", "no-cache");
                    response.setHeader("Expires", "0");
                })
            )

            .authorizeHttpRequests(auth -> auth
                // ✅ Allow login endpoints (no auth required)
                .requestMatchers("/api/auth/login-student").permitAll()
                .requestMatchers("/api/auth/login-fa").permitAll()
                .requestMatchers("/api/auth/refresh").permitAll()  // ✅ Allow refresh for Student/FA
                .requestMatchers("/api/auth/logout").permitAll()   // ✅ Allow logout for Student/FA
                .requestMatchers("/api/auth/**").permitAll()
                // ✅ Admin endpoints
                .requestMatchers("/api/admin/login").permitAll()
                .requestMatchers("/api/admin/forgot-password").permitAll()
                .requestMatchers("/api/admin/reset-password").permitAll()
                .requestMatchers("/api/admin/refresh").permitAll()
                .requestMatchers("/api/admin/logout").permitAll()
                
                .requestMatchers("/error").permitAll()
                
                // ✅ Protected endpoints (require authentication)
                .requestMatchers("/api/student/**").hasRole("STUDENT")
                .requestMatchers("/api/fa/**").hasRole("FA")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                .anyRequest().authenticated()
            )

            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        // ✅ Add filters in correct order
        http.addFilterBefore(
            csrfFilter,
            UsernamePasswordAuthenticationFilter.class
        );

        http.addFilterBefore(
            loginRateLimitFilterStudFa,
            UsernamePasswordAuthenticationFilter.class
        );
    
        http.addFilterBefore(
            loginRateLimitFilter,
            UsernamePasswordAuthenticationFilter.class
        );

        http.addFilterBefore(
            jwtRequestFilter,
            UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                "http://localtest.me:5173"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of(
                "Content-Type",
                "Authorization",
                "X-Requested-With",
                "X-CSRF-Token"
        ));
        configuration.addExposedHeader("Authorization");
        configuration.addExposedHeader("X-CSRF-Token");  // ✅ Expose CSRF token header
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}