package com.spendsmart.expenseservice.messaging;

import com.spendsmart.expenseservice.domain.Expense;
import java.math.BigDecimal;
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
    private final String bigExpenseRoutingKey;

    public NotificationEventPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${app.messaging.exchange}") String exchange,
            @Value("${app.messaging.routing.big-expense}") String bigExpenseRoutingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.bigExpenseRoutingKey = bigExpenseRoutingKey;
    }

    public void publishBigExpenseAlert(
            Expense expense,
            BigDecimal averageMonthlyIncome,
            BigDecimal thresholdAmount,
            String recipientName,
            String recipientEmail
    ) {
        try {
            Map<String, Object> event = new LinkedHashMap<>();
            event.put("eventType", "BIG_EXPENSE_EMAIL");
            event.put("recipientId", expense.getUserId());
            event.put("recipientName", recipientName);
            event.put("recipientEmail", recipientEmail);
            event.put("relatedId", expense.getExpenseId());
            event.put("relatedType", "EXPENSE");
            event.put("title", "Large expense alert");
            event.put("message", String.format(
                    "Expense '%s' (%s %s) is above your alert threshold (%s) based on average monthly income (%s).",
                    expense.getTitle(),
                    expense.getCurrency(),
                    expense.getAmount(),
                    thresholdAmount,
                    averageMonthlyIncome
            ));

            rabbitTemplate.convertAndSend(exchange, bigExpenseRoutingKey, event);
        } catch (Exception exception) {
            LOGGER.warn(
                    "Could not publish big-expense event for expenseId={}, userId={}. Reason={}",
                    expense.getExpenseId(),
                    expense.getUserId(),
                    exception.getMessage()
            );
        }
    }
}
