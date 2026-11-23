package com.example.finance.web;

import com.example.finance.dto.SummaryResponse;
import com.example.finance.dto.TransactionRequest;
import com.example.finance.model.*;
import com.example.finance.service.FinanceService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class FinanceController {

    private final FinanceService service;

    public FinanceController(FinanceService service) {
        this.service = service;
    }

    @GetMapping("/accounts")
    public List<Account> accounts() { return service.listAccounts(); }

    @PostMapping("/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public Account createAccount(@Valid @RequestBody Account account) {
        return service.createAccount(account);
    }

    @GetMapping("/categories")
    public List<Category> categories() { return service.listCategories(); }

    @GetMapping("/transactions")
    public List<Transaction> transactions(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return service.listTransactions(categoryId, start, end);
    }

    @PostMapping("/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    public Transaction createTransaction(@Valid @RequestBody TransactionRequest request) {
        return service.createTransaction(request);
    }

    @DeleteMapping("/transactions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTransaction(@PathVariable Long id) {
        service.deleteTransaction(id);
    }

    @GetMapping("/budgets")
    public List<Budget> budgets(@RequestParam(defaultValue = "11") int month,
                                @RequestParam(defaultValue = "2026") int year) {
        return service.listBudgets(month, year);
    }

    @PostMapping("/budgets")
    @ResponseStatus(HttpStatus.CREATED)
    public Budget createBudget(@Valid @RequestBody Budget budget) {
        return service.createBudget(budget);
    }

    @GetMapping("/goals")
    public List<SavingsGoal> goals() { return service.listGoals(); }

    @PostMapping("/goals")
    @ResponseStatus(HttpStatus.CREATED)
    public SavingsGoal createGoal(@Valid @RequestBody SavingsGoal goal) {
        return service.createGoal(goal);
    }

    @PostMapping("/goals/{id}/contribute")
    public SavingsGoal contribute(@PathVariable Long id, @RequestBody Map<String, BigDecimal> body) {
        return service.contributeToGoal(id, body.get("amount"));
    }

    @GetMapping("/holdings")
    public List<Holding> holdings() { return service.listHoldings(); }

    @PostMapping("/holdings")
    @ResponseStatus(HttpStatus.CREATED)
    public Holding createHolding(@Valid @RequestBody Holding holding) {
        return service.createHolding(holding);
    }

    @GetMapping("/summary")
    public SummaryResponse summary(@RequestParam(defaultValue = "11") int month,
                                   @RequestParam(defaultValue = "2026") int year) {
        return service.monthlySummary(month, year);
    }
}
