package com.spendsmart.analyticsservice.web;

import com.spendsmart.analyticsservice.domain.FinancialSnapshot;
import com.spendsmart.analyticsservice.dto.MonthlySnapshotRequest;
import com.spendsmart.analyticsservice.service.AnalyticsService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics")
public class AnalyticsResource {

    private final AnalyticsService analyticsService;

    public AnalyticsResource(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @PostMapping("/monthlySnapshot")
    @ResponseStatus(HttpStatus.CREATED)
    public FinancialSnapshot generateMonthlySnapshot(@Valid @RequestBody MonthlySnapshotRequest request) {
        return analyticsService.generateMonthlySnapshot(
                request.userId(),
                request.year(),
                request.month(),
                request.totalIncome(),
                request.totalExpenses(),
                request.topCategory()
        );
    }

    @GetMapping("/monthlySummary")
    public Map<String, Object> getMonthlySummary(@RequestParam Long userId, @RequestParam int year, @RequestParam int month) {
        return analyticsService.getMonthlySummary(userId, year, month);
    }

    @GetMapping("/yearlySummary")
    public Map<String, Object> getYearlySummary(@RequestParam Long userId, @RequestParam int year) {
        return analyticsService.getYearlySummary(userId, year);
    }

    @GetMapping("/categoryBreakdown")
    public List<Map<String, Object>> getCategoryBreakdown(@RequestParam Long userId, @RequestParam int year, @RequestParam int month) {
        return analyticsService.getExpenseBreakdownByCategory(userId, year, month);
    }

    @GetMapping("/incomeVsExpense")
    public List<Map<String, Object>> getIncomeVsExpense(@RequestParam Long userId, @RequestParam(defaultValue = "12") int trailingMonths) {
        return analyticsService.getIncomeVsExpenseTrend(userId, trailingMonths);
    }

    @GetMapping("/dailyTrend")
    public List<Map<String, Object>> getDailyTrend(@RequestParam Long userId, @RequestParam int year, @RequestParam int month) {
        return analyticsService.getDailyExpenseTrend(userId, year, month);
    }

    @GetMapping("/savingsRate")
    public List<Map<String, Object>> getSavingsRate(@RequestParam Long userId, @RequestParam(defaultValue = "12") int trailingMonths) {
        return analyticsService.getSavingsRateTrend(userId, trailingMonths);
    }

    @GetMapping("/topCategories")
    public List<Map<String, Object>> getTopCategories(@RequestParam Long userId, @RequestParam int year, @RequestParam int month) {
        return analyticsService.getTopSpendingCategories(userId, year, month);
    }

    @GetMapping("/cashflow")
    public List<Map<String, Object>> getCashflow(@RequestParam Long userId, @RequestParam(defaultValue = "12") int trailingMonths) {
        return analyticsService.getCashflowData(userId, trailingMonths);
    }

    @GetMapping("/forecast")
    public Map<String, Object> getForecast(@RequestParam Long userId) {
        return analyticsService.getSpendingForecast(userId);
    }

    @GetMapping("/healthScore")
    public Map<String, Object> getHealthScore(@RequestParam Long userId, @RequestParam(required = false) BigDecimal monthlyBudgetGoal) {
        return analyticsService.getFinancialHealthScore(userId, monthlyBudgetGoal);
    }
}
