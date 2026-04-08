package com.spendsmart.analyticsservice.service;

import com.spendsmart.analyticsservice.domain.FinancialSnapshot;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface AnalyticsService {
    FinancialSnapshot generateMonthlySnapshot(Long userId, int year, int month, BigDecimal totalIncome, BigDecimal totalExpenses, String topCategory);

    Map<String, Object> getMonthlySummary(Long userId, int year, int month);

    Map<String, Object> getYearlySummary(Long userId, int year);

    List<Map<String, Object>> getExpenseBreakdownByCategory(Long userId, int year, int month);

    List<Map<String, Object>> getIncomeVsExpenseTrend(Long userId, int trailingMonths);

    List<Map<String, Object>> getSavingsRateTrend(Long userId, int trailingMonths);

    List<Map<String, Object>> getTopSpendingCategories(Long userId, int year, int month);

    List<Map<String, Object>> getDailyExpenseTrend(Long userId, int year, int month);

    List<Map<String, Object>> getCashflowData(Long userId, int trailingMonths);

    Map<String, Object> getSpendingForecast(Long userId);

    Map<String, Object> getFinancialHealthScore(Long userId, BigDecimal monthlyBudgetGoal);
}
