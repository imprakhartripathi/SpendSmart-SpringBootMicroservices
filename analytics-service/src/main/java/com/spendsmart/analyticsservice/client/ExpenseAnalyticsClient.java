package com.spendsmart.analyticsservice.client;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ExpenseAnalyticsClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpenseAnalyticsClient.class);

    private final RestTemplate restTemplate;

    @Value("${app.services.expense-url:http://expense-service}")
    private String expenseServiceUrl;

    public ExpenseAnalyticsClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public BigDecimal getTotalByMonth(Long userId, int year, int month) {
        try {
            BigDecimal total = restTemplate.getForObject(
                    expenseServiceUrl + "/expenses/totals/month?userId={userId}&year={year}&month={month}",
                    BigDecimal.class,
                    Map.of("userId", userId, "year", year, "month", month)
            );
            return total == null ? BigDecimal.ZERO : total;
        } catch (Exception exception) {
            LOGGER.warn("Could not read expense totals for userId={}, {}/{}. Reason={}", userId, year, month, exception.getMessage());
            return BigDecimal.ZERO;
        }
    }

    public List<Map<String, Object>> getExpensesByMonth(Long userId, int year, int month) {
        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    expenseServiceUrl + "/expenses/month?userId={userId}&year={year}&month={month}",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    },
                    Map.of("userId", userId, "year", year, "month", month)
            );
            List<Map<String, Object>> body = response.getBody();
            return body == null ? Collections.emptyList() : body;
        } catch (Exception exception) {
            LOGGER.warn("Could not read expenses by month for userId={}, {}/{}. Reason={}", userId, year, month, exception.getMessage());
            return Collections.emptyList();
        }
    }
}
