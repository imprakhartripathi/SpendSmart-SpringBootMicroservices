package com.spendsmart.recurringservice.web;

import com.spendsmart.recurringservice.domain.RecurringTransaction;
import com.spendsmart.recurringservice.enums.RecurringFrequency;
import com.spendsmart.recurringservice.enums.RecurringPaymentMethod;
import com.spendsmart.recurringservice.enums.RecurringTransactionType;
import com.spendsmart.recurringservice.service.RecurringService;
import java.math.BigDecimal;
import java.time.LocalDate;
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

@WebMvcTest(RecurringResource.class)
class RecurringResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecurringService recurringService;

    @Test
    void addRecurringReturnsCreatedRule() throws Exception {
        RecurringTransaction recurring = new RecurringTransaction();
        recurring.setRecurringId(5L);
        recurring.setUserId(7L);
        recurring.setCategoryId(3L);
        recurring.setTitle("Netflix");
        recurring.setAmount(new BigDecimal("499"));
        recurring.setType(RecurringTransactionType.EXPENSE);
        recurring.setFrequency(RecurringFrequency.MONTHLY);
        recurring.setStartDate(LocalDate.of(2026, 4, 18));
        recurring.setNextDueDate(LocalDate.of(2026, 5, 18));
        recurring.setPaymentMethod(RecurringPaymentMethod.CARD);
        recurring.setActive(true);

        when(recurringService.addRecurring(any(RecurringTransaction.class))).thenReturn(recurring);

        mockMvc.perform(
                        post("/recurring")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "userId": 7,
                                          "categoryId": 3,
                                          "title": "Netflix",
                                          "amount": 499,
                                          "type": "EXPENSE",
                                          "frequency": "MONTHLY",
                                          "startDate": "2026-04-18",
                                          "nextDueDate": "2026-05-18",
                                          "paymentMethod": "CARD",
                                          "active": true
                                        }
                                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recurringId").value(5))
                .andExpect(jsonPath("$.title").value("Netflix"));
    }
}
