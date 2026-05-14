package com.spendsmart.budgetservice.service.impl;

import com.spendsmart.budgetservice.client.NotificationClient;
import com.spendsmart.budgetservice.domain.Budget;
import com.spendsmart.budgetservice.enums.BudgetPeriod;
import com.spendsmart.budgetservice.repository.BudgetRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetServiceImplTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private BudgetServiceImpl budgetService;

    @Test
    void updateSpentAmountTriggersAlert() {
        Budget budget = new Budget();
        budget.setBudgetId(1L);
        budget.setUserId(7L);
        budget.setName("Home");
        budget.setLimitAmount(new BigDecimal("100"));
        budget.setSpentAmount(BigDecimal.ZERO);
        budget.setCurrency("INR");
        budget.setPeriod(BudgetPeriod.MONTHLY);
        budget.setStartDate(LocalDate.of(2026, 4, 1));
        budget.setEndDate(LocalDate.of(2026, 4, 30));
        budget.setAlertThreshold(new BigDecimal("80"));
        budget.setActive(true);

        when(budgetRepository.findByBudgetId(1L)).thenReturn(Optional.of(budget));
        when(budgetRepository.save(any(Budget.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Budget saved = budgetService.updateSpentAmount(1L, new BigDecimal("90"));

        assertThat(saved.getSpentAmount()).isEqualByComparingTo("90");
        verify(notificationClient).sendBudgetAlert(7L, 1L, "Home", new BigDecimal("90.00"), false);
    }
}
