package com.spendsmart.notificationservice.dto;

import com.spendsmart.notificationservice.enums.NotificationSeverity;
import com.spendsmart.notificationservice.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record BulkNotificationRequest(
        @NotNull List<Long> recipientIds,
        @NotNull NotificationType type,
        @NotNull NotificationSeverity severity,
        @NotBlank String title,
        @NotBlank String message
) {
}
