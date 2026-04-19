package com.spendsmart.expenseservice.web;

import com.spendsmart.expenseservice.domain.Expense;
import com.spendsmart.expenseservice.enums.ExpenseType;
import com.spendsmart.expenseservice.enums.PaymentMethod;
import com.spendsmart.expenseservice.service.ExpenseService;
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

@WebMvcTest(ExpenseResource.class)
class ExpenseResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExpenseService expenseService;

    @Test
    void addExpenseReturnsCreatedExpense() throws Exception {
        Expense expense = new Expense();
        expense.setExpenseId(9L);
        expense.setUserId(7L);
        expense.setCategoryId(3L);
        expense.setTitle("Groceries");
        expense.setAmount(new BigDecimal("234.00"));
        expense.setCurrency("INR");
        expense.setType(ExpenseType.EXPENSE);
        expense.setPaymentMethod(PaymentMethod.CARD);
        expense.setDate(LocalDate.of(2026, 4, 18));

        when(expenseService.addExpense(any(Expense.class))).thenReturn(expense);

        mockMvc.perform(
                        post("/expenses")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "userId": 7,
                                          "categoryId": 3,
                                          "title": "Groceries",
                                          "amount": 234,
                                          "currency": "INR",
                                          "type": "EXPENSE",
                                          "paymentMethod": "CARD",
                                          "date": "2026-04-18",
                                          "notes": "Weekly run",
                                          "receiptUrl": "https://example.com/receipt",
                                          "recurring": false
                                        }
                                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.expenseId").value(9))
                .andExpect(jsonPath("$.title").value("Groceries"));
    }
}
