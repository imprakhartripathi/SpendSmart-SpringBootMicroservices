package com.spendsmart.analyticsservice.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record MonthlySnapshotRequest(
        @NotNull Long userId,
        @NotNull Integer year,
        @NotNull Integer month,
        @NotNull BigDecimal totalIncome,
        @NotNull BigDecimal totalExpenses,
        String topCategory
) {
}
