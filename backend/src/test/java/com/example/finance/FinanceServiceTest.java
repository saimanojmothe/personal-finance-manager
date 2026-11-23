package com.example.finance;

import com.example.finance.dto.SummaryResponse;
import com.example.finance.model.*;
import com.example.finance.repository.*;
import com.example.finance.service.FinanceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinanceServiceTest {

    @Mock AccountRepository accounts;
    @Mock CategoryRepository categories;
    @Mock TransactionRepository transactions;
    @Mock BudgetRepository budgets;
    @Mock SavingsGoalRepository goals;
    @Mock HoldingRepository holdings;

    @InjectMocks FinanceService service;

    private static Transaction tx(String type, BigDecimal amount, Category category) {
        return new Transaction(new Account("Checking", "CHECKING", BigDecimal.ZERO),
                category, amount, LocalDate.of(2026, 11, 5), "demo", type);
    }

    @Test
    void goalProgressIsCappedAt100() {
        assertEquals(50.0, FinanceService.goalProgressPercent(new BigDecimal("500"), new BigDecimal("1000")));
        assertEquals(100.0, FinanceService.goalProgressPercent(new BigDecimal("1500"), new BigDecimal("1000")));
        assertEquals(0.0, FinanceService.goalProgressPercent(BigDecimal.ZERO, new BigDecimal("1000")));
        assertEquals(0.0, FinanceService.goalProgressPercent(new BigDecimal("100"), BigDecimal.ZERO));
    }

    @Test
    void holdingProfitAndLoss() {
        Holding h = new Holding("DEMO", "Demo Fund", new BigDecimal("10"), new BigDecimal("100.00"), new BigDecimal("110.00"));
        assertEquals(new BigDecimal("1000.00"), FinanceService.holdingCost(h));
        assertEquals(new BigDecimal("1100.00"), FinanceService.holdingValue(h));
        assertEquals(new BigDecimal("100.00"),
                FinanceService.holdingValue(h).subtract(FinanceService.holdingCost(h)));
    }

    @Test
    void budgetUsageUsesOnlyExpensesForThatMonth() {
        Category groceries = new Category("Groceries", "EXPENSE");
        // Both expense transactions share this category instance (id is null in
        // this unit test), and the income transaction is filtered out before grouping.
        Budget budget = new Budget(groceries, 11, 2026, new BigDecimal("400.00"));
        List<Transaction> txs = List.of(
                tx("EXPENSE", new BigDecimal("100.00"), groceries),
                tx("EXPENSE", new BigDecimal("50.00"), groceries),
                tx("INCOME", new BigDecimal("999.00"), new Category("Salary", "INCOME")));

        var usage = service.budgetUsage(List.of(budget), txs);
        assertEquals(1, usage.size());
        assertEquals(new BigDecimal("150.00"), usage.get(0).spent());
        assertEquals(new BigDecimal("250.00"), usage.get(0).remaining());
        assertEquals(37.5, usage.get(0).percentUsed(), 0.001);
    }

    @Test
    void monthlySummaryTotalsIncomeExpensesAndPortfolio() {
        Category salary = new Category("Salary", "INCOME");
        Category food = new Category("Groceries", "EXPENSE");
        List<Transaction> txs = List.of(
                tx("INCOME", new BigDecimal("2000.00"), salary),
                tx("EXPENSE", new BigDecimal("300.00"), food),
                tx("EXPENSE", new BigDecimal("100.00"), food));
        when(transactions.findByDateBetweenOrderByDateDesc(any(), any())).thenReturn(txs);
        when(budgets.findByMonthAndYear(11, 2026)).thenReturn(List.of());
        when(goals.findAll()).thenReturn(List.of(
                new SavingsGoal("Trip", new BigDecimal("1000.00"), new BigDecimal("250.00"), null)));
        when(holdings.findAll()).thenReturn(List.of(
                new Holding("DEMO", "Demo", new BigDecimal("10"), new BigDecimal("100.00"), new BigDecimal("120.00"))));

        SummaryResponse summary = service.monthlySummary(11, 2026);
        assertEquals(new BigDecimal("2000.00"), summary.totalIncome());
        assertEquals(new BigDecimal("400.00"), summary.totalExpenses());
        assertEquals(new BigDecimal("1600.00"), summary.net());
        assertEquals(1, summary.expensesByCategory().size());
        assertEquals(new BigDecimal("400.00"), summary.expensesByCategory().get(0).spent());
        assertEquals(25.0, summary.goals().get(0).percent(), 0.001);
        assertEquals(new BigDecimal("1000.00"), summary.portfolioCost());
        assertEquals(new BigDecimal("1200.00"), summary.portfolioValue());
        assertEquals(new BigDecimal("200.00"), summary.portfolioProfitLoss());
        assertEquals(20.0, summary.portfolioProfitLossPercent(), 0.001);
    }

    @Test
    void contributingBeyondTargetStillCapsProgress() {
        SavingsGoal goal = new SavingsGoal("Trip", new BigDecimal("1000.00"), new BigDecimal("900.00"), null);
        when(goals.findById(1L)).thenReturn(java.util.Optional.of(goal));
        when(goals.save(any())).thenAnswer(i -> i.getArgument(0));
        SavingsGoal updated = service.contributeToGoal(1L, new BigDecimal("500.00"));
        assertEquals(new BigDecimal("1400.00"), updated.getSavedAmount());
        assertEquals(100.0, FinanceService.goalProgressPercent(updated.getSavedAmount(), updated.getTargetAmount()));
    }
}
