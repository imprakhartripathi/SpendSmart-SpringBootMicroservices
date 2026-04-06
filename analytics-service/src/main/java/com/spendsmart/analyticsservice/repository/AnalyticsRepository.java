package com.spendsmart.analyticsservice.repository;

import com.spendsmart.analyticsservice.domain.FinancialSnapshot;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AnalyticsRepository extends JpaRepository<FinancialSnapshot, Long> {
    List<FinancialSnapshot> findByUserId(Long userId);

    Optional<FinancialSnapshot> findByUserIdAndYearAndMonth(Long userId, int year, int month);

    List<FinancialSnapshot> findByUserIdAndYear(Long userId, int year);

    @Query("select coalesce(avg(f.savingsRate), 0) from FinancialSnapshot f where f.userId = :userId")
    BigDecimal avgSavingsRateByUserId(Long userId);

    @Query("select f from FinancialSnapshot f where f.userId = :userId order by f.totalExpenses desc")
    List<FinancialSnapshot> findTopSpendingMonths(Long userId);

    long countByUserId(Long userId);
}
