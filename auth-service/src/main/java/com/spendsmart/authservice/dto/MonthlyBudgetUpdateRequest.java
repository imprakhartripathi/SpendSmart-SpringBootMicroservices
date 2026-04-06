package com.spendsmart.authservice.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record MonthlyBudgetUpdateRequest(@NotNull BigDecimal monthlyBudget) {
}
