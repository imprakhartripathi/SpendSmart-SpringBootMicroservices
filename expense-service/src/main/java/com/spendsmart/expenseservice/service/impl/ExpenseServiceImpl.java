package com.spendsmart.expenseservice.service.impl;

import com.spendsmart.expenseservice.client.BudgetClient;
import com.spendsmart.expenseservice.domain.Expense;
import com.spendsmart.expenseservice.enums.ExpenseType;
import com.spendsmart.expenseservice.repository.ExpenseRepository;
import com.spendsmart.expenseservice.service.ExpenseService;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final BudgetClient budgetClient;

    public ExpenseServiceImpl(ExpenseRepository expenseRepository, BudgetClient budgetClient) {
        this.expenseRepository = expenseRepository;
        this.budgetClient = budgetClient;
    }

    @Override
    public Expense addExpense(Expense expense) {
        Expense saved = expenseRepository.save(expense);
        applyBudgetDelta(saved.getUserId(), saved.getCategoryId(), saved.getAmount());
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
}
