package com.spendsmart.recurringservice.client;

import java.time.LocalDate;
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

    @Value("${app.services.notification-url:http://notification-service}")
    private String notificationServiceUrl;

    public NotificationClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void sendRecurringReminder(Long recipientId, Long recurringId, String title, LocalDate dueDate) {
        try {
            restTemplate.exchange(
                    notificationServiceUrl + "/notifications",
                    HttpMethod.POST,
                    new HttpEntity<>(Map.of(
                            "recipientId", recipientId,
                            "type", "RECURRING_DUE",
                            "severity", "INFO",
                            "title", "Recurring payment due soon",
                            "message", String.format("'%s' is due on %s.", title, dueDate),
                            "relatedId", recurringId,
                            "relatedType", "RECURRING"
                    )),
                    Object.class
            );
        } catch (Exception exception) {
            LOGGER.warn(
                    "Could not dispatch recurring reminder for recurringId={}, userId={}. Reason={}",
                    recurringId,
                    recipientId,
                    exception.getMessage()
            );
        }
    }
}
