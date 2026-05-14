package com.spendsmart.expenseservice.repository;

import com.spendsmart.expenseservice.domain.Expense;
import com.spendsmart.expenseservice.enums.ExpenseType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUserId(Long userId);

    List<Expense> findByUserIdAndType(Long userId, ExpenseType type);

    List<Expense> findByCategoryId(Long categoryId);

    List<Expense> findByUserIdAndDate(Long userId, LocalDate date);

    List<Expense> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    Optional<Expense> findByExpenseId(Long expenseId);

    void deleteByExpenseId(Long expenseId);

    @Query("select coalesce(sum(e.amount), 0) from Expense e where e.userId = :userId")
    BigDecimal sumAmountByUserId(Long userId);

    @Query("select coalesce(sum(e.amount), 0) from Expense e where e.userId = :userId and e.categoryId = :categoryId")
    BigDecimal sumAmountByUserIdAndCategoryId(Long userId, Long categoryId);

    List<Expense> findByUserIdAndTitleContainingIgnoreCaseOrUserIdAndNotesContainingIgnoreCase(
            Long userIdTitle,
            String titleKeyword,
            Long userIdNotes,
            String notesKeyword
    );
}
