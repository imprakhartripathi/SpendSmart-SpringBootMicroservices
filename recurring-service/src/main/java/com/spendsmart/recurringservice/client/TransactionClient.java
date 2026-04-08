package com.spendsmart.recurringservice.client;

import com.spendsmart.recurringservice.domain.RecurringTransaction;
import com.spendsmart.recurringservice.enums.RecurringTransactionType;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TransactionClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionClient.class);

    private final RestTemplate restTemplate;

    @Value("${app.services.expense-url}")
    private String expenseServiceUrl;

    @Value("${app.services.income-url}")
    private String incomeServiceUrl;

    public TransactionClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<String, Object> generate(RecurringTransaction recurring, LocalDate date) {
        try {
            return recurring.getType() == RecurringTransactionType.EXPENSE
                    ? createExpense(recurring, date)
                    : createIncome(recurring, date);
        } catch (Exception exception) {
            LOGGER.warn("Failed to generate transaction from recurringId={}. Reason={}", recurring.getRecurringId(), exception.getMessage());
            return Map.of(
                    "status", "FAILED",
                    "reason", exception.getMessage(),
                    "sourceRecurringId", recurring.getRecurringId()
            );
        }
    }

    private Map<String, Object> createExpense(RecurringTransaction recurring, LocalDate date) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("userId", recurring.getUserId());
        payload.put("categoryId", recurring.getCategoryId());
        payload.put("title", recurring.getTitle());
        payload.put("amount", recurring.getAmount());
        payload.put("currency", "USD");
        payload.put("type", "EXPENSE");
        payload.put("paymentMethod", recurring.getPaymentMethod().name());
        payload.put("date", date.toString());
        payload.put("notes", recurring.getDescription());
        payload.put("receiptUrl", "");
        payload.put("recurring", true);

        ResponseEntity<Map> response = restTemplate.exchange(
                expenseServiceUrl + "/expenses",
                HttpMethod.POST,
                new HttpEntity<>(payload),
                Map.class
        );

        return Map.of(
                "status", "GENERATED",
                "targetService", "expense-service",
                "response", response.getBody(),
                "sourceRecurringId", recurring.getRecurringId()
        );
    }

    private Map<String, Object> createIncome(RecurringTransaction recurring, LocalDate date) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("userId", recurring.getUserId());
        payload.put("categoryId", recurring.getCategoryId());
        payload.put("title", recurring.getTitle());
        payload.put("amount", recurring.getAmount());
        payload.put("currency", "USD");
        payload.put("source", "OTHER");
        payload.put("date", date.toString());
        payload.put("notes", recurring.getDescription());
        payload.put("recurring", true);
        payload.put("recurrencePeriod", recurring.getFrequency().name());

        ResponseEntity<Map> response = restTemplate.exchange(
                incomeServiceUrl + "/incomes",
                HttpMethod.POST,
                new HttpEntity<>(payload),
                Map.class
        );

        return Map.of(
                "status", "GENERATED",
                "targetService", "income-service",
                "response", response.getBody(),
                "sourceRecurringId", recurring.getRecurringId()
        );
    }
}
