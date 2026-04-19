package com.spendsmart.expenseservice.client;

import java.math.BigDecimal;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class IncomeClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(IncomeClient.class);

    private final RestTemplate restTemplate;

    @Value("${app.services.income-url:http://income-service}")
    private String incomeServiceUrl;

    public IncomeClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public BigDecimal getAverageMonthlyIncome(Long userId, int trailingMonths) {
        try {
            BigDecimal amount = restTemplate.getForObject(
                    incomeServiceUrl + "/incomes/average-monthly?userId={userId}&months={months}",
                    BigDecimal.class,
                    Map.of("userId", userId, "months", trailingMonths)
            );
            return amount == null ? BigDecimal.ZERO : amount;
        } catch (Exception exception) {
            LOGGER.warn(
                    "Could not fetch average monthly income for userId={} months={}. Reason={}",
                    userId,
                    trailingMonths,
                    exception.getMessage()
            );
            return BigDecimal.ZERO;
        }
    }
}
