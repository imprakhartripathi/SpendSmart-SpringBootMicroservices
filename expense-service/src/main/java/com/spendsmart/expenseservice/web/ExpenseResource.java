package com.spendsmart.expenseservice.web;

import com.spendsmart.expenseservice.domain.Expense;
import com.spendsmart.expenseservice.dto.ExpenseUpsertRequest;
import com.spendsmart.expenseservice.enums.ExpenseType;
import com.spendsmart.expenseservice.service.ExpenseService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/expenses")
public class ExpenseResource {

    private final ExpenseService expenseService;

    public ExpenseResource(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Expense addExpense(@Valid @RequestBody ExpenseUpsertRequest request) {
        return expenseService.addExpense(mapRequest(request));
    }

    @GetMapping("/{expenseId}")
    public Expense getExpenseById(@PathVariable Long expenseId) {
        return expenseService.getExpenseById(expenseId);
    }

    @GetMapping("/user/{userId}")
    public List<Expense> getExpensesByUser(@PathVariable Long userId) {
        return expenseService.getExpensesByUser(userId);
    }

    @GetMapping("/category/{categoryId}")
    public List<Expense> getExpensesByCategory(@PathVariable Long categoryId) {
        return expenseService.getExpensesByCategory(categoryId);
    }

    @GetMapping("/date-range")
    public List<Expense> getByDateRange(
            @RequestParam Long userId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        return expenseService.getExpensesByDateRange(userId, startDate, endDate);
    }

    @GetMapping("/month")
    public List<Expense> getByMonth(
            @RequestParam Long userId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        return expenseService.getExpensesByMonth(userId, year, month);
    }

    @PutMapping("/{expenseId}")
    public Expense updateExpense(@PathVariable Long expenseId, @Valid @RequestBody ExpenseUpsertRequest request) {
        return expenseService.updateExpense(expenseId, mapRequest(request));
    }

    @DeleteMapping("/{expenseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExpense(@PathVariable Long expenseId) {
        expenseService.deleteExpense(expenseId);
    }

    @GetMapping("/totals/user/{userId}")
    public BigDecimal getTotalByUser(@PathVariable Long userId) {
        return expenseService.getTotalByUser(userId);
    }

    @GetMapping("/totals/month")
    public BigDecimal getTotalByMonth(
            @RequestParam Long userId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        return expenseService.getTotalByMonth(userId, year, month);
    }

    @GetMapping("/totals/user/{userId}/category/{categoryId}")
    public BigDecimal getTotalByCategory(@PathVariable Long userId, @PathVariable Long categoryId) {
        return expenseService.getTotalByCategory(userId, categoryId);
    }

    @GetMapping("/type")
    public List<Expense> getByType(@RequestParam Long userId, @RequestParam ExpenseType type) {
        return expenseService.getExpensesByType(userId, type);
    }

    @GetMapping("/search")
    public List<Expense> search(@RequestParam Long userId, @RequestParam String keyword) {
        return expenseService.searchExpenses(userId, keyword);
    }

    private Expense mapRequest(ExpenseUpsertRequest request) {
        Expense expense = new Expense();
        expense.setUserId(request.userId());
        expense.setCategoryId(request.categoryId());
        expense.setTitle(request.title());
        expense.setAmount(request.amount());
        expense.setCurrency(request.currency());
        expense.setType(request.type());
        expense.setPaymentMethod(request.paymentMethod());
        expense.setDate(request.date());
        expense.setNotes(request.notes());
        expense.setReceiptUrl(request.receiptUrl());
        expense.setRecurring(request.recurring());
        return expense;
    }
}
