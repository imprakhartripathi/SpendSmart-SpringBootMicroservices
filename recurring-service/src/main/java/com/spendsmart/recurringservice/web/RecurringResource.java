package com.spendsmart.recurringservice.web;

import com.spendsmart.recurringservice.domain.RecurringTransaction;
import com.spendsmart.recurringservice.dto.RecurringUpsertRequest;
import com.spendsmart.recurringservice.service.RecurringService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
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
@RequestMapping("/recurring")
public class RecurringResource {

    private final RecurringService recurringService;

    public RecurringResource(RecurringService recurringService) {
        this.recurringService = recurringService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecurringTransaction addRecurring(@Valid @RequestBody RecurringUpsertRequest request) {
        return recurringService.addRecurring(mapRequest(request));
    }

    @GetMapping("/user/{userId}")
    public List<RecurringTransaction> getByUser(@PathVariable Long userId) {
        return recurringService.getByUser(userId);
    }

    @GetMapping("/{recurringId}")
    public RecurringTransaction getById(@PathVariable Long recurringId) {
        return recurringService.getById(recurringId);
    }

    @GetMapping("/active/{userId}")
    public List<RecurringTransaction> getActive(@PathVariable Long userId) {
        return recurringService.getActiveRecurring(userId);
    }

    @PutMapping("/{recurringId}")
    public RecurringTransaction update(@PathVariable Long recurringId, @Valid @RequestBody RecurringUpsertRequest request) {
        return recurringService.updateRecurring(recurringId, mapRequest(request));
    }

    @DeleteMapping("/{recurringId}/deactivate")
    public RecurringTransaction deactivate(@PathVariable Long recurringId) {
        return recurringService.deactivateRecurring(recurringId);
    }

    @DeleteMapping("/{recurringId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long recurringId) {
        recurringService.deleteRecurring(recurringId);
    }

    @PostMapping("/processUpcoming")
    public List<Map<String, Object>> processUpcoming() {
        return recurringService.processUpcomingDue();
    }

    @GetMapping("/upcomingMonth/{userId}")
    public List<RecurringTransaction> getUpcomingMonth(@PathVariable Long userId) {
        return recurringService.getUpcomingThisMonth(userId);
    }

    private RecurringTransaction mapRequest(RecurringUpsertRequest request) {
        RecurringTransaction recurring = new RecurringTransaction();
        recurring.setUserId(request.userId());
        recurring.setCategoryId(request.categoryId());
        recurring.setTitle(request.title());
        recurring.setAmount(request.amount());
        recurring.setType(request.type());
        recurring.setFrequency(request.frequency());
        recurring.setStartDate(request.startDate());
        recurring.setEndDate(request.endDate());
        recurring.setNextDueDate(request.nextDueDate());
        recurring.setDescription(request.description());
        recurring.setPaymentMethod(request.paymentMethod());
        recurring.setActive(request.active() == null || request.active());
        return recurring;
    }
}
