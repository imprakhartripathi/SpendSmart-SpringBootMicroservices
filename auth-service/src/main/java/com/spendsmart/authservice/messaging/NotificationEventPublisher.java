package com.spendsmart.authservice.messaging;

import com.spendsmart.authservice.domain.User;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String welcomeRoutingKey;

    public NotificationEventPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${app.messaging.exchange}") String exchange,
            @Value("${app.messaging.routing.welcome}") String welcomeRoutingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.welcomeRoutingKey = welcomeRoutingKey;
    }

    public void publishWelcome(User user) {
        try {
            Map<String, Object> event = new LinkedHashMap<>();
            event.put("eventType", "WELCOME_EMAIL");
            event.put("recipientId", user.getUserId());
            event.put("recipientName", user.getFullName());
            event.put("recipientEmail", user.getEmail());
            event.put("title", "Welcome to SpendSmart");
            event.put("message", String.format(
                    "Hi %s, your SpendSmart account is ready. Let's start tracking your money smarter.",
                    user.getFullName()
            ));

            rabbitTemplate.convertAndSend(exchange, welcomeRoutingKey, event);
        } catch (Exception exception) {
            LOGGER.warn("Could not publish welcome event for userId={}. Reason={}", user.getUserId(), exception.getMessage());
        }
    }
}
