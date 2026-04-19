package com.spendsmart.incomeservice.web;

import com.spendsmart.incomeservice.domain.Income;
import com.spendsmart.incomeservice.enums.IncomeSource;
import com.spendsmart.incomeservice.enums.RecurrencePeriod;
import com.spendsmart.incomeservice.service.IncomeService;
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

@WebMvcTest(IncomeResource.class)
class IncomeResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IncomeService incomeService;

    @Test
    void addIncomeReturnsCreatedIncome() throws Exception {
        Income income = new Income();
        income.setIncomeId(15L);
        income.setUserId(7L);
        income.setCategoryId(2L);
        income.setTitle("Salary");
        income.setAmount(new BigDecimal("50000"));
        income.setCurrency("INR");
        income.setSource(IncomeSource.SALARY);
        income.setDate(LocalDate.of(2026, 4, 1));
        income.setRecurring(true);
        income.setRecurrencePeriod(RecurrencePeriod.MONTHLY);

        when(incomeService.addIncome(any(Income.class))).thenReturn(income);

        mockMvc.perform(
                        post("/incomes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "userId": 7,
                                          "categoryId": 2,
                                          "title": "Salary",
                                          "amount": 50000,
                                          "currency": "INR",
                                          "source": "SALARY",
                                          "date": "2026-04-01",
                                          "notes": "Monthly salary",
                                          "recurring": true,
                                          "recurrencePeriod": "MONTHLY"
                                        }
                                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.incomeId").value(15))
                .andExpect(jsonPath("$.title").value("Salary"));
    }
}
