package com.spendsmart.authservice.messaging;

import com.spendsmart.authservice.domain.User;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.messaging.enabled", havingValue = "true")
public class NotificationEventPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String welcomeRoutingKey;
    private final boolean messagingEnabled;

    public NotificationEventPublisher(
            ObjectProvider<RabbitTemplate> rabbitTemplateProvider,
            @Value("${app.messaging.exchange:spendsmart.events}") String exchange,
            @Value("${app.messaging.routing.welcome:email.welcome}") String welcomeRoutingKey,
            @Value("${app.messaging.enabled:false}") boolean messagingEnabled
    ) {
        this.rabbitTemplate = rabbitTemplateProvider == null ? null : rabbitTemplateProvider.getIfAvailable();
        this.exchange = exchange;
        this.welcomeRoutingKey = welcomeRoutingKey;
        this.messagingEnabled = messagingEnabled;
    }

    public void publishWelcome(User user) {
        if (!messagingEnabled) {
            return;
        }

        if (rabbitTemplate == null) {
            LOGGER.warn("RabbitTemplate is unavailable. Skipping welcome event for userId={}", user.getUserId());
            return;
        }

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
