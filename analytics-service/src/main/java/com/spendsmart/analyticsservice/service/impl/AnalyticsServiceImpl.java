package com.spendsmart.analyticsservice.service.impl;

import com.spendsmart.analyticsservice.client.ExpenseAnalyticsClient;
import com.spendsmart.analyticsservice.client.IncomeAnalyticsClient;
import com.spendsmart.analyticsservice.domain.FinancialSnapshot;
import com.spendsmart.analyticsservice.repository.AnalyticsRepository;
import com.spendsmart.analyticsservice.service.AnalyticsService;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AnalyticsServiceImpl implements AnalyticsService {

    private final AnalyticsRepository analyticsRepository;
    private final ExpenseAnalyticsClient expenseClient;
    private final IncomeAnalyticsClient incomeClient;

    public AnalyticsServiceImpl(
            AnalyticsRepository analyticsRepository,
            ExpenseAnalyticsClient expenseClient,
            IncomeAnalyticsClient incomeClient
    ) {
        this.analyticsRepository = analyticsRepository;
        this.expenseClient = expenseClient;
        this.incomeClient = incomeClient;
    }

    @Override
    public FinancialSnapshot generateMonthlySnapshot(
            Long userId,
            int year,
            int month,
            BigDecimal totalIncome,
            BigDecimal totalExpenses,
            String topCategory
    ) {
        FinancialSnapshot snapshot = analyticsRepository.findByUserIdAndYearAndMonth(userId, year, month)
                .orElseGet(FinancialSnapshot::new);

        BigDecimal netSavings = totalIncome.subtract(totalExpenses);
        BigDecimal savingsRate = savingsRate(totalIncome, totalExpenses);

        snapshot.setUserId(userId);
        snapshot.setYear(year);
        snapshot.setMonth(month);
        snapshot.setPeriod(year + "-" + String.format("%02d", month));
        snapshot.setTotalIncome(totalIncome);
        snapshot.setTotalExpenses(totalExpenses);
        snapshot.setNetSavings(netSavings);
        snapshot.setSavingsRate(savingsRate);
        snapshot.setTopCategory(topCategory);

        return analyticsRepository.save(snapshot);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "analytics-monthly-summary", key = "#userId + ':' + #year + ':' + #month")
    public Map<String, Object> getMonthlySummary(Long userId, int year, int month) {
        BigDecimal income = incomeClient.getTotalByMonth(userId, year, month);
        BigDecimal expenses = expenseClient.getTotalByMonth(userId, year, month);
        BigDecimal netSavings = income.subtract(expenses);
        BigDecimal savingsRate = savingsRate(income, expenses);

        List<Map<String, Object>> topCategories = getTopSpendingCategories(userId, year, month);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", userId);
        result.put("year", year);
        result.put("month", month);
        result.put("totalIncome", income);
        result.put("totalExpenses", expenses);
        result.put("netSavings", netSavings);
        result.put("savingsRate", savingsRate);
        result.put("topCategory", topCategories.isEmpty() ? "Uncategorised" : topCategories.get(0).get("category"));
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "analytics-yearly-summary", key = "#userId + ':' + #year")
    public Map<String, Object> getYearlySummary(Long userId, int year) {
        List<Map<String, Object>> monthlyBreakdown = new ArrayList<>();
        BigDecimal annualIncome = BigDecimal.ZERO;
        BigDecimal annualExpenses = BigDecimal.ZERO;

        for (int month = 1; month <= 12; month++) {
            BigDecimal income = incomeClient.getTotalByMonth(userId, year, month);
            BigDecimal expense = expenseClient.getTotalByMonth(userId, year, month);

            annualIncome = annualIncome.add(income);
            annualExpenses = annualExpenses.add(expense);

            monthlyBreakdown.add(Map.of(
                    "period", year + "-" + String.format("%02d", month),
                    "income", income,
                    "expense", expense,
                    "savings", income.subtract(expense)
            ));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", userId);
        result.put("year", year);
        result.put("annualIncome", annualIncome);
        result.put("annualExpenses", annualExpenses);
        result.put("annualSavings", annualIncome.subtract(annualExpenses));
        result.put("monthlyBreakdown", monthlyBreakdown);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "analytics-category-breakdown", key = "#userId + ':' + #year + ':' + #month")
    public List<Map<String, Object>> getExpenseBreakdownByCategory(Long userId, int year, int month) {
        Map<String, BigDecimal> totals = new LinkedHashMap<>();

        for (Map<String, Object> expense : expenseClient.getExpensesByMonth(userId, year, month)) {
            String key = "category-" + String.valueOf(expense.getOrDefault("categoryId", "unknown"));
            totals.merge(key, toBigDecimal(expense.get("amount")), BigDecimal::add);
        }

        return totals.entrySet().stream()
                .sorted((left, right) -> right.getValue().compareTo(left.getValue()))
                .map(entry -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("category", entry.getKey());
                    row.put("amount", entry.getValue());
                    return row;
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "analytics-income-expense-trend", key = "#userId + ':' + #trailingMonths")
    public List<Map<String, Object>> getIncomeVsExpenseTrend(Long userId, int trailingMonths) {
        return buildTrailingMetrics(userId, trailingMonths).stream()
                .map(metric -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("period", metric.period());
                    row.put("income", metric.income());
                    row.put("expense", metric.expense());
                    return row;
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "analytics-savings-rate-trend", key = "#userId + ':' + #trailingMonths")
    public List<Map<String, Object>> getSavingsRateTrend(Long userId, int trailingMonths) {
        return buildTrailingMetrics(userId, trailingMonths).stream()
                .map(metric -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("period", metric.period());
                    row.put("savingsRate", metric.savingsRate());
                    return row;
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "analytics-top-categories", key = "#userId + ':' + #year + ':' + #month")
    public List<Map<String, Object>> getTopSpendingCategories(Long userId, int year, int month) {
        return getExpenseBreakdownByCategory(userId, year, month).stream()
                .limit(5)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "analytics-daily-trend", key = "#userId + ':' + #year + ':' + #month")
    public List<Map<String, Object>> getDailyExpenseTrend(Long userId, int year, int month) {
        Map<Integer, BigDecimal> dailyTotals = new LinkedHashMap<>();
        int daysInMonth = YearMonth.of(year, month).lengthOfMonth();

        for (int day = 1; day <= daysInMonth; day++) {
            dailyTotals.put(day, BigDecimal.ZERO);
        }

        for (Map<String, Object> expense : expenseClient.getExpensesByMonth(userId, year, month)) {
            Object date = expense.get("date");
            if (date == null) {
                continue;
            }
            int day = LocalDate.parse(date.toString()).getDayOfMonth();
            dailyTotals.merge(day, toBigDecimal(expense.get("amount")), BigDecimal::add);
        }

        return dailyTotals.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("day", entry.getKey());
                    row.put("expense", entry.getValue());
                    return row;
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "analytics-cashflow", key = "#userId + ':' + #trailingMonths")
    public List<Map<String, Object>> getCashflowData(Long userId, int trailingMonths) {
        return buildTrailingMetrics(userId, trailingMonths).stream()
                .map(metric -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("period", metric.period());
                    row.put("inflow", metric.income());
                    row.put("outflow", metric.expense());
                    row.put("net", metric.savings());
                    return row;
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "analytics-forecast", key = "#userId")
    public Map<String, Object> getSpendingForecast(Long userId) {
        List<MonthMetric> metrics = buildTrailingMetrics(userId, 3);
        if (metrics.isEmpty()) {
            return Map.of(
                    "forecast", BigDecimal.ZERO,
                    "method", "weighted-3m-average-with-momentum",
                    "dataPoints", 0
            );
        }

        BigDecimal weightedSum = BigDecimal.ZERO;
        BigDecimal weightTotal = BigDecimal.ZERO;

        for (int i = 0; i < metrics.size(); i++) {
            BigDecimal weight = BigDecimal.valueOf(i + 1);
            weightedSum = weightedSum.add(metrics.get(i).expense().multiply(weight));
            weightTotal = weightTotal.add(weight);
        }

        BigDecimal weightedAverage = weightedSum.divide(weightTotal, 2, RoundingMode.HALF_UP);

        BigDecimal momentum = BigDecimal.ZERO;
        if (metrics.size() > 1) {
            BigDecimal latest = metrics.get(metrics.size() - 1).expense();
            BigDecimal previous = metrics.get(metrics.size() - 2).expense();
            momentum = latest.subtract(previous).multiply(BigDecimal.valueOf(0.3));
        }

        BigDecimal forecast = weightedAverage.add(momentum).max(BigDecimal.ZERO);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("forecast", forecast);
        result.put("weightedAverage", weightedAverage);
        result.put("momentumAdjustment", momentum);
        result.put("method", "weighted-3m-average-with-momentum");
        result.put("dataPoints", metrics.size());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = "analytics-health-score",
            key = "#userId + ':' + (#monthlyBudgetGoal == null ? 'null' : #monthlyBudgetGoal.toPlainString())"
    )
    public Map<String, Object> getFinancialHealthScore(Long userId, BigDecimal monthlyBudgetGoal) {
        List<MonthMetric> metrics = buildTrailingMetrics(userId, 12);
        if (metrics.isEmpty()) {
            throw new EntityNotFoundException("No financial data available for this user");
        }

        BigDecimal totalIncome = metrics.stream().map(MonthMetric::income).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpense = metrics.stream().map(MonthMetric::expense).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgSavingsRate = metrics.stream()
                .map(MonthMetric::savingsRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(metrics.size()), 2, RoundingMode.HALF_UP)
                .max(BigDecimal.ZERO)
                .min(BigDecimal.valueOf(100));

        BigDecimal expenseToIncomeRatio = totalIncome.compareTo(BigDecimal.ZERO) > 0
                ? totalExpense.multiply(BigDecimal.valueOf(100)).divide(totalIncome, 2, RoundingMode.HALF_UP)
                : BigDecimal.valueOf(100);

        BigDecimal expenseControlScore = BigDecimal.valueOf(100).subtract(expenseToIncomeRatio.min(BigDecimal.valueOf(100)));

        BigDecimal budgetAdherence = BigDecimal.valueOf(100);
        if (monthlyBudgetGoal != null && monthlyBudgetGoal.compareTo(BigDecimal.ZERO) > 0) {
            MonthMetric latest = metrics.get(metrics.size() - 1);
            BigDecimal utilization = latest.expense().multiply(BigDecimal.valueOf(100))
                    .divide(monthlyBudgetGoal, 2, RoundingMode.HALF_UP);
            budgetAdherence = BigDecimal.valueOf(100)
                    .subtract(utilization.subtract(BigDecimal.valueOf(100)).max(BigDecimal.ZERO))
                    .max(BigDecimal.ZERO)
                    .min(BigDecimal.valueOf(100));
        }

        BigDecimal score = avgSavingsRate.multiply(BigDecimal.valueOf(0.4))
                .add(budgetAdherence.multiply(BigDecimal.valueOf(0.4)))
                .add(expenseControlScore.multiply(BigDecimal.valueOf(0.2)))
                .setScale(2, RoundingMode.HALF_UP);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("score", score);
        result.put("savingsRateComponent", avgSavingsRate);
        result.put("budgetAdherenceComponent", budgetAdherence);
        result.put("expenseControlComponent", expenseControlScore);
        result.put("weights", Map.of("savingsRate", 0.4, "budgetAdherence", 0.4, "expenseControl", 0.2));
        return result;
    }

    private List<MonthMetric> buildTrailingMetrics(Long userId, int trailingMonths) {
        if (trailingMonths <= 0) {
            return List.of();
        }

        List<MonthMetric> metrics = new ArrayList<>();
        YearMonth current = YearMonth.now();
        YearMonth start = current.minusMonths(trailingMonths - 1L);

        YearMonth cursor = start;
        while (!cursor.isAfter(current)) {
            BigDecimal income = incomeClient.getTotalByMonth(userId, cursor.getYear(), cursor.getMonthValue());
            BigDecimal expense = expenseClient.getTotalByMonth(userId, cursor.getYear(), cursor.getMonthValue());
            BigDecimal savings = income.subtract(expense);
            BigDecimal savingsRate = income.compareTo(BigDecimal.ZERO) > 0
                    ? savings.multiply(BigDecimal.valueOf(100)).divide(income, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            metrics.add(new MonthMetric(
                    cursor.toString(),
                    income,
                    expense,
                    savings,
                    savingsRate
            ));
            cursor = cursor.plusMonths(1);
        }

        return metrics;
    }

    private BigDecimal savingsRate(BigDecimal income, BigDecimal expenses) {
        if (income.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return income.subtract(expenses)
                .multiply(BigDecimal.valueOf(100))
                .divide(income, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return new BigDecimal(value.toString());
    }

    private record MonthMetric(
            String period,
            BigDecimal income,
            BigDecimal expense,
            BigDecimal savings,
            BigDecimal savingsRate
    ) {
    }
}
