package com.spendsmart.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ProfileUpdateRequest(
        @NotBlank String fullName,
        @NotBlank @Email String email,
        String avatarUrl,
        String bio,
        String timezone
) {
}
