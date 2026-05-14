package com.spendsmart.incomeservice.dto;

import com.spendsmart.incomeservice.enums.IncomeSource;
import com.spendsmart.incomeservice.enums.RecurrencePeriod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record IncomeUpsertRequest(
        @NotNull Long userId,
        @NotNull Long categoryId,
        @NotBlank String title,
        @NotNull BigDecimal amount,
        String currency,
        IncomeSource source,
        LocalDate date,
        String notes,
        boolean recurring,
        RecurrencePeriod recurrencePeriod
) {
}
