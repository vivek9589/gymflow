package com.gymflow.gymflow.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private final long EXPIRATION = 1000 * 60 * 60 * 10; // 10 hours

    // In 0.12+, we use SecretKey specifically
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String email, Long gymId,String gymName,String ownerName, String role) {
        return Jwts.builder()
                .subject(email) // .setSubject is now .subject
                .claim("gymId", gymId)
                .claim("gymName",gymName)
                .claim("ownerName",ownerName)
                .claim("role", role)
                .issuedAt(new Date()) // .setIssuedAt is now .issuedAt
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getSigningKey()) // Algorithm is detected automatically
                .compact();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extractGymId(String token) {
        return extractAllClaims(token).get("gymId", Long.class);
    }

    public boolean validateToken(String token) {
        try {
            // .parserBuilder().build() is now .parser().verifyWith().build()
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token) // parseClaimsJws is now parseSignedClaims
                .getPayload(); // getBody is now getPayload
    }
}