package com.example.finance.dto;

import java.math.BigDecimal;

public record BudgetUsage(String category, BigDecimal budget, BigDecimal spent, BigDecimal remaining, double percentUsed) {}
