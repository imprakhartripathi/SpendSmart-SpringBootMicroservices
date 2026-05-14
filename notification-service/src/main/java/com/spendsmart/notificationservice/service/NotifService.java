package com.spendsmart.notificationservice.service;

import com.spendsmart.notificationservice.domain.Notification;
import java.math.BigDecimal;
import java.util.List;

public interface NotifService {
    Notification send(Notification notification);

    Notification sendBudgetAlert(Long recipientId, Long budgetId, String budgetName, BigDecimal percentageUsed, boolean exceeded);

    List<Notification> sendBulk(List<Long> recipientIds, Notification notificationTemplate);

    Notification markAsRead(Long notificationId);

    List<Notification> markAllRead(Long recipientId);

    Notification acknowledge(Long notificationId);

    List<Notification> getByRecipient(Long recipientId);

    long getUnreadCount(Long recipientId);

    void deleteNotification(Long notificationId);

    void sendEmail(Long recipientId, String subject, String body);

    void sendEmail(String recipientEmail, String subject, String body);

    List<Notification> getAll();
}
