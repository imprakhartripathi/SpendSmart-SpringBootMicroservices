package com.spendsmart.authservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenService {

    private final SecretKey signingKey;
    private final long expiryHours;
    private final Set<String> revokedTokens = ConcurrentHashMap.newKeySet();

    public JwtTokenService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiry-hours:24}") long expiryHours
    ) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes.length < 32
                ? String.format("%-32s", secret).replace(' ', 'x').getBytes(StandardCharsets.UTF_8)
                : keyBytes);
        this.expiryHours = expiryHours;
    }

    public String generateToken(Long userId, String email) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(expiryHours, ChronoUnit.HOURS);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claims(Map.of("email", email))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }

    public boolean isValid(String token) {
        if (token == null || token.isBlank() || revokedTokens.contains(token)) {
            return false;
        }

        try {
            parseClaims(token);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public Long extractUserId(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    public String extractEmail(String token) {
        Object email = parseClaims(token).get("email");
        return email == null ? null : email.toString();
    }

    public void revoke(String token) {
        if (token != null && !token.isBlank()) {
            revokedTokens.add(token);
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
