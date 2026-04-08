package com.spendsmart.budgetservice.service;

import com.spendsmart.budgetservice.domain.Budget;
import com.spendsmart.budgetservice.dto.BudgetProgress;
import java.math.BigDecimal;
import java.util.List;

public interface BudgetService {
    Budget createBudget(Budget budget);

    Budget getBudgetById(Long budgetId);

    List<Budget> getBudgetsByUser(Long userId);

    List<Budget> getActiveBudgets(Long userId);

    Budget updateBudget(Long budgetId, Budget budget);

    void deleteBudget(Long budgetId);

    Budget updateSpentAmount(Long budgetId, BigDecimal deltaAmount);

    BudgetProgress getBudgetProgress(Long budgetId);

    List<BudgetProgress> checkBudgetAlerts(Long userId);

    List<BudgetProgress> dispatchBudgetAlerts(Long userId);

    List<Budget> resetBudgetPeriod();

    List<Budget> getBudgetsByCategory(Long userId, Long categoryId);
}
