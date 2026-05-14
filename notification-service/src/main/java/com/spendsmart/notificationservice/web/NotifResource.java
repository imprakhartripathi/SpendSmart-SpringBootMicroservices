package com.spendsmart.notificationservice.web;

import com.spendsmart.notificationservice.domain.Notification;
import com.spendsmart.notificationservice.dto.BudgetAlertRequest;
import com.spendsmart.notificationservice.dto.BulkNotificationRequest;
import com.spendsmart.notificationservice.dto.NotificationRequest;
import com.spendsmart.notificationservice.service.NotifService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
public class NotifResource {

    private final NotifService notifService;

    public NotifResource(NotifService notifService) {
        this.notifService = notifService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Notification send(@Valid @RequestBody NotificationRequest request) {
        return notifService.send(mapRequest(request));
    }

    @PostMapping("/budget-alert")
    @ResponseStatus(HttpStatus.CREATED)
    public Notification sendBudgetAlert(@Valid @RequestBody BudgetAlertRequest request) {
        return notifService.sendBudgetAlert(
                request.recipientId(),
                request.budgetId(),
                request.budgetName(),
                request.percentageUsed(),
                request.exceeded()
        );
    }

    @PostMapping("/sendBulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<Notification> sendBulk(@Valid @RequestBody BulkNotificationRequest request) {
        Notification template = new Notification();
        template.setType(request.type());
        template.setSeverity(request.severity());
        template.setTitle(request.title());
        template.setMessage(request.message());
        return notifService.sendBulk(request.recipientIds(), template);
    }

    @GetMapping("/recipient/{recipientId}")
    public List<Notification> getByRecipient(@PathVariable Long recipientId) {
        return notifService.getByRecipient(recipientId);
    }

    @PutMapping("/{notificationId}/read")
    public Notification markAsRead(@PathVariable Long notificationId) {
        return notifService.markAsRead(notificationId);
    }

    @PutMapping("/recipient/{recipientId}/read-all")
    public List<Notification> markAllRead(@PathVariable Long recipientId) {
        return notifService.markAllRead(recipientId);
    }

    @PutMapping("/{notificationId}/acknowledge")
    public Notification acknowledge(@PathVariable Long notificationId) {
        return notifService.acknowledge(notificationId);
    }

    @GetMapping("/recipient/{recipientId}/unread-count")
    public long unreadCount(@PathVariable Long recipientId) {
        return notifService.getUnreadCount(recipientId);
    }

    @DeleteMapping("/{notificationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long notificationId) {
        notifService.deleteNotification(notificationId);
    }

    @GetMapping("/all")
    public List<Notification> getAll() {
        return notifService.getAll();
    }

    private Notification mapRequest(NotificationRequest request) {
        Notification notification = new Notification();
        notification.setRecipientId(request.recipientId());
        notification.setType(request.type());
        notification.setSeverity(request.severity());
        notification.setTitle(request.title());
        notification.setMessage(request.message());
        notification.setRelatedId(request.relatedId());
        notification.setRelatedType(request.relatedType());
        return notification;
    }
}
