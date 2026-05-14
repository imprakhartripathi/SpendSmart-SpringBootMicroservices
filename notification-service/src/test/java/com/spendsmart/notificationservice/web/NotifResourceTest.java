package com.spendsmart.notificationservice.web;

import com.spendsmart.notificationservice.domain.Notification;
import com.spendsmart.notificationservice.enums.NotificationSeverity;
import com.spendsmart.notificationservice.enums.NotificationType;
import com.spendsmart.notificationservice.service.NotifService;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotifResource.class)
class NotifResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotifService notifService;

    @Test
    void sendBudgetAlertReturnsNotification() throws Exception {
        Notification notification = new Notification();
        notification.setNotificationId(4L);
        notification.setRecipientId(7L);
        notification.setType(NotificationType.BUDGET_EXCEEDED);
        notification.setSeverity(NotificationSeverity.CRITICAL);
        notification.setTitle("Budget exceeded");
        notification.setMessage("Budget 'Home' is at 92% usage.");
        notification.setRelatedId(33L);
        notification.setRelatedType("BUDGET");
        notification.setRead(false);
        notification.setAcknowledged(false);
        notification.setCreatedAt(Instant.parse("2026-04-18T00:00:00Z"));

        when(notifService.sendBudgetAlert(7L, 33L, "Home", new BigDecimal("92"), true)).thenReturn(notification);

        mockMvc.perform(
                        post("/notifications/budget-alert")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "recipientId": 7,
                                          "budgetId": 33,
                                          "budgetName": "Home",
                                          "percentageUsed": 92,
                                          "exceeded": true
                                        }
                                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.notificationId").value(4))
                .andExpect(jsonPath("$.type").value("BUDGET_EXCEEDED"));
    }
}
