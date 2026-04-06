package com.spendsmart.budgetservice.client;

import java.math.BigDecimal;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class NotificationClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationClient.class);

    private final RestTemplate restTemplate;

    @Value("${app.services.notification-url}")
    private String notificationServiceUrl;

    public NotificationClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void sendBudgetAlert(Long recipientId, Long budgetId, String budgetName, BigDecimal percentageUsed, boolean exceeded) {
        try {
            restTemplate.exchange(
                    notificationServiceUrl + "/notifications/budget-alert",
                    HttpMethod.POST,
                    new HttpEntity<>(Map.of(
                            "recipientId", recipientId,
                            "budgetId", budgetId,
                            "budgetName", budgetName,
                            "percentageUsed", percentageUsed,
                            "exceeded", exceeded
                    )),
                    Object.class
            );
        } catch (Exception exception) {
            LOGGER.warn("Could not dispatch budget alert for budgetId={}, userId={}. Reason={}", budgetId, recipientId, exception.getMessage());
        }
    }
}
