package com.example.finance.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Entity
@Table(name = "budgets", uniqueConstraints = @UniqueConstraint(columnNames = {"category_id", "budget_month", "budget_year"}))
public class Budget {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    @NotNull @Min(1) @Max(12)
    @Column(name = "budget_month")
    private int month;

    @NotNull @Min(2000) @Max(2100)
    @Column(name = "budget_year")
    private int year;

    @NotNull
    @DecimalMin(value = "0.01", message = "Budget amount must be greater than zero")
    @Column(precision = 19, scale = 2)
    private BigDecimal amount;

    protected Budget() {}

    public Budget(Category category, int month, int year, BigDecimal amount) {
        this.category = category;
        this.month = month;
        this.year = year;
        this.amount = amount;
    }

    public Long getId() { return id; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
