package com.example.student_activity_points.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${myapp.secret-key}")
    private String SECRET_KEY;

    private static final long TOKEN_VALIDITY =
            1000L * 60 * 60 * 10; // 10 hours

    /* ===================== CORE ===================== */

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    /* ===================== COMMON EXTRACTORS ===================== */

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }


    public String generateToken(String username, String role) { 
        Map<String, Object> claims = new HashMap<>(); 
        claims.put("role", role); 
        
        return createToken(claims, username); }

    /* ===================== STUDENT ===================== */

    public String generateStudentToken(String email, String sid) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "STUDENT");
        claims.put("sid", sid);

        return createToken(claims, email);
    }

    public String extractStudentSid(String token) {
        return extractAllClaims(token).get("sid", String.class);
    }

    /* ===================== FA ===================== */

    public String generateFaToken(String email, Long faid) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "FA");
        claims.put("faid", faid);

        return createToken(claims, email);
    }

    public Long extractFaId(String token) {
        return extractAllClaims(token).get("faid", Long.class);
    }

    /* ===================== TOKEN CREATION ===================== */

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)          // email
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + TOKEN_VALIDITY))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

        public boolean validateToken(String token, String expectedEmail) {
            return extractEmail(token).equals(expectedEmail)
                    && !isTokenExpired(token);
        }

        // ✅ NEW - Add this simpler version
        public boolean validateToken(String token) {
            try {
                return !isTokenExpired(token);
            } catch (Exception e) {
                return false;
            }
        }

        // ✅ NEW - Safe extraction methods that handle errors
        public String getEmailFromToken(String token) {
            try {
                return extractEmail(token);
            } catch (Exception e) {
                return null;
            }
        }

        public String getRoleFromToken(String token) {
            try {
                return extractRole(token);
            } catch (Exception e) {
                return null;
            }
        }

}
