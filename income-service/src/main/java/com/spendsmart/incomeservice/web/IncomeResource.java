package com.spendsmart.incomeservice.web;

import com.spendsmart.incomeservice.domain.Income;
import com.spendsmart.incomeservice.dto.IncomeUpsertRequest;
import com.spendsmart.incomeservice.enums.IncomeSource;
import com.spendsmart.incomeservice.service.IncomeService;
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
@RequestMapping("/incomes")
public class IncomeResource {

    private final IncomeService incomeService;

    public IncomeResource(IncomeService incomeService) {
        this.incomeService = incomeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Income addIncome(@Valid @RequestBody IncomeUpsertRequest request) {
        return incomeService.addIncome(mapRequest(request));
    }

    @GetMapping("/{incomeId}")
    public Income getIncomeById(@PathVariable Long incomeId) {
        return incomeService.getIncomeById(incomeId);
    }

    @GetMapping("/user/{userId}")
    public List<Income> getIncomesByUser(@PathVariable Long userId) {
        return incomeService.getIncomesByUser(userId);
    }

    @GetMapping("/source")
    public List<Income> getIncomesBySource(@RequestParam Long userId, @RequestParam IncomeSource source) {
        return incomeService.getIncomesBySource(userId, source);
    }

    @GetMapping("/date-range")
    public List<Income> getByDateRange(
            @RequestParam Long userId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        return incomeService.getIncomesByDateRange(userId, startDate, endDate);
    }

    @GetMapping("/month")
    public List<Income> getByMonth(
            @RequestParam Long userId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        return incomeService.getIncomesByMonth(userId, year, month);
    }

    @PutMapping("/{incomeId}")
    public Income updateIncome(@PathVariable Long incomeId, @Valid @RequestBody IncomeUpsertRequest request) {
        return incomeService.updateIncome(incomeId, mapRequest(request));
    }

    @DeleteMapping("/{incomeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteIncome(@PathVariable Long incomeId) {
        incomeService.deleteIncome(incomeId);
    }

    @GetMapping("/totals/user/{userId}")
    public BigDecimal getTotalIncomeByUser(@PathVariable Long userId) {
        return incomeService.getTotalIncomeByUser(userId);
    }

    @GetMapping("/totals/month")
    public BigDecimal getTotalIncomeByMonth(
            @RequestParam Long userId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        return incomeService.getTotalIncomeByMonth(userId, year, month);
    }

    @GetMapping("/recurring/{userId}")
    public List<Income> getRecurring(@PathVariable Long userId) {
        return incomeService.getRecurringIncomes(userId);
    }

    @GetMapping("/search")
    public List<Income> search(@RequestParam Long userId, @RequestParam String keyword) {
        return incomeService.searchIncome(userId, keyword);
    }

    private Income mapRequest(IncomeUpsertRequest request) {
        Income income = new Income();
        income.setUserId(request.userId());
        income.setCategoryId(request.categoryId());
        income.setTitle(request.title());
        income.setAmount(request.amount());
        income.setCurrency(request.currency());
        income.setSource(request.source());
        income.setDate(request.date());
        income.setNotes(request.notes());
        income.setRecurring(request.recurring());
        income.setRecurrencePeriod(request.recurrencePeriod());
        return income;
    }
}
