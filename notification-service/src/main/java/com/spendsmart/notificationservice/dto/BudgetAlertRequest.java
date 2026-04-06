package com.spendsmart.notificationservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record BudgetAlertRequest(
        @NotNull Long recipientId,
        @NotNull Long budgetId,
        @NotBlank String budgetName,
        @NotNull BigDecimal percentageUsed,
        boolean exceeded
) {
}
