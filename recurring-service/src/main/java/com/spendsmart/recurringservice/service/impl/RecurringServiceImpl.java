package com.spendsmart.recurringservice.service.impl;

import com.spendsmart.recurringservice.client.NotificationClient;
import com.spendsmart.recurringservice.client.TransactionClient;
import com.spendsmart.recurringservice.domain.RecurringTransaction;
import com.spendsmart.recurringservice.enums.RecurringFrequency;
import com.spendsmart.recurringservice.repository.RecurringRepository;
import com.spendsmart.recurringservice.service.RecurringService;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RecurringServiceImpl implements RecurringService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecurringServiceImpl.class);

    private final RecurringRepository recurringRepository;
    private final TransactionClient transactionClient;
    private final NotificationClient notificationClient;

    public RecurringServiceImpl(
            RecurringRepository recurringRepository,
            TransactionClient transactionClient,
            NotificationClient notificationClient
    ) {
        this.recurringRepository = recurringRepository;
        this.transactionClient = transactionClient;
        this.notificationClient = notificationClient;
    }

    @Override
    public RecurringTransaction addRecurring(RecurringTransaction recurringTransaction) {
        return recurringRepository.save(recurringTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecurringTransaction> getByUser(Long userId) {
        return recurringRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public RecurringTransaction getById(Long recurringId) {
        return recurringRepository.findByRecurringId(recurringId)
                .orElseThrow(() -> new EntityNotFoundException("Recurring transaction not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecurringTransaction> getActiveRecurring(Long userId) {
        return recurringRepository.findByUserIdAndIsActive(userId, true);
    }

    @Override
    public RecurringTransaction updateRecurring(Long recurringId, RecurringTransaction recurringTransaction) {
        RecurringTransaction existing = getById(recurringId);
        existing.setTitle(recurringTransaction.getTitle());
        existing.setAmount(recurringTransaction.getAmount());
        existing.setType(recurringTransaction.getType());
        existing.setCategoryId(recurringTransaction.getCategoryId());
        existing.setFrequency(recurringTransaction.getFrequency());
        existing.setStartDate(recurringTransaction.getStartDate());
        existing.setEndDate(recurringTransaction.getEndDate());
        existing.setDescription(recurringTransaction.getDescription());
        existing.setPaymentMethod(recurringTransaction.getPaymentMethod());
        return recurringRepository.save(existing);
    }

    @Override
    public RecurringTransaction deactivateRecurring(Long recurringId) {
        RecurringTransaction existing = getById(recurringId);
        existing.setActive(false);
        return recurringRepository.save(existing);
    }

    @Override
    public void deleteRecurring(Long recurringId) {
        if (!recurringRepository.existsById(recurringId)) {
            throw new EntityNotFoundException("Recurring transaction not found");
        }
        recurringRepository.deleteById(recurringId);
    }

    @Override
    public List<Map<String, Object>> processUpcomingDue() {
        LocalDate today = LocalDate.now();
        List<RecurringTransaction> dueTransactions = recurringRepository.findByNextDueDateBeforeAndIsActive(today.plusDays(1), true);
        List<Map<String, Object>> generated = new ArrayList<>();

        for (RecurringTransaction recurring : dueTransactions) {
            if (recurring.getEndDate() != null && recurring.getNextDueDate().isAfter(recurring.getEndDate())) {
                recurring.setActive(false);
                recurringRepository.save(recurring);
                continue;
            }

            generated.add(generateTransactionFromRecurring(recurring));
            recurring.setNextDueDate(nextDueDateFor(recurring.getNextDueDate(), recurring.getFrequency()));
            recurringRepository.save(recurring);
        }

        return generated;
    }

    @Override
    public RecurringTransaction updateNextDueDate(Long recurringId) {
        RecurringTransaction recurring = getById(recurringId);
        recurring.setNextDueDate(nextDueDateFor(recurring.getNextDueDate(), recurring.getFrequency()));
        return recurringRepository.save(recurring);
    }

    @Override
    public Map<String, Object> generateTransactionFromRecurring(RecurringTransaction recurringTransaction) {
        Map<String, Object> generated = new LinkedHashMap<>();
        generated.put("sourceRecurringId", recurringTransaction.getRecurringId());
        generated.put("userId", recurringTransaction.getUserId());
        generated.put("type", recurringTransaction.getType());
        generated.put("title", recurringTransaction.getTitle());
        generated.put("amount", recurringTransaction.getAmount());
        generated.put("generatedOn", recurringTransaction.getNextDueDate());

        Map<String, Object> transactionResult = transactionClient.generate(recurringTransaction, recurringTransaction.getNextDueDate());
        generated.put("transaction", transactionResult);
        generated.put("status", transactionResult.getOrDefault("status", "UNKNOWN"));

        return generated;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecurringTransaction> getUpcomingThisMonth(Long userId) {
        LocalDate now = LocalDate.now();
        LocalDate start = now.withDayOfMonth(1);
        LocalDate end = now.withDayOfMonth(now.lengthOfMonth());

        return recurringRepository.findByUserIdAndIsActive(userId, true).stream()
                .filter(recurring -> !recurring.getNextDueDate().isBefore(start) && !recurring.getNextDueDate().isAfter(end))
                .toList();
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void scheduledRecurringProcessor() {
        processUpcomingDue();
    }

    @Scheduled(cron = "${app.scheduling.recurring-reminder-cron:0 30 8 * * *}")
    public void scheduledRecurringReminderDispatcher() {
        LocalDate reminderDate = LocalDate.now().plusDays(3);
        List<RecurringTransaction> dueSoon = recurringRepository.findByNextDueDateAndIsActive(reminderDate, true);

        for (RecurringTransaction recurring : dueSoon) {
            notificationClient.sendRecurringReminder(
                    recurring.getUserId(),
                    recurring.getRecurringId(),
                    recurring.getTitle(),
                    recurring.getNextDueDate()
            );
        }

        if (!dueSoon.isEmpty()) {
            LOGGER.info("Scheduled recurring reminders dispatched. reminderCount={}, dueDate={}", dueSoon.size(), reminderDate);
        }
    }

    private LocalDate nextDueDateFor(LocalDate currentDueDate, RecurringFrequency frequency) {
        return switch (frequency) {
            case DAILY -> currentDueDate.plusDays(1);
            case WEEKLY -> currentDueDate.plusWeeks(1);
            case MONTHLY -> currentDueDate.plusMonths(1);
            case QUARTERLY -> currentDueDate.plusMonths(3);
            case YEARLY -> currentDueDate.plusYears(1);
        };
    }
}
