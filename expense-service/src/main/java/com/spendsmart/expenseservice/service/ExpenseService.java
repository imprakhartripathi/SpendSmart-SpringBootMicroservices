package com.spendsmart.expenseservice.service;

import com.spendsmart.expenseservice.domain.Expense;
import com.spendsmart.expenseservice.enums.ExpenseType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseService {
    Expense addExpense(Expense expense);

    Expense getExpenseById(Long expenseId);

    List<Expense> getExpensesByUser(Long userId);

    List<Expense> getExpensesByCategory(Long categoryId);

    List<Expense> getExpensesByDateRange(Long userId, LocalDate startDate, LocalDate endDate);

    List<Expense> getExpensesByMonth(Long userId, int year, int month);

    Expense updateExpense(Long expenseId, Expense updated);

    void deleteExpense(Long expenseId);

    BigDecimal getTotalByUser(Long userId);

    BigDecimal getTotalByMonth(Long userId, int year, int month);

    BigDecimal getTotalByCategory(Long userId, Long categoryId);

    List<Expense> getExpensesByType(Long userId, ExpenseType type);

    List<Expense> searchExpenses(Long userId, String keyword);
}
