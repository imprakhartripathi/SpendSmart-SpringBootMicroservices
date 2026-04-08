package com.spendsmart.incomeservice.service.impl;

import com.spendsmart.incomeservice.domain.Income;
import com.spendsmart.incomeservice.enums.IncomeSource;
import com.spendsmart.incomeservice.repository.IncomeRepository;
import com.spendsmart.incomeservice.service.IncomeService;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class IncomeServiceImpl implements IncomeService {

    private final IncomeRepository incomeRepository;

    public IncomeServiceImpl(IncomeRepository incomeRepository) {
        this.incomeRepository = incomeRepository;
    }

    @Override
    public Income addIncome(Income income) {
        return incomeRepository.save(income);
    }

    @Override
    @Transactional(readOnly = true)
    public Income getIncomeById(Long incomeId) {
        return incomeRepository.findByIncomeId(incomeId)
                .orElseThrow(() -> new EntityNotFoundException("Income not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Income> getIncomesByUser(Long userId) {
        return incomeRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Income> getIncomesBySource(Long userId, IncomeSource source) {
        return incomeRepository.findByUserIdAndSource(userId, source);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Income> getIncomesByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return incomeRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Income> getIncomesByMonth(Long userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        return incomeRepository.findByUserIdAndDateBetween(userId, yearMonth.atDay(1), yearMonth.atEndOfMonth());
    }

    @Override
    public Income updateIncome(Long incomeId, Income updated) {
        Income existing = getIncomeById(incomeId);
        existing.setTitle(updated.getTitle());
        existing.setAmount(updated.getAmount());
        existing.setCategoryId(updated.getCategoryId());
        existing.setCurrency(updated.getCurrency());
        existing.setSource(updated.getSource());
        existing.setDate(updated.getDate());
        existing.setNotes(updated.getNotes());
        existing.setRecurring(updated.isRecurring());
        existing.setRecurrencePeriod(updated.getRecurrencePeriod());
        return incomeRepository.save(existing);
    }

    @Override
    public void deleteIncome(Long incomeId) {
        if (!incomeRepository.existsById(incomeId)) {
            throw new EntityNotFoundException("Income not found");
        }
        incomeRepository.deleteByIncomeId(incomeId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalIncomeByUser(Long userId) {
        return incomeRepository.sumAmountByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalIncomeByMonth(Long userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        return incomeRepository.sumAmountByUserIdAndPeriod(userId, yearMonth.atDay(1), yearMonth.atEndOfMonth());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Income> getRecurringIncomes(Long userId) {
        return incomeRepository.findByUserIdAndIsRecurring(userId, true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Income> searchIncome(Long userId, String keyword) {
        String searchTerm = keyword == null ? "" : keyword;
        return incomeRepository.findByUserIdAndTitleContainingIgnoreCaseOrUserIdAndNotesContainingIgnoreCase(
                userId,
                searchTerm,
                userId,
                searchTerm
        );
    }
}
