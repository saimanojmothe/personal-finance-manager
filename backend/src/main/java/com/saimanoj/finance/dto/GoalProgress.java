package com.saimanoj.finance.dto;

import java.math.BigDecimal;

public record GoalProgress(Long id, String name, BigDecimal targetAmount, BigDecimal savedAmount, double percent) {}
