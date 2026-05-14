package com.spendsmart.recurringservice.service;

import com.spendsmart.recurringservice.domain.RecurringTransaction;
import java.util.List;
import java.util.Map;

public interface RecurringService {
    RecurringTransaction addRecurring(RecurringTransaction recurringTransaction);

    List<RecurringTransaction> getByUser(Long userId);

    RecurringTransaction getById(Long recurringId);

    List<RecurringTransaction> getActiveRecurring(Long userId);

    RecurringTransaction updateRecurring(Long recurringId, RecurringTransaction recurringTransaction);

    RecurringTransaction deactivateRecurring(Long recurringId);

    void deleteRecurring(Long recurringId);

    List<Map<String, Object>> processUpcomingDue();

    RecurringTransaction updateNextDueDate(Long recurringId);

    Map<String, Object> generateTransactionFromRecurring(RecurringTransaction recurringTransaction);

    List<RecurringTransaction> getUpcomingThisMonth(Long userId);
}
