package com.spendsmart.recurringservice.dto;

import com.spendsmart.recurringservice.enums.RecurringFrequency;
import com.spendsmart.recurringservice.enums.RecurringPaymentMethod;
import com.spendsmart.recurringservice.enums.RecurringTransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record RecurringUpsertRequest(
        @NotNull Long userId,
        @NotNull Long categoryId,
        @NotBlank String title,
        @NotNull BigDecimal amount,
        @NotNull RecurringTransactionType type,
        @NotNull RecurringFrequency frequency,
        @NotNull LocalDate startDate,
        LocalDate endDate,
        LocalDate nextDueDate,
        String description,
        RecurringPaymentMethod paymentMethod,
        Boolean active
) {
}
