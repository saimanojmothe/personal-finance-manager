package com.example.finance.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequest(
        @NotNull Long accountId,
        @NotNull Long categoryId,
        @NotNull @DecimalMin(value = "0.01", message = "Amount must be greater than zero") BigDecimal amount,
        @NotNull LocalDate date,
        @NotBlank String description,
        @NotBlank @Pattern(regexp = "INCOME|EXPENSE", message = "Type must be INCOME or EXPENSE") String type) {}
