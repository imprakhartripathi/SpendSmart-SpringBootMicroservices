package com.spendsmart.analyticsservice.service.impl;

import com.spendsmart.analyticsservice.client.ExpenseAnalyticsClient;
import com.spendsmart.analyticsservice.client.IncomeAnalyticsClient;
import com.spendsmart.analyticsservice.domain.FinancialSnapshot;
import com.spendsmart.analyticsservice.repository.AnalyticsRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {

    @Mock
    private AnalyticsRepository analyticsRepository;

    @Mock
    private ExpenseAnalyticsClient expenseClient;

    @Mock
    private IncomeAnalyticsClient incomeClient;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    @Test
    void generateMonthlySnapshotPersistsComputedValues() {
        when(analyticsRepository.findByUserIdAndYearAndMonth(7L, 2026, 4)).thenReturn(Optional.empty());
        when(analyticsRepository.save(any(FinancialSnapshot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FinancialSnapshot snapshot = analyticsService.generateMonthlySnapshot(
                7L,
                2026,
                4,
                new BigDecimal("5000"),
                new BigDecimal("1200"),
                "category-1"
        );

        assertThat(snapshot.getPeriod()).isEqualTo("2026-04");
        assertThat(snapshot.getNetSavings()).isEqualByComparingTo("3800");
        assertThat(snapshot.getSavingsRate()).isEqualByComparingTo("76.00");
    }
}
