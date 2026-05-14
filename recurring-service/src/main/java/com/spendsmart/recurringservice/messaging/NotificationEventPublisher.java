package com.spendsmart.recurringservice.messaging;

import com.spendsmart.recurringservice.domain.RecurringTransaction;
import java.time.LocalDate;
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
    private final String autopayRoutingKey;
    private final boolean messagingEnabled;

    public NotificationEventPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${app.messaging.exchange:spendsmart.events}") String exchange,
            @Value("${app.messaging.routing.autopay:email.autopay}") String autopayRoutingKey,
            @Value("${app.messaging.enabled:false}") boolean messagingEnabled
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.autopayRoutingKey = autopayRoutingKey;
        this.messagingEnabled = messagingEnabled;
    }

    public void publishAutopayReminder(
            RecurringTransaction recurring,
            LocalDate dueDate,
            String recipientName,
            String recipientEmail
    ) {
        if (!messagingEnabled) {
            return;
        }

        try {
            Map<String, Object> event = new LinkedHashMap<>();
            event.put("eventType", "AUTOPAY_EMAIL");
            event.put("recipientId", recurring.getUserId());
            event.put("recipientName", recipientName);
            event.put("recipientEmail", recipientEmail);
            event.put("relatedId", recurring.getRecurringId());
            event.put("relatedType", "RECURRING");
            event.put("title", "Recurring payment due soon");
            event.put("message", String.format(
                    "'%s' is due on %s for %s.",
                    recurring.getTitle(),
                    dueDate,
                    recurring.getAmount()
            ));

            rabbitTemplate.convertAndSend(exchange, autopayRoutingKey, event);
        } catch (Exception exception) {
            LOGGER.warn(
                    "Could not publish autopay reminder for recurringId={}, userId={}. Reason={}",
                    recurring.getRecurringId(),
                    recurring.getUserId(),
                    exception.getMessage()
            );
        }
    }
}
