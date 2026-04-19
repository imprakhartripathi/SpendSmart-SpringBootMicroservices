package com.spendsmart.incomeservice.service.impl;

import com.spendsmart.incomeservice.domain.Income;
import com.spendsmart.incomeservice.enums.IncomeSource;
import com.spendsmart.incomeservice.repository.IncomeRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncomeServiceImplTest {

    @Mock
    private IncomeRepository incomeRepository;

    @InjectMocks
    private IncomeServiceImpl incomeService;

    @Test
    void addIncomePersistsIncome() {
        Income income = new Income();
        income.setUserId(7L);
        income.setCategoryId(2L);
        income.setTitle("Salary");
        income.setAmount(new BigDecimal("50000"));
        income.setCurrency("INR");
        income.setSource(IncomeSource.SALARY);
        income.setDate(LocalDate.of(2026, 4, 1));
        income.setRecurring(true);

        when(incomeRepository.save(any(Income.class))).thenAnswer(invocation -> {
            Income saved = invocation.getArgument(0);
            saved.setIncomeId(15L);
            return saved;
        });

        Income saved = incomeService.addIncome(income);

        assertThat(saved.getIncomeId()).isEqualTo(15L);
        assertThat(saved.getTitle()).isEqualTo("Salary");
    }

    @Test
    void getAverageMonthlyIncomeUsesTrailingMonths() {
        when(incomeRepository.sumAmountByUserIdAndPeriod(any(), any(), any())).thenReturn(new BigDecimal("6000"));

        BigDecimal average = incomeService.getAverageMonthlyIncome(7L, 6);

        assertThat(average).isEqualByComparingTo("1000.00");
    }

    @Test
    void getIncomeByIdThrowsWhenMissing() {
        when(incomeRepository.findByIncomeId(99L)).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(
                jakarta.persistence.EntityNotFoundException.class,
                () -> incomeService.getIncomeById(99L)
        );
    }
}
