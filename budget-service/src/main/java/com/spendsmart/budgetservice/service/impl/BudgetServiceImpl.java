package com.spendsmart.budgetservice.service.impl;

import com.spendsmart.budgetservice.client.NotificationClient;
import com.spendsmart.budgetservice.domain.Budget;
import com.spendsmart.budgetservice.dto.BudgetProgress;
import com.spendsmart.budgetservice.enums.BudgetPeriod;
import com.spendsmart.budgetservice.repository.BudgetRepository;
import com.spendsmart.budgetservice.service.BudgetService;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BudgetServiceImpl implements BudgetService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BudgetServiceImpl.class);

    private final BudgetRepository budgetRepository;
    private final NotificationClient notificationClient;

    public BudgetServiceImpl(BudgetRepository budgetRepository, NotificationClient notificationClient) {
        this.budgetRepository = budgetRepository;
        this.notificationClient = notificationClient;
    }

    @Override
    public Budget createBudget(Budget budget) {
        if (budget.getPeriod() == BudgetPeriod.MONTHLY) {
            LocalDate now = LocalDate.now();
            budget.setStartDate(now.withDayOfMonth(1));
            budget.setEndDate(now.withDayOfMonth(now.lengthOfMonth()));
        }
        return budgetRepository.save(budget);
    }

    @Override
    @Transactional(readOnly = true)
    public Budget getBudgetById(Long budgetId) {
        return budgetRepository.findByBudgetId(budgetId)
                .orElseThrow(() -> new EntityNotFoundException("Budget not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Budget> getBudgetsByUser(Long userId) {
        return budgetRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Budget> getActiveBudgets(Long userId) {
        return budgetRepository.findByUserIdAndIsActive(userId, true);
    }

    @Override
    public Budget updateBudget(Long budgetId, Budget budget) {
        Budget existing = getBudgetById(budgetId);
        existing.setName(budget.getName());
        existing.setCategoryId(budget.getCategoryId());
        existing.setLimitAmount(budget.getLimitAmount());
        existing.setCurrency(budget.getCurrency());
        existing.setPeriod(budget.getPeriod());
        existing.setStartDate(budget.getStartDate());
        existing.setEndDate(budget.getEndDate());
        if (budget.getAlertThreshold() != null) {
            existing.setAlertThreshold(budget.getAlertThreshold());
        }
        existing.setActive(budget.isActive());
        return budgetRepository.save(existing);
    }

    @Override
    public void deleteBudget(Long budgetId) {
        if (!budgetRepository.existsById(budgetId)) {
            throw new EntityNotFoundException("Budget not found");
        }
        budgetRepository.deleteByBudgetId(budgetId);
    }

    @Override
    public Budget updateSpentAmount(Long budgetId, BigDecimal deltaAmount) {
        Budget budget = getBudgetById(budgetId);
        BigDecimal updatedSpent = budget.getSpentAmount().add(deltaAmount);
        if (updatedSpent.compareTo(BigDecimal.ZERO) < 0) {
            updatedSpent = BigDecimal.ZERO;
        }
        budget.setSpentAmount(updatedSpent);

        Budget saved = budgetRepository.save(budget);
        BudgetProgress progress = progressOf(saved);

        if (saved.isActive() && (progress.thresholdReached() || progress.exceeded())) {
            notificationClient.sendBudgetAlert(
                    saved.getUserId(),
                    saved.getBudgetId(),
                    saved.getName(),
                    progress.percentageUsed(),
                    progress.exceeded()
            );
        }

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetProgress getBudgetProgress(Long budgetId) {
        return progressOf(getBudgetById(budgetId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetProgress> checkBudgetAlerts(Long userId) {
        List<BudgetProgress> alerting = new ArrayList<>();
        for (Budget budget : getActiveBudgets(userId)) {
            BudgetProgress progress = progressOf(budget);
            if (progress.thresholdReached() || progress.exceeded()) {
                alerting.add(progress);
            }
        }
        return alerting;
    }

    @Override
    public List<BudgetProgress> dispatchBudgetAlerts(Long userId) {
        List<BudgetProgress> alerts = checkBudgetAlerts(userId);
        for (BudgetProgress progress : alerts) {
            Budget budget = getBudgetById(progress.budgetId());
            notificationClient.sendBudgetAlert(
                    userId,
                    budget.getBudgetId(),
                    budget.getName(),
                    progress.percentageUsed(),
                    progress.exceeded()
            );
        }
        return alerts;
    }

    @Override
    public List<Budget> resetBudgetPeriod() {
        LocalDate today = LocalDate.now();
        List<Budget> budgets = budgetRepository.findByIsActive(true);
        List<Budget> updated = new ArrayList<>();

        for (Budget budget : budgets) {
            boolean needsReset = false;

            if (budget.getPeriod() == BudgetPeriod.MONTHLY && budget.getEndDate() != null && !today.isBefore(budget.getEndDate())) {
                budget.setStartDate(today.withDayOfMonth(1));
                budget.setEndDate(today.withDayOfMonth(today.lengthOfMonth()));
                needsReset = true;
            }

            if (budget.getPeriod() == BudgetPeriod.WEEKLY && budget.getEndDate() != null && !today.isBefore(budget.getEndDate())) {
                budget.setStartDate(today);
                budget.setEndDate(today.plusDays(6));
                needsReset = true;
            }

            if (budget.getPeriod() == BudgetPeriod.CUSTOM && budget.getEndDate() != null && !today.isBefore(budget.getEndDate())) {
                needsReset = true;
            }

            if (needsReset) {
                budget.setSpentAmount(BigDecimal.ZERO);
                updated.add(budgetRepository.save(budget));
            }
        }

        return updated;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Budget> getBudgetsByCategory(Long userId, Long categoryId) {
        return budgetRepository.findByUserIdAndCategoryId(userId, categoryId);
    }

    @Scheduled(cron = "${app.scheduling.budget-reset-cron:0 5 0 * * *}")
    public void scheduledBudgetPeriodReset() {
        List<Budget> resetBudgets = resetBudgetPeriod();
        if (!resetBudgets.isEmpty()) {
            LOGGER.info("Scheduled budget reset completed. resetCount={}", resetBudgets.size());
        }
    }

    private BudgetProgress progressOf(Budget budget) {
        BigDecimal threshold = budget.getAlertThreshold() == null ? BigDecimal.valueOf(80) : budget.getAlertThreshold();

        BigDecimal percentageUsed = BigDecimal.ZERO;
        if (budget.getLimitAmount().compareTo(BigDecimal.ZERO) > 0) {
            percentageUsed = budget.getSpentAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(budget.getLimitAmount(), 2, RoundingMode.HALF_UP);
        }

        BigDecimal remaining = budget.getLimitAmount().subtract(budget.getSpentAmount());
        boolean thresholdReached = percentageUsed.compareTo(threshold) >= 0;
        boolean exceeded = budget.getSpentAmount().compareTo(budget.getLimitAmount()) > 0;

        return new BudgetProgress(
                budget.getBudgetId(),
                budget.getLimitAmount(),
                budget.getSpentAmount(),
                percentageUsed,
                remaining,
                thresholdReached,
                exceeded
        );
    }
}
