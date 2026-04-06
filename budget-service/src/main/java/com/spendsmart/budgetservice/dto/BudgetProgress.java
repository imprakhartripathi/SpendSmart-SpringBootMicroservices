package com.spendsmart.budgetservice.dto;

import java.math.BigDecimal;

public record BudgetProgress(
        Long budgetId,
        BigDecimal limitAmount,
        BigDecimal spentAmount,
        BigDecimal percentageUsed,
        BigDecimal remainingAmount,
        boolean thresholdReached,
        boolean exceeded
) {
}
