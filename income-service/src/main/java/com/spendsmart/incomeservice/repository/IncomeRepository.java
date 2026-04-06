package com.spendsmart.incomeservice.repository;

import com.spendsmart.incomeservice.domain.Income;
import com.spendsmart.incomeservice.enums.IncomeSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface IncomeRepository extends JpaRepository<Income, Long> {
    List<Income> findByUserId(Long userId);

    List<Income> findByUserIdAndSource(Long userId, IncomeSource source);

    List<Income> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    List<Income> findByUserIdAndDate(Long userId, LocalDate date);

    List<Income> findByUserIdAndIsRecurring(Long userId, boolean recurring);

    Optional<Income> findByIncomeId(Long incomeId);

    void deleteByIncomeId(Long incomeId);

    @Query("select coalesce(sum(i.amount), 0) from Income i where i.userId = :userId")
    BigDecimal sumAmountByUserId(Long userId);

    @Query("select coalesce(sum(i.amount), 0) from Income i where i.userId = :userId and i.date between :startDate and :endDate")
    BigDecimal sumAmountByUserIdAndPeriod(Long userId, LocalDate startDate, LocalDate endDate);

    List<Income> findByUserIdAndTitleContainingIgnoreCaseOrUserIdAndNotesContainingIgnoreCase(
            Long userIdTitle,
            String titleKeyword,
            Long userIdNotes,
            String notesKeyword
    );
}
