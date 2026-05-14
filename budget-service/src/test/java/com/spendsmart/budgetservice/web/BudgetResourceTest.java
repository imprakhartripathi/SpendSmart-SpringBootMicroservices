package com.spendsmart.budgetservice.web;

import com.spendsmart.budgetservice.domain.Budget;
import com.spendsmart.budgetservice.enums.BudgetPeriod;
import com.spendsmart.budgetservice.service.BudgetService;
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

@WebMvcTest(BudgetResource.class)
class BudgetResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BudgetService budgetService;

    @Test
    void createBudgetReturnsCreatedBudget() throws Exception {
        Budget budget = new Budget();
        budget.setBudgetId(3L);
        budget.setUserId(7L);
        budget.setCategoryId(5L);
        budget.setName("Monthly Home");
        budget.setLimitAmount(new BigDecimal("40000"));
        budget.setCurrency("INR");
        budget.setPeriod(BudgetPeriod.MONTHLY);
        budget.setStartDate(LocalDate.of(2026, 4, 1));
        budget.setEndDate(LocalDate.of(2026, 4, 30));
        budget.setAlertThreshold(new BigDecimal("80"));
        budget.setActive(true);

        when(budgetService.createBudget(any(Budget.class))).thenReturn(budget);

        mockMvc.perform(
                        post("/budgets")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "userId": 7,
                                          "categoryId": 5,
                                          "name": "Monthly Home",
                                          "limitAmount": 40000,
                                          "currency": "INR",
                                          "period": "MONTHLY",
                                          "startDate": "2026-04-01",
                                          "endDate": "2026-04-30",
                                          "alertThreshold": 80,
                                          "active": true
                                        }
                                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.budgetId").value(3))
                .andExpect(jsonPath("$.name").value("Monthly Home"));
    }
}
