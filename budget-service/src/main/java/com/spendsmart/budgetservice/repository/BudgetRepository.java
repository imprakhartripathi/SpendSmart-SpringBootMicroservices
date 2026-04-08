package com.spendsmart.budgetservice.repository;

import com.spendsmart.budgetservice.domain.Budget;
import com.spendsmart.budgetservice.enums.BudgetPeriod;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUserId(Long userId);

    Optional<Budget> findByBudgetId(Long budgetId);

    List<Budget> findByUserIdAndCategoryId(Long userId, Long categoryId);

    List<Budget> findByPeriod(BudgetPeriod period);

    List<Budget> findByIsActive(boolean isActive);

    List<Budget> findByUserIdAndIsActive(Long userId, boolean isActive);

    long countByUserId(Long userId);

    void deleteByBudgetId(Long budgetId);
}
