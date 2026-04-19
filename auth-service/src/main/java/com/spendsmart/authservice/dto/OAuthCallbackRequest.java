package com.spendsmart.authservice.dto;

import jakarta.validation.constraints.NotBlank;

public record OAuthCallbackRequest(@NotBlank String code) {
}
