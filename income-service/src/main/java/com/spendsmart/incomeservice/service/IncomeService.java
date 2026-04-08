package com.spendsmart.incomeservice.service;

import com.spendsmart.incomeservice.domain.Income;
import com.spendsmart.incomeservice.enums.IncomeSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface IncomeService {
    Income addIncome(Income income);

    Income getIncomeById(Long incomeId);

    List<Income> getIncomesByUser(Long userId);

    List<Income> getIncomesBySource(Long userId, IncomeSource source);

    List<Income> getIncomesByDateRange(Long userId, LocalDate startDate, LocalDate endDate);

    List<Income> getIncomesByMonth(Long userId, int year, int month);

    Income updateIncome(Long incomeId, Income updated);

    void deleteIncome(Long incomeId);

    BigDecimal getTotalIncomeByUser(Long userId);

    BigDecimal getTotalIncomeByMonth(Long userId, int year, int month);

    List<Income> getRecurringIncomes(Long userId);

    List<Income> searchIncome(Long userId, String keyword);
}
