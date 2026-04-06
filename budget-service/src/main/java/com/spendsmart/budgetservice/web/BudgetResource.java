package com.spendsmart.budgetservice.web;

import com.spendsmart.budgetservice.domain.Budget;
import com.spendsmart.budgetservice.dto.BudgetProgress;
import com.spendsmart.budgetservice.dto.BudgetUpsertRequest;
import com.spendsmart.budgetservice.dto.SpentAmountUpdateRequest;
import com.spendsmart.budgetservice.service.BudgetService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/budgets")
public class BudgetResource {

    private final BudgetService budgetService;

    public BudgetResource(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Budget createBudget(@Valid @RequestBody BudgetUpsertRequest request) {
        return budgetService.createBudget(mapRequest(request));
    }

    @GetMapping("/{budgetId}")
    public Budget getBudgetById(@PathVariable Long budgetId) {
        return budgetService.getBudgetById(budgetId);
    }

    @GetMapping("/user/{userId}")
    public List<Budget> getBudgetsByUser(@PathVariable Long userId) {
        return budgetService.getBudgetsByUser(userId);
    }

    @GetMapping("/active/{userId}")
    public List<Budget> getActiveBudgets(@PathVariable Long userId) {
        return budgetService.getActiveBudgets(userId);
    }

    @PutMapping("/{budgetId}")
    public Budget updateBudget(@PathVariable Long budgetId, @Valid @RequestBody BudgetUpsertRequest request) {
        return budgetService.updateBudget(budgetId, mapRequest(request));
    }

    @DeleteMapping("/{budgetId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBudget(@PathVariable Long budgetId) {
        budgetService.deleteBudget(budgetId);
    }

    @PutMapping("/{budgetId}/spent")
    public Budget updateSpentAmount(@PathVariable Long budgetId, @Valid @RequestBody SpentAmountUpdateRequest request) {
        return budgetService.updateSpentAmount(budgetId, request.deltaAmount());
    }

    @GetMapping("/{budgetId}/progress")
    public BudgetProgress getBudgetProgress(@PathVariable Long budgetId) {
        return budgetService.getBudgetProgress(budgetId);
    }

    @GetMapping("/alerts/{userId}")
    public List<BudgetProgress> getBudgetAlerts(@PathVariable Long userId) {
        return budgetService.checkBudgetAlerts(userId);
    }

    @PostMapping("/alerts/{userId}/dispatch")
    public List<BudgetProgress> dispatchBudgetAlerts(@PathVariable Long userId) {
        return budgetService.dispatchBudgetAlerts(userId);
    }

    @PostMapping("/reset-period")
    public List<Budget> resetBudgetPeriod() {
        return budgetService.resetBudgetPeriod();
    }

    @GetMapping("/category")
    public List<Budget> getByCategory(@RequestParam Long userId, @RequestParam Long categoryId) {
        return budgetService.getBudgetsByCategory(userId, categoryId);
    }

    private Budget mapRequest(BudgetUpsertRequest request) {
        Budget budget = new Budget();
        budget.setUserId(request.userId());
        budget.setCategoryId(request.categoryId());
        budget.setName(request.name());
        budget.setLimitAmount(request.limitAmount());
        budget.setCurrency(request.currency());
        budget.setPeriod(request.period());
        budget.setStartDate(request.startDate());
        budget.setEndDate(request.endDate());
        budget.setAlertThreshold(request.alertThreshold());
        budget.setActive(request.active() == null || request.active());
        return budget;
    }
}
