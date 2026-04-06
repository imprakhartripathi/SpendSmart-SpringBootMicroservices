package com.spendsmart.categoryservice.dto;

import com.spendsmart.categoryservice.enums.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoryUpsertRequest(
        @NotNull Long userId,
        @NotBlank String name,
        @NotNull CategoryType type,
        String icon,
        String colorCode,
        boolean defaultCategory
) {
}
