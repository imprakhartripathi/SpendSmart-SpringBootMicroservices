package com.spendsmart.recurringservice.domain;

import com.spendsmart.recurringservice.enums.RecurringFrequency;
import com.spendsmart.recurringservice.enums.RecurringPaymentMethod;
import com.spendsmart.recurringservice.enums.RecurringTransactionType;
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
@Table(name = "recurring_transactions")
public class RecurringTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recurringId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long categoryId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecurringTransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecurringFrequency frequency;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Column(nullable = false)
    private LocalDate nextDueDate;

    @Column(nullable = false)
    private boolean isActive;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    private RecurringPaymentMethod paymentMethod;

    @PrePersist
    void onCreate() {
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (nextDueDate == null) {
            nextDueDate = startDate;
        }
        if (type == null) {
            type = RecurringTransactionType.EXPENSE;
        }
        if (frequency == null) {
            frequency = RecurringFrequency.MONTHLY;
        }
        if (paymentMethod == null) {
            paymentMethod = RecurringPaymentMethod.CASH;
        }
        isActive = true;
    }

    public Long getRecurringId() {
        return recurringId;
    }

    public void setRecurringId(Long recurringId) {
        this.recurringId = recurringId;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public RecurringTransactionType getType() {
        return type;
    }

    public void setType(RecurringTransactionType type) {
        this.type = type;
    }

    public RecurringFrequency getFrequency() {
        return frequency;
    }

    public void setFrequency(RecurringFrequency frequency) {
        this.frequency = frequency;
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

    public LocalDate getNextDueDate() {
        return nextDueDate;
    }

    public void setNextDueDate(LocalDate nextDueDate) {
        this.nextDueDate = nextDueDate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RecurringPaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(RecurringPaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
