package com.saimanoj.finance.dto;

import java.math.BigDecimal;
import java.util.List;

public record SummaryResponse(
        int year,
        int month,
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal net,
        List<CategorySpending> expensesByCategory,
        List<BudgetUsage> budgetUsage,
        List<GoalProgress> goals,
        BigDecimal portfolioCost,
        BigDecimal portfolioValue,
        BigDecimal portfolioProfitLoss,
        double portfolioProfitLossPercent) {}
