package com.spendsmart.authservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenService.class);
    private static final String REDIS_REVOKED_TOKEN_PREFIX = "auth:revoked:";

    private final SecretKey signingKey;
    private final long expiryHours;
    private final StringRedisTemplate stringRedisTemplate;
    private final Set<String> fallbackRevokedTokens = ConcurrentHashMap.newKeySet();

    public JwtTokenService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiry-hours:24}") long expiryHours,
            ObjectProvider<StringRedisTemplate> stringRedisTemplateProvider
    ) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes.length < 32
                ? String.format("%-32s", secret).replace(' ', 'x').getBytes(StandardCharsets.UTF_8)
                : keyBytes);
        this.expiryHours = expiryHours;
        this.stringRedisTemplate = stringRedisTemplateProvider.getIfAvailable();
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
        if (token == null || token.isBlank()) {
            return false;
        }
        if (fallbackRevokedTokens.contains(token) || isRevokedInRedis(token)) {
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
            fallbackRevokedTokens.add(token);
            storeRevokedTokenInRedis(token);
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isRevokedInRedis(String token) {
        if (stringRedisTemplate == null) {
            return false;
        }

        try {
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(revocationKey(token)));
        } catch (Exception exception) {
            LOGGER.warn("Redis revoked-token lookup failed. Falling back to in-memory set. reason={}", exception.getMessage());
            return false;
        }
    }

    private void storeRevokedTokenInRedis(String token) {
        if (stringRedisTemplate == null) {
            return;
        }

        try {
            Instant expiresAt;
            try {
                expiresAt = parseClaims(token).getExpiration().toInstant();
            } catch (Exception ignored) {
                expiresAt = Instant.now().plus(expiryHours, ChronoUnit.HOURS);
            }

            Duration ttl = Duration.between(Instant.now(), expiresAt);
            if (ttl.isNegative() || ttl.isZero()) {
                ttl = Duration.ofMinutes(1);
            }

            stringRedisTemplate.opsForValue().set(revocationKey(token), "1", ttl);
        } catch (Exception exception) {
            LOGGER.warn("Redis token revoke write failed. Falling back to in-memory set. reason={}", exception.getMessage());
        }
    }

    private String revocationKey(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return REDIS_REVOKED_TOKEN_PREFIX + hex;
        } catch (Exception ignored) {
            return REDIS_REVOKED_TOKEN_PREFIX + token;
        }
    }
}
