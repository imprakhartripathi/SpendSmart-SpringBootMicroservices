package com.spendsmart.analyticsservice.client;

import java.math.BigDecimal;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class IncomeAnalyticsClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(IncomeAnalyticsClient.class);

    private final RestTemplate restTemplate;

    @Value("${app.services.income-url}")
    private String incomeServiceUrl;

    public IncomeAnalyticsClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public BigDecimal getTotalByMonth(Long userId, int year, int month) {
        try {
            BigDecimal total = restTemplate.getForObject(
                    incomeServiceUrl + "/incomes/totals/month?userId={userId}&year={year}&month={month}",
                    BigDecimal.class,
                    Map.of("userId", userId, "year", year, "month", month)
            );
            return total == null ? BigDecimal.ZERO : total;
        } catch (Exception exception) {
            LOGGER.warn("Could not read income totals for userId={}, {}/{}. Reason={}", userId, year, month, exception.getMessage());
            return BigDecimal.ZERO;
        }
    }
}
