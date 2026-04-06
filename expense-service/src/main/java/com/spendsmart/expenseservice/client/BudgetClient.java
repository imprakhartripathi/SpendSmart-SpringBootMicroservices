package com.spendsmart.expenseservice.client;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class BudgetClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(BudgetClient.class);

    private final RestTemplate restTemplate;

    @Value("${app.services.budget-url}")
    private String budgetServiceUrl;

    public BudgetClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Long> getBudgetIdsForCategory(Long userId, Long categoryId) {
        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    budgetServiceUrl + "/budgets/category?userId={userId}&categoryId={categoryId}",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    },
                    Map.of("userId", userId, "categoryId", categoryId)
            );

            List<Map<String, Object>> budgets = response.getBody();
            if (budgets == null || budgets.isEmpty()) {
                return Collections.emptyList();
            }

            return budgets.stream()
                    .map(item -> item.get("budgetId"))
                    .filter(value -> value instanceof Number)
                    .map(value -> ((Number) value).longValue())
                    .toList();
        } catch (Exception exception) {
            LOGGER.warn("Could not fetch budgets for userId={}, categoryId={}. Reason={}", userId, categoryId, exception.getMessage());
            return Collections.emptyList();
        }
    }

    public void updateSpentAmount(Long budgetId, BigDecimal deltaAmount) {
        try {
            restTemplate.exchange(
                    budgetServiceUrl + "/budgets/{budgetId}/spent",
                    HttpMethod.PUT,
                    new HttpEntity<>(Map.of("deltaAmount", deltaAmount)),
                    Object.class,
                    Map.of("budgetId", budgetId)
            );
        } catch (Exception exception) {
            LOGGER.warn("Could not update budgetId={} spent amount by {}. Reason={}", budgetId, deltaAmount, exception.getMessage());
        }
    }
}
