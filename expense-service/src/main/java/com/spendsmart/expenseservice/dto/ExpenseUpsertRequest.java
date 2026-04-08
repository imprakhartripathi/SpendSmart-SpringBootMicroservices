package com.spendsmart.expenseservice.dto;

import com.spendsmart.expenseservice.enums.ExpenseType;
import com.spendsmart.expenseservice.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseUpsertRequest(
        @NotNull Long userId,
        @NotNull Long categoryId,
        @NotBlank String title,
        @NotNull BigDecimal amount,
        String currency,
        ExpenseType type,
        PaymentMethod paymentMethod,
        LocalDate date,
        String notes,
        String receiptUrl,
        boolean recurring
) {
}
