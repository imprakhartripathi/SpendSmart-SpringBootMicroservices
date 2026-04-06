package com.spendsmart.categoryservice.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CategoryBudgetRequest(@NotNull BigDecimal budgetLimit) {
}
