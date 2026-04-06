package com.spendsmart.budgetservice.dto;

import com.spendsmart.budgetservice.enums.BudgetPeriod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record BudgetUpsertRequest(
        @NotNull Long userId,
        Long categoryId,
        @NotBlank String name,
        @NotNull BigDecimal limitAmount,
        String currency,
        @NotNull BudgetPeriod period,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal alertThreshold,
        Boolean active
) {
}
