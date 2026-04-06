package com.spendsmart.recurringservice.repository;

import com.spendsmart.recurringservice.domain.RecurringTransaction;
import com.spendsmart.recurringservice.enums.RecurringFrequency;
import com.spendsmart.recurringservice.enums.RecurringTransactionType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecurringRepository extends JpaRepository<RecurringTransaction, Long> {
    List<RecurringTransaction> findByUserId(Long userId);

    List<RecurringTransaction> findByUserIdAndType(Long userId, RecurringTransactionType type);

    List<RecurringTransaction> findByUserIdAndIsActive(Long userId, boolean isActive);

    List<RecurringTransaction> findByNextDueDateAndIsActive(LocalDate date, boolean isActive);

    List<RecurringTransaction> findByNextDueDateBeforeAndIsActive(LocalDate date, boolean isActive);

    Optional<RecurringTransaction> findByRecurringId(Long recurringId);

    List<RecurringTransaction> findByFrequency(RecurringFrequency frequency);

    long countByUserIdAndIsActive(Long userId, boolean isActive);
}
