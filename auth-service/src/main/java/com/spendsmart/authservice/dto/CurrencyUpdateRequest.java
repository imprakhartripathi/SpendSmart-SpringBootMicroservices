package com.spendsmart.authservice.dto;

import jakarta.validation.constraints.NotBlank;

public record CurrencyUpdateRequest(@NotBlank String currency) {
}
