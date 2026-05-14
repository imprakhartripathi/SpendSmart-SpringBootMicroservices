package com.spendsmart.notificationservice.repository;

import com.spendsmart.notificationservice.domain.Notification;
import com.spendsmart.notificationservice.enums.NotificationSeverity;
import com.spendsmart.notificationservice.enums.NotificationType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientId(Long recipientId);

    List<Notification> findByRecipientIdAndIsRead(Long recipientId, boolean isRead);

    long countByRecipientIdAndIsRead(Long recipientId, boolean isRead);

    List<Notification> findByType(NotificationType type);

    List<Notification> findBySeverity(NotificationSeverity severity);

    List<Notification> findByIsAcknowledged(boolean isAcknowledged);

    void deleteByNotificationId(Long notificationId);
}
