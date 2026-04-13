package com.spendsmart.notificationservice.service.impl;

import com.spendsmart.notificationservice.domain.Notification;
import com.spendsmart.notificationservice.enums.NotificationSeverity;
import com.spendsmart.notificationservice.enums.NotificationType;
import com.spendsmart.notificationservice.repository.NotificationRepository;
import com.spendsmart.notificationservice.service.NotifService;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NotifServiceImpl implements NotifService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotifServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;
    private final String mailFrom;

    public NotifServiceImpl(
            NotificationRepository notificationRepository,
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${app.mail.from:no-reply@spendsmart.local}") String mailFrom
    ) {
        this.notificationRepository = notificationRepository;
        this.mailSender = mailSenderProvider.getIfAvailable();
        this.mailFrom = mailFrom;
    }

    @Override
    public Notification send(Notification notification) {
        return notificationRepository.save(notification);
    }

    @Override
    public Notification sendBudgetAlert(Long recipientId, Long budgetId, String budgetName, BigDecimal percentageUsed, boolean exceeded) {
        Notification notification = new Notification();
        notification.setRecipientId(recipientId);
        notification.setType(exceeded ? NotificationType.BUDGET_EXCEEDED : NotificationType.BUDGET_ALERT);
        notification.setSeverity(exceeded ? NotificationSeverity.CRITICAL : NotificationSeverity.WARNING);
        notification.setTitle(exceeded ? "Budget exceeded" : "Budget threshold reached");
        notification.setMessage(String.format(
                "Budget '%s' is at %s%% usage.",
                budgetName,
                percentageUsed
        ));
        notification.setRelatedId(budgetId);
        notification.setRelatedType("BUDGET");

        Notification saved = notificationRepository.save(notification);
        if (saved.getSeverity() == NotificationSeverity.CRITICAL) {
            sendEmail(recipientId, saved.getTitle(), saved.getMessage());
        }
        return saved;
    }

    @Override
    public List<Notification> sendBulk(List<Long> recipientIds, Notification notificationTemplate) {
        List<Notification> sent = new ArrayList<>();
        for (Long recipientId : recipientIds) {
            Notification cloned = new Notification();
            cloned.setRecipientId(recipientId);
            cloned.setType(notificationTemplate.getType());
            cloned.setSeverity(notificationTemplate.getSeverity());
            cloned.setTitle(notificationTemplate.getTitle());
            cloned.setMessage(notificationTemplate.getMessage());
            cloned.setRelatedId(notificationTemplate.getRelatedId());
            cloned.setRelatedType(notificationTemplate.getRelatedType());
            sent.add(notificationRepository.save(cloned));
        }
        return sent;
    }

    @Override
    public Notification markAsRead(Long notificationId) {
        Notification notification = getOrThrow(notificationId);
        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    @Override
    public List<Notification> markAllRead(Long recipientId) {
        List<Notification> notifications = notificationRepository.findByRecipientIdAndIsRead(recipientId, false);
        for (Notification notification : notifications) {
            notification.setRead(true);
        }
        return notificationRepository.saveAll(notifications);
    }

    @Override
    public Notification acknowledge(Long notificationId) {
        Notification notification = getOrThrow(notificationId);
        notification.setAcknowledged(true);
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getByRecipient(Long recipientId) {
        return notificationRepository.findByRecipientId(recipientId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long recipientId) {
        return notificationRepository.countByRecipientIdAndIsRead(recipientId, false);
    }

    @Override
    public void deleteNotification(Long notificationId) {
        if (!notificationRepository.existsById(notificationId)) {
            throw new EntityNotFoundException("Notification not found");
        }
        notificationRepository.deleteByNotificationId(notificationId);
    }

    @Override
    public void sendEmail(Long recipientId, String subject, String body) {
        LOGGER.info("Email dispatch simulated. recipientId={}, subject={}, body={}", recipientId, subject, body);
    }

    @Override
    public void sendEmail(String recipientEmail, String subject, String body) {
        if (recipientEmail == null || recipientEmail.isBlank()) {
            LOGGER.warn("Skipping email dispatch because recipient email is missing. subject={}", subject);
            return;
        }

        if (mailSender == null) {
            LOGGER.info(
                    "Email sender not configured. simulated dispatch recipientEmail={}, subject={}, body={}",
                    recipientEmail,
                    subject,
                    body
            );
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(recipientEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception exception) {
            LOGGER.warn(
                    "Email dispatch failed. recipientEmail={}, subject={}, reason={}",
                    recipientEmail,
                    subject,
                    exception.getMessage()
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getAll() {
        return notificationRepository.findAll();
    }

    private Notification getOrThrow(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));
    }
}
