package com.spendsmart.budgetservice.domain;

import com.spendsmart.budgetservice.enums.BudgetPeriod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "budgets")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long budgetId;

    @Column(nullable = false)
    private Long userId;

    private Long categoryId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal limitAmount;

    @Column(nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BudgetPeriod period;

    private LocalDate startDate;

    private LocalDate endDate;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal spentAmount;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal alertThreshold;

    @Column(nullable = false)
    private boolean isActive;

    @PrePersist
    void onCreate() {
        if (spentAmount == null) {
            spentAmount = BigDecimal.ZERO;
        }
        if (currency == null || currency.isBlank()) {
            currency = "USD";
        }
        if (alertThreshold == null) {
            alertThreshold = BigDecimal.valueOf(80);
        }
        isActive = true;
    }

    public Long getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(Long budgetId) {
        this.budgetId = budgetId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getLimitAmount() {
        return limitAmount;
    }

    public void setLimitAmount(BigDecimal limitAmount) {
        this.limitAmount = limitAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BudgetPeriod getPeriod() {
        return period;
    }

    public void setPeriod(BudgetPeriod period) {
        this.period = period;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(BigDecimal spentAmount) {
        this.spentAmount = spentAmount;
    }

    public BigDecimal getAlertThreshold() {
        return alertThreshold;
    }

    public void setAlertThreshold(BigDecimal alertThreshold) {
        this.alertThreshold = alertThreshold;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
