package com.spendsmart.expenseservice.service.impl;

import com.spendsmart.expenseservice.client.AuthClient;
import com.spendsmart.expenseservice.client.BudgetClient;
import com.spendsmart.expenseservice.client.IncomeClient;
import com.spendsmart.expenseservice.domain.Expense;
import com.spendsmart.expenseservice.enums.ExpenseType;
import com.spendsmart.expenseservice.enums.PaymentMethod;
import com.spendsmart.expenseservice.messaging.NotificationEventPublisher;
import com.spendsmart.expenseservice.repository.ExpenseRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceImplTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private BudgetClient budgetClient;

    @Mock
    private AuthClient authClient;

    @Mock
    private IncomeClient incomeClient;

    @Mock
    private NotificationEventPublisher notificationEventPublisher;

    private ExpenseServiceImpl expenseService;

    private ExpenseServiceImpl buildService() {
        return new ExpenseServiceImpl(
                expenseRepository,
                budgetClient,
                authClient,
                incomeClient,
                notificationEventPublisher,
                new BigDecimal("20"),
                6
        );
    }

    @Test
    void addExpenseUpdatesLinkedBudgets() {
        Expense expense = new Expense();
        expense.setUserId(7L);
        expense.setCategoryId(3L);
        expense.setTitle("Groceries");
        expense.setAmount(new BigDecimal("234.00"));
        expense.setCurrency("INR");
        expense.setType(ExpenseType.EXPENSE);
        expense.setPaymentMethod(PaymentMethod.CARD);
        expense.setDate(LocalDate.of(2026, 4, 18));

        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> {
            Expense saved = invocation.getArgument(0);
            saved.setExpenseId(9L);
            return saved;
        });
        when(budgetClient.getBudgetIdsForCategory(7L, 3L)).thenReturn(List.of(11L, 12L));
        when(incomeClient.getAverageMonthlyIncome(7L, 6)).thenReturn(BigDecimal.ZERO);

        Expense saved = buildService().addExpense(expense);

        assertThat(saved.getExpenseId()).isEqualTo(9L);
        verify(budgetClient).updateSpentAmount(11L, new BigDecimal("234.00"));
        verify(budgetClient).updateSpentAmount(12L, new BigDecimal("234.00"));
        verify(notificationEventPublisher, never()).publishBigExpenseAlert(any(), any(), any(), any(), any());
    }
}
