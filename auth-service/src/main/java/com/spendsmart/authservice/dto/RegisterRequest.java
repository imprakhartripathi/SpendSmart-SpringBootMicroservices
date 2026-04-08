package com.spendsmart.authservice.dto;

import com.spendsmart.authservice.enums.AuthProvider;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record RegisterRequest(
        @NotBlank String fullName,
        @NotBlank @Email String email,
        @NotBlank String password,
        String currency,
        String timezone,
        String avatarUrl,
        String bio,
        AuthProvider provider,
        BigDecimal monthlyBudget
) {
}
