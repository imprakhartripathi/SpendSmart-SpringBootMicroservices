package com.spendsmart.notificationservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfig {

    @Bean
    public TopicExchange spendSmartEventsExchange(@Value("${app.messaging.exchange:spendsmart.events}") String exchangeName) {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public Queue emailEventQueue(@Value("${app.messaging.email-queue:spendsmart.notification.email}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    public Binding emailEventBinding(
            Queue emailEventQueue,
            TopicExchange spendSmartEventsExchange,
            @Value("${app.messaging.routing-pattern:email.*}") String routingPattern
    ) {
        return BindingBuilder.bind(emailEventQueue).to(spendSmartEventsExchange).with(routingPattern);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
