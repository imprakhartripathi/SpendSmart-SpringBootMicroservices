package com.spendsmart.notificationservice.messaging;

import com.spendsmart.notificationservice.domain.Notification;
import com.spendsmart.notificationservice.enums.NotificationSeverity;
import com.spendsmart.notificationservice.enums.NotificationType;
import com.spendsmart.notificationservice.service.NotifService;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.messaging.enabled", havingValue = "true")
public class NotificationEventConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final NotifService notifService;

    public NotificationEventConsumer(NotifService notifService) {
        this.notifService = notifService;
    }

    @RabbitListener(queues = "${app.messaging.email-queue}")
    public void consume(Map<String, Object> event) {
        String eventType = asString(event.get("eventType"));
        Long recipientId = toLong(event.get("recipientId"));
        String recipientEmail = asString(event.get("recipientEmail"));
        String title = asString(event.getOrDefault("title", "SpendSmart notification"));
        String message = asString(event.getOrDefault("message", ""));
        Long relatedId = toLong(event.get("relatedId"));
        String relatedType = asString(event.get("relatedType"));

        if (eventType == null || recipientId == null) {
            LOGGER.warn("Skipping malformed notification event: {}", event);
            return;
        }

        Notification notification = new Notification();
        notification.setRecipientId(recipientId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRelatedId(relatedId);
        notification.setRelatedType(relatedType);

        switch (eventType) {
            case "WELCOME_EMAIL" -> {
                notification.setType(NotificationType.WELCOME);
                notification.setSeverity(NotificationSeverity.INFO);
            }
            case "AUTOPAY_EMAIL" -> {
                notification.setType(NotificationType.RECURRING_DUE);
                notification.setSeverity(NotificationSeverity.INFO);
            }
            case "BIG_EXPENSE_EMAIL" -> {
                notification.setType(NotificationType.BIG_EXPENSE_ALERT);
                notification.setSeverity(NotificationSeverity.WARNING);
            }
            default -> {
                LOGGER.warn("Unhandled eventType='{}', rawEvent={}", eventType, event);
                return;
            }
        }

        notifService.send(notification);
        notifService.sendEmail(recipientEmail, title, message);
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
