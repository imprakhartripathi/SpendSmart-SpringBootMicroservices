package com.spendsmart.budgetservice.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record SpentAmountUpdateRequest(@NotNull BigDecimal deltaAmount) {
}
