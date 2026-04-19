package com.spendsmart.recurringservice.service.impl;

import com.spendsmart.recurringservice.client.AuthClient;
import com.spendsmart.recurringservice.client.TransactionClient;
import com.spendsmart.recurringservice.domain.RecurringTransaction;
import com.spendsmart.recurringservice.enums.RecurringFrequency;
import com.spendsmart.recurringservice.enums.RecurringPaymentMethod;
import com.spendsmart.recurringservice.enums.RecurringTransactionType;
import com.spendsmart.recurringservice.messaging.NotificationEventPublisher;
import com.spendsmart.recurringservice.repository.RecurringRepository;
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
class RecurringServiceImplTest {

    @Mock
    private RecurringRepository recurringRepository;

    @Mock
    private TransactionClient transactionClient;

    @Mock
    private AuthClient authClient;

    @Mock
    private NotificationEventPublisher notificationEventPublisher;

    @InjectMocks
    private RecurringServiceImpl recurringService;

    @Test
    void updateNextDueDateAdvancesTheSchedule() {
        RecurringTransaction recurring = new RecurringTransaction();
        recurring.setRecurringId(1L);
        recurring.setUserId(7L);
        recurring.setCategoryId(3L);
        recurring.setTitle("Subscription");
        recurring.setAmount(new BigDecimal("499"));
        recurring.setType(RecurringTransactionType.EXPENSE);
        recurring.setFrequency(RecurringFrequency.MONTHLY);
        recurring.setStartDate(LocalDate.of(2026, 4, 18));
        recurring.setNextDueDate(LocalDate.of(2026, 4, 18));
        recurring.setPaymentMethod(RecurringPaymentMethod.CARD);
        recurring.setActive(true);

        when(recurringRepository.findByRecurringId(1L)).thenReturn(Optional.of(recurring));
        when(recurringRepository.save(any(RecurringTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RecurringTransaction updated = recurringService.updateNextDueDate(1L);

        assertThat(updated.getNextDueDate()).isEqualTo(LocalDate.of(2026, 5, 18));
    }
}
