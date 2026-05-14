package com.spendsmart.expenseservice.service.impl;

import com.spendsmart.expenseservice.client.AuthClient;
import com.spendsmart.expenseservice.client.BudgetClient;
import com.spendsmart.expenseservice.client.IncomeClient;
import com.spendsmart.expenseservice.domain.Expense;
import com.spendsmart.expenseservice.enums.ExpenseType;
import com.spendsmart.expenseservice.messaging.NotificationEventPublisher;
import com.spendsmart.expenseservice.repository.ExpenseRepository;
import com.spendsmart.expenseservice.service.ExpenseService;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ExpenseServiceImpl implements ExpenseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpenseServiceImpl.class);

    private final ExpenseRepository expenseRepository;
    private final BudgetClient budgetClient;
    private final AuthClient authClient;
    private final IncomeClient incomeClient;
    private final NotificationEventPublisher notificationEventPublisher;
    private final BigDecimal bigExpenseThresholdPercent;
    private final int bigExpenseAverageMonths;

    public ExpenseServiceImpl(
            ExpenseRepository expenseRepository,
            BudgetClient budgetClient,
            AuthClient authClient,
            IncomeClient incomeClient,
            NotificationEventPublisher notificationEventPublisher,
            @Value("${app.rules.big-expense-threshold-percent:20}") BigDecimal bigExpenseThresholdPercent,
            @Value("${app.rules.big-expense-average-months:6}") int bigExpenseAverageMonths
    ) {
        this.expenseRepository = expenseRepository;
        this.budgetClient = budgetClient;
        this.authClient = authClient;
        this.incomeClient = incomeClient;
        this.notificationEventPublisher = notificationEventPublisher;
        this.bigExpenseThresholdPercent = bigExpenseThresholdPercent;
        this.bigExpenseAverageMonths = bigExpenseAverageMonths;
    }

    @Override
    public Expense addExpense(Expense expense) {
        Expense saved = expenseRepository.save(expense);
        applyBudgetDelta(saved.getUserId(), saved.getCategoryId(), saved.getAmount());
        maybePublishBigExpenseAlert(saved);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Expense getExpenseById(Long expenseId) {
        return expenseRepository.findByExpenseId(expenseId)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Expense> getExpensesByUser(Long userId) {
        return expenseRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Expense> getExpensesByCategory(Long categoryId) {
        return expenseRepository.findByCategoryId(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Expense> getExpensesByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Expense> getExpensesByMonth(Long userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        return expenseRepository.findByUserIdAndDateBetween(userId, yearMonth.atDay(1), yearMonth.atEndOfMonth());
    }

    @Override
    public Expense updateExpense(Long expenseId, Expense updated) {
        Expense existing = getExpenseById(expenseId);

        Long oldUserId = existing.getUserId();
        Long oldCategoryId = existing.getCategoryId();
        BigDecimal oldAmount = existing.getAmount();

        existing.setUserId(updated.getUserId());
        existing.setTitle(updated.getTitle());
        existing.setAmount(updated.getAmount());
        existing.setCategoryId(updated.getCategoryId());
        existing.setCurrency(updated.getCurrency());
        existing.setType(updated.getType());
        existing.setPaymentMethod(updated.getPaymentMethod());
        existing.setDate(updated.getDate());
        existing.setNotes(updated.getNotes());
        existing.setReceiptUrl(updated.getReceiptUrl());
        existing.setRecurring(updated.isRecurring());

        Expense saved = expenseRepository.save(existing);

        if (oldUserId.equals(saved.getUserId()) && oldCategoryId.equals(saved.getCategoryId())) {
            BigDecimal delta = saved.getAmount().subtract(oldAmount);
            applyBudgetDelta(saved.getUserId(), saved.getCategoryId(), delta);
        } else {
            applyBudgetDelta(oldUserId, oldCategoryId, oldAmount.negate());
            applyBudgetDelta(saved.getUserId(), saved.getCategoryId(), saved.getAmount());
        }

        maybePublishBigExpenseAlert(saved);
        return saved;
    }

    @Override
    public void deleteExpense(Long expenseId) {
        Expense existing = getExpenseById(expenseId);
        expenseRepository.deleteByExpenseId(expenseId);
        applyBudgetDelta(existing.getUserId(), existing.getCategoryId(), existing.getAmount().negate());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalByUser(Long userId) {
        return expenseRepository.sumAmountByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalByMonth(Long userId, int year, int month) {
        return getExpensesByMonth(userId, year, month).stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalByCategory(Long userId, Long categoryId) {
        return expenseRepository.sumAmountByUserIdAndCategoryId(userId, categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Expense> getExpensesByType(Long userId, ExpenseType type) {
        return expenseRepository.findByUserIdAndType(userId, type);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Expense> searchExpenses(Long userId, String keyword) {
        String searchTerm = keyword == null ? "" : keyword;
        return expenseRepository.findByUserIdAndTitleContainingIgnoreCaseOrUserIdAndNotesContainingIgnoreCase(
                userId,
                searchTerm,
                userId,
                searchTerm
        );
    }

    private void applyBudgetDelta(Long userId, Long categoryId, BigDecimal delta) {
        if (delta == null || delta.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        List<Long> budgetIds = budgetClient.getBudgetIdsForCategory(userId, categoryId);
        for (Long budgetId : budgetIds) {
            budgetClient.updateSpentAmount(budgetId, delta);
        }
    }

    private void maybePublishBigExpenseAlert(Expense expense) {
        if (expense.getType() != ExpenseType.EXPENSE || expense.getAmount() == null) {
            return;
        }
        if (bigExpenseThresholdPercent == null || bigExpenseThresholdPercent.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        int trailingMonths = bigExpenseAverageMonths <= 0 ? 6 : bigExpenseAverageMonths;
        BigDecimal averageIncome = incomeClient.getAverageMonthlyIncome(expense.getUserId(), trailingMonths);
        if (averageIncome.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        BigDecimal thresholdAmount = averageIncome
                .multiply(bigExpenseThresholdPercent)
                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);

        if (expense.getAmount().compareTo(thresholdAmount) <= 0) {
            return;
        }

        authClient.getUserSummary(expense.getUserId()).ifPresentOrElse(
                user -> notificationEventPublisher.publishBigExpenseAlert(
                        expense,
                        averageIncome,
                        thresholdAmount,
                        user.fullName(),
                        user.email()
                ),
                () -> LOGGER.warn("Skipping big-expense alert because user profile was unavailable for userId={}", expense.getUserId())
        );
    }
}
