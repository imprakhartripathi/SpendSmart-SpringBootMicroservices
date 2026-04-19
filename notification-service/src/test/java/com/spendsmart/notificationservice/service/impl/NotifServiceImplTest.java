package com.spendsmart.notificationservice.service.impl;

import com.spendsmart.notificationservice.domain.Notification;
import com.spendsmart.notificationservice.enums.NotificationSeverity;
import com.spendsmart.notificationservice.enums.NotificationType;
import com.spendsmart.notificationservice.repository.NotificationRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotifServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private ObjectProvider<JavaMailSender> mailSenderProvider;

    @Test
    void sendBudgetAlertCreatesCriticalNotification() {
        when(mailSenderProvider.getIfAvailable()).thenReturn(null);
        NotifServiceImpl notifService = new NotifServiceImpl(
                notificationRepository,
                mailSenderProvider,
                "no-reply@spendsmart.local"
        );
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification notification = notifService.sendBudgetAlert(7L, 33L, "Home", new BigDecimal("92"), true);

        assertThat(notification.getType()).isEqualTo(NotificationType.BUDGET_EXCEEDED);
        assertThat(notification.getSeverity()).isEqualTo(NotificationSeverity.CRITICAL);
    }
}
