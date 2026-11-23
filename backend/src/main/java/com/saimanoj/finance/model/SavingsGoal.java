package com.saimanoj.finance.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "savings_goals")
public class SavingsGoal {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotNull
    @DecimalMin(value = "0.01", message = "Target amount must be greater than zero")
    @Column(precision = 19, scale = 2)
    private BigDecimal targetAmount;

    @NotNull
    @DecimalMin(value = "0.00", message = "Saved amount must not be negative")
    @Column(precision = 19, scale = 2)
    private BigDecimal savedAmount = BigDecimal.ZERO;

    private LocalDate deadline;

    protected SavingsGoal() {}

    public SavingsGoal(String name, BigDecimal targetAmount, BigDecimal savedAmount, LocalDate deadline) {
        this.name = name;
        this.targetAmount = targetAmount;
        this.savedAmount = savedAmount;
        this.deadline = deadline;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getTargetAmount() { return targetAmount; }
    public void setTargetAmount(BigDecimal targetAmount) { this.targetAmount = targetAmount; }
    public BigDecimal getSavedAmount() { return savedAmount; }
    public void setSavedAmount(BigDecimal savedAmount) { this.savedAmount = savedAmount; }
    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
}
