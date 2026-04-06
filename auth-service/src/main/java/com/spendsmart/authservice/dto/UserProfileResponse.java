package com.spendsmart.authservice.dto;

import com.spendsmart.authservice.enums.AuthProvider;
import java.math.BigDecimal;
import java.time.Instant;

public record UserProfileResponse(
        Long userId,
        String fullName,
        String email,
        String currency,
        String timezone,
        String avatarUrl,
        String bio,
        AuthProvider provider,
        boolean active,
        Instant createdAt,
        BigDecimal monthlyBudget
) {
}
