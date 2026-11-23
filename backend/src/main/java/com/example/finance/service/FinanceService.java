package com.example.finance.service;

import com.example.finance.dto.*;
import com.example.finance.model.*;
import com.example.finance.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class FinanceService {

    private final AccountRepository accounts;
    private final CategoryRepository categories;
    private final TransactionRepository transactions;
    private final BudgetRepository budgets;
    private final SavingsGoalRepository goals;
    private final HoldingRepository holdings;

    public FinanceService(AccountRepository accounts, CategoryRepository categories,
                          TransactionRepository transactions, BudgetRepository budgets,
                          SavingsGoalRepository goals, HoldingRepository holdings) {
        this.accounts = accounts;
        this.categories = categories;
        this.transactions = transactions;
        this.budgets = budgets;
        this.goals = goals;
        this.holdings = holdings;
    }

    // ----- read -----
    public List<Account> listAccounts() { return accounts.findAll(); }
    public List<Category> listCategories() { return categories.findAll(); }
    public List<Budget> listBudgets(int month, int year) { return budgets.findByMonthAndYear(month, year); }
    public List<SavingsGoal> listGoals() { return goals.findAll(); }
    public List<Holding> listHoldings() { return holdings.findAll(); }

    public List<Transaction> listTransactions(Long categoryId, LocalDate start, LocalDate end) {
        return transactions.search(categoryId, start, end);
    }

    // ----- write -----
    @Transactional
    public Account createAccount(Account account) { return accounts.save(account); }

    @Transactional
    public Transaction createTransaction(TransactionRequest request) {
        Account account = accounts.findById(request.accountId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        Category category = categories.findById(request.categoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        if (!category.getType().equals(request.type())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transaction type must match the category type");
        }
        Transaction tx = new Transaction(account, category, request.amount(), request.date(),
                request.description(), request.type());
        Transaction saved = transactions.save(tx);
        // Keep the demo account balance in step with its transactions.
        BigDecimal delta = "INCOME".equals(request.type()) ? request.amount() : request.amount().negate();
        account.setBalance(account.getBalance().add(delta));
        accounts.save(account);
        return saved;
    }

    @Transactional
    public void deleteTransaction(Long id) {
        Transaction tx = transactions.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));
        Account account = tx.getAccount();
        BigDecimal delta = "INCOME".equals(tx.getType()) ? tx.getAmount().negate() : tx.getAmount();
        account.setBalance(account.getBalance().add(delta));
        accounts.save(account);
        transactions.delete(tx);
    }

    @Transactional
    public Budget createBudget(Budget budget) { return budgets.save(budget); }

    @Transactional
    public SavingsGoal createGoal(SavingsGoal goal) { return goals.save(goal); }

    @Transactional
    public SavingsGoal contributeToGoal(Long id, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contribution must be greater than zero");
        }
        SavingsGoal goal = goals.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Savings goal not found"));
        goal.setSavedAmount(goal.getSavedAmount().add(amount));
        return goals.save(goal);
    }

    @Transactional
    public Holding createHolding(Holding holding) { return holdings.save(holding); }

    // ----- calculations (unit-tested) -----
    public static double goalProgressPercent(BigDecimal saved, BigDecimal target) {
        if (target == null || target.compareTo(BigDecimal.ZERO) <= 0 || saved == null) {
            return 0.0;
        }
        double pct = saved.divide(target, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
        return Math.min(100.0, Math.max(0.0, pct));
    }

    public static BigDecimal holdingCost(Holding h) {
        return h.getShares().multiply(h.getPurchasePrice()).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal holdingValue(Holding h) {
        return h.getShares().multiply(h.getCurrentPrice()).setScale(2, RoundingMode.HALF_UP);
    }

    public List<BudgetUsage> budgetUsage(List<Budget> monthBudgets, List<Transaction> monthTransactions) {
        Map<String, BigDecimal> spentByCategory = monthTransactions.stream()
                .filter(t -> "EXPENSE".equals(t.getType()) && t.getCategory() != null)
                .collect(Collectors.groupingBy(t -> t.getCategory().getName(),
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)));
        List<BudgetUsage> result = new ArrayList<>();
        for (Budget b : monthBudgets) {
            BigDecimal spent = spentByCategory.getOrDefault(b.getCategory().getName(), BigDecimal.ZERO);
            BigDecimal remaining = b.getAmount().subtract(spent);
            double pct = b.getAmount().compareTo(BigDecimal.ZERO) == 0 ? 0.0
                    : spent.divide(b.getAmount(), 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
            result.add(new BudgetUsage(b.getCategory().getName(), b.getAmount(), spent, remaining, pct));
        }
        result.sort(Comparator.comparing(BudgetUsage::category));
        return result;
    }

    public SummaryResponse monthlySummary(int month, int year) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        List<Transaction> txs = transactions.findByDateBetweenOrderByDateDesc(start, end);

        BigDecimal income = sumByType(txs, "INCOME");
        BigDecimal expenses = sumByType(txs, "EXPENSE");

        Map<String, BigDecimal> byCategory = txs.stream()
                .filter(t -> "EXPENSE".equals(t.getType()) && t.getCategory() != null)
                .collect(Collectors.groupingBy(t -> t.getCategory().getName(),
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)));
        List<CategorySpending> expensesByCategory = byCategory.entrySet().stream()
                .map(e -> new CategorySpending(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(CategorySpending::spent).reversed())
                .toList();

        List<BudgetUsage> usage = budgetUsage(budgets.findByMonthAndYear(month, year), txs);

        List<GoalProgress> goalProgress = goals.findAll().stream()
                .map(g -> new GoalProgress(g.getId(), g.getName(), g.getTargetAmount(), g.getSavedAmount(),
                        goalProgressPercent(g.getSavedAmount(), g.getTargetAmount())))
                .toList();

        BigDecimal cost = BigDecimal.ZERO;
        BigDecimal value = BigDecimal.ZERO;
        for (Holding h : holdings.findAll()) {
            cost = cost.add(holdingCost(h));
            value = value.add(holdingValue(h));
        }
        BigDecimal pnl = value.subtract(cost);
        double pnlPct = cost.compareTo(BigDecimal.ZERO) == 0 ? 0.0
                : pnl.divide(cost, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;

        return new SummaryResponse(year, month, income, expenses, income.subtract(expenses),
                expensesByCategory, usage, goalProgress, cost, value, pnl, pnlPct);
    }

    private static BigDecimal sumByType(List<Transaction> txs, String type) {
        return txs.stream()
                .filter(t -> type.equals(t.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
